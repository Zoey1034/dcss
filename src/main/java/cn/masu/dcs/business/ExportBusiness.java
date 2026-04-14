package cn.masu.dcs.business;

import cn.masu.dcs.service.DashboardService;
import cn.masu.dcs.service.ExportService;
import cn.masu.dcs.vo.DashboardOverviewVO;
import cn.masu.dcs.vo.DashboardTrendVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 导出业务层
 * <p>
 * 协调 {@link ExportService} 和 {@link DashboardService}，生成综合统计报表。
 * 当导出操作需要跨多个服务聚合数据时，在此处进行编排。
 * </p>
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExportBusiness {

    private final ExportService exportService;
    private final DashboardService dashboardService;

    /**
     * 生成统计报表
     * <p>
     * 根据报表类型从 DashboardService 获取统计数据，并通过 ExportService 格式化为 Excel 输出流。
     * </p>
     *
     * @param type 报表类型（overview / trend）
     * @return Excel 输出流
     */
    public ByteArrayOutputStream generateReport(String type) {
        log.info("开始生成统计报表: type={}", type);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            if ("overview".equals(type)) {
                buildOverviewSheet(workbook);
            } else if ("trend".equals(type)) {
                buildTrendSheet(workbook);
            } else {
                throw new IllegalArgumentException("不支持的报表类型: " + type);
            }

            workbook.write(outputStream);
            log.info("统计报表生成完成: type={}", type);
            return outputStream;

        } catch (IOException e) {
            log.error("生成统计报表失败: type={}", type, e);
            throw new RuntimeException("报表生成失败", e);
        }
    }

    /**
     * 构建概览统计 Sheet
     * <p>
     * 从 DashboardService 查询概览数据并填充到工作簿中
     * </p>
     *
     * @param workbook Excel 工作簿
     */
    private void buildOverviewSheet(Workbook workbook) {
        DashboardOverviewVO overview = dashboardService.queryOverview();
        Sheet sheet = workbook.createSheet("概览统计");
        CellStyle headerStyle = createHeaderStyle(workbook);
        int rowNum = 0;

        /* 文件统计区块 */
        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("文件统计");
        titleCell.setCellStyle(headerStyle);

        writeDataRow(sheet, rowNum++, "文件总数", overview.getFileStats().getTotal());
        writeDataRow(sheet, rowNum++, "已处理", overview.getFileStats().getProcessed());
        writeDataRow(sheet, rowNum++, "待处理", overview.getFileStats().getPending());
        writeDataRow(sheet, rowNum++, "待审核", overview.getFileStats().getNeedReview());
        writeDataRow(sheet, rowNum++, "已归档", overview.getFileStats().getArchived());
        writeDataRow(sheet, rowNum++, "失败", overview.getFileStats().getFailed());
        rowNum++;

        /* 任务统计区块 */
        titleRow = sheet.createRow(rowNum++);
        titleCell = titleRow.createCell(0);
        titleCell.setCellValue("任务统计");
        titleCell.setCellStyle(headerStyle);

        writeDataRow(sheet, rowNum++, "任务总数", overview.getTaskStats().getTotal());
        writeDataRow(sheet, rowNum++, "成功数", overview.getTaskStats().getSuccess());
        writeDataRow(sheet, rowNum, "平均置信度", overview.getTaskStats().getAvgConfidence());

        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);
    }

    /**
     * 构建趋势数据 Sheet
     * <p>
     * 从 DashboardService 查询最近 7 天趋势并填充到工作簿中
     * </p>
     *
     * @param workbook Excel 工作簿
     */
    private void buildTrendSheet(Workbook workbook) {
        /* 查询最近 7 天趋势数据 */
        DashboardTrendVO trend = dashboardService.queryTrend(7);
        Sheet sheet = workbook.createSheet("趋势数据");
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = {"日期", "文件数", "平均置信度", "成功数", "失败数", "待审核数"};
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<String> dates = trend.getDates();
        List<Long> fileCount = trend.getFileCount();
        List<Double> avgConfidence = trend.getAvgConfidence();
        List<Long> successCount = trend.getSuccessCount();
        List<Long> failCount = trend.getFailCount();
        List<Long> reviewCount = trend.getReviewCount();

        for (int i = 0; i < dates.size(); i++) {
            Row row = sheet.createRow(i + 1);
            row.createCell(0).setCellValue(dates.get(i));
            row.createCell(1).setCellValue(fileCount.get(i));
            row.createCell(2).setCellValue(avgConfidence.get(i));
            row.createCell(3).setCellValue(successCount.get(i));
            row.createCell(4).setCellValue(failCount.get(i));
            row.createCell(5).setCellValue(reviewCount.get(i));
        }

        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    /**
     * 创建 Excel 表头单元格样式
     *
     * @param workbook 工作簿
     * @return 表头样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * 向指定行写入标签与数值
     *
     * @param sheet  工作表
     * @param rowNum 行号
     * @param label  标签
     * @param value  值
     */
    private void writeDataRow(Sheet sheet, int rowNum, String label, Object value) {
        Row row = sheet.createRow(rowNum);
        row.createCell(0).setCellValue(label);
        if (value instanceof Number) {
            row.createCell(1).setCellValue(((Number) value).doubleValue());
        } else if (value != null) {
            row.createCell(1).setCellValue(value.toString());
        }
    }
}
