package cn.masu.dcs.service;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.dto.FileUpdateStatusDTO;
import cn.masu.dcs.entity.DocumentFile;
import cn.masu.dcs.vo.FileDetailVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务接口
 *
 * @author zyq
 * @since 2025-12-06
 */
public interface FileService extends IService<DocumentFile> {

    /**
     * 上传文件
     *
     * @param file       上传的文件
     * @param templateId 模板ID
     * @param userId     用户ID
     * @return 文件ID
     */
    Long insertFile(MultipartFile file, Long templateId, Long userId);

    /**
     * 下载文件
     *
     * @param id 文件ID
     * @return 文件字节数组
     */
    byte[] queryFileContent(Long id);

    /**
     * 删除文件
     *
     * @param id 文件ID
     * @return 是否成功
     */
    Boolean deleteFile(Long id);

    /**
     * 获取文件详情
     *
     * @param id 文件ID
     * @return 文件详情
     */
    FileDetailVO queryFileById(Long id);

    /**
     * 更新文件状态
     *
     * @param dto 状态更新DTO
     * @return 是否成功
     */
    Boolean updateFileStatus(FileUpdateStatusDTO dto);

    /**
     * 更新文件关联的模板
     *
     * @param fileId     文件ID
     * @param templateId 新模板ID
     * @return 是否成功
     */
    Boolean updateFileTemplate(Long fileId, Long templateId);

    /**
     * 分页查询文件列表
     *
     * @param current 当前页
     * @param size    每页大小
     * @param keyword 关键字
     * @param status  状态
     * @param userId  用户ID
     * @return 分页结果
     */
    PageResult<FileDetailVO> queryFilePage(Long current, Long size, String keyword, Integer status, Long userId);
}

