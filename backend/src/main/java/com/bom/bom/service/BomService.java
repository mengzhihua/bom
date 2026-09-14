package com.bom.bom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.entity.BomItem;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.bom.mapper.BomItemMapper;
import com.bom.common.BizException;
import com.bom.common.CodeGenerator;
import com.bom.common.Csv;
import com.bom.master.entity.Part;
import com.bom.master.mapper.PartMapper;
import com.bom.process.entity.WorkStation;
import com.bom.process.mapper.WorkStationMapper;
import com.bom.system.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {
    private final BomHeaderMapper headers;
    private final BomItemMapper items;
    private final PartMapper parts;
    private final CodeGenerator codes;
    private final WorkStationMapper stations;

    public BomHeader load(Long id) {
        BomHeader header = headers.selectById(id);
        if (header == null) {
            throw new BizException("BOM不存在");
        }
        return header;
    }

    public List<BomItem> itemList(Long id) {
        return items.selectList(new QueryWrapper<BomItem>()
                .eq("bom_id", id)
                .orderByAsc("find_no", "id"));
    }

    @Transactional
    public BomHeader create(BomHeader header) {
        header.setId(null);
        if (header.getBomNo() == null) {
            header.setBomNo(codes.next("BOM").replace("-", ""));
        }
        if (header.getVersion() == null) {
            header.setVersion(1);
        }
        if (header.getStatus() == null) {
            header.setStatus("DRAFT");
        }
        headers.insert(header);
        return header;
    }

    public BomHeader update(Long id, BomHeader incoming) {
        BomHeader current = load(id);
        if (!"DRAFT".equals(current.getStatus())) {
            throw new BizException("仅 DRAFT BOM 可编辑");
        }
        current.setDescription(incoming.getDescription());
        current.setVehicleModelId(incoming.getVehicleModelId());
        current.setPlantId(incoming.getPlantId());
        current.setEffectiveFrom(incoming.getEffectiveFrom());
        current.setEffectiveTo(incoming.getEffectiveTo());
        headers.updateById(current);
        return current;
    }

    public BomItem add(Long id, BomItem item) {
        BomHeader header = load(id);
        if (!"DRAFT".equals(header.getStatus())) {
            throw new BizException("RELEASED BOM 行不可修改");
        }
        if (item.getParentPartId() == null) {
            item.setParentPartId(header.getRootPartId());
        }
        if (Objects.equals(item.getParentPartId(), item.getChildPartId())) {
            throw new BizException("不允许成环");
        }
        if (parts.selectById(item.getChildPartId()) == null) {
            throw new BizException("子零件不存在");
        }
        if (wouldCycle(id, item.getParentPartId(), item.getChildPartId(), null)) {
            throw new BizException("不允许成环");
        }
        item.setId(null);
        item.setBomId(id);
        if (item.getFindNo() == null) {
            item.setFindNo(itemList(id).stream()
                    .map(BomItem::getFindNo)
                    .filter(Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0) + 10);
        }
        if (item.getQty() == null) {
            item.setQty(BigDecimal.ONE);
        }
        items.insert(item);
        return item;
    }

    private boolean wouldCycle(
            Long id,
            Long parent,
            Long child,
            Long excludedItemId) {
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        BomHeader header = load(id);
        for (BomItem item : itemList(id)) {
            if (Objects.equals(item.getId(), excludedItemId)) {
                continue;
            }
            Long parentId = item.getParentPartId() == null
                    ? header.getRootPartId()
                    : item.getParentPartId();
            childrenByParent
                    .computeIfAbsent(parentId, key -> new ArrayList<>())
                    .add(item.getChildPartId());
        }
        Set<Long> seen = new HashSet<>();
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(child);
        while (!queue.isEmpty()) {
            Long current = queue.remove();
            if (!seen.add(current)) {
                continue;
            }
            if (Objects.equals(current, parent)) {
                return true;
            }
            queue.addAll(childrenByParent.getOrDefault(current, Collections.emptyList()));
        }
        return false;
    }

    public BomItem updateItem(Long id, Long itemId, BomItem item) {
        BomHeader header = load(id);
        if (!"DRAFT".equals(header.getStatus())) {
            throw new BizException("BOM不可修改");
        }
        BomItem existing = items.selectOne(new QueryWrapper<BomItem>()
                .eq("id", itemId)
                .eq("bom_id", id));
        if (existing == null) {
            throw new BizException("BOM行不存在");
        }
        if (item.getParentPartId() == null) {
            item.setParentPartId(header.getRootPartId());
        }
        if (item.getChildPartId() == null
                || parts.selectById(item.getChildPartId()) == null) {
            throw new BizException("子零件不存在");
        }
        if (Objects.equals(item.getParentPartId(), item.getChildPartId())
                || wouldCycle(id, item.getParentPartId(), item.getChildPartId(),
                itemId)) {
            throw new BizException("不允许成环");
        }
        item.setId(itemId);
        item.setBomId(id);
        items.updateById(item);
        return items.selectById(itemId);
    }

    @Transactional
    public BomHeader transition(Long id, String from, String to) {
        BomHeader header = load(id);
        if (!from.equals(header.getStatus())) {
            throw new BizException("当前状态不允许操作");
        }
        if ("RELEASED".equals(to)) {
            List<String> invalid = new ArrayList<>();
            for (BomItem item : itemList(id)) {
                Part part = parts.selectById(item.getChildPartId());
                if (part == null) {
                    invalid.add(String.valueOf(item.getChildPartId()));
                } else if (!"RELEASED".equals(part.getLifecycle())) {
                    invalid.add(part.getPartNo());
                }
            }
            if (!invalid.isEmpty()) {
                throw new BizException("存在未发布子零件: " + String.join(", ", invalid));
            }
            header.setReleasedBy(CurrentUser.get() == null
                    ? null
                    : CurrentUser.get().getUsername());
            header.setReleasedAt(LocalDateTime.now());
        }
        header.setStatus(to);
        headers.updateById(header);
        if ("RELEASED".equals(to)) {
            obsoletePreviousVersions(header);
        }
        return header;
    }

    private void obsoletePreviousVersions(BomHeader released) {
        QueryWrapper<BomHeader> query = new QueryWrapper<BomHeader>()
                .eq("root_part_id", released.getRootPartId())
                .eq("bom_type", released.getBomType())
                .in("status", "RELEASED", "FROZEN")
                .ne("id", released.getId());
        if (released.getPlantId() == null) {
            query.isNull("plant_id");
        } else {
            query.eq("plant_id", released.getPlantId());
        }
        for (BomHeader previous : headers.selectList(query)) {
            previous.setStatus("OBSOLETE");
            headers.updateById(previous);
        }
    }

    @Transactional
    public BomHeader newVersion(Long id) {
        BomHeader old = load(id);
        if (!"RELEASED".equals(old.getStatus())
                && !"FROZEN".equals(old.getStatus())) {
            throw new BizException("仅 RELEASED 或 FROZEN BOM 可升版");
        }
        Long draftCount = headers.selectCount(new QueryWrapper<BomHeader>()
                .eq("bom_no", old.getBomNo())
                .eq("status", "DRAFT"));
        if (draftCount != null && draftCount > 0) {
            throw new BizException("已存在未发布的新版本");
        }
        BomHeader latest = headers.selectOne(new QueryWrapper<BomHeader>()
                .select("version")
                .eq("bom_no", old.getBomNo())
                .orderByDesc("version")
                .last("LIMIT 1"));
        int nextVersion = latest == null || latest.getVersion() == null
                ? 1
                : latest.getVersion() + 1;
        BomHeader header = new BomHeader();
        BeanUtils.copyProperties(old, header);
        header.setId(null);
        header.setBomNo(old.getBomNo());
        header.setVersion(nextVersion);
        header.setStatus("DRAFT");
        headers.insert(header);
        for (BomItem oldItem : itemList(id)) {
            BomItem item = new BomItem();
            BeanUtils.copyProperties(oldItem, item);
            item.setId(null);
            item.setBomId(header.getId());
            items.insert(item);
        }
        return header;
    }

    @Transactional
    public BomHeader deriveMbom(
            Long id,
            Long plantId,
            String description) {
        BomHeader source = load(id);
        if (!"EBOM".equals(source.getBomType())) {
            throw new BizException("仅 EBOM 可派生");
        }
        BomHeader target = new BomHeader();
        BeanUtils.copyProperties(source, target);
        target.setId(null);
        target.setBomNo(null);
        target.setBomType("MBOM");
        target.setPlantId(plantId);
        target.setDescription(description);
        target.setSourceBomId(id);
        target.setStatus("DRAFT");
        target = create(target);
        for (BomItem sourceItem : itemList(id)) {
            BomItem item = new BomItem();
            BeanUtils.copyProperties(sourceItem, item);
            item.setId(null);
            item.setBomId(target.getId());
            items.insert(item);
        }
        return target;
    }

    public List<Map<String, Object>> explode(
            Long id,
            Integer maxLevel,
            Map<String, String> selections) {
        BomContext context = context(id);
        List<Map<String, Object>> output = new ArrayList<>();
        walk(context, context.header.getRootPartId(), BigDecimal.ONE, 0, "",
                output, maxLevel == null ? 99 : maxLevel, selections,
                new HashSet<>(Collections.singleton(context.header.getRootPartId())));
        return output;
    }

    public List<Map<String, Object>> tree(Long id, Map<String, String> selections) {
        BomContext context = context(id);
        List<Map<String, Object>> output = new ArrayList<>();
        for (BomItem item : context.childrenByParent
                .getOrDefault(context.header.getRootPartId(), Collections.emptyList())) {
            Map<String, Object> node = node(context, item, BigDecimal.ONE, 1, "",
                    selections,
                    new HashSet<>(Collections.singleton(context.header.getRootPartId())));
            if (node != null) {
                output.add(node);
            }
        }
        return output;
    }

    private void walk(
            BomContext context,
            Long parent,
            BigDecimal factor,
            int level,
            String path,
            List<Map<String, Object>> output,
            int maxLevel,
            Map<String, String> selections,
            Set<Long> pathVisited) {
        if (level >= maxLevel) {
            return;
        }
        for (BomItem item : context.childrenByParent
                .getOrDefault(parent, Collections.emptyList())) {
            if (!pathVisited.add(item.getChildPartId())) {
                continue;
            }
            Map<String, Object> value = node(context, item, factor, level + 1,
                    path, selections, pathVisited);
            if (value == null) {
                pathVisited.remove(item.getChildPartId());
                continue;
            }
            output.add(value);
            walk(context, item.getChildPartId(), factor.multiply(item.getQty()),
                    level + 1, String.valueOf(value.get("path")), output,
                    maxLevel, selections, pathVisited);
            pathVisited.remove(item.getChildPartId());
        }
    }

    private Map<String, Object> node(
            BomContext context,
            BomItem item,
            BigDecimal factor,
            int level,
            String path,
            Map<String, String> selections,
            Set<Long> pathVisited) {
        if (selections != null
                && !UsageConditionEvaluator.evaluate(item.getUsageCondition(),
                selections)) {
            return null;
        }
        Part part = context.partsById.get(item.getChildPartId());
        String itemPath = path.isEmpty()
                ? String.valueOf(item.getChildPartId())
                : path + "/" + item.getChildPartId();
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("level", level);
        value.put("path", itemPath);
        value.put("itemId", item.getId());
        value.put("parentPartId", item.getParentPartId());
        value.put("partId", item.getChildPartId());
        value.put("partNo", part == null ? null : part.getPartNo());
        value.put("partName", part == null ? null : part.getPartName());
        value.put("revision", part == null ? null : part.getRevision());
        value.put("partType", part == null ? null : part.getPartType());
        value.put("qty", item.getQty());
        value.put("uom", item.getUom());
        value.put("extendedQty", factor.multiply(item.getQty()));
        value.put("usageType", item.getUsageType());
        value.put("usageCondition", item.getUsageCondition());
        value.put("stationCode", item.getStationCode());
        value.put("alternateGroup", item.getAlternateGroup());
        value.put("effectiveFrom", item.getEffectiveFrom());
        value.put("effectiveTo", item.getEffectiveTo());
        value.put("findNo", item.getFindNo());
        List<Map<String, Object>> children = new ArrayList<>();
        for (BomItem child : context.childrenByParent
                .getOrDefault(item.getChildPartId(), Collections.emptyList())) {
            if (!pathVisited.add(child.getChildPartId())) {
                continue;
            }
            Map<String, Object> childNode = node(context, child,
                    factor.multiply(item.getQty()), level + 1, itemPath,
                    selections, pathVisited);
            if (childNode != null) {
                children.add(childNode);
            }
            pathVisited.remove(child.getChildPartId());
        }
        value.put("children", children);
        return value;
    }

    public List<Map<String, Object>> summarized(
            Long id,
            Map<String, String> selections) {
        Map<Long, Map<String, Object>> summarized = new LinkedHashMap<>();
        for (Map<String, Object> value : explode(id, null, selections)) {
            Long partId = (Long) value.get("partId");
            if (!summarized.containsKey(partId)) {
                summarized.put(partId, value);
            } else {
                BigDecimal quantity = (BigDecimal) summarized.get(partId)
                        .get("extendedQty");
                summarized.get(partId).put("extendedQty",
                        quantity.add((BigDecimal) value.get("extendedQty")));
            }
        }
        return new ArrayList<>(summarized.values());
    }

    public Map<String, List<Map<String, Object>>> byStation(Long id) {
        BomHeader header = load(id);
        if (!"MBOM".equals(header.getBomType())) {
            throw new BizException("仅 MBOM 支持按工位查看");
        }
        Map<String, String> stationNames = stations.selectList(new QueryWrapper<WorkStation>()
                        .eq("plant_id", header.getPlantId()))
                .stream()
                .collect(Collectors.toMap(WorkStation::getStationCode,
                        WorkStation::getStationName, (left, right) -> left));
            Map<String, List<Map<String, Object>>> grouped = new LinkedHashMap<>();
        BomContext context = context(id);
        for (BomItem item : context.items) {
            String stationCode = item.getStationCode() == null
                    ? "UNASSIGNED"
                    : item.getStationCode();
            Map<String, Object> row = node(context, item, BigDecimal.ONE, 1, "",
                    null,
                    new HashSet<>());
            grouped.computeIfAbsent(stationCode, key -> new ArrayList<>()).add(row);
        }
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : grouped.entrySet()) {
            String key = entry.getKey() + " - "
                    + stationNames.getOrDefault(entry.getKey(), "未分配工位");
            result.put(key, entry.getValue());
        }
        return result;
    }

    @Transactional
    public int importCsv(Long id, MultipartFile file) {
        BomHeader header = load(id);
        if (!"DRAFT".equals(header.getStatus())) {
            throw new BizException("仅 DRAFT BOM 可导入");
        }
        try {
            List<String[]> rows = Csv.read(file.getInputStream());
            int count = 0;
            for (int rowIndex = 1; rowIndex < rows.size(); rowIndex++) {
                String[] row = rows.get(rowIndex);
                if (row.length < 7) {
                    continue;
                }
                BomItem item = new BomItem();
                item.setParentPartId(findPartId(row[0]));
                item.setChildPartId(findPartId(row[1]));
                item.setFindNo(parseInteger(row[2]));
                item.setQty(new BigDecimal(row[3]));
                item.setUom(row[4]);
                item.setUsageCondition(emptyToNull(row[5]));
                item.setStationCode(emptyToNull(row[6]));
                add(id, item);
                count++;
            }
            return count;
        } catch (IOException | NumberFormatException exception) {
            throw new BizException("BOM CSV 导入失败: " + exception.getMessage());
        }
    }

    public ResponseEntity<byte[]> exportCsv(Long id) {
        List<BomItem> values = itemList(id);
        Set<Long> partIds = values.stream()
                .flatMap(item -> java.util.stream.Stream.of(
                        item.getParentPartId(), item.getChildPartId()))
                .collect(Collectors.toSet());
        Map<Long, Part> partMap = parts.selectBatchIds(partIds).stream()
                .collect(Collectors.toMap(Part::getId, part -> part));
        return Csv.download(
                "bom-items.csv",
                new String[]{"parentPartNo", "childPartNo", "findNo", "qty",
                        "uom", "usageCondition", "stationCode"},
                values,
                item -> new Object[]{
                        partMap.get(item.getParentPartId()) == null
                                ? item.getParentPartId()
                                : partMap.get(item.getParentPartId()).getPartNo(),
                        partMap.get(item.getChildPartId()) == null
                                ? item.getChildPartId()
                                : partMap.get(item.getChildPartId()).getPartNo(),
                        item.getFindNo(),
                        item.getQty(),
                        item.getUom(),
                        item.getUsageCondition(),
                        item.getStationCode()
                });
    }

    private Long findPartId(String partNo) {
        Part part = parts.selectOne(new QueryWrapper<Part>().eq("part_no", partNo));
        if (part == null) {
            throw new BizException("零件不存在: " + partNo);
        }
        return part.getId();
    }

    private Integer parseInteger(String value) {
        return value == null || value.isEmpty()
                ? null
                : Integer.valueOf(value);
    }

    private String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private BomContext context(Long id) {
        BomHeader header = load(id);
        List<BomItem> bomItems = itemList(id);
        Set<Long> partIds = new HashSet<>();
        partIds.add(header.getRootPartId());
        for (BomItem item : bomItems) {
            partIds.add(item.getParentPartId());
            partIds.add(item.getChildPartId());
        }
        Map<Long, Part> partMap = parts.selectBatchIds(partIds).stream()
                .collect(Collectors.toMap(Part::getId, part -> part));
        Map<Long, List<BomItem>> children = new LinkedHashMap<>();
        for (BomItem item : bomItems) {
            Long parentPartId = item.getParentPartId() == null
                    ? header.getRootPartId()
                    : item.getParentPartId();
            children.computeIfAbsent(parentPartId, key -> new ArrayList<>())
                    .add(item);
        }
        return new BomContext(header, bomItems, partMap, children);
    }

    private static final class BomContext {
        private final BomHeader header;
        private final List<BomItem> items;
        private final Map<Long, Part> partsById;
        private final Map<Long, List<BomItem>> childrenByParent;

        private BomContext(
                BomHeader header,
                List<BomItem> items,
                Map<Long, Part> partsById,
                Map<Long, List<BomItem>> childrenByParent) {
            this.header = header;
            this.items = items;
            this.partsById = partsById;
            this.childrenByParent = childrenByParent;
        }
    }
}
