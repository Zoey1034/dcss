package cn.masu.dcs.service.impl;

import cn.masu.dcs.business.DocumentProcessBusiness;
import cn.masu.dcs.dto.AiDocProcessRequest;
import cn.masu.dcs.service.AiProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * AI文档处理服务实现
 * <p>
 * 委托给 {@link DocumentProcessBusiness} 执行实际的多服务编排逻辑。
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiProcessorServiceImpl implements AiProcessorService {

    private final DocumentProcessBusiness documentProcessBusiness;

    @Override
    public Map<String, Object> processDocument(AiDocProcessRequest request) {
        return documentProcessBusiness.processDocument(request);
    }

    @Override
    public Map<String, Object> processFile(MultipartFile file, String optionsJson, String templateConfigJson) {
        return documentProcessBusiness.processFile(file, optionsJson, templateConfigJson);
    }

    @Override
    public Map<String, Object> queryServiceStatus() {
        return documentProcessBusiness.queryServiceStatus();
    }
}

