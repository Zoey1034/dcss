package cn.masu.dcs.service;

import cn.masu.dcs.vo.*;

/**
 * 数据驾驶舱服务接口
 *
 * @author zyq
 * @since 2025-12-07
 */
public interface DashboardService {

    /**
     * 获取概览统计数据
     *
     * @return 概览统计
     */
    DashboardOverviewVO queryOverview();

    /**
     * 获取趋势数据
     *
     * @param days 天数
     * @return 趋势数据
     */
    DashboardTrendVO queryTrend(Integer days);

    /**
     * 获取效率分析数据
     *
     * @return 效率分析
     */
    EfficiencyAnalysisVO queryEfficiencyAnalysis();

    /**
     * 获取置信度分布
     *
     * @return 置信度分布
     */
    ConfidenceDistributionVO queryConfidenceDistribution();

    /**
     * 获取文件类型分布
     *
     * @return 文件类型分布
     */
    FileTypeDistributionVO queryFileTypeDistribution();

    /**
     * 获取处理状态分布
     *
     * @return 处理状态分布
     */
    StatusDistributionVO queryStatusDistribution();
}

