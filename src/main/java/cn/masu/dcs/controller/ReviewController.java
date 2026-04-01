package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.ReviewQueryDTO;
import cn.masu.dcs.dto.ReviewSaveDTO;
import cn.masu.dcs.dto.ReviewCompleteDTO;
import cn.masu.dcs.service.ReviewService;
import cn.masu.dcs.vo.ReviewTaskVO;
import cn.masu.dcs.vo.ReviewDetailVO;
import cn.masu.dcs.vo.AuditHistoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 校对工作台控制器
 * <p>
 * 提供待审核任务管理、字段编辑、校对完成等功能
 * </p>
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 获取待审核任务列表
     * <p>
     * 查询process_status=3（待人工）的文件
     * </p>
     *
     * @param dto 查询参数
     * @return 待审核任务分页列表
     */
    @GetMapping("/pending")
    public R<PageResult<ReviewTaskVO>> listPendingTasks(@Validated ReviewQueryDTO dto) {
        log.info("查询待审核任务列表: {}", dto);
        PageResult<ReviewTaskVO> result = reviewService.queryPendingTasks(dto);
        return R.ok(result);
    }

    /**
     * 获取任务详情
     * <p>
     * 包含：
     * 1. 文件基本信息
     * 2. AI识别的字段数据
     * 3. 字段置信度
     * 4. 历史修改记录
     * </p>
     *
     * @param fileId 文件ID
     * @return 任务详情
     */
    @GetMapping("/{fileId}/detail")
    public R<ReviewDetailVO> getReviewDetail(@PathVariable Long fileId) {
        log.info("获取任务详情: fileId={}", fileId);
        ReviewDetailVO detail = reviewService.queryReviewById(fileId);
        return R.ok(detail);
    }

    /**
     * 保存字段修改（草稿）
     * <p>
     * 保存当前修改，不改变文件状态
     * 记录字段修改历史
     * </p>
     *
     * @param dto 保存参数
     * @return 保存结果
     */
    @PostMapping("/save-draft")
    public R<Boolean> addDraft(@Validated @RequestBody ReviewSaveDTO dto) {
        Long userId = getCurrentUserId();
        log.info("保存草稿: fileId={}, userId={}", dto.getFileId(), userId);
        Boolean result = reviewService.insertDraft(dto, userId);
        return R.ok("保存成功", result);
    }

    /**
     * 完成校对
     * <p>
     * 1. 保存所有字段修改
     * 2. 更新文件状态为已归档（process_status=4）
     * 3. 创建审核记录
     * </p>
     *
     * @param dto 完成参数
     * @return 完成结果
     */
    @PostMapping("/complete")
    public R<Boolean> editReviewComplete(@Validated @RequestBody ReviewCompleteDTO dto) {
        Long userId = getCurrentUserId();
        log.info("完成校对: fileId={}, userId={}", dto.getFileId(), userId);
        Boolean result = reviewService.updateReviewComplete(dto, userId);
        return R.ok("校对完成", result);
    }

    /**
     * 获取审核历史
     *
     * @param fileId 文件ID
     * @return 审核历史列表
     */
    @GetMapping("/{fileId}/history")
    public R<List<AuditHistoryVO>> listAuditHistory(@PathVariable Long fileId) {
        log.info("获取审核历史: fileId={}", fileId);
        List<AuditHistoryVO> history = reviewService.queryAuditHistory(fileId);
        return R.ok(history);
    }

    /**
     * 获取文件预览URL
     *
     * @param fileId 文件ID
     * @return 预览URL
     */
    @GetMapping("/{fileId}/preview-url")
    public R<String> getPreviewUrl(@PathVariable Long fileId) {
        log.info("获取文件预览URL: fileId={}", fileId);
        String url = reviewService.queryFilePreviewUrl(fileId);
        return R.ok(url);
    }

    /**
     * 批量完成校对
     *
     * @param fileIds 文件ID列表
     * @return 处理结果
     */
    @PostMapping("/batch-complete")
    public R<Map<String, Object>> editBatchComplete(@RequestBody List<Long> fileIds) {
        Long userId = getCurrentUserId();
        log.info("批量完成校对: fileIds={}, count={}", fileIds, fileIds.size());
        Map<String, Object> result = reviewService.updateBatchComplete(fileIds, userId);
        return R.ok(result);
    }

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Object principal = authentication.getPrincipal();
            if (principal instanceof Map) {

                Map<String, Object> userMap = (Map<String, Object>) principal;
                Object userIdObj = userMap.get("userId");
                if (userIdObj instanceof Long) {
                    return (Long) userIdObj;
                } else if (userIdObj instanceof Integer) {
                    return ((Integer) userIdObj).longValue();
                } else if (userIdObj != null) {
                    return Long.parseLong(userIdObj.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败", e);
        }
        throw new RuntimeException("未登录或登录已过期");
    }
}

