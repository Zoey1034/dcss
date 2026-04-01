package cn.masu.dcs.service;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.vo.DocumentClassificationVO;
import cn.masu.dcs.vo.DocumentDetailVO;

import java.util.List;
import java.util.Map;

/**
 * 文档分类服务接口
 *
 * @author zyq
 * @since 2025-12-09
 */
public interface ClassificationService {

    /**
     * 获取文档类型统计
     *
     * @return 文档类型及数量
     */
    List<Map<String, Object>> queryDocumentTypeStatistics();

    /**
     * 按文档类型分页查询
     *
     * @param documentType 文档类型
     * @param current 当前页
     * @param size 每页数量
     * @param keyword 搜索关键词
     * @return 分页结果
     */
    PageResult<DocumentClassificationVO> queryDocumentsByType(
            String documentType, Long current, Long size, String keyword);

    /**
     * 获取文档详细信息
     *
     * @param fileId 文件ID
     * @return 详细信息
     */
    DocumentDetailVO queryDocumentById(String fileId);

    /**
     * 搜索文档
     *
     * @param keyword 关键词
     * @param current 当前页
     * @param size 每页数量
     * @return 搜索结果
     */
    PageResult<DocumentClassificationVO> queryDocumentsByKeyword(
            String keyword, Long current, Long size);
}

