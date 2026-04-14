package cn.masu.dcs.service;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.entity.SysTaskLog;
import cn.masu.dcs.vo.TaskLogVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;

/**
 * 任务日志服务接口
 * @author System
 */
public interface TaskLogService extends IService<SysTaskLog> {

    /**
     * 创建任务日志
     */
    Long insertTaskLog(String taskName, String taskType, Long targetId);

    /**
     * 更新任务状态为成功
     */
    Boolean updateTaskSuccess(Long id);

    /**
     * 更新任务状态为失败
     */
    Boolean updateTaskFail(Long id, String errorMessage);

    /**
     * 获取任务详情
     */
    TaskLogVO queryTaskById(Long id);

    /**
     * 分页查询任务日志
     */
    PageResult<TaskLogVO> queryTaskPage(Long current, Long size, String taskType, Integer status, Date startDate, Date endDate);

    /**
     * 获取任务统计信息
     */
    Object queryTaskStatistics();
}

