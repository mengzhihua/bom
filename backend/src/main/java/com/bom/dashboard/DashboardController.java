package com.bom.dashboard;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.change.entity.*;
import com.bom.change.mapper.*;
import com.bom.integration.entity.IntegrationLog;
import com.bom.integration.mapper.IntegrationLogMapper;
import com.bom.master.entity.Part;
import com.bom.master.mapper.PartMapper;
import com.bom.master.entity.VehicleModel;
import com.bom.master.mapper.VehicleModelMapper;
import com.bom.system.entity.OpLog;
import com.bom.system.mapper.OpLogMapper;
import com.bom.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

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
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("partCount", parts.selectCount(null));
        Map<String, Long> lc = new LinkedHashMap<>();
        for (Part p : parts.selectList(null)) {
            lc.put(p.getLifecycle(), lc.getOrDefault(p.getLifecycle(), 0L) + 1);
        }
        m.put("partsByLifecycle", lc);
        m.put("bomCount", boms.selectCount(null));
        m.put("bomByType", boms.selectMaps(new QueryWrapper<BomHeader>().select("bom_type,count(*) as count").groupBy("bom_type")));
        m.put("pendingEcr", ecrs.selectCount(new QueryWrapper<Ecr>().eq("status", "SUBMITTED")));
        m.put("pendingEcn", ecns.selectCount(new QueryWrapper<Ecn>().eq("status", "APPROVED")));
        m.put("vehicleModels", models.selectList(null));
        m.put("recentLogs", logs.selectList(new QueryWrapper<IntegrationLog>().orderByDesc("id").last("LIMIT 10")));
        m.put("recentOpLogs", opLogs.selectList(new QueryWrapper<OpLog>().orderByDesc("id").last("LIMIT 10")));
        return R.ok(m);
    }
}
