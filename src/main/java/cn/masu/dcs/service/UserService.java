package cn.masu.dcs.service;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.dto.UserAssignRoleDTO;
import cn.masu.dcs.dto.UserCreateDTO;
import cn.masu.dcs.dto.UserUpdateDTO;
import cn.masu.dcs.entity.SysUser;
import cn.masu.dcs.vo.UserVO;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 用户服务接口
 * @author System
 */
public interface UserService extends IService<SysUser> {

    /**
     * 创建用户
     */
    Long insertUser(UserCreateDTO dto);

    /**
     * 更新用户
     */
    Boolean updateUser(UserUpdateDTO dto);

    /**
     * 删除用户
     */
    Boolean deleteUser(Long id);

    /**
     * 获取用户详情
     */
    UserVO queryUserById(Long id);

    /**
     * 根据用户名获取用户
     */
    SysUser queryUserByUsername(String username);

    /**
     * 分页查询用户列表
     */
    PageResult<UserVO> queryUserPage(Long current, Long size, String keyword, Integer status);

    /**
     * 分配角色
     */
    Boolean updateUserRoles(UserAssignRoleDTO dto);

    /**
     * 修改密码
     */
    Boolean updatePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     */
    Boolean updateResetPassword(Long userId, String newPassword);

    /**
     * 使Token失效
     */
    Boolean deleteToken(Long userId);
}

