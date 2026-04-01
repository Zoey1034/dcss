package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.dto.ExtractDetailCreateDTO;
import cn.masu.dcs.dto.ExtractDetailUpdateDTO;
import cn.masu.dcs.dto.ExtractMainUpdateDTO;
import cn.masu.dcs.entity.DocumentExtractDetail;
import cn.masu.dcs.entity.DocumentExtractMain;
import cn.masu.dcs.mapper.DocumentExtractDetailMapper;
import cn.masu.dcs.mapper.DocumentExtractMainMapper;
import cn.masu.dcs.service.ExtractService;
import cn.masu.dcs.vo.ExtractDetailVO;
import cn.masu.dcs.vo.ExtractEditVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据提取编辑服务实现
 * <p>
 * 提供数据提取结果的增删改查功能，用于人工校对AI提取的数据
 * </p>
 *
 * @author zyq
 * @since 2025-12-06
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExtractServiceImpl implements ExtractService {

    private final DocumentExtractMainMapper mainMapper;
    private final DocumentExtractDetailMapper detailMapper;
    private final SnowflakeIdGenerator idGenerator;

    /**
     * 获取编辑数据
     *
     * @param fileId 文件ID
     * @return 编辑VO对象
     */
    @Override
    public ExtractEditVO queryEditData(Long fileId) {
        log.info("获取提取数据: fileId={}", fileId);

        LambdaQueryWrapper<DocumentExtractMain> mainWrapper = new LambdaQueryWrapper<>();
        mainWrapper.eq(DocumentExtractMain::getFileId, fileId);
        DocumentExtractMain main = mainMapper.selectOne(mainWrapper);

        if (main == null) {
            log.warn("提取数据不存在: fileId={}", fileId);
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "提取数据不存在");
        }

        ExtractEditVO vo = new ExtractEditVO();
        vo.setMainId(main.getId());
        vo.setFileId(main.getFileId());
        vo.setTemplateId(main.getTemplateId());
        vo.setStatus(main.getStatus());

        // 查询明细
        LambdaQueryWrapper<DocumentExtractDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(DocumentExtractDetail::getMainId, main.getId())
                     .orderByAsc(DocumentExtractDetail::getRowIndex);
        List<DocumentExtractDetail> details = detailMapper.selectList(detailWrapper);

        vo.setDetails(details.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList()));

        log.info("获取提取数据成功: fileId={}, detailCount={}", fileId, details.size());
        return vo;
    }

    /**
     * 更新主表数据
     *
     * @param dto 更新DTO
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateMain(ExtractMainUpdateDTO dto) {
        log.info("更新主表数据: id={}", dto.getId());

        DocumentExtractMain main = mainMapper.selectById(dto.getId());
        if (main == null) {
            log.warn("主表数据不存在: id={}", dto.getId());
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "数据不存在");
        }

        if (dto.getExtractResult() != null) {
            main.setExtractResult(dto.getExtractResult());
        }
        if (dto.getConfidence() != null) {
            main.setConfidence(dto.getConfidence());
        }
        if (dto.getStatus() != null) {
            main.setStatus(dto.getStatus());
        }

        int result = mainMapper.updateById(main);
        log.info("更新主表数据完成: id={}, success={}", dto.getId(), result > 0);
        return result > 0;
    }

    /**
     * 更新明细数据
     *
     * @param dto 更新DTO
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateDetail(ExtractDetailUpdateDTO dto) {
        log.info("更新明细数据: id={}", dto.getId());

        DocumentExtractDetail detail = detailMapper.selectById(dto.getId());
        if (detail == null) {
            log.warn("明细数据不存在: id={}", dto.getId());
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "明细不存在");
        }

        if (dto.getFieldValue() != null) {
            detail.setFieldValue(dto.getFieldValue());
        }
        if (dto.getConfidence() != null) {
            detail.setConfidence(dto.getConfidence());
        }
        if (dto.getIsVerified() != null) {
            detail.setIsVerified(dto.getIsVerified());
        }

        int result = detailMapper.updateById(detail);
        log.info("更新明细数据完成: id={}, success={}", dto.getId(), result > 0);
        return result > 0;
    }

    /**
     * 创建明细数据
     *
     * @param dto 创建DTO
     * @return 明细ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long insertDetail(ExtractDetailCreateDTO dto) {
        log.info("创建明细数据: mainId={}", dto.getMainId());

        DocumentExtractDetail detail = new DocumentExtractDetail();
        BeanUtils.copyProperties(dto, detail);
        detail.setId(idGenerator.nextId());
        detail.setIsVerified(0);

        detailMapper.insert(detail);
        log.info("创建明细数据成功: id={}", detail.getId());
        return detail.getId();
    }

    /**
     * 删除明细数据
     *
     * @param id 明细ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDetail(Long id) {
        log.info("删除明细数据: id={}", id);

        int result = detailMapper.deleteById(id);
        log.info("删除明细数据完成: id={}, success={}", id, result > 0);
        return result > 0;
    }

    /**
     * 标记为已校对
     *
     * @param detailId 明细ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateVerified(Long detailId) {
        log.info("标记为已校对: detailId={}", detailId);

        DocumentExtractDetail detail = detailMapper.selectById(detailId);
        if (detail == null) {
            log.warn("明细数据不存在: id={}", detailId);
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), "明细不存在");
        }

        detail.setIsVerified(1);
        int result = detailMapper.updateById(detail);
        log.info("标记为已校对完成: detailId={}, success={}", detailId, result > 0);
        return result > 0;
    }

    /**
     * 转换为VO对象
     *
     * @param detail 明细实体
     * @return VO对象
     */
    private ExtractDetailVO convertToVO(DocumentExtractDetail detail) {
        ExtractDetailVO vo = new ExtractDetailVO();
        BeanUtils.copyProperties(detail, vo);
        return vo;
    }
}

