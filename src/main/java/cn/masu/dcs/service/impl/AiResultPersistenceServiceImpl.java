package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.constant.AiConstants;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.entity.DocumentExtractDetail;
import cn.masu.dcs.entity.DocumentExtractMain;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.mapper.DocumentExtractDetailMapper;
import cn.masu.dcs.mapper.DocumentExtractMainMapper;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.service.AiResultPersistenceService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AI结果持久化服务实现
 * <p>
 * 专门负责AI处理结果的数据库事务操作，解决事务自调用问题
 * </p>
 *
 * @author zyq
 * @since 2025-12-17
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiResultPersistenceServiceImpl implements AiResultPersistenceService {

    private final SnowflakeIdGenerator idGenerator;
    private final DocumentFileMapper fileMapper;
    private final DocumentExtractMainMapper extractMainMapper;
    private final DocumentExtractDetailMapper extractDetailMapper;
    private final ObjectMapper objectMapper;

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_UNVERIFIED = 0;

    /** 课程名字段（本地扩展字段，不在通用 AiConstants 中） */
    private static final String FIELD_COURSE_NAME = "course_name";

    /** 成绩字段 */
    private static final String FIELD_SCORE = "score";

    /** 等级字段 */
    private static final String FIELD_GRADE = "grade";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void insertAiResult(Long fileId, JsonNode aiResult, String fileUrl) {
        try {
            DocumentExtractMain extractMain = buildExtractMain(fileId, aiResult, fileUrl);
            extractMainMapper.insert(extractMain);
            saveCourseDetails(fileId, extractMain.getId(), aiResult);
            log.info("AI结果保存完成: fileId={}", fileId);
        } catch (Exception e) {
            log.error("保存AI结果失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "保存AI结果失败");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateFileStatus(Long fileId, Integer processStatus, String failReason) {
        DocumentFile file = fileMapper.selectById(fileId);
        if (file == null) {
            log.warn("文件不存在，无法更新状态: fileId={}", fileId);
            return;
        }
        file.setProcessStatus(processStatus);
        if (StringUtils.hasText(failReason)) {
            file.setFailReason(failReason);
        }
        fileMapper.updateById(file);
        log.info("文件状态更新成功: fileId={}, status={}", fileId, processStatus);
    }

    /**
     * 构建提取主表记录
     */
    private DocumentExtractMain buildExtractMain(Long fileId, JsonNode aiResult, String fileUrl) throws Exception {
        DocumentExtractMain extractMain = new DocumentExtractMain();
        extractMain.setId(idGenerator.nextId());
        extractMain.setFileId(fileId);
        extractMain.setDocumentType(getStringFromJson(aiResult, AiConstants.FIELD_DOCUMENT_TYPE));
        extractMain.setDocumentUrl(fileUrl);
        extractMain.setRawText(getStringFromJson(aiResult, AiConstants.FIELD_TEXT));
        extractMain.setSummary(getStringFromJson(aiResult, AiConstants.FIELD_SUMMARY));
        extractMain.setConfidence(getConfidenceFromJson(aiResult));
        extractMain.setExtractResult(objectMapper.writeValueAsString(aiResult));
        extractMain.setStatus(STATUS_PENDING);

        extractBasicInfo(extractMain, aiResult);
        extractJsonFields(extractMain, aiResult);

        return extractMain;
    }

    /**
     * 提取基本信息到主表
     */
    private void extractBasicInfo(DocumentExtractMain extractMain, JsonNode aiResult) {
        if (!aiResult.has(AiConstants.FIELD_BASIC_INFO) || !aiResult.get(AiConstants.FIELD_BASIC_INFO).isObject()) {
            return;
        }
        JsonNode basicInfo = aiResult.get(AiConstants.FIELD_BASIC_INFO);
        String name = getStringFromJson(basicInfo, AiConstants.FIELD_NAME);
        String studentId = getStringFromJson(basicInfo, AiConstants.FIELD_STUDENT_ID);
        String idNumber = getStringFromJson(basicInfo, AiConstants.FIELD_ID_NUMBER);

        if (StringUtils.hasText(name)) {
            extractMain.setOwnerName(name);
        }
        if (StringUtils.hasText(studentId)) {
            extractMain.setOwnerId(studentId);
        } else if (StringUtils.hasText(idNumber)) {
            extractMain.setOwnerId(idNumber);
        }
    }

    /**
     * 提取各类JSON字段
     */
    private void extractJsonFields(DocumentExtractMain extractMain, JsonNode aiResult) throws Exception {
        if (aiResult.has(AiConstants.FIELD_BASIC_INFO) && aiResult.get(AiConstants.FIELD_BASIC_INFO).isObject()) {
            extractMain.setBasicInfoJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_BASIC_INFO)));
        }
        if (aiResult.has(AiConstants.FIELD_ACADEMIC_INFO) && aiResult.get(AiConstants.FIELD_ACADEMIC_INFO).isObject()) {
            extractMain.setAcademicInfoJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_ACADEMIC_INFO)));
        }
        if (aiResult.has(AiConstants.FIELD_CERTIFICATE_INFO) && aiResult.get(AiConstants.FIELD_CERTIFICATE_INFO).isObject()) {
            extractMain.setCertificateInfoJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_CERTIFICATE_INFO)));
        }
        if (aiResult.has(AiConstants.FIELD_FINANCIAL_INFO) && aiResult.get(AiConstants.FIELD_FINANCIAL_INFO).isObject()) {
            extractMain.setFinancialInfoJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_FINANCIAL_INFO)));
        }
        if (aiResult.has(AiConstants.FIELD_LEAVE_INFO) && aiResult.get(AiConstants.FIELD_LEAVE_INFO).isObject()) {
            extractMain.setLeaveInfoJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_LEAVE_INFO)));
        }
        if (aiResult.has(AiConstants.FIELD_TABLES) && aiResult.get(AiConstants.FIELD_TABLES).isArray()) {
            extractMain.setTablesJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_TABLES)));
        }
        if (aiResult.has(AiConstants.FIELD_FIELDS) && aiResult.get(AiConstants.FIELD_FIELDS).isObject()) {
            extractMain.setKvDataJson(objectMapper.writeValueAsString(aiResult.get(AiConstants.FIELD_FIELDS)));
        }
    }

    /**
     * 保存课程详情
     */
    private void saveCourseDetails(Long fileId, Long mainId, JsonNode aiResult) {
        if (!aiResult.has(AiConstants.FIELD_COURSES) || !aiResult.get(AiConstants.FIELD_COURSES).isArray()) {
            return;
        }
        JsonNode courses = aiResult.get(AiConstants.FIELD_COURSES);
        int rowIndex = 0;
        for (JsonNode course : courses) {
            DocumentExtractDetail detail = new DocumentExtractDetail();
            detail.setId(idGenerator.nextId());
            detail.setFileId(fileId);
            detail.setMainId(mainId);
            detail.setRowIndex(rowIndex++);
            detail.setRowDataJson(course.toString());
            detail.setIsVerified(STATUS_UNVERIFIED);

            if (course.has(AiConstants.FIELD_COURSE)) {
                detail.setFieldName(course.get(AiConstants.FIELD_COURSE).asText());
            } else if (course.has(FIELD_COURSE_NAME)) {
                detail.setFieldName(course.get(FIELD_COURSE_NAME).asText());
            }
            if (course.has(FIELD_SCORE)) {
                detail.setFieldValue(course.get(FIELD_SCORE).asText());
            } else if (course.has(FIELD_GRADE)) {
                detail.setFieldValue(course.get(FIELD_GRADE).asText());
            }

            extractDetailMapper.insert(detail);
        }
    }

    private String getStringFromJson(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        return node.get(field).asText();
    }

    private Double getConfidenceFromJson(JsonNode aiResult) {
        if (aiResult.has(AiConstants.FIELD_CONFIDENCE) && !aiResult.get(AiConstants.FIELD_CONFIDENCE).isNull()) {
            return aiResult.get(AiConstants.FIELD_CONFIDENCE).asDouble();
        }
        return null;
    }
}

