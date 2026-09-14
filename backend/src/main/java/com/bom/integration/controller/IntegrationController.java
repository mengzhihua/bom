package com.bom.integration.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bom.common.*;
import com.bom.integration.entity.IntegrationLog;
import com.bom.integration.mapper.IntegrationLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/integration")
@RequiredArgsConstructor
public class IntegrationController {
    private final IntegrationLogMapper logs;
    @GetMapping("/logs")
public R<Page<IntegrationLog>> logs(
    @RequestParam(defaultValue = "1") long current,
    @RequestParam(defaultValue = "20") long size) {
        return R.ok(logs.selectPage(new Page <>(current, size), new QueryWrapper<IntegrationLog>().orderByDesc("id")));
    }
    @PostMapping("/logs/{id}/retry")
public R<IntegrationLog> retry(
    @PathVariable Long id) {
        IntegrationLog x = logs.selectById(id);
        if (x == null) throw new BizException("日志不存在");
        x.setRetryCount(x.getRetryCount() == null ? 1: x.getRetryCount() + 1);
        logs.updateById(x);
        return R.ok(x);
    }
}
