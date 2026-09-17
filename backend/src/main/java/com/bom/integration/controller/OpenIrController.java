package com.bom.integration.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.bom.service.BomService;
import com.bom.change.entity.Ecn;
import com.bom.change.mapper.EcnMapper;
import com.bom.common.BizException;
import com.bom.common.R;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** IR 控制塔：BOM / ECN 快照与 BOM 展开。 */
@RestController
@RequestMapping("/api/open/ir")
@RequiredArgsConstructor
public class OpenIrController {
    private final BomHeaderMapper headers;
    private final BomService bomService;
    private final EcnMapper ecns;

    @Value("${bom.open.api-key:bom-open-key}")
    private String apiKey;

    @GetMapping("/snapshots")
    public R<Map<String, Object>> snapshots(
            @RequestHeader(value = "X-Api-Key", required = false) String key) {
        checkKey(key);
        List<Map<String, Object>> rows = new ArrayList<>();
        for (BomHeader header : headers.selectList(new QueryWrapper<BomHeader>().orderByDesc("id"))) {
            rows.add(row("BOM", header.getBomNo(), header.getStatus(),
                    header.getRootPartNo(), BigDecimal.valueOf(header.getVersion() == null ? 0 : header.getVersion()),
                    null, header.getPlantCode(), header.getDescription()));
        }
        for (Ecn ecn : ecns.selectList(null)) {
            rows.add(row("ECN", ecn.getEcnNo(), ecn.getStatus(), null,
                    BigDecimal.ONE, null, null, ecn.getTitle()));
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
        if ("BOM_EXPLODE".equals(type)) {
            BomHeader header = headers.selectOne(new QueryWrapper<BomHeader>()
                    .eq("bom_no", targetKey)
                    .orderByDesc("version")
                    .last("LIMIT 1"));
            if (header == null) {
                throw new BizException("BOM 不存在: " + targetKey);
            }
            return R.ok(bomService.explode(header.getId(), null, null));
        }
        throw new BizException("不支持的 IR 指令: " + type);
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
