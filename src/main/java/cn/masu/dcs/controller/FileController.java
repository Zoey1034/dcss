package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.FileUpdateStatusDTO;
import cn.masu.dcs.service.FileService;
import cn.masu.dcs.vo.FileDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理控制器
 * <p>
 * 同时支持 /api/file 和 /api/files 路径，保证向后兼容
 * </p>
 * @author zyq
 */
@RestController
@RequestMapping({"/api/file"})
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    /**
     * 上传文件
     */
    @PostMapping("/upload")
    public R<Long> addFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long templateId,
            @RequestParam(required = false) Long userId) {

        // 如果未提供userId，尝试从SecurityContext获取
        if (userId == null) {
            try {
                Object principal = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication().getPrincipal();
                if (principal instanceof Long) {
                    userId = (Long) principal;
                }
            } catch (Exception e) {
                // 忽略异常，使用默认用户
            }
        }

        Long id = fileService.insertFile(file, templateId, userId);
        return R.ok("上传成功", id);
    }

    /**
     * 下载文件
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> getFile(@PathVariable Long id) {
        FileDetailVO fileDetail = fileService.queryFileById(id);
        byte[] fileContent = fileService.queryFileContent(id);

        String encodedFileName;
        try {
            encodedFileName = java.net.URLEncoder.encode(fileDetail.getFileName(),
                java.nio.charset.StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        } catch (Exception e) {
            encodedFileName = fileDetail.getFileName();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(fileContent);
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/{id}")
    public R<Boolean> removeFile(@PathVariable Long id) {
        Boolean result = fileService.deleteFile(id);
        return R.ok("删除成功", result);
    }

    /**
     * 获取文件详情
     */
    @GetMapping("/{id}")
    public R<FileDetailVO> getFileDetail(@PathVariable Long id) {
        FileDetailVO vo = fileService.queryFileById(id);
        return R.ok(vo);
    }

    /**
     * 更新文件状态
     */
    @PutMapping("/status")
    public R<Boolean> editFileStatus(@Validated @RequestBody FileUpdateStatusDTO dto) {
        Boolean result = fileService.updateFileStatus(dto);
        return R.ok("状态更新成功", result);
    }

    /**
     * 分页查询文件列表
     */
    @GetMapping("/page")
    public R<PageResult<FileDetailVO>> listFiles(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long userId) {
        PageResult<FileDetailVO> pageResult = fileService.queryFilePage(current, size, keyword, status, userId);
        return R.ok(pageResult);
    }
}

