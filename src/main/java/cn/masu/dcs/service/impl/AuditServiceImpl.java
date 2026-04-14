package cn.masu.dcs.service.impl;

import cn.masu.dcs.dto.AuditSubmitDTO;
import cn.masu.dcs.entity.AuditRecord;
import cn.masu.dcs.entity.DocumentExtractMain;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.mapper.AuditRecordMapper;
import cn.masu.dcs.mapper.DocumentExtractMainMapper;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import cn.masu.dcs.service.AuditService;
import cn.masu.dcs.vo.AuditRecordVO;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.common.util.MinioUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 审核服务实现
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl extends ServiceImpl<AuditRecordMapper, AuditRecord> implements AuditService {

    private final SnowflakeIdGenerator idGenerator;
    private final DocumentFileMapper fileMapper;
    private final DocumentExtractMainMapper extractMainMapper;
    private final SysUserMapper userMapper;
    private final MinioUtils minioUtils;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean insertAudit(AuditSubmitDTO dto, Long auditorId) {
        // 检查文件是否存在
        DocumentFile file = fileMapper.selectById(dto.getFileId());
        if (file == null) {
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 获取提取主表
        LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentExtractMain::getFileId, dto.getFileId());
        DocumentExtractMain extractMain = extractMainMapper.selectOne(wrapper);
        if (extractMain == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "提取数据不存在");
        }

        // 创建审核记录
        AuditRecord record = new AuditRecord();
        record.setId(idGenerator.nextId());
        record.setFileId(dto.getFileId());
        record.setExtractMainId(extractMain.getId());
        record.setAuditorId(auditorId);
        record.setAuditStatus(dto.getAuditStatus());
        record.setAuditComment(dto.getAuditComment());
        baseMapper.insert(record);

        // 根据审核状态更新文件处理状态
        // 审核状态：0=待审核, 1=审核中, 2=已通过, 3=已驳回
        // 文件状态：0=pending, 1=queued, 2=processing, 3=manual, 4=archived, 5=failed
        final int auditApproved = 2;
        final int auditRejected = 3;
        final int statusArchived = 4;
        final int statusManual = 3;

        if (dto.getAuditStatus() != null) {
            if (dto.getAuditStatus() == auditApproved) {
                // 审核通过：设为已归档
                file.setProcessStatus(statusArchived);
                fileMapper.updateById(file);

                // 同步提取主表状态
                extractMain.setStatus(2);
                extractMainMapper.updateById(extractMain);
            } else if (dto.getAuditStatus() == auditRejected) {
                // 审核驳回：设为待人工处理
                file.setProcessStatus(statusManual);
                file.setFailReason("审核驳回: " + (dto.getAuditComment() != null ? dto.getAuditComment() : ""));
                fileMapper.updateById(file);
            }
        }

        return true;
    }

    @Override
    public List<AuditRecordVO> queryAuditHistory(Long fileId) {
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AuditRecord::getFileId, fileId);
        wrapper.orderByDesc(AuditRecord::getCreateTime);

        List<AuditRecord> records = baseMapper.selectList(wrapper);
        return records.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public PageResult<AuditRecordVO> queryAuditPage(Long current, Long size, Integer auditStatus, Long auditorId) {
        Page<AuditRecord> page = new Page<>(current, size);
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();

        if (auditStatus != null) {
            wrapper.eq(AuditRecord::getAuditStatus, auditStatus);
        }
        if (auditorId != null) {
            wrapper.eq(AuditRecord::getAuditorId, auditorId);
        }
        wrapper.orderByDesc(AuditRecord::getCreateTime);

        Page<AuditRecord> result = baseMapper.selectPage(page, wrapper);
        List<AuditRecordVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    private AuditRecordVO convertToVO(AuditRecord record) {
        AuditRecordVO vo = new AuditRecordVO();
        BeanUtils.copyProperties(record, vo);
        vo.setAuditStatusName(getAuditStatusName(record.getAuditStatus()));

        // 查询文件名
        if (record.getFileId() != null) {
            DocumentFile file = fileMapper.selectById(record.getFileId());
            if (file != null) {
                vo.setFileName(file.getFileName());
            }
        }

        // 查询审核人姓名
        if (record.getAuditorId() != null) {
            SysUser auditor = userMapper.selectById(record.getAuditorId());
            if (auditor != null) {
                vo.setAuditorName(auditor.getNickname() != null ?
                    auditor.getNickname() : auditor.getUsername());
            }
        }

        return vo;
    }

    private String getAuditStatusName(Integer status) {
        final String unknown = "未知";
        if (status == null) {
            return unknown;
        }
        return switch (status) {
            case 0 -> "待审核";
            case 1 -> "审核中";
            case 2 -> "已通过";
            case 3 -> "已驳回";
            default -> unknown;
        };
    }

    @Override
    public String queryFilePreviewUrl(Long fileId) {
        log.info("获取文件预览URL: fileId={}", fileId);

        // 查询文件信息
        DocumentFile file = fileMapper.selectById(fileId);
        if (file == null) {
            log.error("文件不存在: fileId={}", fileId);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 检查MinIO存储信息
        if (!StringUtils.hasText(file.getMinioBucket()) || !StringUtils.hasText(file.getMinioObject())) {
            log.error("文件MinIO信息不完整: fileId={}, bucket={}, object={}",
                    fileId, file.getMinioBucket(), file.getMinioObject());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "文件存储信息不完整");
        }

        try {
            // 生成7天有效期的预签名URL
            String url = minioUtils.getPresignedObjectUrl(file.getMinioBucket(), file.getMinioObject());
            log.info("文件预览URL生成成功: fileId={}, url={}", fileId, url);
            return url;
        } catch (Exception e) {
            log.error("生成文件预览URL失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "生成预览链接失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> queryProcessResult(Long fileId) {
        log.info("获取AI处理结果: fileId={}", fileId);

        // 查询提取主表数据
        LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentExtractMain::getFileId, fileId);
        wrapper.orderByDesc(DocumentExtractMain::getCreateTime);
        wrapper.last("LIMIT 1");

        DocumentExtractMain extractMain = extractMainMapper.selectOne(wrapper);
        if (extractMain == null) {
            log.warn("提取数据不存在: fileId={}, 文件可能尚未进行AI处理", fileId);
            // 返回空结果而不是抛出异常，便于前端判断
            Map<String, Object> emptyResult = new HashMap<>(4);
            emptyResult.put("fileId", fileId);
            emptyResult.put("status", -1);
            emptyResult.put("message", "文件尚未进行AI处理，请先触发AI处理");
            emptyResult.put("needProcess", true);
            return emptyResult;
        }

        try {
            Map<String, Object> result = new HashMap<>(16);

            // 基本信息
            result.put("fileId", fileId);
            result.put("extractId", extractMain.getId());
            result.put("confidence", extractMain.getConfidence());
            result.put("status", extractMain.getStatus());
            result.put("createTime", extractMain.getCreateTime());

            // 解析KV数据
            if (StringUtils.hasText(extractMain.getKvDataJson())) {
                try {
                    Map<String, Object> kvData = objectMapper.readValue(
                            extractMain.getKvDataJson(),
                            objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)
                    );
                    result.put("kvData", kvData);
                } catch (Exception e) {
                    log.warn("解析KV数据失败: fileId={}", fileId, e);
                    result.put("kvData", new HashMap<>(0));
                }
            }

            // 解析完整提取结果
            if (StringUtils.hasText(extractMain.getExtractResult())) {
                try {
                    Map<String, Object> extractResult = objectMapper.readValue(
                            extractMain.getExtractResult(),
                            objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)
                    );
                    result.put("extractResult", extractResult);
                } catch (Exception e) {
                    log.warn("解析完整提取结果失败: fileId={}", fileId, e);
                    result.put("extractResult", new HashMap<>(0));
                }
            }

            log.info("获取AI处理结果成功: fileId={}, confidence={}", fileId, extractMain.getConfidence());
            return result;

        } catch (Exception e) {
            log.error("获取AI处理结果失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "获取处理结果失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateFields(Long fileId, Map<String, Object> fields, Long auditorId) {
        log.info("修改字段: fileId={}, auditorId={}, fieldsCount={}", fileId, auditorId, fields != null ? fields.size() : 0);

        if (fields == null || fields.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "修改字段不能为空");
        }

        // 查询文件
        DocumentFile file = fileMapper.selectById(fileId);
        if (file == null) {
            log.error("文件不存在: fileId={}", fileId);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }

        // 查询提取主表数据
        LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentExtractMain::getFileId, fileId);
        wrapper.orderByDesc(DocumentExtractMain::getCreateTime);
        wrapper.last("LIMIT 1");

        DocumentExtractMain extractMain = extractMainMapper.selectOne(wrapper);
        if (extractMain == null) {
            log.error("提取数据不存在: fileId={}", fileId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "未找到提取数据");
        }

        try {
            Map<String, Object> currentKvData = parseJsonToMap(
                extractMain.getKvDataJson(), "解析现有KV数据");
            Map<String, Object> extractResultData = parseJsonToMap(
                extractMain.getExtractResult(), "解析提取结果");

            currentKvData.putAll(fields);
            extractResultData.putAll(fields);

            extractMain.setKvDataJson(objectMapper.writeValueAsString(currentKvData));
            extractMain.setExtractResult(objectMapper.writeValueAsString(extractResultData));
            extractMain.setStatus(0);
            extractMainMapper.updateById(extractMain);

            // 创建审核记录
            AuditRecord record = new AuditRecord();
            record.setId(idGenerator.nextId());
            record.setFileId(fileId);
            record.setExtractMainId(extractMain.getId());
            record.setAuditorId(auditorId);
            // 审核中
            record.setAuditStatus(1);
            record.setAuditComment("修改了 " + fields.size() + " 个字段");
            baseMapper.insert(record);

            log.info("字段修改成功: fileId={}, fieldsCount={}", fileId, fields.size());
            return true;

        } catch (Exception e) {
            log.error("修改字段失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "修改字段失败: " + e.getMessage());
        }
    }

    private Map<String, Object> parseJsonToMap(String json, String logPrefix) {
        Map<String, Object> result = new HashMap<>(16);
        if (!StringUtils.hasText(json)) {
            return result;
        }
        try {
            return objectMapper.readValue(
                json,
                objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class)
            );
        } catch (Exception e) {
            log.warn("{}失败: {}", logPrefix, e.getMessage());
            return result;
        }
    }
}
