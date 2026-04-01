package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.dto.AiProcessRequest;
import cn.masu.dcs.dto.AiDocProcessRequest;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.entity.SysDocTemplate;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.mapper.SysDocTemplateMapper;

import cn.masu.dcs.service.AiService;
import cn.masu.dcs.service.AiProcessorService;
import cn.masu.dcs.service.FileService;
import cn.masu.dcs.vo.AiProcessResponse;
import cn.masu.dcs.vo.FileDetailVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * AI智能处理业务服务实现
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final FileService fileService;
    private final AiProcessorService aiProcessorService;
    private final DocumentFileMapper fileMapper;
    private final SysDocTemplateMapper templateMapper;

    /**
     * 批量处理最大文件数限制
     */
    private static final int MAX_BATCH_FILES = 50;

    /**
     * 已归档状态
     */
    private static final int STATUS_ARCHIVED = 4;

    /**
     * 结果字段名常量
     */
    private static final String KEY_FILE_ID = "fileId";
    private static final String KEY_AI_RESULT = "aiResult";
    private static final String KEY_CONFIDENCE = "confidence_overall";

    @Override
    public AiProcessResponse uploadAndProcess(MultipartFile file, Long templateId, Long userId) {
        // 参数校验
        validateFile(file);
        validateUserId(userId);

        String fileName = file.getOriginalFilename();
        log.info("开始处理文件上传与解析: fileName={}, fileSize={}, templateId={}, userId={}",
                fileName, file.getSize(), templateId, userId);

        try {
            // 1. 上传文件到MinIO并创建记录
            Long fileId = fileService.insertFile(file, templateId, userId);
            log.info("文件上传成功: fileId={}, fileName={}", fileId, fileName);

            // 2. 调用AI服务解析
            AiProcessResponse response = processFileWithAi(fileId);
            response.setFileId(fileId);
            response.setFileName(fileName);

            log.info("文件解析完成: fileId={}, fileName={}, confidence={}",
                    fileId, fileName, response.getConfidence());

            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件上传与解析失败: fileName={}", fileName, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "文件上传与解析失败: " + e.getMessage());
        }
    }

    @Override
    public AiProcessResponse processText(AiProcessRequest request) {
        // 参数校验
        if (request.getText() == null && request.getFilePath() == null) {
            log.warn("文本和文件路径不能同时为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                    "文本内容或文件路径至少提供一个");
        }

        int textLength = request.getText() != null ? request.getText().length() : 0;
        log.info("开始处理文本解析: textLength={}, fileId={}", textLength, request.getFileId());

        try {
            // 转换为新的请求格式
            AiDocProcessRequest docRequest = convertToDocRequest(request);
            Map<String, Object> result = aiProcessorService.processDocument(docRequest);

            // 转换结果为 AiProcessResponse
            AiProcessResponse response = convertToAiProcessResponse(result);
            validateAiResponse(response, "文本解析");

            log.info("文本解析完成: confidence={}", response.getConfidence());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文本解析失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "文本解析失败: " + e.getMessage());
        }
    }

    @Override
    public AiProcessResponse reprocessFile(Long fileId, Long templateId) {
        log.info("开始重新解析文件: fileId={}, templateId={}", fileId, templateId);

        try {
            // 1. 获取文件详情
            FileDetailVO fileDetail = getFileDetailOrThrow(fileId);

            // 2. 如果提供了新模板ID，更新文件关联
            if (templateId != null && !Objects.equals(templateId, fileDetail.getTemplateId())) {
                updateFileTemplate(fileId, templateId, fileDetail.getTemplateId());
            }

            // 3. 重新解析
            AiProcessResponse response = processFileWithAi(fileId);
            response.setFileId(fileId);
            response.setFileName(fileDetail.getFileName());

            log.info("文件重新解析完成: fileId={}, confidence={}", fileId, response.getConfidence());
            return response;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("文件重新解析失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "文件重新解析失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateVerifiedData(Long fileId, AiProcessResponse response) {
        log.info("开始保存校对数据: fileId={}", fileId);

        // 校验数据一致性
        validateDataConsistency(fileId, response);

        // 验证文件存在
        getFileDetailOrThrow(fileId);

        try {
            // 获取文件实体并更新状态为已归档
            DocumentFile file = fileMapper.selectById(fileId);
            if (file != null) {
                file.setProcessStatus(STATUS_ARCHIVED);
                fileMapper.updateById(file);
            }

            log.info("数据保存成功: fileId={}", fileId);

        } catch (Exception e) {
            log.error("数据保存失败: fileId={}", fileId, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(),
                    "数据保存失败: " + e.getMessage());
        }
    }

    @Override
    public List<AiProcessResponse> batchUploadAndProcess(MultipartFile[] files, Long templateId, Long userId) {
        // 参数校验
        validateBatchFiles(files);
        validateUserId(userId);

        log.info("开始批量处理: fileCount={}, templateId={}, userId={}",
                files.length, templateId, userId);

        List<AiProcessResponse> responses = new ArrayList<>(files.length);
        int successCount = 0;
        int failCount = 0;

        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];
            String fileName = file.getOriginalFilename();

            // 跳过空文件
            if (file.isEmpty()) {
                log.warn("跳过空文件: index={}, fileName={}", i, fileName);
                responses.add(createErrorResponse(fileName, "文件为空"));
                failCount++;
                continue;
            }

            try {
                AiProcessResponse response = uploadAndProcess(file, templateId, userId);
                responses.add(response);
                successCount++;
                log.info("批量处理成功: index={}, fileId={}, fileName={}",
                        i, response.getFileId(), fileName);

            } catch (Exception e) {
                log.error("批量处理失败: index={}, fileName={}", i, fileName, e);
                responses.add(createErrorResponse(fileName, e.getMessage()));
                failCount++;
            }
        }

        log.info("批量处理完成: 总数={}, 成功={}, 失败={}", files.length, successCount, failCount);
        return responses;
    }

    /**
     * 校验文件
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("上传的文件为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "上传的文件不能为空");
        }
    }

    /**
     * 校验用户ID
     */
    private void validateUserId(Long userId) {
        if (userId == null) {
            log.error("用户ID为空");
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    /**
     * 校验批量文件
     */
    private void validateBatchFiles(MultipartFile[] files) {
        if (files == null || files.length == 0) {
            log.warn("批量上传文件列表为空");
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "至少上传一个文件");
        }

        if (files.length > MAX_BATCH_FILES) {
            log.warn("批量上传文件数量超过限制: count={}, max={}", files.length, MAX_BATCH_FILES);
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(),
                    "批量上传文件数量不能超过" + MAX_BATCH_FILES + "个");
        }
    }

    /**
     * 获取文件详情，不存在则抛异常
     */
    private FileDetailVO getFileDetailOrThrow(Long fileId) {
        FileDetailVO fileDetail = fileService.queryFileById(fileId);
        if (fileDetail == null) {
            log.error("文件不存在: fileId={}", fileId);
            throw new BusinessException(ErrorCode.FILE_NOT_FOUND);
        }
        return fileDetail;
    }

    /**
     * 使用AI服务处理文件
     */
    private AiProcessResponse processFileWithAi(Long fileId) {
        // 获取文件信息，检查是否有模板配置
        DocumentFile file = fileMapper.selectById(fileId);
        if (file != null && file.getTemplateId() != null) {
            SysDocTemplate template = templateMapper.selectById(file.getTemplateId());
            if (template != null) {
                log.info("文件关联模板: templateId={}, templateCode={}",
                    template.getId(), template.getTemplateCode());
            }
        }

        // 构建请求
        AiDocProcessRequest docRequest = new AiDocProcessRequest();
        docRequest.setFileId(fileId);

        // 调用AI服务
        Map<String, Object> result = aiProcessorService.processDocument(docRequest);

        // 转换结果
        AiProcessResponse response = convertToAiProcessResponse(result);
        validateAiResponse(response, "AI处理");

        return response;
    }

    /**
     * 校验AI响应
     */
    private void validateAiResponse(AiProcessResponse response, String operation) {
        if (response == null) {
            log.error("{}返回结果为空", operation);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), operation + "失败");
        }
    }

    /**
     * 更新文件模板
     */
    private void updateFileTemplate(Long fileId, Long newTemplateId, Long oldTemplateId) {
        boolean updated = fileService.updateFileTemplate(fileId, newTemplateId);
        if (!updated) {
            log.error("更新文件模板失败: fileId={}, templateId={}", fileId, newTemplateId);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "更新文件模板失败");
        }
        log.info("文件模板已更新: fileId={}, oldTemplateId={}, newTemplateId={}",
                fileId, oldTemplateId, newTemplateId);
    }

    /**
     * 校验数据一致性
     */
    private void validateDataConsistency(Long fileId, AiProcessResponse response) {
        if (response.getFileId() != null && !Objects.equals(fileId, response.getFileId())) {
            log.warn("文件ID不一致: pathFileId={}, bodyFileId={}", fileId, response.getFileId());
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "文件ID不一致");
        }
        response.setFileId(fileId);
    }

    /**
     * 创建错误响应
     */
    private AiProcessResponse createErrorResponse(String fileName, String errorMessage) {
        AiProcessResponse errorResponse = new AiProcessResponse();
        errorResponse.setFileName(fileName);
        errorResponse.setSuccess(false);
        errorResponse.setErrorMessage(errorMessage);
        return errorResponse;
    }

    /**
     * 将AiProcessRequest转换为AiDocProcessRequest
     */
    private AiDocProcessRequest convertToDocRequest(AiProcessRequest request) {
        AiDocProcessRequest docRequest = new AiDocProcessRequest();
        docRequest.setFileId(request.getFileId());
        docRequest.setText(request.getText());
        return docRequest;
    }

    /**
     * 将Map结果转换为AiProcessResponse
     */
    @SuppressWarnings("unchecked")
    private AiProcessResponse convertToAiProcessResponse(Map<String, Object> result) {
        if (result == null) {
            return null;
        }

        AiProcessResponse response = new AiProcessResponse();
        response.setSuccess(true);

        if (result.containsKey(KEY_FILE_ID)) {
            Object fileIdObj = result.get(KEY_FILE_ID);
            if (fileIdObj instanceof Long) {
                response.setFileId((Long) fileIdObj);
            } else if (fileIdObj instanceof Number) {
                response.setFileId(((Number) fileIdObj).longValue());
            }
        }

        if (result.containsKey(KEY_AI_RESULT)) {
            Object aiResult = result.get(KEY_AI_RESULT);
            if (aiResult instanceof Map) {
                Map<String, Object> aiResultMap = (Map<String, Object>) aiResult;
                if (aiResultMap.containsKey(KEY_CONFIDENCE)) {
                    Object conf = aiResultMap.get(KEY_CONFIDENCE);
                    if (conf instanceof Number) {
                        response.setConfidence(((Number) conf).doubleValue());
                    }
                }
            }
        }

        return response;
    }
}

