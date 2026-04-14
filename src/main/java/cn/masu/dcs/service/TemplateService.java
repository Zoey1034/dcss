package cn.masu.dcs.service;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.dto.TemplateCreateDTO;
import cn.masu.dcs.dto.TemplateUpdateDTO;
import cn.masu.dcs.entity.SysDocTemplate;
import cn.masu.dcs.vo.TemplateVO;
import com.baomidou.mybatisplus.extension.service.IService;
/**
 * 模板服务接口
 * @author System
 */
public interface TemplateService extends IService<SysDocTemplate> {
    /**
     * 创建模板
     */
    Long insertTemplate(TemplateCreateDTO dto);
    /**
     * 更新模板
     */
    Boolean updateTemplate(TemplateUpdateDTO dto);
    /**
     * 删除模板
     */
    Boolean deleteTemplate(Long id);
    /**
     * 获取模板详情
     */
    TemplateVO queryTemplateById(Long id);
    /**
     * 根据编码获取模板
     */
    TemplateVO queryTemplateByCode(String code);
    /**
     * 分页查询模板列表
     */
    PageResult<TemplateVO> queryTemplatePage(Long current, Long size, String keyword, Integer status);
    /**
     * 启用/禁用模板
     */
    Boolean updateTemplateStatus(Long id, Integer status);
}