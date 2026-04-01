package cn.masu.dcs.service.impl;

import cn.masu.dcs.business.AuthBusiness;
import cn.masu.dcs.dto.LoginRequest;
import cn.masu.dcs.dto.LoginResponse;
import cn.masu.dcs.dto.UserCreateDTO;
import cn.masu.dcs.service.AuthService;
import cn.masu.dcs.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 * <p>
 * 委托给 {@link AuthBusiness} 执行实际的多服务编排逻辑。
 * </p>
 *
 * @author zyq
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthBusiness authBusiness;

    @Override
    public LoginResponse login(LoginRequest request) {
        return authBusiness.login(request);
    }

    @Override
    public LoginResponse register(UserCreateDTO request) {
        return authBusiness.register(request);
    }

    @Override
    public void logout(Long userId) {
        authBusiness.logout(userId);
    }

    @Override
    public UserInfoVO getCurrentUserInfo(Long userId) {
        return authBusiness.getCurrentUserInfo(userId);
    }

    @Override
    public String refreshToken(String oldToken) {
        return authBusiness.refreshToken(oldToken);
    }
}

