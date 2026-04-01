package cn.masu.dcs.common.constant;

/**
 * 文件相关常量
 * <p>
 * 包含文件扩展名、MIME 类型等与文件处理有关的公共常量。
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
public final class FileConstants {

    /** 工具类，禁止实例化 */
    private FileConstants() {
    }

    /* ==================== 文件扩展名 ==================== */

    /** PDF 文件扩展名 */
    public static final String EXT_PDF = ".pdf";

    /** PNG 图片扩展名 */
    public static final String EXT_PNG = ".png";

    /** JPG 图片扩展名 */
    public static final String EXT_JPG = ".jpg";

    /** JPEG 图片扩展名 */
    public static final String EXT_JPEG = ".jpeg";

    /** GIF 图片扩展名 */
    public static final String EXT_GIF = ".gif";

    /** BMP 图片扩展名 */
    public static final String EXT_BMP = ".bmp";

    /** TIFF 图片扩展名 */
    public static final String EXT_TIFF = ".tiff";

    /** TIF 图片扩展名 */
    public static final String EXT_TIF = ".tif";

    /** WEBP 图片扩展名 */
    public static final String EXT_WEBP = ".webp";

    /* ==================== MIME 类型 ==================== */

    /** PDF MIME 类型 */
    public static final String MIME_PDF = "application/pdf";

    /** PNG MIME 类型 */
    public static final String MIME_PNG = "image/png";

    /** JPEG MIME 类型 */
    public static final String MIME_JPEG = "image/jpeg";

    /** GIF MIME 类型 */
    public static final String MIME_GIF = "image/gif";

    /** BMP MIME 类型 */
    public static final String MIME_BMP = "image/bmp";

    /** TIFF MIME 类型 */
    public static final String MIME_TIFF = "image/tiff";

    /** WEBP MIME 类型 */
    public static final String MIME_WEBP = "image/webp";

    /** 通用二进制流 MIME 类型（默认） */
    public static final String MIME_OCTET_STREAM = "application/octet-stream";

    /* ==================== Content-Type 头部值 ==================== */

    /** JPEG Content-Type（含旧写法 image/jpg）*/
    public static final String CONTENT_TYPE_IMAGE_JPEG = "image/jpeg";

    /** JPG Content-Type */
    public static final String CONTENT_TYPE_IMAGE_JPG = "image/jpg";

    /** PNG Content-Type */
    public static final String CONTENT_TYPE_IMAGE_PNG = "image/png";
}
