package cn.masu.dcs.business;

import cn.masu.dcs.common.constant.RedisConstants;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.JwtUtils;
import cn.masu.dcs.dto.LoginRequest;
import cn.masu.dcs.dto.LoginResponse;
import cn.masu.dcs.dto.UserCreateDTO;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.service.UserService;
import cn.masu.dcs.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证业务层
 * <p>
 * 编排 {@link UserService}、{@link JwtUtils}、{@link PasswordEncoder} 和 {@link StringRedisTemplate}，
 * 实现登录、注册、登出、获取用户信息和刷新 Token 等跨服务认证流程。
 * </p>
 *
 * @author zyq
 * @since 2025-12-07
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthBusiness {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMillis;

    /**
     * 用户登录
     * <p>
     * 校验用户名和密码，生成 JWT Token 并缓存会话信息。
     * </p>
     *
     * @param request 登录请求（用户名、密码）
     * @return 登录响应（Token、用户信息）
     */
    public LoginResponse login(LoginRequest request) {
        SysUser user = userService.queryUserByUsername(request.getUsername());
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN.getCode(), "用户已被禁用");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        cacheUserSession(user, token);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    /**
     * 用户注册
     * <p>
     * 通过 {@link UserService} 创建用户，并自动完成登录（生成 Token 并缓存会话）。
     * </p>
     *
     * @param request 用户创建信息
     * @return 登录响应（Token、用户信息）
     */
    public LoginResponse register(UserCreateDTO request) {
        Long userId = userService.insertUser(request);
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        cacheUserSession(user, token);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    /**
     * 用户登出
     * <p>
     * 通过更新 Token 版本号使旧 Token 失效，并删除 Redis 会话缓存。
     * </p>
     *
     * @param userId 用户ID
     */
    public void logout(Long userId) {
        userService.deleteToken(userId);
        evictUserSession(userId);
        log.info("用户登出成功: userId={}", userId);
    }

    /**
     * 获取当前用户信息
     * <p>
     * 优先从 Redis 缓存读取，缓存未命中时从数据库查询。
     * </p>
     *
     * @param userId 用户ID
     * @return 用户信息 VO
     */
    public UserInfoVO getCurrentUserInfo(Long userId) {
        SysUser user = userService.getById(userId);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        UserInfoVO vo = getCachedUserInfo(user);
        if (vo != null) {
            return vo;
        }

        vo = new UserInfoVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setEmail(user.getEmail());
        vo.setPhone(user.getPhone());
        vo.setAvatar(user.getAvatar());
        return vo;
    }

    /**
     * 刷新 Token
     * <p>
     * 校验旧 Token 有效性及 tokenVersion 一致性后，生成新的 Token。
     * </p>
     *
     * @param oldToken 旧 Token
     * @return 新 Token
     */
    public String refreshToken(String oldToken) {
        if (!jwtUtils.validateToken(oldToken)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        Long userId = jwtUtils.getUserIdFromToken(oldToken);
        String username = jwtUtils.getUsernameFromToken(oldToken);
        Integer tokenVersion = jwtUtils.getTokenVersionFromToken(oldToken);

        SysUser user = userService.getById(userId);
        if (user == null || !user.getTokenVersion().equals(tokenVersion)) {
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }

        return jwtUtils.generateToken(userId, username, tokenVersion);
    }

    /**
     * 缓存用户会话到 Redis
     */
    private void cacheUserSession(SysUser user, String token) {
        try {
            String key = buildSessionKey(user.getId());
            Map<String, String> cache = new HashMap<>(8);
            cache.put("userId", String.valueOf(user.getId()));
            cache.put("username", user.getUsername());
            cache.put("nickname", user.getNickname());
            cache.put("token", token);
            if (user.getEmail() != null) {
                cache.put("email", user.getEmail());
            }
            if (user.getPhone() != null) {
                cache.put("phone", user.getPhone());
            }
            if (user.getAvatar() != null) {
                cache.put("avatar", user.getAvatar());
            }
            stringRedisTemplate.opsForHash().putAll(key, cache);
            stringRedisTemplate.expire(key, Duration.ofMillis(jwtExpirationMillis));
        } catch (Exception e) {
            log.warn("缓存用户会话失败: userId={}", user.getId(), e);
        }
    }

    /**
     * 从 Redis 删除用户会话
     */
    private void evictUserSession(Long userId) {
        try {
            stringRedisTemplate.delete(buildSessionKey(userId));
        } catch (Exception e) {
            log.warn("删除用户会话缓存失败: userId={}", userId, e);
        }
    }

    /**
     * 从 Redis 读取用户缓存信息
     */
    private UserInfoVO getCachedUserInfo(SysUser user) {
        try {
            Map<Object, Object> cache = stringRedisTemplate.opsForHash().entries(buildSessionKey(user.getId()));
            if (cache == null || cache.isEmpty()) {
                return null;
            }
            UserInfoVO vo = new UserInfoVO();
            vo.setUserId(user.getId());
            vo.setUsername((String) cache.getOrDefault("username", user.getUsername()));
            vo.setNickname((String) cache.getOrDefault("nickname", user.getNickname()));
            vo.setEmail((String) cache.getOrDefault("email", user.getEmail()));
            vo.setPhone((String) cache.getOrDefault("phone", user.getPhone()));
            vo.setAvatar((String) cache.getOrDefault("avatar", user.getAvatar()));
            return vo;
        } catch (Exception e) {
            log.warn("读取用户缓存失败: userId={}", user.getId(), e);
            return null;
        }
    }

    /**
     * 构建 Redis 会话 key
     */
    private String buildSessionKey(Long userId) {
        return RedisConstants.SESSION_KEY_PREFIX + userId;
    }
}
