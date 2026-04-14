package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.ExtractDetailCreateDTO;
import cn.masu.dcs.dto.ExtractDetailUpdateDTO;
import cn.masu.dcs.dto.ExtractMainUpdateDTO;
import cn.masu.dcs.service.ExtractService;
import cn.masu.dcs.vo.ExtractEditVO;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 数据提取编辑控制器
 * @author System
 */
@RestController
@RequestMapping("/api/extract")
@RequiredArgsConstructor
public class ExtractController {

    private final ExtractService extractService;

    /**
     * 获取编辑数据
     */
    @GetMapping("/edit/{fileId}")
    public R<ExtractEditVO> getEditData(@PathVariable Long fileId) {
        ExtractEditVO vo = extractService.queryEditData(fileId);
        return R.ok(vo);
    }

    /**
     * 更新主表
     */
    @PutMapping("/main")
    public R<Boolean> editMain(@Validated @RequestBody ExtractMainUpdateDTO dto) {
        Boolean result = extractService.updateMain(dto);
        return R.ok("更新成功", result);
    }

    /**
     * 更新明细
     */
    @PutMapping("/detail")
    public R<Boolean> editDetail(@Validated @RequestBody ExtractDetailUpdateDTO dto) {
        Boolean result = extractService.updateDetail(dto);
        return R.ok("更新成功", result);
    }

    /**
     * 创建明细
     */
    @PostMapping("/detail")
    public R<Long> addDetail(@Validated @RequestBody ExtractDetailCreateDTO dto) {
        Long id = extractService.insertDetail(dto);
        return R.ok("创建成功", id);
    }

    /**
     * 删除明细
     */
    @DeleteMapping("/detail/{id}")
    public R<Boolean> removeDetail(@PathVariable Long id) {
        Boolean result = extractService.deleteDetail(id);
        return R.ok("删除成功", result);
    }

    /**
     * 标记为已验证
     */
    @PutMapping("/detail/{id}/verify")
    public R<Boolean> editVerified(@PathVariable Long id) {
        Boolean result = extractService.updateVerified(id);
        return R.ok("标记成功", result);
    }
}

