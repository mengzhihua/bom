package com.bom.dashboard.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.change.entity.Ecn;
import com.bom.change.entity.Ecr;
import com.bom.change.mapper.EcnMapper;
import com.bom.change.mapper.EcrMapper;
import com.bom.common.R;
import com.bom.integration.entity.IntegrationLog;
import com.bom.integration.mapper.IntegrationLogMapper;
import com.bom.master.entity.Part;
import com.bom.master.entity.VehicleModel;
import com.bom.master.mapper.PartMapper;
import com.bom.master.mapper.VehicleModelMapper;
import com.bom.system.entity.OpLog;
import com.bom.system.mapper.OpLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {
    private final PartMapper parts;
    private final BomHeaderMapper boms;
    private final EcrMapper ecrs;
    private final EcnMapper ecns;
    private final VehicleModelMapper models;
    private final IntegrationLogMapper logs;
    private final OpLogMapper opLogs;

    @GetMapping("/summary")
    public R<Map<String, Object>> summary() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("partCount", parts.selectCount(null));

        Map<String, Long> lifecycle = new LinkedHashMap<>();
        for (Part part : parts.selectList(null)) {
            lifecycle.put(part.getLifecycle(),
                    lifecycle.getOrDefault(part.getLifecycle(), 0L) + 1);
        }
        result.put("partsByLifecycle", lifecycle);
        result.put("bomCount", boms.selectCount(null));
        result.put("bomByType", boms.selectMaps(new QueryWrapper<BomHeader>()
                .select("bom_type,count(*) as count")
                .groupBy("bom_type")));
        result.put("pendingEcr", ecrs.selectCount(
                new QueryWrapper<Ecr>().eq("status", "SUBMITTED")));
        result.put("pendingEcn", ecns.selectCount(
                new QueryWrapper<Ecn>().eq("status", "APPROVED")));
        result.put("vehicleModels", vehicleStatuses());
        result.put("recentLogs", logs.selectList(new QueryWrapper<IntegrationLog>()
                .orderByDesc("id")
                .last("LIMIT 10")));
        result.put("recentOpLogs", opLogs.selectList(new QueryWrapper<OpLog>()
                .orderByDesc("id")
                .last("LIMIT 10")));
        return R.ok(result);
    }

    private List<Map<String, Object>> vehicleStatuses() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (VehicleModel model : models.selectList(null)) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("id", model.getId());
            value.put("modelCode", model.getModelCode());
            value.put("modelName", model.getModelName());
            value.put("platform", model.getPlatform());
            value.put("program", model.getProgram());
            value.put("status", model.getStatus());
            value.put("ebomStatus", latestStatus(model.getId(), "EBOM"));
            value.put("mbomStatus", latestStatus(model.getId(), "MBOM"));
            result.add(value);
        }
        return result;
    }

    private String latestStatus(Long modelId, String bomType) {
        BomHeader header = boms.selectOne(new QueryWrapper<BomHeader>()
                .eq("vehicle_model_id", modelId)
                .eq("bom_type", bomType)
                .in("status", "RELEASED", "FROZEN")
                .orderByDesc("version")
                .last("LIMIT 1"));
        return header == null ? "暂无" : header.getStatus();
    }
}
