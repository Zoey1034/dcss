package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.TemplateCreateDTO;
import cn.masu.dcs.dto.TemplateUpdateDTO;
import cn.masu.dcs.entity.SysDocTemplate;
import cn.masu.dcs.mapper.SysDocTemplateMapper;
import cn.masu.dcs.service.TemplateService;
import cn.masu.dcs.vo.TemplateVO;
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
 * Template service implementation.
 * @author zyq
 */
@Service
@RequiredArgsConstructor
public class TemplateServiceImpl extends ServiceImpl<SysDocTemplateMapper, SysDocTemplate> implements TemplateService {

    private final SnowflakeIdGenerator idGenerator;

    @Override
    public Long insertTemplate(TemplateCreateDTO dto) {
        LambdaQueryWrapper<SysDocTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDocTemplate::getTemplateCode, dto.getTemplateCode());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Template code already exists");
        }

        SysDocTemplate template = new SysDocTemplate();
        BeanUtils.copyProperties(dto, template);
        template.setId(idGenerator.nextId());

        baseMapper.insert(template);
        return template.getId();
    }

    @Override
    public Boolean updateTemplate(TemplateUpdateDTO dto) {
        SysDocTemplate template = baseMapper.selectById(dto.getId());
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }

        if (StringUtils.hasText(dto.getTemplateCode()) && !dto.getTemplateCode().equals(template.getTemplateCode())) {
            LambdaQueryWrapper<SysDocTemplate> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysDocTemplate::getTemplateCode, dto.getTemplateCode());
            if (baseMapper.selectCount(wrapper) > 0) {
                throw new BusinessException(ErrorCode.PARAM_ERROR.getCode(), "Template code already exists");
            }
        }

        BeanUtils.copyProperties(dto, template, "id", "createTime");
        return baseMapper.updateById(template) > 0;
    }

    @Override
    public Boolean deleteTemplate(Long id) {
        SysDocTemplate template = baseMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        return baseMapper.deleteById(id) > 0;
    }

    @Override
    public TemplateVO queryTemplateById(Long id) {
        SysDocTemplate template = baseMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        return convertToVO(template);
    }

    @Override
    public TemplateVO queryTemplateByCode(String code) {
        LambdaQueryWrapper<SysDocTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysDocTemplate::getTemplateCode, code);
        wrapper.eq(SysDocTemplate::getStatus, 1);
        SysDocTemplate template = baseMapper.selectOne(wrapper);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        return convertToVO(template);
    }

    @Override
    public PageResult<TemplateVO> queryTemplatePage(Long current, Long size, String keyword, Integer status) {
        Page<SysDocTemplate> page = new Page<>(current, size);
        LambdaQueryWrapper<SysDocTemplate> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysDocTemplate::getTemplateName, keyword)
                .or().like(SysDocTemplate::getTemplateCode, keyword));
        }

        if (status != null) {
            wrapper.eq(SysDocTemplate::getStatus, status);
        }

        wrapper.orderByDesc(SysDocTemplate::getCreateTime);

        Page<SysDocTemplate> result = baseMapper.selectPage(page, wrapper);

        List<TemplateVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public Boolean updateTemplateStatus(Long id, Integer status) {
        SysDocTemplate template = baseMapper.selectById(id);
        if (template == null) {
            throw new BusinessException(ErrorCode.TEMPLATE_NOT_FOUND);
        }
        template.setStatus(status);
        return baseMapper.updateById(template) > 0;
    }

    private TemplateVO convertToVO(SysDocTemplate template) {
        TemplateVO vo = new TemplateVO();
        BeanUtils.copyProperties(template, vo);
        return vo;
    }
}

