package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.RoleCreateDTO;
import cn.masu.dcs.dto.RoleUpdateDTO;
import cn.masu.dcs.entity.SysRole;
import cn.masu.dcs.mapper.SysRoleMapper;
import cn.masu.dcs.service.RoleService;
import cn.masu.dcs.vo.RoleVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Role service implementation.
 * @author zyq
 */
@Service
@RequiredArgsConstructor
public class RoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements RoleService {

    private final SnowflakeIdGenerator idGenerator;

    @Override
    public Long insertRole(RoleCreateDTO dto) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getRoleKey, dto.getRoleKey());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Role key already exists");
        }

        SysRole role = new SysRole();
        BeanUtils.copyProperties(dto, role);
        role.setId(idGenerator.nextId());
        role.setDeleted(0);
        baseMapper.insert(role);

        return role.getId();
    }

    @Override
    public Boolean updateRole(RoleUpdateDTO dto) {
        SysRole role = baseMapper.selectById(dto.getId());
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "Role not found");
        }

        BeanUtils.copyProperties(dto, role, "id", "createTime");
        return baseMapper.updateById(role) > 0;
    }

    @Override
    public Boolean deleteRole(Long id) {
        SysRole role = baseMapper.selectById(id);
        if (role == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "Role not found");
        }

        role.setDeleted(1);
        return baseMapper.updateById(role) > 0;
    }

    @Override
    public RoleVO queryRoleById(Long id) {
        SysRole role = baseMapper.selectById(id);
        if (role == null || role.getDeleted() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "Role not found");
        }

        return convertToVO(role);
    }

    @Override
    public PageResult<RoleVO> queryRolePage(Long current, Long size, String keyword, Integer status) {
        Page<SysRole> page = new Page<>(current, size);
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);

        if (status != null) {
            wrapper.eq(SysRole::getStatus, status);
        }

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysRole::getRoleName, keyword)
                .or()
                .like(SysRole::getRoleKey, keyword));
        }

        wrapper.orderByDesc(SysRole::getCreateTime);
        Page<SysRole> rolePage = baseMapper.selectPage(page, wrapper);

        PageResult<RoleVO> result = new PageResult<>();
        result.setTotal(rolePage.getTotal());
        result.setRecords(rolePage.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList()));

        return result;
    }

    @Override
    public List<RoleVO> queryAllRoles() {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysRole::getDeleted, 0);
        wrapper.orderByAsc(SysRole::getId);

        List<SysRole> roles = baseMapper.selectList(wrapper);
        return roles.stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());
    }

    private RoleVO convertToVO(SysRole role) {
        RoleVO vo = new RoleVO();
        BeanUtils.copyProperties(role, vo);
        return vo;
    }
}
