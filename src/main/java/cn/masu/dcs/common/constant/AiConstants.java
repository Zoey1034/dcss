package cn.masu.dcs.common.constant;

/**
 * AI 处理相关常量
 * <p>
 * 包含 AI 返回结果中各字段的 Key 名称以及业务处理中使用的固定阈值。
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
public final class AiConstants {

    /** 工具类，禁止实例化 */
    private AiConstants() {
    }

    /* ==================== 结果 Map Key ==================== */

    /** 结果 Map 中的文件 ID Key */
    public static final String KEY_FILE_ID = "fileId";

    /** 结果 Map 中的 AI 结果 Key */
    public static final String KEY_AI_RESULT = "aiResult";

    /** AI 结果中的整体置信度 Key */
    public static final String KEY_CONFIDENCE = "confidence_overall";

    /* ==================== AI 结果字段名 ==================== */

    /** 基本信息字段 */
    public static final String FIELD_BASIC_INFO = "basic_info";

    /** 学术信息字段 */
    public static final String FIELD_ACADEMIC_INFO = "academic_info";

    /** 证书信息字段 */
    public static final String FIELD_CERTIFICATE_INFO = "certificate_info";

    /** 财务信息字段 */
    public static final String FIELD_FINANCIAL_INFO = "financial_info";

    /** 请假信息字段 */
    public static final String FIELD_LEAVE_INFO = "leave_info";

    /** 表格数据字段 */
    public static final String FIELD_TABLES = "tables";

    /** 字段集合 Key */
    public static final String FIELD_FIELDS = "fields";

    /** 课程字段 */
    public static final String FIELD_COURSES = "courses";

    /** 文档类型字段 */
    public static final String FIELD_DOCUMENT_TYPE = "document_type";

    /** 正文文本字段 */
    public static final String FIELD_TEXT = "text";

    /** 摘要字段 */
    public static final String FIELD_SUMMARY = "summary";

    /** 置信度字段 */
    public static final String FIELD_CONFIDENCE = "confidence_overall";

    /** 姓名字段 */
    public static final String FIELD_NAME = "name";

    /** 学号字段 */
    public static final String FIELD_STUDENT_ID = "student_id";

    /** 身份证号字段 */
    public static final String FIELD_ID_NUMBER = "id_number";

    /** 课程名字段 */
    public static final String FIELD_COURSE = "course";

    /* ==================== 批量处理限制 ==================== */

    /** 批量上传最大文件数 */
    public static final int MAX_BATCH_FILES = 50;

    /* ==================== 文件处理状态 ==================== */

    /** 已归档状态值 */
    public static final int STATUS_ARCHIVED = 4;
}
