package com.bom.bom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.entity.BomItem;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.bom.mapper.BomItemMapper;
import com.bom.bom.service.BomService;
import com.bom.common.BizException;
import com.bom.common.R;
import com.bom.integration.service.SapIntegrationService;
import com.bom.master.entity.Part;
import com.bom.master.entity.Plant;
import com.bom.master.entity.VehicleModel;
import com.bom.master.mapper.PartMapper;
import com.bom.master.mapper.PlantMapper;
import com.bom.master.mapper.VehicleModelMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boms")
@RequiredArgsConstructor
public class BomController {
    private final BomHeaderMapper headers;
    private final BomItemMapper items;
    private final BomService service;
    private final PartMapper parts;
    private final VehicleModelMapper models;
    private final PlantMapper plants;
    private final SapIntegrationService sap;

    @GetMapping
    public R<List<BomHeader>> list() {
        List<BomHeader> values = headers.selectList(
                new QueryWrapper<BomHeader>().orderByDesc("id"));
        values.forEach(this::fillNames);
        return R.ok(values);
    }

    @GetMapping("/{id}")
    public R<BomHeader> get(@PathVariable Long id) {
        BomHeader header = service.load(id);
        fillNames(header);
        return R.ok(header);
    }

    @PostMapping
    public R<BomHeader> create(@RequestBody BomHeader header) {
        return R.ok(service.create(header));
    }

    @PutMapping("/{id}")
    public R<BomHeader> update(
            @PathVariable Long id,
            @RequestBody BomHeader header) {
        header.setId(id);
        headers.updateById(header);
        return get(id);
    }

    @GetMapping("/{id}/items")
    public R<List<BomItem>> itemList(@PathVariable Long id) {
        return R.ok(service.itemList(id));
    }

    @PostMapping("/{id}/items")
    public R<BomItem> add(
            @PathVariable Long id,
            @RequestBody BomItem item) {
        return R.ok(service.add(id, item));
    }

