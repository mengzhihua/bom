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
            Set<String> emitted = new HashSet<>();
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
                    path.add(parentId);
                    path.add(item.getChildPartId());
                    List<BomItem> chain = new ArrayList<>();
                    chain.add(item);
                    queue.add(new State(
                            header,
                            path,
                            chain,
                            new HashSet<>(path)));
                }
            }
            while (!queue.isEmpty()) {
                State state = queue.remove();
                BomItem current = state.chain.get(0);
                Long parentId = current.getParentPartId() == null
                        ? state.header.getRootPartId()
                        : current.getParentPartId();
                List<BomItem> parents = new ArrayList<>();
                if (recursive) {
                    for (BomItem candidate : bomItems) {
                        if (java.util.Objects.equals(
                                candidate.getChildPartId(), parentId)) {
                            parents.add(candidate);
                        }
                    }
                }
                if (parents.isEmpty()) {
                    emitChain(out, state, partById, emitted);
                    continue;
                }
                for (BomItem parentItem : parents) {
                    Long prefix = parentItem.getParentPartId() == null
                            ? state.header.getRootPartId()
                            : parentItem.getParentPartId();
                    if (state.pathIds.contains(prefix)) {
                        emitChain(out, state, partById, emitted);
                        continue;
                    }
                    List<Long> path = new ArrayList<>(state.path);
                    path.add(0, prefix);
                    List<BomItem> chain = new ArrayList<>(state.chain);
                    chain.add(0, parentItem);
                    Set<Long> pathIds = new HashSet<>(state.pathIds);
                    pathIds.add(prefix);
                    queue.add(new State(state.header, path, chain, pathIds));
                }
            }
        }
        return R.ok(out);
    }

    private void emitChain(
            List<Map<String, Object>> out,
            State state,
            Map<Long, Part> partById,
            Set<String> emitted) {
        String path = formatPath(state.path, partById);
        Part root = partById.get(state.header.getRootPartId());
        for (int index = 0; index < state.chain.size(); index++) {
            BomItem item = state.chain.get(index);
            Long parentId = item.getParentPartId() == null
                    ? state.header.getRootPartId()
                    : item.getParentPartId();
            Part parent = partById.get(parentId);
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("bomNo", state.header.getBomNo());
            value.put("bomType", state.header.getBomType());
            value.put("parentPartId", item.getParentPartId());
            value.put("parentPartNo", parent == null ? null : parent.getPartNo());
            value.put("parentPartName", parent == null ? null : parent.getPartName());
            value.put("rootPartId", state.header.getRootPartId());
            value.put("rootPartNo", root == null ? null : root.getPartNo());
            value.put("qty", item.getQty());
            value.put("level", state.path.size() - 2 - index + 1);
            value.put("path", path);
            String key = item.getId() + ":" + path;
            if (emitted.add(key)) {
                out.add(value);
            }
        }
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
        private final BomHeader header;
        private final List<Long> path;
        private final List<BomItem> chain;
        private final Set<Long> pathIds;

        private State(
                BomHeader header,
                List<Long> path,
                List<BomItem> chain,
                Set<Long> pathIds) {
            this.header = header;
            this.path = path;
            this.chain = chain;
            this.pathIds = pathIds;
        }
    }
}
