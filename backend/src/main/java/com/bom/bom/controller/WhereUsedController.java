package com.bom.bom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.entity.BomItem;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.bom.mapper.BomItemMapper;
import com.bom.common.R;
import com.bom.master.entity.Part;
import com.bom.master.mapper.PartMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

@RestController
@RequiredArgsConstructor
public class WhereUsedController {
    private final BomItemMapper items;
    private final BomHeaderMapper headers;
    private final PartMapper parts;

    @GetMapping("/api/parts/{partId}/where-used")
    public R<List<Map<String, Object>>> where(
            @PathVariable Long partId,
            @RequestParam(defaultValue = "true") boolean recursive) {
        List<Map<String, Object>> out = new ArrayList<>();
        Map<Long, BomHeader> headerById = new HashMap<>();
        for (BomHeader header : headers.selectList(null)) {
            headerById.put(header.getId(), header);
        }
        Map<Long, List<BomItem>> reverse = new HashMap<>();
        for (BomItem item : items.selectList(null)) {
            reverse.computeIfAbsent(item.getBomId(), key -> new ArrayList<>())
                    .add(item);
        }
        Map<Long, Part> partById = new HashMap<>();
        for (Part part : parts.selectList(null)) {
            partById.put(part.getId(), part);
        }
        for (List<BomItem> bomItems : reverse.values()) {
            Queue<State> queue = new ArrayDeque<>();
            Set<String> visited = new HashSet<>();
            for (BomItem item : bomItems) {
                if (java.util.Objects.equals(item.getChildPartId(), partId)) {
                    BomHeader header = headerById.get(item.getBomId());
                    if (header == null) {
                        continue;
                    }
                    Long parentId = item.getParentPartId() == null
                            ? header.getRootPartId()
                            : item.getParentPartId();
                    List<Long> path = new ArrayList<>();
                    path.add(header.getRootPartId());
                    if (!java.util.Objects.equals(parentId, header.getRootPartId())) {
                        path.add(parentId);
                    }
                    path.add(item.getChildPartId());
                    queue.add(new State(item, header, path, 1));
                }
            }
            while (!queue.isEmpty()) {
                State state = queue.remove();
                String visitKey = state.header.getId() + ":" + state.item.getId();
                if (!visited.add(visitKey)) {
                    continue;
                }
                Part parent = partById.get(state.item.getParentPartId() == null
                        ? state.header.getRootPartId()
                        : state.item.getParentPartId());
                Part root = partById.get(state.header.getRootPartId());
                Map<String, Object> value = new LinkedHashMap<>();
                value.put("bomNo", state.header.getBomNo());
                value.put("bomType", state.header.getBomType());
                value.put("parentPartId", state.item.getParentPartId());
                value.put("parentPartNo", parent == null ? null : parent.getPartNo());
                value.put("parentPartName", parent == null ? null : parent.getPartName());
                value.put("rootPartId", state.header.getRootPartId());
                value.put("rootPartNo", root == null ? null : root.getPartNo());
                value.put("qty", state.item.getQty());
                value.put("level", state.level);
                value.put("path", formatPath(state.path, partById));
                out.add(value);
                if (!recursive) {
                    continue;
                }
                Long parentId = state.item.getParentPartId() == null
                        ? state.header.getRootPartId()
                        : state.item.getParentPartId();
                for (BomItem parentItem : reverse.getOrDefault(
                        state.header.getId(), java.util.Collections.emptyList())) {
                    if (java.util.Objects.equals(parentItem.getChildPartId(), parentId)) {
                        List<Long> path = new ArrayList<>(state.path);
                        Long prefix = parentItem.getParentPartId() == null
                                ? state.header.getRootPartId()
                                : parentItem.getParentPartId();
                        if (path.isEmpty() || !java.util.Objects.equals(path.get(0), prefix)) {
                            path.add(0, prefix);
                        }
                        queue.add(new State(parentItem, state.header, path,
                                state.level + 1));
                    }
                }
            }
        }
        return R.ok(out);
    }

    private String formatPath(List<Long> path, Map<Long, Part> partById) {
        List<String> names = new ArrayList<>();
        for (Long id : path) {
            Part part = partById.get(id);
            names.add(part == null ? String.valueOf(id) : part.getPartNo());
        }
        return String.join(" > ", names);
    }

    private static final class State {
        private final BomItem item;
        private final BomHeader header;
        private final List<Long> path;
        private final int level;

        private State(
                BomItem item,
                BomHeader header,
                List<Long> path,
                int level) {
            this.item = item;
            this.header = header;
            this.path = path;
            this.level = level;
        }
    }
}
