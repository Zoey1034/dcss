package cn.masu.dcs.controller;

import cn.masu.dcs.business.ExportBusiness;
import cn.masu.dcs.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 导出控制器
 * <p>
 * 提供文件列表、审核记录、统计报表等数据的导出功能
 * </p>
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;
    private final ExportBusiness exportBusiness;

    /**
     * 导出文件列表为Excel
     *
     * @param status 状态筛选（可选）
     * @param keyword 关键字筛选（可选）
     * @return Excel文件
     */
    @GetMapping("/files")
    public ResponseEntity<byte[]> exportFiles(
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        log.info("导出文件列表: status={}, keyword={}", status, keyword);

        try {
            ByteArrayOutputStream outputStream = exportService.queryExportFiles(status, keyword);

            String fileName = "文件列表_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFileName)
                .body(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("导出文件列表失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出审核记录为Excel
     *
     * @param fileId 文件ID（可选）
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return Excel文件
     */
    @GetMapping("/audit-records")
    public ResponseEntity<byte[]> exportAuditRecords(
            @RequestParam(required = false) Long fileId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        log.info("导出审核记录: fileId={}, startDate={}, endDate={}",
            fileId, startDate, endDate);

        try {
            ByteArrayOutputStream outputStream = exportService.queryExportAuditRecords(
                fileId, startDate, endDate);

            String fileName = "审核记录_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFileName)
                .body(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("导出审核记录失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 导出统计报表
     *
     * @param type 报表类型（overview/trend）
     * @return Excel文件
     */
    @GetMapping("/report/{type}")
    public ResponseEntity<byte[]> exportReport(@PathVariable String type) {
        log.info("导出统计报表: type={}", type);

        try {
            ByteArrayOutputStream outputStream = exportBusiness.generateReport(type);

            String fileName = "统计报表_" + type + "_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFileName)
                .body(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("导出统计报表失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }

    /**
     * 批量导出文件数据
     *
     * @param fileIds 文件ID列表
     * @return Excel文件
     */
    @PostMapping("/batch-export")
    public ResponseEntity<byte[]> batchExport(@RequestBody List<Long> fileIds) {
        log.info("批量导出文件数据: count={}", fileIds.size());

        try {
            ByteArrayOutputStream outputStream = exportService.queryBatchExportData(fileIds);

            String fileName = "批量导出_" + System.currentTimeMillis() + ".xlsx";
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

            return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename*=UTF-8''" + encodedFileName)
                .body(outputStream.toByteArray());
        } catch (Exception e) {
            log.error("批量导出失败", e);
            throw new RuntimeException("导出失败: " + e.getMessage());
        }
    }
}


