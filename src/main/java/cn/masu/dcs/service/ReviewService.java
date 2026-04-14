package cn.masu.dcs.service;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.dto.ReviewQueryDTO;
import cn.masu.dcs.dto.ReviewSaveDTO;
import cn.masu.dcs.dto.ReviewCompleteDTO;
import cn.masu.dcs.vo.ReviewTaskVO;
import cn.masu.dcs.vo.ReviewDetailVO;
import cn.masu.dcs.vo.AuditHistoryVO;

import java.util.List;
import java.util.Map;

/**
 * 校对服务接口
 *
 * @author zyq
 * @since 2025-12-07
 */
public interface ReviewService {

    /**
     * 获取待审核任务列表
     *
     * @param dto 查询参数
     * @return 待审核任务分页列表
     */
    PageResult<ReviewTaskVO> queryPendingTasks(ReviewQueryDTO dto);

    /**
     * 获取任务详情
     *
     * @param fileId 文件ID
     * @return 任务详情
     */
    ReviewDetailVO queryReviewById(Long fileId);

    /**
     * 保存草稿
     *
     * @param dto 保存参数
     * @param userId 用户ID
     * @return 保存结果
     */
    Boolean insertDraft(ReviewSaveDTO dto, Long userId);

    /**
     * 完成校对
     *
     * @param dto 完成参数
     * @param userId 用户ID
     * @return 完成结果
     */
    Boolean updateReviewComplete(ReviewCompleteDTO dto, Long userId);

    /**
     * 获取审核历史
     *
     * @param fileId 文件ID
     * @return 审核历史列表
     */
    List<AuditHistoryVO> queryAuditHistory(Long fileId);

    /**
     * 获取文件预览URL
     *
     * @param fileId 文件ID
     * @return 预览URL
     */
    String queryFilePreviewUrl(Long fileId);

    /**
     * 批量完成校对
     *
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     * @return 处理结果
     */
    Map<String, Object> updateBatchComplete(List<Long> fileIds, Long userId);
}

