package cn.masu.dcs.service;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.dto.RoleCreateDTO;
import cn.masu.dcs.dto.RoleUpdateDTO;
import cn.masu.dcs.entity.SysRole;
import cn.masu.dcs.vo.RoleVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 角色服务接口
 * @author System
 */
public interface RoleService extends IService<SysRole> {

    /**
     * 创建角色
     */
    Long insertRole(RoleCreateDTO dto);

    /**
     * 更新角色
     */
    Boolean updateRole(RoleUpdateDTO dto);

    /**
     * 删除角色
     */
    Boolean deleteRole(Long id);

    /**
     * 获取角色详情
     */
    RoleVO queryRoleById(Long id);

    /**
     * 分页查询角色列表
     */
    PageResult<RoleVO> queryRolePage(Long current, Long size, String keyword, Integer status);

    /**
     * 获取所有角色列表
     */
    List<RoleVO> queryAllRoles();
}

