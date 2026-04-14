package cn.masu.dcs.service;

import cn.masu.dcs.dto.AuditSubmitDTO;
import cn.masu.dcs.entity.AuditRecord;
import cn.masu.dcs.vo.AuditRecordVO;
import cn.masu.dcs.common.result.PageResult;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 审核服务接口
 * @author zyq
 */
public interface AuditService extends IService<AuditRecord> {

    /**
     * 提交审核
     *
     * @param dto 审核提交DTO
     * @param auditorId 审核人ID
     * @return 提交是否成功
     */
    Boolean insertAudit(AuditSubmitDTO dto, Long auditorId);

    /**
     * 获取文件的审核历史
     *
     * @param fileId 文件ID
     * @return 审核记录列表
     */
    List<AuditRecordVO> queryAuditHistory(Long fileId);

    /**
     * 分页查询审核记录
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param auditStatus 审核状态（可选）
     * @param auditorId 审核人ID（可选）
     * @return 审核记录分页结果
     */
    PageResult<AuditRecordVO> queryAuditPage(Long current, Long size, Integer auditStatus, Long auditorId);

    /**
     * 获取文件预览URL（MinIO预签名链接）
     *
     * @param fileId 文件ID
     * @return MinIO预签名访问URL
     */
    String queryFilePreviewUrl(Long fileId);

    /**
     * 获取AI处理结果
     *
     * @param fileId 文件ID
     * @return AI处理结果（包含KV数据和完整提取结果）
     */
    Map<String, Object> queryProcessResult(Long fileId);

    /**
     * 修改字段
     *
     * @param fileId 文件ID
     * @param fields 要修改的字段（键值对）
     * @param auditorId 审核人ID
     * @return 修改是否成功
     */
    Boolean updateFields(Long fileId, Map<String, Object> fields, Long auditorId);
}

