package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.AiDocProcessRequest;
import cn.masu.dcs.service.AiProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * AI处理控制器
 * <p>
 * 提供统一的AI文档处理接口，支持OCR、NLP、LLM等多种模式
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiProcessController {

    private final AiProcessorService aiProcessorService;

    /**
     * 统一AI处理接口
     *
     * @param request 处理请求
     * @return AI处理结果
     */
    @PostMapping("/process")
    public R<Map<String, Object>> processDocument(@RequestBody AiDocProcessRequest request) {
        log.info("收到AI处理请求");
        Map<String, Object> result = aiProcessorService.processDocument(request);
        return R.ok(result);
    }

    /**
     * 文件上传处理接口
     *
     * @param file               上传的文件
     * @param optionsJson        处理选项JSON字符串
     * @param templateConfigJson 模板配置JSON字符串
     * @return AI处理结果
     */
    @PostMapping("/process-file")
    public R<Map<String, Object>> processFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "options", required = false) String optionsJson,
            @RequestParam(value = "template_config", required = false) String templateConfigJson) {
        log.info("收到文件上传请求: fileName={}, size={}", file.getOriginalFilename(), file.getSize());
        Map<String, Object> result = aiProcessorService.processFile(file, optionsJson, templateConfigJson);
        return R.ok(result);
    }

    /**
     * 获取AI服务状态
     *
     * @return 服务状态信息
     */
    @GetMapping("/status")
    public R<Map<String, Object>> getStatus() {
        Map<String, Object> status = aiProcessorService.queryServiceStatus();
        return R.ok(status);
    }
}

