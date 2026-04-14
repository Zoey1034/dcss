package cn.masu.dcs.common.constant;

/**
 * Redis 缓存键常量
 * <p>
 * 集中管理系统中所有 Redis 缓存的 Key 前缀，避免魔法字符串散落各处。
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
public final class RedisConstants {

    /** 工具类，禁止实例化 */
    private RedisConstants() {
    }

    /* ==================== 认证会话 ==================== */

    /** 用户会话缓存键前缀，格式：auth:session:{userId} */
    public static final String SESSION_KEY_PREFIX = "auth:session:";

    /* ==================== 其他公共键前缀 ==================== */

    /** 限流缓存键前缀，格式：rate_limit:{clientKey} */
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";
}
