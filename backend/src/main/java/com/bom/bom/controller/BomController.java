package com.bom.bom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.*;
import com.bom.bom.mapper.*;
import com.bom.bom.service.*;
import com.bom.common.*;
import com.bom.integration.service.SapIntegrationService;
import com.bom.master.entity.Part;
import com.bom.master.mapper.PartMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.math.*;
import java.util.*;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {
    private final BomHeaderMapper headers;
    private final BomItemMapper items;
    private final BomService service;
    private final PartMapper parts;
    private final SapIntegrationService sap;
    @GetMapping
public R<List<BomHeader>> list() {
        return R.ok(headers.selectList(new QueryWrapper<BomHeader>().orderByDesc("id")));
    }
    @GetMapping("/{id}")
public R<BomHeader> get(
    @PathVariable Long id) {
        return R.ok(service.load(id));
    }
    @PostMapping
public R<BomHeader> create(
    @RequestBody BomHeader h) {
        return R.ok(service.create(h));
    }
    @PutMapping("/{id}")
public R<BomHeader> update(
    @PathVariable Long id,
    @RequestBody BomHeader h) {
        h.setId(id);
        headers.updateById(h);
        return R.ok(service.load(id));
    }
    @GetMapping("/{id}/items")
public R<List<BomItem>> itemList(
    @PathVariable Long id) {
        return R.ok(service.itemList(id));
    }
    @PostMapping("/{id}/items")
public R<BomItem> add(
    @PathVariable Long id,
    @RequestBody BomItem x) {
        return R.ok(service.add(id, x));
    }
    @PutMapping("/{id}/items/{itemId}")
public R<BomItem> edit(
    @PathVariable Long id,
    @PathVariable Long itemId,
    @RequestBody BomItem x) {
        return R.ok(service.updateItem(id, itemId, x));
    }
    @DeleteMapping("/{id}/items/{itemId}")
public R<Void> del(
    @PathVariable Long id,
    @PathVariable Long itemId) {
        if (!"DRAFT".equals(service.load(id).getStatus())) throw new BizException("BOM不可修改");
        items.deleteById(itemId);
        return R.ok();
    }
    @GetMapping("/{id}/tree")
public R<List<Map<String, Object>>> tree(
    @PathVariable Long id) {
        return R.ok(service.explode(id, null, null));
    }
    @GetMapping("/{id}/explode")
public R<List<Map<String, Object>>> explode(
    @PathVariable Long id,
    @RequestParam(required = false) Integer level) {
        return R.ok(service.explode(id, level, null));
    }
    @GetMapping("/{id}/summarized")
public R<List<Map<String, Object>>> summarized(
    @PathVariable Long id) {
        return R.ok(service.summarized(id, null));
    }
    @GetMapping("/{id}/rollup")
public R<Map<String, Object>> rollup(
    @PathVariable Long id) {
        BigDecimal cost = BigDecimal.ZERO, weight = BigDecimal.ZERO;
        for (Map<String, Object> x: service.explode(id, null, null)) {
            Part p = parts.selectById((Long) x.get("partId"));
            BigDecimal q = (BigDecimal) x.get("extendedQty");
            if (p != null) {
                cost = cost.add(q.multiply(p.getUnitCost() == null ? BigDecimal.ZERO: p.getUnitCost()));
                weight = weight.add(q.multiply(p.getWeightKg() == null ? BigDecimal.ZERO: p.getWeightKg()));
            }
        }
        Map<String, Object> r = new LinkedHashMap <>();
        r.put("totalCost", cost);
        r.put("totalWeight", weight);
        r.put("details", service.explode(id, null, null));
        return R.ok(r);
    }
    @PostMapping("/{id}/release")
public R<BomHeader> release(
    @PathVariable Long id) {
        return R.ok(service.transition(id, "DRAFT", "RELEASED"));
    }
    @PostMapping("/{id}/freeze")
public R<BomHeader> freeze(
    @PathVariable Long id) {
        return R.ok(service.transition(id, "RELEASED", "FROZEN"));
    }
    @PostMapping("/{id}/obsolete")
public R<BomHeader> obsolete(
    @PathVariable Long id) {
        return R.ok(service.transition(id, "RELEASED", "OBSOLETE"));
    }
    @PostMapping("/{id}/new-version")
public R<BomHeader> version(
    @PathVariable Long id) {
        return R.ok(service.newVersion(id));
    }
    @PostMapping("/{id}/configure")
public R<Map<String, Object>> configure(
    @PathVariable Long id,
    @RequestBody Config c) {
        return result(id, c.selections);
    }
    @GetMapping("/compare")
public R<List<Map<String, Object>>> compare(
    @RequestParam Long leftId,
    @RequestParam Long rightId) {
        Map<String, Map<String, Object>> left = keyed(leftId), right = keyed(rightId);
        List<Map<String, Object>> out = new ArrayList <>();
        for (String k: left.keySet()) if (!right.containsKey(k)) {
            Map<String, Object> x = new LinkedHashMap <>();
            x.put("type", "REMOVED");
            x.put("key", k);
            x.put("left", left.get(k));
            out.add(x);
        }
        for (String k: right.keySet()) if (!left.containsKey(k)) {
            Map<String, Object> x = new LinkedHashMap <>();
            x.put("type", "ADDED");
            x.put("key", k);
            x.put("right", right.get(k));
            out.add(x);
        }
        for (String k: left.keySet()) if (right.containsKey(k) && !String.valueOf(left.get(k)).equals(String.valueOf(right.get(k)))) {
            Map<String, Object> x = new LinkedHashMap <>();
            x.put("type", "CHANGED");
            x.put("key", k);
            x.put("left", left.get(k));
            x.put("right", right.get(k));
            out.add(x);
        }
        return R.ok(out);
    }
    private Map<String, Map<String, Object>> keyed(Long id) {
        Map<String, Map<String, Object>> r = new LinkedHashMap <>();
        for (BomItem i: service.itemList(id)) r.put(i.getParentPartId() + "-" + i.getChildPartId(), new LinkedHashMap<String, Object>() {
            {
                put("qty", i.getQty()); put("findNo", i.getFindNo()); put("usageCondition", i.getUsageCondition()); put("stationCode", i.getStationCode());
            }
        }
        );
        return r;
    }
    private R<Map<String, Object>> result(Long id, Map<String, String> s) {
        Map<String, Object> r = new LinkedHashMap <>();
        r.put("tree", service.explode(id, null, s));
        r.put("summarized", service.summarized(id, s));
        BigDecimal cost = BigDecimal.ZERO, weight = BigDecimal.ZERO;
        for (Map<String, Object> x: service.explode(id, null, s)) {
            Part p = parts.selectById((Long) x.get("partId"));
            BigDecimal q = (BigDecimal) x.get("extendedQty");
            if (p != null) {
                cost = cost.add(q.multiply(p.getUnitCost() == null ? BigDecimal.ZERO: p.getUnitCost()));
                weight = weight.add(q.multiply(p.getWeightKg() == null ? BigDecimal.ZERO: p.getWeightKg()));
            }
        }
        r.put("totalCost", cost);
        r.put("totalWeight", weight);
        return R.ok(r);
    }
    @PostMapping("/{id}/derive-mbom")
public R<BomHeader> derive(
    @PathVariable Long id,
    @RequestBody Derive d) {
        BomHeader src = service.load(id);
        if (!"EBOM".equals(src.getBomType())) throw new BizException("仅 EBOM 可派生");
        BomHeader h = new BomHeader();
        org.springframework.beans.BeanUtils.copyProperties(src, h);
        h.setId(null);
        h.setBomNo(null);
        h.setBomType("MBOM");
        h.setPlantId(d.plantId);
        h.setDescription(d.description);
        h.setSourceBomId(id);
        h.setStatus("DRAFT");
        h = service.create(h);
        for (BomItem i: service.itemList(id)) {
            i.setId(null);
            i.setBomId(h.getId());
            items.insert(i);
        }
        return R.ok(h);
    }
    @PostMapping("/{id}/sync-sap")
public R<BomHeader> sync(
    @PathVariable Long id) {
        return R.ok(sap.syncBom(id));
    }
    @Data
public static class Config {
        private Map<String, String> selections;
    }
    @Data
public static class Derive {
        private Long plantId;
        private String description;
    }
}
