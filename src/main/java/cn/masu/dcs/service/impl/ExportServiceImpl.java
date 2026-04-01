package cn.masu.dcs.service.impl;

import cn.masu.dcs.entity.AuditRecord;
import cn.masu.dcs.entity.DocumentExtractMain;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.mapper.AuditRecordMapper;
import cn.masu.dcs.mapper.DocumentExtractMainMapper;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.service.ExportService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 导出服务实现
 * <p>
 * 使用Apache POI导出Excel文件，处理文件列表和审核记录的导出。
 * 统计报表导出（需聚合 DashboardService 数据）已移至 {@link cn.masu.dcs.business.ExportBusiness}。
 * </p>
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final DocumentFileMapper fileMapper;
    private final AuditRecordMapper auditRecordMapper;
    private final DocumentExtractMainMapper extractMainMapper;

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public ByteArrayOutputStream queryExportFiles(Integer status, String keyword) {
        log.info("开始导出文件列表");

        // 查询数据
        LambdaQueryWrapper<DocumentFile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentFile::getDeleted, 0);

        if (status != null) {
            wrapper.eq(DocumentFile::getProcessStatus, status);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.like(DocumentFile::getFileName, keyword);
        }

        List<DocumentFile> files = fileMapper.selectList(wrapper);

        // 创建工作簿
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("文件列表");

            // 创建表头样式
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"文件ID", "文件名", "文件类型", "文件大小",
                "处理状态", "创建时间", "更新时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (DocumentFile file : files) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(file.getId());
                row.createCell(1).setCellValue(file.getFileName());
                row.createCell(2).setCellValue(file.getFileType());
                row.createCell(3).setCellValue(formatFileSize(file.getFileSize()));
                row.createCell(4).setCellValue(getStatusName(file.getProcessStatus()));
                row.createCell(5).setCellValue(
                    file.getCreateTime() != null ? DATE_FORMAT.format(file.getCreateTime()) : "");
                row.createCell(6).setCellValue(
                    file.getUpdateTime() != null ? DATE_FORMAT.format(file.getUpdateTime()) : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("文件列表导出完成, 共{}条记录", files.size());
            return outputStream;

        } catch (IOException e) {
            log.error("导出文件列表失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public ByteArrayOutputStream queryExportAuditRecords(Long fileId, String startDate, String endDate) {
        log.info("开始导出审核记录");

        // 查询数据
        LambdaQueryWrapper<AuditRecord> wrapper = new LambdaQueryWrapper<>();
        if (fileId != null) {
            wrapper.eq(AuditRecord::getFileId, fileId);
        }

        // 添加日期范围筛选
        if (startDate != null && !startDate.isEmpty()) {
            try {
                LocalDateTime startDateTime = LocalDate.parse(startDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).atStartOfDay();
                wrapper.ge(AuditRecord::getCreateTime, startDateTime);
            } catch (Exception e) {
                log.warn("开始日期格式错误: {}", startDate);
            }
        }

        if (endDate != null && !endDate.isEmpty()) {
            try {
                LocalDateTime endDateTime = LocalDate.parse(endDate,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd")).atTime(LocalTime.MAX);
                wrapper.le(AuditRecord::getCreateTime, endDateTime);
            } catch (Exception e) {
                log.warn("结束日期格式错误: {}", endDate);
            }
        }

        wrapper.orderByDesc(AuditRecord::getCreateTime);

        List<AuditRecord> records = auditRecordMapper.selectList(wrapper);

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("审核记录");
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"记录ID", "文件ID", "审核人ID", "审核状态",
                "审核意见", "创建时间"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (AuditRecord record : records) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getFileId());
                row.createCell(2).setCellValue(record.getAuditorId());
                row.createCell(3).setCellValue(getAuditStatusName(record.getAuditStatus()));
                row.createCell(4).setCellValue(record.getAuditComment());
                row.createCell(5).setCellValue(
                    record.getCreateTime() != null ? DATE_FORMAT.format(record.getCreateTime()) : "");
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("审核记录导出完成, 共{}条记录", records.size());
            return outputStream;

        } catch (IOException e) {
            log.error("导出审核记录失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    @Override
    public ByteArrayOutputStream queryBatchExportData(List<Long> fileIds) {
        log.info("开始批量导出文件数据, 数量: {}", fileIds.size());

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("批量导出数据");
            CellStyle headerStyle = createHeaderStyle(workbook);

            // 创建表头
            Row headerRow = sheet.createRow(0);
            String[] headers = {"文件ID", "文件名", "姓名", "学号", "置信度", "状态"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // 填充数据
            int rowNum = 1;
            for (Long fileId : fileIds) {
                DocumentFile file = fileMapper.selectById(fileId);
                if (file == null) {
                    continue;
                }

                // 查询提取信息
                LambdaQueryWrapper<DocumentExtractMain> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(DocumentExtractMain::getFileId, fileId)
                    .orderByDesc(DocumentExtractMain::getCreateTime)
                    .last("LIMIT 1");
                DocumentExtractMain extractMain = extractMainMapper.selectOne(wrapper);

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(file.getId());
                row.createCell(1).setCellValue(file.getFileName());
                row.createCell(2).setCellValue(
                    extractMain != null ? extractMain.getOwnerName() : "");
                row.createCell(3).setCellValue(
                    extractMain != null ? extractMain.getOwnerId() : "");
                row.createCell(4).setCellValue(
                    extractMain != null && extractMain.getConfidence() != null
                        ? extractMain.getConfidence() : 0.0);
                row.createCell(5).setCellValue(getStatusName(file.getProcessStatus()));
            }

            // 自动调整列宽
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(outputStream);
            log.info("批量导出完成");
            return outputStream;

        } catch (IOException e) {
            log.error("批量导出失败", e);
            throw new RuntimeException("导出失败", e);
        }
    }

    /**
     * 创建表头样式
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
     * 格式化文件大小
     */
    private String formatFileSize(Long size) {
        if (size == null || size == 0) {
            return "0B";
        }
        final int unit = 1024;
        if (size < unit) {
            return size + "B";
        }
        int exp = (int) (Math.log(size) / Math.log(unit));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f%sB", size / Math.pow(unit, exp), pre);
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待处理";
            case 1: return "已入队";
            case 2: return "处理中";
            case 3: return "待人工";
            case 4: return "已归档";
            case 5: return "失败";
            default: return "未知";
        }
    }

    /**
     * 获取审核状态名称
     */
    private String getAuditStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核中";
            case 2: return "已通过";
            case 3: return "已驳回";
            default: return "未知";
        }
    }
}


