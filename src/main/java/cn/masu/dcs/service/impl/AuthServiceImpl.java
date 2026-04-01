package cn.masu.dcs.service.impl;

import cn.masu.dcs.dto.LoginRequest;
import cn.masu.dcs.dto.LoginResponse;
import cn.masu.dcs.service.AuthService;
import cn.masu.dcs.vo.UserInfoVO;
import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.JwtUtils;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.service.UserService;
import cn.masu.dcs.dto.UserCreateDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${jwt.expiration:86400000}")
    private long jwtExpirationMillis;

    @Override
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

    @Override
    public LoginResponse register(UserCreateDTO request) {
        // 使用 UserService 创建用户
        Long userId = userService.insertUser(request);
        SysUser user = userService.getById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), user.getTokenVersion());
        cacheUserSession(user, token);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    @Override
    public void logout(Long userId) {
        userService.deleteToken(userId);
        evictUserSession(userId);
        log.info("用户登出成功: userId={}", userId);
    }

    @Override
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

    @Override
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

    private void evictUserSession(Long userId) {
        try {
            stringRedisTemplate.delete(buildSessionKey(userId));
        } catch (Exception e) {
            log.warn("删除用户会话缓存失败: userId={}", userId, e);
        }
    }

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

    private String buildSessionKey(Long userId) {
        return "auth:session:" + userId;
    }
}
