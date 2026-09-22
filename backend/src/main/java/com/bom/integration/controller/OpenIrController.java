package com.bom.integration.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.bom.service.BomService;
import com.bom.change.entity.Ecn;
import com.bom.change.mapper.EcnMapper;
import com.bom.change.service.EcnService;
import com.bom.common.BizException;
import com.bom.common.R;
import com.bom.master.entity.Plant;
import com.bom.master.mapper.PlantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/** IR 控制塔：BOM / ECN 快照与 BOM 展开。 */
@RestController
@RequestMapping("/api/open/ir")
@RequiredArgsConstructor
public class OpenIrController {
    private final BomHeaderMapper headers;
    private final BomService bomService;
    private final EcnMapper ecns;
    private final EcnService ecnService;
    private final PlantMapper plants;
    private final ConcurrentHashMap<String, Object> actionCache = new ConcurrentHashMap<String, Object>();

    @Value("${bom.open.api-key:bom-open-key}")
    private String apiKey;

    @GetMapping("/snapshots")
    public R<Map<String, Object>> snapshots(
            @RequestHeader(value = "X-Api-Key", required = false) String key) {
        checkKey(key);
        Map<Long, String> plantById = new HashMap<Long, String>();
        for (Plant plant : plants.selectList(null)) {
            if (plant.getId() != null) {
                plantById.put(plant.getId(), plant.getPlantCode());
            }
        }
        Map<Long, BomHeader> headerById = new LinkedHashMap<Long, BomHeader>();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (BomHeader header : headers.selectList(new QueryWrapper<BomHeader>().orderByDesc("id"))) {
            headerById.put(header.getId(), header);
            rows.add(row("BOM", header.getBomNo(), header.getStatus(),
                    header.getRootPartNo(), BigDecimal.valueOf(header.getVersion() == null ? 0 : header.getVersion()),
                    null, plantById.get(header.getPlantId()), header.getDescription()));
        }
        for (Ecn ecn : ecns.selectList(null)) {
            BomHeader header = ecn.getBomId() == null ? null : headerById.get(ecn.getBomId());
            if (header == null && ecn.getBomId() != null) {
                header = headers.selectById(ecn.getBomId());
            }
            String plant = header == null ? null : plantById.get(header.getPlantId());
            if (plant == null && header != null) {
                for (BomHeader other : headerById.values()) {
                    if (header.getId().equals(other.getSourceBomId()) && other.getPlantId() != null) {
                        plant = plantById.get(other.getPlantId());
                        break;
                    }
                }
            }
            rows.add(row("ECN", ecn.getEcnNo(), ecn.getStatus(),
                    header == null ? null : header.getBomNo(),
                    BigDecimal.ONE, null, plant, ecn.getTitle()));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("system", "BOM");
        data.put("snapshots", rows);
        return R.ok(data);
    }

    @PostMapping("/actions")
    public R<Object> actions(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        checkKey(key);
        String type = String.valueOf(body.getOrDefault("type", ""));
        String targetKey = String.valueOf(body.getOrDefault("targetKey", ""));
        return R.ok(executeOnce(cacheKey(type, targetKey, body.get("idempotencyKey")), () -> {
            if ("BOM_EXPLODE".equals(type)) {
                BomHeader header = headers.selectOne(new QueryWrapper<BomHeader>()
                        .eq("bom_no", targetKey)
                        .orderByDesc("version")
                        .last("LIMIT 1"));
                if (header == null) {
                    throw new BizException("BOM 不存在: " + targetKey);
                }
                return bomService.explode(header.getId(), null, null);
            }
            if ("BOM_IMPLEMENT_ECN".equals(type) || "BOM_SUBMIT_ECN".equals(type)
                    || "BOM_APPROVE_ECN".equals(type)) {
                Ecn ecn = ecns.selectOne(new QueryWrapper<Ecn>()
                        .eq("ecn_no", targetKey)
                        .orderByDesc("id")
                        .last("LIMIT 1"));
                if (ecn == null) {
                    throw new BizException("ECN 不存在: " + targetKey);
                }
                if ("BOM_SUBMIT_ECN".equals(type)) {
                    return ecnService.submit(ecn.getId());
                }
                if ("BOM_APPROVE_ECN".equals(type)) {
                    return ecnService.approve(ecn.getId());
                }
                return ecnService.implement(ecn.getId());
            }
            throw new BizException("不支持的 IR 指令: " + type);
        }));
    }

    @PostMapping("/explode")
    public R<Object> explode(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        return typedAction(key, body, "BOM_EXPLODE", "bomNo");
    }

    @PostMapping("/submit-ecn")
    public R<Object> submitEcn(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        return typedAction(key, body, "BOM_SUBMIT_ECN", "ecnNo");
    }

    @PostMapping("/approve-ecn")
    public R<Object> approveEcn(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        return typedAction(key, body, "BOM_APPROVE_ECN", "ecnNo");
    }

    @PostMapping("/implement-ecn")
    public R<Object> implementEcn(
            @RequestHeader(value = "X-Api-Key", required = false) String key,
            @RequestBody Map<String, Object> body) {
        return typedAction(key, body, "BOM_IMPLEMENT_ECN", "ecnNo");
    }

    private R<Object> typedAction(
            String key, Map<String, Object> body, String type, String... altKeys) {
        if (body == null) {
            body = new LinkedHashMap<String, Object>();
        }
        body.put("type", type);
        if (blank(body.get("targetKey"))) {
            for (String altKey : altKeys) {
                Object value = body.get(altKey);
                if (!blank(value)) {
                    body.put("targetKey", value);
                    break;
                }
            }
        }
        return actions(key, body);
    }

    private static boolean blank(Object value) {
        return value == null || String.valueOf(value).trim().isEmpty()
                || "null".equals(String.valueOf(value));
    }

    private Object executeOnce(String cacheKey, Supplier<Object> work) {
        if (cacheKey == null) {
            return work.get();
        }
        Object cached = actionCache.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        synchronized (actionCache) {
            cached = actionCache.get(cacheKey);
            if (cached != null) {
                return cached;
            }
            Object created = work.get();
            actionCache.put(cacheKey, created);
            return created;
        }
    }

    private static String cacheKey(String type, String targetKey, Object idempotencyKey) {
        if (idempotencyKey == null) {
            return null;
        }
        String key = String.valueOf(idempotencyKey).trim();
        if (key.isEmpty() || "null".equals(key)) {
            return null;
        }
        return type + "|" + (targetKey == null ? "" : targetKey) + "|" + key;
    }

    private void checkKey(String key) {
        if (apiKey == null || apiKey.isEmpty() || !apiKey.equals(key)) {
            throw new BizException("无效的 API Key");
        }
    }

    private static Map<String, Object> row(
            String dataType, String bizKey, String status, String sku,
            BigDecimal qty, BigDecimal amount, String plantCode, String title) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("dataType", dataType);
        row.put("bizKey", bizKey);
        row.put("status", status);
        row.put("sku", sku);
        row.put("qty", qty);
        row.put("amount", amount);
        row.put("plantCode", plantCode);
        row.put("title", title);
        return row;
    }
}
