package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.R;
import cn.masu.dcs.service.DashboardService;
import cn.masu.dcs.vo.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 数据驾驶舱控制器
 * <p>
 * 提供系统运行状态、统计分析、趋势图表等数据
 * </p>
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取概览统计数据
     * <p>
     * 包含：
     * 1. 文件统计（总数、已处理、待处理、待审核）
     * 2. 任务统计（总数、成功数、失败数、平均置信度）
     * 3. 效率统计（AI处理时间、人工校对时间、直通率）
     * </p>
     *
     * @return 概览统计数据
     */
    @GetMapping("/overview")
    public R<DashboardOverviewVO> getOverview() {
        log.info("获取驾驶舱概览数据");
        DashboardOverviewVO overview = dashboardService.queryOverview();
        return R.ok(overview);
    }

    /**
     * 获取统计数据（别名接口，兼容前端）
     *
     * @return 概览统计数据
     */
    @GetMapping("/stats")
    public R<DashboardOverviewVO> getStats() {
        log.info("获取驾驶舱统计数据(stats接口)");
        return getOverview();
    }

    /**
     * 获取趋势数据
     * <p>
     * 支持查询最近N天的趋势数据
     * </p>
     *
     * @param days 天数（默认7天）
     * @return 趋势数据
     */
    @GetMapping("/trend")
    public R<DashboardTrendVO> getTrend(@RequestParam(defaultValue = "7") Integer days) {
        log.info("获取趋势数据: days={}", days);
        DashboardTrendVO trend = dashboardService.queryTrend(days);
        return R.ok(trend);
    }

    /**
     * 获取效率分析数据
     *
     * @return 效率分析数据
     */
    @GetMapping("/efficiency")
    public R<EfficiencyAnalysisVO> getEfficiencyAnalysis() {
        log.info("获取效率分析数据");
        EfficiencyAnalysisVO efficiency = dashboardService.queryEfficiencyAnalysis();
        return R.ok(efficiency);
    }

    /**
     * 获取置信度分布数据
     *
     * @return 置信度分布数据
     */
    @GetMapping("/confidence-distribution")
    public R<ConfidenceDistributionVO> getConfidenceDistribution() {
        log.info("获取置信度分布数据");
        ConfidenceDistributionVO distribution = dashboardService.queryConfidenceDistribution();
        return R.ok(distribution);
    }

    /**
     * 获取文件类型分布
     *
     * @return 文件类型分布
     */
    @GetMapping("/file-type-distribution")
    public R<FileTypeDistributionVO> getFileTypeDistribution() {
        log.info("获取文件类型分布");
        FileTypeDistributionVO distribution = dashboardService.queryFileTypeDistribution();
        return R.ok(distribution);
    }

    /**
     * 获取处理状态分布
     *
     * @return 处理状态分布
     */
    @GetMapping("/status-distribution")
    public R<StatusDistributionVO> getStatusDistribution() {
        log.info("获取处理状态分布");
        StatusDistributionVO distribution = dashboardService.queryStatusDistribution();
        return R.ok(distribution);
    }
}

