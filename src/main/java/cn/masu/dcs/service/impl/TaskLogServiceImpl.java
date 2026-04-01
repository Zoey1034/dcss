package cn.masu.dcs.service.impl;

import cn.masu.dcs.common.exception.BusinessException;
import cn.masu.dcs.common.result.ErrorCode;
import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.util.SnowflakeIdGenerator;
import cn.masu.dcs.entity.SysTaskLog;
import cn.masu.dcs.mapper.SysTaskLogMapper;
import cn.masu.dcs.service.TaskLogService;
import cn.masu.dcs.vo.TaskLogVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 任务日志服务实现
 * @author System
 */
@Service
@RequiredArgsConstructor
public class TaskLogServiceImpl extends ServiceImpl<SysTaskLogMapper, SysTaskLog> implements TaskLogService {

    private final SnowflakeIdGenerator idGenerator;

    @Override
    public Long insertTaskLog(String taskName, String taskType, Long targetId) {
        SysTaskLog taskLog = new SysTaskLog();
        taskLog.setId(idGenerator.nextId());
        taskLog.setTaskName(taskName);
        taskLog.setTaskType(taskType);
        taskLog.setTargetId(targetId);
        taskLog.setStatus(1);
        taskLog.setStartTime(new Date());

        baseMapper.insert(taskLog);
        return taskLog.getId();
    }

    @Override
    public Boolean updateTaskSuccess(Long id) {
        SysTaskLog taskLog = baseMapper.selectById(id);
        if (taskLog == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "任务日志不存在");
        }

        Date endTime = new Date();
        taskLog.setStatus(2); // 2-成功
        taskLog.setEndTime(endTime);

        // 计算执行时长（毫秒）
        if (taskLog.getStartTime() != null) {
            taskLog.setDuration(endTime.getTime() - taskLog.getStartTime().getTime());
        }

        return baseMapper.updateById(taskLog) > 0;
    }

    @Override
    public Boolean updateTaskFail(Long id, String errorMessage) {
        SysTaskLog taskLog = baseMapper.selectById(id);
        if (taskLog == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "任务日志不存在");
        }

        Date endTime = new Date();
        taskLog.setStatus(3); // 3-失败
        taskLog.setEndTime(endTime);
        taskLog.setErrorMessage(errorMessage);

        // 计算执行时长（毫秒）
        if (taskLog.getStartTime() != null) {
            taskLog.setDuration(endTime.getTime() - taskLog.getStartTime().getTime());
        }

        return baseMapper.updateById(taskLog) > 0;
    }

    @Override
    public TaskLogVO queryTaskById(Long id) {
        SysTaskLog taskLog = baseMapper.selectById(id);
        if (taskLog == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR.getCode(), "任务日志不存在");
        }
        return convertToVO(taskLog);
    }

    @Override
    public PageResult<TaskLogVO> queryTaskPage(Long current, Long size, String taskType, Integer status, Date startDate, Date endDate) {
        Page<SysTaskLog> page = new Page<>(current, size);
        LambdaQueryWrapper<SysTaskLog> wrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(taskType)) {
            wrapper.eq(SysTaskLog::getTaskType, taskType);
        }

        if (status != null) {
            wrapper.eq(SysTaskLog::getStatus, status);
        }

        if (startDate != null) {
            wrapper.ge(SysTaskLog::getCreateTime, startDate);
        }

        if (endDate != null) {
            wrapper.le(SysTaskLog::getCreateTime, endDate);
        }

        wrapper.orderByDesc(SysTaskLog::getCreateTime);

        Page<SysTaskLog> result = baseMapper.selectPage(page, wrapper);

        List<TaskLogVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResult.of(result.getTotal(), result.getCurrent(), result.getSize(), voList);
    }

    @Override
    public Object queryTaskStatistics() {
        // 统计各状态的任务数量
        LambdaQueryWrapper<SysTaskLog> wrapper = new LambdaQueryWrapper<>();

        Map<String, Object> statistics = new HashMap<>();

        // 总任务数
        long total = baseMapper.selectCount(wrapper);
        statistics.put("total", total);

        // 运行中
        wrapper.clear();
        wrapper.eq(SysTaskLog::getStatus, 1);
        long running = baseMapper.selectCount(wrapper);
        statistics.put("running", running);

        // 成功
        wrapper.clear();
        wrapper.eq(SysTaskLog::getStatus, 2);
        long success = baseMapper.selectCount(wrapper);
        statistics.put("success", success);

        // 失败
        wrapper.clear();
        wrapper.eq(SysTaskLog::getStatus, 3);
        long failed = baseMapper.selectCount(wrapper);
        statistics.put("failed", failed);

        // 成功率
        if (total > 0) {
            double successRate = (double) success / total * 100;
            statistics.put("successRate", String.format("%.2f%%", successRate));
        } else {
            statistics.put("successRate", "0.00%");
        }

        return statistics;
    }

    private TaskLogVO convertToVO(SysTaskLog taskLog) {
        TaskLogVO vo = new TaskLogVO();
        BeanUtils.copyProperties(taskLog, vo);

        vo.setTaskTypeName(getTaskTypeName(taskLog.getTaskType()));
        vo.setStatusName(getStatusName(taskLog.getStatus()));
        vo.setDurationStr(formatDuration(taskLog.getDuration()));

        return vo;
    }

    private String getTaskTypeName(String taskType) {
        if (taskType == null) return "未知";
        return switch (taskType) {
            case "OCR" -> "OCR识别";
            case "NLP" -> "信息提取";
            case "AUDIT" -> "审核处理";
            case "TRAINING" -> "样本生成";
            default -> taskType;
        };
    }

    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        return switch (status) {
            case 0 -> "待执行";
            case 1 -> "运行中";
            case 2 -> "成功";
            case 3 -> "失败";
            default -> "未知";
        };
    }

    private String formatDuration(Long duration) {
        if (duration == null) return "-";

        long seconds = duration / 1000;
        if (seconds < 60) {
            return seconds + "秒";
        } else if (seconds < 3600) {
            long minutes = seconds / 60;
            long secs = seconds % 60;
            return minutes + "分" + secs + "秒";
        } else {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            return hours + "小时" + minutes + "分";
        }
    }
}

