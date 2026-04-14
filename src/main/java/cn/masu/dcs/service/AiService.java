package cn.masu.dcs.service;

import cn.masu.dcs.dto.AiProcessRequest;
import cn.masu.dcs.vo.AiProcessResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI智能处理业务服务接口
 * <p>
 * 封装AI处理的核心业务逻辑
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
public interface AiService {

    /**
     * 文件上传与智能解析
     * <p>
     * 完成文件上传、MinIO存储和AI智能解析的完整流程
     * </p>
     *
     * @param file       上传的文件
     * @param templateId 模板ID
     * @param userId     用户ID
     * @return AI解析结果
     */
    AiProcessResponse uploadAndProcess(MultipartFile file, Long templateId, Long userId);

    /**
     * 纯文本智能解析
     *
     * @param request AI处理请求
     * @return AI解析结果
     */
    AiProcessResponse processText(AiProcessRequest request);

    /**
     * 重新解析文件
     *
     * @param fileId     文件ID
     * @param templateId 新模板ID（可选）
     * @return AI解析结果
     */
    AiProcessResponse reprocessFile(Long fileId, Long templateId);

    /**
     * 保存校对后的数据
     *
     * @param fileId   文件ID
     * @param response 校对后的数据
     */
    void updateVerifiedData(Long fileId, AiProcessResponse response);

    /**
     * 批量上传与解析
     *
     * @param files      文件列表
     * @param templateId 模板ID
     * @param userId     用户ID
     * @return 批量处理结果
     */
    List<AiProcessResponse> batchUploadAndProcess(MultipartFile[] files, Long templateId, Long userId);
}

