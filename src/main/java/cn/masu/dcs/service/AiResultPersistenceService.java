package cn.masu.dcs.service;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * AI结果持久化服务接口
 * <p>
 * 专门负责AI处理结果的数据库事务操作，解决事务自调用问题
 * </p>
 *
 * @author zyq
 * @since 2025-12-17
 */
public interface AiResultPersistenceService {

    /**
     * 保存AI处理结果到数据库（事务方法）
     *
     * @param fileId   文件ID
     * @param aiResult AI处理结果
     * @param fileUrl  文件URL
     */
    void insertAiResult(Long fileId, JsonNode aiResult, String fileUrl);

    /**
     * 更新文件处理状态（事务方法）
     *
     * @param fileId        文件ID
     * @param processStatus 处理状态
     * @param failReason    失败原因（可选）
     */
    void updateFileStatus(Long fileId, Integer processStatus, String failReason);
}

