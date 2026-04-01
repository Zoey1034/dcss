package cn.masu.dcs.service;

import cn.masu.dcs.common.config.MinioConfig;
import cn.masu.dcs.common.constant.FileConstants;
import cn.masu.dcs.common.util.MinioUtils;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.AiDocProcessRequest;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.mapper.DocumentFileMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

/**
 * AI 文件持久化服务
 * <p>
 * 负责将前端上传的 Base64 文件持久化到 MinIO 与数据库，确保事务在代理中生效。
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiFileService {

    private final MinioUtils minioUtils;
    private final MinioConfig minioConfig;
    private final SnowflakeIdGenerator idGenerator;
    private final DocumentFileMapper fileMapper;
    private final SysUserMapper userMapper;

    /** 逗号分隔符，用于解析 Data URI */
    private static final String COMMA = ",";

    /** 冒号分隔符，用于解析 Content-Type */
    private static final String COLON = ":";

    /** 分号分隔符，用于解析 Content-Type */
    private static final String SEMICOLON = ";";

    /** 扩展名分隔符 */
    private static final String DOT = ".";

    /** 文件扩展名：二进制默认扩展名 */
    private static final String EXT_BIN = "bin";

    /** PDF 扩展名（无点前缀，用于 Content-Type 匹配） */
    private static final String EXT_PDF_PLAIN = "pdf";

    /** JPG 扩展名（无点前缀） */
    private static final String EXT_JPG_PLAIN = "jpg";

    /** PNG 扩展名（无点前缀） */
    private static final String EXT_PNG_PLAIN = "png";

    /**
     * 将 Base64 编码的文件内容保存到 MinIO 并在数据库中创建文件记录
     *
     * @param request   AI 文档处理请求，包含 fileContent（Base64）和 fileName
     * @param requestId 请求 ID，用作批次号（可为 null）
     * @return 新建文件记录的 ID
     * @throws Exception MinIO 上传或数据库插入异常
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveFileToMinioAndDatabase(AiDocProcessRequest request, String requestId) throws Exception {
        String fileContent = request.getFileContent();
        String fileName = request.getFileName();

        String base64Data;
        String contentType = FileConstants.MIME_OCTET_STREAM;

        if (fileContent.contains(COMMA)) {
            String[] parts = fileContent.split(COMMA, 2);
            base64Data = parts[1];
            if (parts[0].contains(COLON) && parts[0].contains(SEMICOLON)) {
                contentType = parts[0].substring(parts[0].indexOf(COLON) + 1, parts[0].indexOf(SEMICOLON));
            }
        } else {
            base64Data = fileContent;
        }

        byte[] fileBytes = Base64.getDecoder().decode(base64Data);

        String fileExtension = resolveExtension(fileName, contentType);
        String bucketName = minioConfig.getBucketName();

        try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
            String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String uuid = java.util.UUID.randomUUID().toString();
            String objectName = String.format("%s/%s.%s", datePath, uuid, fileExtension);

            minioUtils.uploadFile(bucketName, objectName, inputStream, fileBytes.length, contentType);
            log.info("文件上传到MinIO成功: bucket={}, object={}", bucketName, objectName);

            Long userId = getCurrentUserId();
            if (userId == null) {
                userId = getDefaultUserId();
                log.warn("未获取到当前用户ID，使用默认用户ID: {}", userId);
            }

            DocumentFile documentFile = new DocumentFile();
            documentFile.setId(idGenerator.nextId());
            documentFile.setFileName(fileName != null ? fileName : "document." + fileExtension);
            documentFile.setMinioBucket(bucketName);
            documentFile.setMinioObject(objectName);
            documentFile.setFileType(fileExtension);
            documentFile.setFileSize((long) fileBytes.length);
            documentFile.setUserId(userId);
            documentFile.setProcessStatus(1);
            documentFile.setProcessMode("AI_PROCESS");
            documentFile.setRetryCount(0);
            documentFile.setDeleted(0);
            documentFile.setVersion(0);
            if (StringUtils.hasText(requestId)) {
                documentFile.setBatchNo(requestId);
            }

            fileMapper.insert(documentFile);
            log.info("文件记录已保存到数据库: fileId={}", documentFile.getId());
            return documentFile.getId();
        }
    }

    /**
     * 根据文件名和 Content-Type 推断文件扩展名
     *
     * @param fileName    文件名
     * @param contentType Content-Type 字符串
     * @return 小写扩展名（不含点），如 "pdf"、"jpg"
     */
    private String resolveExtension(String fileName, String contentType) {
        if (fileName != null && fileName.contains(DOT)) {
            return fileName.substring(fileName.lastIndexOf(DOT) + 1).toLowerCase();
        }
        if (contentType.contains(EXT_PDF_PLAIN)) {
            return EXT_PDF_PLAIN;
        }
        if (contentType.contains(FileConstants.CONTENT_TYPE_IMAGE_JPEG)
                || contentType.contains(FileConstants.CONTENT_TYPE_IMAGE_JPG)) {
            return EXT_JPG_PLAIN;
        }
        if (contentType.contains(FileConstants.CONTENT_TYPE_IMAGE_PNG)) {
            return EXT_PNG_PLAIN;
        }
        return EXT_BIN;
    }

    /**
     * 从 Spring Security 上下文中获取当前登录用户 ID
     *
     * @return 用户 ID，获取失败时返回 null
     */
    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof java.util.Map<?, ?> userMap) {
                Object userIdObj = userMap.get("userId");
                if (userIdObj instanceof Long l) {
                    return l;
                } else if (userIdObj instanceof Integer i) {
                    return i.longValue();
                } else if (userIdObj != null) {
                    return Long.parseLong(userIdObj.toString());
                }
            }
        } catch (Exception e) {
            log.warn("获取当前用户ID失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 当 Security 上下文无用户时，从数据库查询第一个用户作为默认上传者
     *
     * @return 默认用户 ID
     * @throws RuntimeException 数据库中无任何用户时抛出
     */
    private Long getDefaultUserId() {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByAsc(SysUser::getId).last("LIMIT 1");
        SysUser user = userMapper.selectOne(wrapper);
        if (user != null) {
            log.info("使用数据库中的第一个用户: userId={}, username={}", user.getId(), user.getUsername());
            return user.getId();
        }
        throw new RuntimeException("无法获取有效用户ID，请先创建用户或登录系统");
    }
}
