package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.UserAssignRoleDTO;
import cn.masu.dcs.dto.UserCreateDTO;
import cn.masu.dcs.dto.UserUpdateDTO;
import cn.masu.dcs.entity.SysRole;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.entity.SysUserRole;
import cn.masu.dcs.mapper.SysRoleMapper;
import cn.masu.dcs.mapper.SysUserMapper;
import cn.masu.dcs.mapper.SysUserRoleMapper;
import cn.masu.dcs.service.UserService;
import cn.masu.dcs.vo.UserVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现
 * @author System
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements UserService {

    private final SnowflakeIdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertUser(UserCreateDTO dto) {
        // 检查用户名是否已存在
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, dto.getUsername());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        SysUser user = new SysUser();
        BeanUtils.copyProperties(dto, user);
        user.setId(idGenerator.nextId());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setTokenVersion(1);
        user.setDeleted(0);
        user.setVersion(0);

        baseMapper.insert(user);
        return user.getId();
    }

    @Override
    public Boolean updateUser(UserUpdateDTO dto) {
        SysUser user = baseMapper.selectById(dto.getId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        BeanUtils.copyProperties(dto, user, "id", "username", "password", "createTime");
        return baseMapper.updateById(user) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteUser(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 逻辑删除
        user.setDeleted(1);
        return baseMapper.updateById(user) > 0;
    }


    @Override
    public UserVO queryUserById(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    @Override
    public SysUser queryUserByUsername(String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, username);
        wrapper.eq(SysUser::getDeleted, 0);
        return baseMapper.selectOne(wrapper);
    }

    @Override
    public PageResult<UserVO> queryUserPage(Long current, Long size, String keyword, Integer status) {
        Page<SysUser> page = new Page<>(current, size);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(SysUser::getDeleted, 0);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysUser::getUsername, keyword)
                    .or().like(SysUser::getNickname, keyword)
                    .or().like(SysUser::getEmail, keyword));
        }

        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }

        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> result = baseMapper.selectPage(page, wrapper);

        List<UserVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateUserRoles(UserAssignRoleDTO dto) {
        SysUser user = baseMapper.selectById(dto.getUserId());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 删除旧的角色关联
        LambdaQueryWrapper<SysUserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUserRole::getUserId, dto.getUserId());
        userRoleMapper.delete(wrapper);

        // 添加新的角色关联
        for (Long roleId : dto.getRoleIds()) {
            SysUserRole userRole = new SysUserRole();
            userRole.setId(idGenerator.nextId());
            userRole.setUserId(dto.getUserId());
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }

        return true;
    }

    @Override
    public Boolean updatePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return baseMapper.updateById(user) > 0;
    }

    @Override
    public Boolean updateResetPassword(Long userId, String newPassword) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setTokenVersion(user.getTokenVersion() + 1);
        return baseMapper.updateById(user) > 0;
    }

    @Override
    public Boolean deleteToken(Long userId) {
        SysUser user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        user.setTokenVersion(user.getTokenVersion() + 1);
        return baseMapper.updateById(user) > 0;
    }

    private UserVO convertToVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        // 查询用户角色列表
        List<SysUserRole> userRoles = userRoleMapper.selectList(
            new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, user.getId())
        );

        if (userRoles != null && !userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                .map(SysUserRole::getRoleId)
                .collect(Collectors.toList());

            if (!roleIds.isEmpty()) {
                // 使用selectList替代已弃用的selectBatchIds
                List<SysRole> roles = roleMapper.selectList(
                    new LambdaQueryWrapper<SysRole>()
                        .in(SysRole::getId, roleIds)
                );
                List<String> roleNames = roles.stream()
                    .map(SysRole::getRoleName)
                    .collect(Collectors.toList());
                vo.setRoleNames(roleNames);
                vo.setRoleIds(roleIds);
            }
        }

        return vo;
    }
}