    @PutMapping("/{id}/items/{itemId}")
    public R<BomItem> edit(
            @PathVariable Long id,
            @PathVariable Long itemId,
            @RequestBody BomItem item) {
        return R.ok(service.updateItem(id, itemId, item));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    public R<Void> delete(
            @PathVariable Long id,
            @PathVariable Long itemId) {
        if (!"DRAFT".equals(service.load(id).getStatus())) {
            throw new BizException("BOM不可修改");
        }
        items.deleteById(itemId);
        return R.ok();
    }

    @GetMapping("/{id}/tree")
    public R<List<Map<String, Object>>> tree(@PathVariable Long id) {
        return R.ok(service.tree(id, null));
    }

    @GetMapping("/{id}/explode")
    public R<List<Map<String, Object>>> explode(
            @PathVariable Long id,
            @RequestParam(required = false) Integer level) {
        return R.ok(service.explode(id, level, null));
    }

    @GetMapping("/{id}/summarized")
    public R<List<Map<String, Object>>> summarized(@PathVariable Long id) {
        return R.ok(service.summarized(id, null));
    }

    @GetMapping("/{id}/rollup")
    public R<Map<String, Object>> rollup(@PathVariable Long id) {
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (Map<String, Object> value : service.explode(id, null, null)) {
            Part part = parts.selectById((Long) value.get("partId"));
            BigDecimal quantity = (BigDecimal) value.get("extendedQty");
            if (part != null) {
                totalCost = totalCost.add(quantity.multiply(
                        part.getUnitCost() == null
                                ? BigDecimal.ZERO
                                : part.getUnitCost()));
                totalWeight = totalWeight.add(quantity.multiply(
                        part.getWeightKg() == null
                                ? BigDecimal.ZERO
                                : part.getWeightKg()));
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalCost", totalCost);
        result.put("totalWeight", totalWeight);
        result.put("details", service.explode(id, null, null));
        return R.ok(result);
    }

    @GetMapping("/{id}/by-station")
    public R<Map<String, List<Map<String, Object>>>> byStation(
            @PathVariable Long id) {
        return R.ok(service.byStation(id));
    }

    @PostMapping("/{id}/import")
    public R<Map<String, Object>> importCsv(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("count", service.importCsv(id, file));
        return R.ok(result);
    }

    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> exportCsv(@PathVariable Long id) {
        return service.exportCsv(id);
    }

    @PostMapping("/{id}/release")
    public R<BomHeader> release(@PathVariable Long id) {
        return R.ok(service.transition(id, "DRAFT", "RELEASED"));
    }

    @PostMapping("/{id}/freeze")
    public R<BomHeader> freeze(@PathVariable Long id) {
        return R.ok(service.transition(id, "RELEASED", "FROZEN"));
    }

    @PostMapping("/{id}/obsolete")
    public R<BomHeader> obsolete(@PathVariable Long id) {
        return R.ok(service.transition(id, "RELEASED", "OBSOLETE"));
    }

    @PostMapping("/{id}/new-version")
    public R<BomHeader> newVersion(@PathVariable Long id) {
        return R.ok(service.newVersion(id));
    }

    @PostMapping("/{id}/configure")
    public R<Map<String, Object>> configure(
            @PathVariable Long id,
            @RequestBody Config config) {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, Object>> exploded = service.explode(
                id,
                null,
                config.selections);
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        for (Map<String, Object> row : exploded) {
            Part part = parts.selectById((Long) row.get("partId"));
            BigDecimal quantity = (BigDecimal) row.get("extendedQty");
            if (part != null) {
                totalCost = totalCost.add(quantity.multiply(
                        part.getUnitCost() == null
                                ? BigDecimal.ZERO
                                : part.getUnitCost()));
                totalWeight = totalWeight.add(quantity.multiply(
                        part.getWeightKg() == null
                                ? BigDecimal.ZERO
                                : part.getWeightKg()));
            }
        }
        result.put("tree", service.tree(id, config.selections));
        result.put("explode", exploded);
        result.put("summarized", service.summarized(id, config.selections));
        result.put("totalCost", totalCost);
        result.put("totalWeight", totalWeight);
        return R.ok(result);
    }

    @GetMapping("/compare")
    public R<List<Map<String, Object>>> compare(
            @RequestParam Long leftId,
            @RequestParam Long rightId) {
        Map<String, Map<String, Object>> left = keyed(leftId);
        Map<String, Map<String, Object>> right = keyed(rightId);
        List<Map<String, Object>> output = new ArrayList<>();
        left.forEach((key, value) -> {
            if (!right.containsKey(key)) {
                output.add(diff("REMOVED", key, value, null));
            } else if (!value.equals(right.get(key))) {
                output.add(diff("CHANGED", key, value, right.get(key)));
            }
        });
        right.forEach((key, value) -> {
            if (!left.containsKey(key)) {
                output.add(diff("ADDED", key, null, value));
            }
        });
        return R.ok(output);
    }

    @PostMapping("/{id}/derive-mbom")
    public R<BomHeader> derive(
            @PathVariable Long id,
            @RequestBody Derive derive) {
        BomHeader source = service.load(id);
        if (!"EBOM".equals(source.getBomType())) {
            throw new BizException("仅 EBOM 可派生");
        }
        BomHeader target = new BomHeader();
        BeanUtils.copyProperties(source, target);
        target.setId(null);
        target.setBomNo(null);
        target.setBomType("MBOM");
        target.setPlantId(derive.plantId);
        target.setDescription(derive.description);
        target.setSourceBomId(id);
        target.setStatus("DRAFT");
        target = service.create(target);
        for (BomItem sourceItem : service.itemList(id)) {
            BomItem item = new BomItem();
            BeanUtils.copyProperties(sourceItem, item);
            item.setId(null);
            item.setBomId(target.getId());
            items.insert(item);
        }
        return R.ok(target);
    }

    @PostMapping("/{id}/sync-sap")
    public R<BomHeader> sync(@PathVariable Long id) {
        return R.ok(sap.syncBom(id));
    }

    private void fillNames(BomHeader header) {
        Part root = parts.selectById(header.getRootPartId());
        if (root != null) {
            header.setRootPartNo(root.getPartNo());
            header.setRootPartName(root.getPartName());
        }
        VehicleModel model = models.selectById(header.getVehicleModelId());
        if (model != null) {
            header.setVehicleModelCode(model.getModelCode());
            header.setVehicleModelName(model.getModelName());
        }
        Plant plant = plants.selectById(header.getPlantId());
        if (plant != null) {
            header.setPlantCode(plant.getPlantCode());
            header.setPlantName(plant.getPlantName());
        }
        BomHeader source = headers.selectById(header.getSourceBomId());
        if (source != null) {
            header.setSourceBomNo(source.getBomNo());
        }
    }

    private Map<String, Map<String, Object>> keyed(Long id) {
        Map<String, Map<String, Object>> output = new LinkedHashMap<>();
        for (BomItem item : service.itemList(id)) {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("qty", item.getQty());
            value.put("findNo", item.getFindNo());
            value.put("usageCondition", item.getUsageCondition());
            value.put("stationCode", item.getStationCode());
            output.put(item.getParentPartId() + "-" + item.getChildPartId(), value);
        }
        return output;
    }

    private Map<String, Object> diff(
            String type,
            String key,
            Map<String, Object> left,
            Map<String, Object> right) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("type", type);
        value.put("key", key);
        value.put("left", left);
        value.put("right", right);
        return value;
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
