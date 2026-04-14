package cn.masu.dcs.service;

import cn.masu.dcs.dto.ExtractDetailCreateDTO;
import cn.masu.dcs.dto.ExtractDetailUpdateDTO;
import cn.masu.dcs.dto.ExtractMainUpdateDTO;
import cn.masu.dcs.vo.ExtractEditVO;

/**
 * 数据提取编辑服务接口
 * @author System
 */
public interface ExtractService {

    /**
     * 获取编辑数据
     */
    ExtractEditVO queryEditData(Long fileId);

    /**
     * 更新主表
     */
    Boolean updateMain(ExtractMainUpdateDTO dto);

    /**
     * 更新明细
     */
    Boolean updateDetail(ExtractDetailUpdateDTO dto);

    /**
     * 创建明细
     */
    Long insertDetail(ExtractDetailCreateDTO dto);

    /**
     * 删除明细
     */
    Boolean deleteDetail(Long id);

    /**
     * 标记为已验证
     */
    Boolean updateVerified(Long detailId);
}

