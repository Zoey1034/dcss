package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.dto.ReviewQueryDTO;
import cn.masu.dcs.dto.ReviewSaveDTO;
import cn.masu.dcs.dto.ReviewCompleteDTO;
import cn.masu.dcs.entity.*;
import cn.masu.dcs.mapper.*;
import cn.masu.dcs.service.ReviewService;
import cn.masu.dcs.vo.ReviewTaskVO;
import cn.masu.dcs.vo.ReviewDetailVO;
import cn.masu.dcs.vo.AuditHistoryVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 校对服务实现
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final DocumentFileMapper fileMapper;
    private final DocumentExtractMainMapper extractMainMapper;
    private final DocumentExtractDetailMapper extractDetailMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final SysUserMapper userMapper;
    private final MinioUtils minioUtils;
    private final ObjectMapper objectMapper;

    private static final int PROCESS_STATUS_NEED_REVIEW = 3;
    private static final int PROCESS_STATUS_ARCHIVED = 4;
    private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#.##");

    @Override
    public PageResult<ReviewTaskVO> queryPendingTasks(ReviewQueryDTO dto) {
        // 构建查询条件
        LambdaQueryWrapper<DocumentFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFile::getProcessStatus, PROCESS_STATUS_NEED_REVIEW)
                .eq(DocumentFile::getDeleted, 0);

        // 关键字搜索
        if (dto.getKeyword() != null && !dto.getKeyword().trim().isEmpty()) {
            wrapper.like(DocumentFile::getFileName, dto.getKeyword());
        }

        // 排序
        if ("confidence".equals(dto.getOrderBy())) {
            // 按置信度排序需要关联查询
            wrapper.orderBy(true, "asc".equals(dto.getOrderDirection()),
                DocumentFile::getCreateTime);
        } else {
            wrapper.orderBy(true, "asc".equals(dto.getOrderDirection()),
                DocumentFile::getCreateTime);
        }

        // 分页查询
        Page<DocumentFile> page = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<DocumentFile> filePage = fileMapper.selectPage(page, wrapper);

        // 转换为VO
        List<ReviewTaskVO> voList = filePage.getRecords().stream()
                .map(this::convertToReviewTaskVO)
                .collect(Collectors.toList());

        return PageResult.of(filePage.getTotal(), filePage.getCurrent(), filePage.getSize(), voList);
    }

    @Override
    public ReviewDetailVO queryReviewById(Long fileId) {
        // 查询文件信息
        DocumentFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        // 查询提取主表
        LambdaQueryWrapper<DocumentExtractMain> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(DocumentExtractMain::getFileId, fileId)
                .orderByDesc(DocumentExtractMain::getCreateTime)
                .last("LIMIT 1");
        DocumentExtractMain extractMain = extractMainMapper.selectOne(mainWrapper);

        if (extractMain == null) {
            throw new RuntimeException("未找到提取结果");
        }

        // 查询提取详情
        LambdaQueryWrapper<DocumentExtractDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(DocumentExtractDetail::getFileId, fileId)
                .eq(DocumentExtractDetail::getMainId, extractMain.getId())
                .orderByAsc(DocumentExtractDetail::getRowIndex);
        List<DocumentExtractDetail> details = extractDetailMapper.selectList(detailWrapper);

        // 构建返回对象
        ReviewDetailVO vo = new ReviewDetailVO();

        // 文件信息
        ReviewDetailVO.FileInfo fileInfo = new ReviewDetailVO.FileInfo();
        fileInfo.setId(file.getId());
        fileInfo.setFileName(file.getFileName());
        fileInfo.setFileType(file.getFileType());
        fileInfo.setFileSize(file.getFileSize());
        fileInfo.setFileSizeFormatted(formatFileSize(file.getFileSize()));
        fileInfo.setPreviewUrl(queryFilePreviewUrl(fileId));
        fileInfo.setCreateTime(dateToLocalDateTime(file.getCreateTime()));
        vo.setFileInfo(fileInfo);

        // 提取信息
        ReviewDetailVO.ExtractInfo extractInfo = new ReviewDetailVO.ExtractInfo();
        extractInfo.setId(extractMain.getId());
        extractInfo.setOwnerName(extractMain.getOwnerName());
        extractInfo.setOwnerId(extractMain.getOwnerId());
        extractInfo.setConfidence(extractMain.getConfidence());
        extractInfo.setStatus(extractMain.getStatus());
        extractInfo.setStatusName(getExtractStatusName(extractMain.getStatus()));
        vo.setExtractInfo(extractInfo);

        // 解析字段
        List<ReviewDetailVO.FieldInfo> fields = parseFieldsFromExtractResult(
            extractMain.getExtractResult());
        vo.setFields(fields);

        // 表格数据
        List<Map<String, Object>> tableData = parseTableData(details);
        vo.setTableData(tableData);

        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertDraft(ReviewSaveDTO dto, Long userId) {
        // 查询提取主表
        DocumentExtractMain extractMain = getExtractMain(dto.getFileId(), dto.getExtractMainId());

        // 更新字段数据
        try {
            String updatedJson = updateExtractResult(extractMain.getExtractResult(), dto.getFields());
            extractMain.setExtractResult(updatedJson);
            extractMain.setStatus(1); // 1=审核中
            extractMainMapper.updateById(extractMain);

            // 创建审核记录
            createAuditRecord(dto.getFileId(), extractMain.getId(), userId,
                "SAVE_DRAFT", dto.getComment(), dto.getFields().size());

            log.info("保存草稿成功: fileId={}, userId={}", dto.getFileId(), userId);
            return true;
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            throw new RuntimeException("保存失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateReviewComplete(ReviewCompleteDTO dto, Long userId) {
        // 查询提取主表
        DocumentExtractMain extractMain = getExtractMain(dto.getFileId(), dto.getExtractMainId());

        try {
            // 更新字段数据
            String updatedJson = updateExtractResult(extractMain.getExtractResult(), dto.getFields());
            extractMain.setExtractResult(updatedJson);
            extractMain.setStatus(2); // 2=已确认
            extractMainMapper.updateById(extractMain);

            // 更新文件状态
            if (dto.getConfirmArchive()) {
                DocumentFile file = fileMapper.selectById(dto.getFileId());
                file.setProcessStatus(PROCESS_STATUS_ARCHIVED);
                fileMapper.updateById(file);
            }

            // 创建审核记录
            createAuditRecord(dto.getFileId(), extractMain.getId(), userId,
                "COMPLETE_REVIEW", dto.getComment(), dto.getFields().size());

            log.info("完成校对成功: fileId={}, userId={}", dto.getFileId(), userId);
            return true;
        } catch (Exception e) {
            log.error("完成校对失败", e);
            throw new RuntimeException("完成校对失败: " + e.getMessage());
        }
    }

    @Override
    public List<AuditHistoryVO> queryAuditHistory(Long fileId) {
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getFileId, fileId)
                .orderByDesc(AuditRecord::getCreateTime);

        List<AuditRecord> records = auditRecordMapper.selectList(wrapper);

        return records.stream()
                .map(this::convertToAuditHistoryVO)
                .collect(Collectors.toList());
    }

    @Override
    public String queryFilePreviewUrl(Long fileId) {
        DocumentFile file = fileMapper.selectById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        try {
            return minioUtils.getPresignedObjectUrl(
                file.getMinioBucket(),
                file.getMinioObject()
            );
        } catch (Exception e) {
            log.error("获取文件预览URL失败", e);
            return null;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> updateBatchComplete(List<Long> fileIds, Long userId) {
        int successCount = 0;
        int failCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long fileId : fileIds) {
            try {
                // 简化的完成逻辑
                DocumentFile file = fileMapper.selectById(fileId);
                if (file != null && file.getProcessStatus() == PROCESS_STATUS_NEED_REVIEW) {
                    file.setProcessStatus(PROCESS_STATUS_ARCHIVED);
                    fileMapper.updateById(file);

                    // 更新提取状态
                    LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
                    wrapper.eq(DocumentExtractMain::getFileId, fileId);
                    DocumentExtractMain extractMain = extractMainMapper.selectOne(wrapper);
                    if (extractMain != null) {
                        extractMain.setStatus(2);
                        extractMainMapper.updateById(extractMain);
                    }

                    successCount++;
                } else {
                    failCount++;
                    errors.add("文件" + fileId + "状态不正确");
                }
            } catch (Exception e) {
                failCount++;
                errors.add("文件" + fileId + "处理失败: " + e.getMessage());
            }
        }

        Map<String, Object> result = new HashMap<>(4);
        result.put("total", fileIds.size());
        result.put("success", successCount);
        result.put("fail", failCount);
        result.put("errors", errors);

        return result;
    }

    // ==================== 私有辅助方法 ====================

    private ReviewTaskVO convertToReviewTaskVO(DocumentFile file) {
        ReviewTaskVO vo = new ReviewTaskVO();
        vo.setId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setFileType(file.getFileType());
        vo.setFileSizeFormatted(formatFileSize(file.getFileSize()));
        vo.setCreateTime(dateToLocalDateTime(file.getCreateTime()));

        // 查询提取信息
        LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentExtractMain::getFileId, file.getId())
                .orderByDesc(DocumentExtractMain::getCreateTime)
                .last("LIMIT 1");
        DocumentExtractMain extractMain = extractMainMapper.selectOne(wrapper);

        if (extractMain != null) {
            vo.setExtractMainId(extractMain.getId());
            vo.setConfidence(extractMain.getConfidence());
            vo.setOwnerName(extractMain.getOwnerName());
            vo.setOwnerId(extractMain.getOwnerId());
            vo.setPartiallyReviewed(extractMain.getStatus() == 1);
        }

        return vo;
    }

    private DocumentExtractMain getExtractMain(Long fileId, Long extractMainId) {
        DocumentExtractMain extractMain;
        if (extractMainId != null) {
            extractMain = extractMainMapper.selectById(extractMainId);
        } else {
            LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DocumentExtractMain::getFileId, fileId)
                    .orderByDesc(DocumentExtractMain::getCreateTime)
                    .last("LIMIT 1");
            extractMain = extractMainMapper.selectOne(wrapper);
        }

        if (extractMain == null) {
            throw new RuntimeException("未找到提取结果");
        }

        return extractMain;
    }

    private String updateExtractResult(String originalJson, Map<String, Object> fields) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = objectMapper.readValue(originalJson, Map.class);

            // 更新字段
            resultMap.putAll(fields);

            return objectMapper.writeValueAsString(resultMap);
        } catch (Exception e) {
            log.error("更新提取结果失败", e);
            throw new RuntimeException("更新失败");
        }
    }

    private void createAuditRecord(Long fileId, Long extractMainId, Long auditorId,
                                  String operationType, String comment, Integer modifiedCount) {
        AuditRecord record = new AuditRecord();
        record.setFileId(fileId);
        record.setExtractMainId(extractMainId);
        record.setAuditorId(auditorId);
        record.setAuditStatus("COMPLETE_REVIEW".equals(operationType) ? 2 : 1);
        record.setAuditComment(comment);
        auditRecordMapper.insert(record);
    }

    private List<ReviewDetailVO.FieldInfo> parseFieldsFromExtractResult(String extractResult) {
        List<ReviewDetailVO.FieldInfo> fields = new ArrayList<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> resultMap = objectMapper.readValue(extractResult, Map.class);

            for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
                if (!"courses".equals(entry.getKey()) && entry.getValue() != null) {
                    ReviewDetailVO.FieldInfo field = new ReviewDetailVO.FieldInfo();
                    field.setFieldName(entry.getKey());
                    field.setFieldLabel(convertFieldLabel(entry.getKey()));
                    field.setFieldValue(entry.getValue());
                    field.setOriginalValue(entry.getValue());
                    field.setFieldType("text");
                    field.setModified(false);
                    fields.add(field);
                }
            }
        } catch (Exception e) {
            log.error("解析字段失败", e);
        }
        return fields;
    }

    private List<Map<String, Object>> parseTableData(List<DocumentExtractDetail> details) {
        return details.stream()
                .map(detail -> {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> data = objectMapper.readValue(
                            detail.getRowDataJson(), Map.class);
                        data.put("id", detail.getId());
                        data.put("rowIndex", detail.getRowIndex());
                        return data;
                    } catch (Exception e) {
                        return new HashMap<String, Object>();
                    }
                })
                .collect(Collectors.toList());
    }

    private AuditHistoryVO convertToAuditHistoryVO(AuditRecord record) {
        AuditHistoryVO vo = new AuditHistoryVO();
        vo.setId(record.getId());
        vo.setAuditorId(record.getAuditorId());
        vo.setAuditStatus(record.getAuditStatus());
        vo.setAuditStatusName(getAuditStatusName(record.getAuditStatus()));
        vo.setAuditComment(record.getAuditComment());
        vo.setCreateTime(dateToLocalDateTime(record.getCreateTime()));

        // 查询审核人姓名
        SysUser user = userMapper.selectById(record.getAuditorId());
        if (user != null) {
            vo.setAuditorName(user.getUsername());
        }

        return vo;
    }

    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0B";
        }
        if (size < 1024) {
            return size + "B";
        } else if (size < 1024 * 1024) {
            return SIZE_FORMAT.format(size / 1024.0) + "KB";
        } else if (size < 1024 * 1024 * 1024) {
            return SIZE_FORMAT.format(size / 1024.0 / 1024.0) + "MB";
        } else {
            return SIZE_FORMAT.format(size / 1024.0 / 1024.0 / 1024.0) + "GB";
        }
    }

    private String getExtractStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核中";
            case 2: return "已确认";
            default: return "未知";
        }
    }

    private String getAuditStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核中";
            case 2: return "已通过";
            case 3: return "已驳回";
            default: return "未知";
        }
    }

    private String convertFieldLabel(String fieldName) {
        Map<String, String> labelMap = new HashMap<>(8);
        labelMap.put("student_name", "学生姓名");
        labelMap.put("student_id", "学号");
        labelMap.put("name", "姓名");
        labelMap.put("id", "证件号");
        labelMap.put("gpa", "绩点");
        labelMap.put("total_credit", "总学分");
        return labelMap.getOrDefault(fieldName, fieldName);
    }

    /**
     * 将Date转换为LocalDateTime
     */
    private LocalDateTime dateToLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}

