package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.vo.DocumentClassificationVO;
import cn.masu.dcs.vo.DocumentDetailVO;
import cn.masu.dcs.service.ClassificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 文档分类查询控制器
 * <p>
 * 提供按文档类型分类查询的功能
 * </p>
 *
 * @author zyq
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/api/classification")
@RequiredArgsConstructor
public class ClassificationController {

    private final ClassificationService classificationService;

    /**
     * 获取所有文档类型统计
     *
     * @return 文档类型及数量统计
     */
    @GetMapping("/types")
    public R<List<Map<String, Object>>> listDocumentTypes() {
        List<Map<String, Object>> types = classificationService.queryDocumentTypeStatistics();
        return R.ok(types);
    }

    /**
     * 按文档类型分页查询文件列表
     *
     * @param documentType 文档类型，如："成绩单"、"毕业证书/学历证书"
     * @param current 当前页
     * @param size 每页数量
     * @param keyword 搜索关键词（可选）
     * @return 分页结果
     */
    @GetMapping("/list")
    public R<PageResult<DocumentClassificationVO>> listDocumentsByType(
            @RequestParam String documentType,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword) {

        PageResult<DocumentClassificationVO> result = classificationService
                .queryDocumentsByType(documentType, current, size, keyword);
        return R.ok(result);
    }

    /**
     * 获取文档详细信息（包含AI识别结果）
     *
     * @param fileId 文件ID
     * @return 文档详细信息
     */
    @GetMapping("/detail/{fileId}")
    public R<DocumentDetailVO> getDocumentDetail(@PathVariable String fileId) {
        DocumentDetailVO detail = classificationService.queryDocumentById(fileId);
        return R.ok(detail);
    }

    /**
     * 搜索文档（跨类型搜索）
     *
     * @param keyword 关键词
     * @param current 当前页
     * @param size 每页数量
     * @return 搜索结果
     */
    @GetMapping("/search")
    public R<PageResult<DocumentClassificationVO>> listSearchDocuments(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {

        PageResult<DocumentClassificationVO> result = classificationService
                .queryDocumentsByKeyword(keyword, current, size);
        return R.ok(result);
    }
}

