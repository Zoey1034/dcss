package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.RoleCreateDTO;
import cn.masu.dcs.dto.RoleUpdateDTO;
import cn.masu.dcs.service.RoleService;
import cn.masu.dcs.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 角色管理控制器
 * <p>
 * 同时支持 /api/role 和 /api/roles 路径，保证向后兼容
 * </p>
 * @author zyq
 */
@RestController
@RequestMapping({"/api/role"})
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * 创建角色
     */
    @PostMapping
    public R<Long> addRole(@Validated @RequestBody RoleCreateDTO dto) {
        Long id = roleService.insertRole(dto);
        return R.ok("创建成功", id);
    }

    /**
     * 更新角色
     */
    @PutMapping
    public R<Boolean> editRole(@Validated @RequestBody RoleUpdateDTO dto) {
        Boolean result = roleService.updateRole(dto);
        return R.ok("更新成功", result);
    }

    /**
     * 删除角色
     */
    @DeleteMapping("/{id}")
    public R<Boolean> removeRole(@PathVariable Long id) {
        Boolean result = roleService.deleteRole(id);
        return R.ok("删除成功", result);
    }

    /**
     * 获取角色详情
     */
    @GetMapping("/{id}")
    public R<RoleVO> getRoleDetail(@PathVariable Long id) {
        RoleVO vo = roleService.queryRoleById(id);
        return R.ok(vo);
    }

    /**
     * 分页查询角色列表
     */
    @GetMapping("/page")
    public R<PageResult<RoleVO>> listRoles(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        PageResult<RoleVO> pageResult = roleService.queryRolePage(current, size, keyword, status);
        return R.ok(pageResult);
    }
}

