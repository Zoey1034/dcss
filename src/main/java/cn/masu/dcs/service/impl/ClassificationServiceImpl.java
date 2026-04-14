package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.entity.DocumentExtractMain;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.mapper.DocumentExtractMainMapper;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.service.ClassificationService;
import cn.masu.dcs.vo.DocumentClassificationVO;
import cn.masu.dcs.vo.DocumentDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文档分类服务实现
 *
 * @author zyq
 * @since 2025-12-09
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassificationServiceImpl implements ClassificationService {

    private final DocumentFileMapper fileMapper;
    private final DocumentExtractMainMapper extractMainMapper;
    private final MinioUtils minioUtils;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map<String, Object>> queryDocumentTypeStatistics() {
        // 查询所有已处理的文件
        List<DocumentExtractMain> allExtracts = extractMainMapper.selectList(
                new LambdaQueryWrapper<DocumentExtractMain>()
                        .isNotNull(DocumentExtractMain::getDocumentType)
        );

        // 按文档类型分组统计
        Map<String, Long> typeCount = allExtracts.stream()
                .collect(Collectors.groupingBy(
                        DocumentExtractMain::getDocumentType,
                        Collectors.counting()
                ));

        // 转换为List并排序
        return typeCount.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> map = new HashMap<>(4);
                    map.put("documentType", entry.getKey());
                    map.put("count", entry.getValue());
                    return map;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("count"),
                        (Long) a.get("count")
                ))
                .collect(Collectors.toList());
    }

    @Override
    public PageResult<DocumentClassificationVO> queryDocumentsByType(
            String documentType, Long current, Long size, String keyword) {

        // 构建查询条件
        LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentExtractMain::getDocumentType, documentType);

        // 添加关键词搜索（使用ownerName和summary字段）
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w
                    .like(DocumentExtractMain::getOwnerName, keyword)
                    .or().like(DocumentExtractMain::getOwnerId, keyword)
                    .or().like(DocumentExtractMain::getSummary, keyword)
            );
        }

        wrapper.orderByDesc(DocumentExtractMain::getCreateTime);

        // 分页查询
        Page<DocumentExtractMain> page = new Page<>(current, size);
        page = extractMainMapper.selectPage(page, wrapper);

        // 转换为VO
        List<DocumentClassificationVO> voList = page.getRecords().stream()
                .map(this::convertToClassificationVO)
                .collect(Collectors.toList());

        // 手动创建PageResult
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }

    @Override
    public DocumentDetailVO queryDocumentById(String fileId) {
        Long id = Long.parseLong(fileId);

        // 查询文件信息
        DocumentFile file = fileMapper.selectById(id);
        if (file == null) {
            throw new RuntimeException("文件不存在：" + fileId);
        }

        // 查询提取信息
        DocumentExtractMain extractMain = extractMainMapper.selectOne(
                new LambdaQueryWrapper<DocumentExtractMain>()
                        .eq(DocumentExtractMain::getFileId, id)
                        .orderByDesc(DocumentExtractMain::getCreateTime)
                        .last("LIMIT 1")
        );

        return convertToDetailVO(file, extractMain);
    }

    @Override
    public PageResult<DocumentClassificationVO> queryDocumentsByKeyword(
            String keyword, Long current, Long size) {

        LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w
                .like(DocumentExtractMain::getOwnerName, keyword)
                .or().like(DocumentExtractMain::getOwnerId, keyword)
                .or().like(DocumentExtractMain::getSummary, keyword)
                .or().like(DocumentExtractMain::getDocumentType, keyword)
        );
        wrapper.orderByDesc(DocumentExtractMain::getCreateTime);

        Page<DocumentExtractMain> page = new Page<>(current, size);
        page = extractMainMapper.selectPage(page, wrapper);

        List<DocumentClassificationVO> voList = page.getRecords().stream()
                .map(this::convertToClassificationVO)
                .collect(Collectors.toList());

        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), voList);
    }

    /**
     * 转换为分类列表VO
     */
    private DocumentClassificationVO convertToClassificationVO(DocumentExtractMain extract) {
        DocumentClassificationVO vo = new DocumentClassificationVO();
        vo.setFileId(extract.getFileId());
        vo.setDocumentType(extract.getDocumentType());
        vo.setConfidence(extract.getConfidence());
        vo.setSummary(extract.getSummary());
        // 使用ownerName作为姓名
        vo.setName(extract.getOwnerName());
        // 使用ownerId作为学号/证件号
        vo.setIdNumber(extract.getOwnerId());
        // DocumentExtractMain中没有schoolName字段，设为null
        vo.setSchoolName(null);
        vo.setProcessStatus(extract.getStatus());

        // LocalDateTime转换为Date
        if (extract.getCreateTime() != null) {
            vo.setUploadTime(Timestamp.valueOf(extract.getCreateTime()));
        }

        // 查询文件信息获取文件名和URL
        DocumentFile file = fileMapper.selectById(extract.getFileId());
        if (file != null) {
            vo.setFileName(file.getFileName());
            // 生成MinIO预签名URL
            try {
                String url = minioUtils.getPresignedObjectUrl(
                        file.getMinioBucket(),
                        file.getMinioObject()
                );
                vo.setFileUrl(url);
            } catch (Exception e) {
                log.warn("生成文件URL失败: fileId={}", extract.getFileId(), e);
            }
        }

        // 设置状态名称
        vo.setProcessStatusName(getStatusName(extract.getStatus()));

        return vo;
    }

    /**
     * 转换为详细信息VO
     */
    private DocumentDetailVO convertToDetailVO(DocumentFile file, DocumentExtractMain extract) {
        DocumentDetailVO vo = new DocumentDetailVO();

        // 文件基本信息
        vo.setFileId(file.getId());
        vo.setFileName(file.getFileName());
        vo.setBucket(file.getMinioBucket());
        vo.setObject(file.getMinioObject());
        vo.setUploadTime(file.getCreateTime());
        vo.setProcessStatus(file.getProcessStatus());
        vo.setProcessStatusName(getStatusName(file.getProcessStatus()));

        // 生成预签名URL
        try {
            String url = minioUtils.getPresignedObjectUrl(
                    file.getMinioBucket(),
                    file.getMinioObject()
            );
            vo.setFileUrl(url);
        } catch (Exception e) {
            log.warn("生成文件URL失败: fileId={}", file.getId(), e);
        }

        if (extract != null) {
            // AI识别结果
            vo.setDocumentType(extract.getDocumentType());
            vo.setConfidenceOverall(extract.getConfidence());
            vo.setSummary(extract.getSummary());

            // 解析JSON字段（使用正确的字段名）
            try {
                TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {};

                if (extract.getBasicInfoJson() != null) {
                    vo.setBasicInfo(objectMapper.readValue(
                            extract.getBasicInfoJson(), typeRef));
                }
                if (extract.getAcademicInfoJson() != null) {
                    vo.setAcademicInfo(objectMapper.readValue(
                            extract.getAcademicInfoJson(), typeRef));
                }
                if (extract.getCertificateInfoJson() != null) {
                    vo.setCertificateInfo(objectMapper.readValue(
                            extract.getCertificateInfoJson(), typeRef));
                }
                if (extract.getFinancialInfoJson() != null) {
                    vo.setFinancialInfo(objectMapper.readValue(
                            extract.getFinancialInfoJson(), typeRef));
                }
                if (extract.getLeaveInfoJson() != null) {
                    vo.setLeaveInfo(objectMapper.readValue(
                            extract.getLeaveInfoJson(), typeRef));
                }

                // 从extractResult中解析courses和tables
                if (extract.getExtractResult() != null) {
                    Map<String, Object> result = objectMapper.readValue(
                            extract.getExtractResult(), typeRef);
                    vo.setCourses(result.get("courses"));

                    Object docTypeCandidates = result.get("document_type_candidates");
                    if (docTypeCandidates instanceof Map) {
                        vo.setDocumentTypeCandidates(objectMapper.convertValue(
                                docTypeCandidates, typeRef));
                    }

                    Object fields = result.get("fields");
                    if (fields instanceof Map) {
                        vo.setFields(objectMapper.convertValue(fields, typeRef));
                    }
                }

                if (extract.getTablesJson() != null) {
                    vo.setTables(objectMapper.readValue(
                            extract.getTablesJson(),
                            new TypeReference<List<Object>>() {}));
                }

                vo.setText(extract.getRawText());

            } catch (Exception e) {
                log.error("解析AI结果JSON失败: fileId={}", file.getId(), e);
            }
        }

        return vo;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        return switch (status) {
            case 0 -> "待处理";
            case 1 -> "队列中";
            case 2 -> "处理中";
            case 3 -> "待人工";
            case 4 -> "已归档";
            case 5 -> "失败";
            default -> "未知";
        };
    }
}

