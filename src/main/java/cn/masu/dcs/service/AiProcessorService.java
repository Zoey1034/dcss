package cn.masu.dcs.service;

import cn.masu.dcs.dto.AiDocProcessRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * AI文档处理服务接口
 * <p>
 * 提供统一的AI文档处理能力，支持OCR、NLP、LLM等多种模式
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
public interface AiProcessorService {

    /**
     * 处理文档（支持fileId、fileContent、text三种输入）
     *
     * @param request 处理请求
     * @return 处理结果，包含fileId和aiResult
     */
    Map<String, Object> processDocument(AiDocProcessRequest request);

    /**
     * 处理上传的文件
     *
     * @param file               上传的文件
     * @param optionsJson        处理选项JSON字符串
     * @param templateConfigJson 模板配置JSON字符串
     * @return 处理结果
     */
    Map<String, Object> processFile(MultipartFile file, String optionsJson, String templateConfigJson);

    /**
     * 获取AI服务状态
     *
     * @return 服务状态信息
     */
    Map<String, Object> queryServiceStatus();
}
