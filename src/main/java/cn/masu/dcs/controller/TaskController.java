package cn.masu.dcs.controller;

import cn.masu.dcs.common.result.PageResult;
import cn.masu.dcs.common.result.R;
import cn.masu.dcs.service.TaskLogService;
import cn.masu.dcs.vo.TaskLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 任务日志控制器
 * <p>
 * 同时支持 /api/task 和 /api/tasks 路径，保证向后兼容
 * </p>
 * @author zyq
 */
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class TaskController {

    private final TaskLogService taskLogService;

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public R<TaskLogVO> getTaskDetail(@PathVariable Long id) {
        TaskLogVO vo = taskLogService.queryTaskById(id);
        return R.ok(vo);
    }

    /**
     * 分页查询任务日志
     */
    @GetMapping("/page")
    public R<PageResult<TaskLogVO>> listTasks(
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size,
            @RequestParam(required = false) String taskType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate) {
        PageResult<TaskLogVO> pageResult = taskLogService.queryTaskPage(current, size, taskType, status, startDate, endDate);
        return R.ok(pageResult);
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    public R<Object> getTaskStatistics() {
        Object statistics = taskLogService.queryTaskStatistics();
        return R.ok(statistics);
    }
}

