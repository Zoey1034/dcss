package cn.masu.dcs.controller;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.dto.AuditSubmitDTO;
import cn.masu.dcs.service.AuditService;
import cn.masu.dcs.vo.AuditRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 审核控制器
 * @author zyq
 */
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * 提交审核，审计人从认证上下文获取
     */
    @PostMapping("/submit")
    public R<Boolean> addAudit(@Validated @RequestBody AuditSubmitDTO dto) {
        Long auditorId = currentUserId();
        Boolean result = auditService.insertAudit(dto, auditorId);
        return R.ok("审核提交成功", result);
    }

    /**
     * 获取审核历史
     */
    @GetMapping("/history/{fileId}")
    public R<List<AuditRecordVO>> listAuditHistory(@PathVariable Long fileId) {
        List<AuditRecordVO> records = auditService.queryAuditHistory(fileId);
        return R.ok(records);
    }

    /**
     * 分页查询审核记录
     */
    @GetMapping("/page")
    public R<PageResult<AuditRecordVO>> listAuditPage(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) Integer auditStatus,
            @RequestParam(required = false) Long auditorId) {
        PageResult<AuditRecordVO> pageResult = auditService.queryAuditPage(current, size, auditStatus, auditorId);
        return R.ok(pageResult);
    }

    /**
     * 获取文件预览URL（MinIO预签名链接）
     */
    @GetMapping("/preview/{fileId}")
    public R<String> getFilePreviewUrl(@PathVariable Long fileId) {
        String url = auditService.queryFilePreviewUrl(fileId);
        return R.ok("获取预览链接成功", url);
    }

    /**
     * 获取AI处理结果（Python返回的JSON数据）
     */
    @GetMapping("/result/{fileId}")
    public R<Map<String, Object>> getProcessResult(@PathVariable Long fileId) {
        Map<String, Object> result = auditService.queryProcessResult(fileId);
        return R.ok("获取处理结果成功", result);
    }

    /**
     * 审核人员修改字段并保存
     */
    @PostMapping("/modify/{fileId}")
    public R<Boolean> editFields(@PathVariable Long fileId, @RequestBody Map<String, Object> fields) {
        Long auditorId = currentUserId();
        Boolean success = auditService.updateFields(fileId, fields, auditorId);
        return R.ok("字段修改成功", success);
    }

    private Long currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof Long id) {
            return id;
        }
        throw new BusinessException(ErrorCode.UNAUTHORIZED.getCode(), "未获取到用户身份");
    }
}
