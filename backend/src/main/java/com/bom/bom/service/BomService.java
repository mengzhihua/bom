package com.bom.bom.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.*;
import com.bom.bom.mapper.*;
import com.bom.common.*;
import com.bom.master.entity.Part;
import com.bom.master.mapper.PartMapper;
import com.bom.master.mapper.PartRevisionMapper;
import com.bom.master.entity.PartRevision;
import com.bom.process.mapper.WorkStationMapper;
import com.bom.process.entity.WorkStation;
import com.bom.system.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.*;
import java.time.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class BomService {
    private final BomHeaderMapper headers;
    private final BomItemMapper items;
    private final PartMapper parts;
    private final CodeGenerator codes;
    private final WorkStationMapper stations;
    public BomHeader load(Long id) {
        BomHeader h = headers.selectById(id);
        if (h == null) throw new BizException("BOM不存在");
        return h;
    }
    public List<BomItem> itemList(Long id) {
        return items.selectList(new QueryWrapper<BomItem>().eq("bom_id", id).orderByAsc("find_no", "id"));
    }
    @Transactional
public BomHeader create(BomHeader h) {
        h.setId(null);
        if (h.getBomNo() == null) h.setBomNo(codes.next("BOM").replace("-", ""));
        if (h.getVersion() == null) h.setVersion(1);
        if (h.getStatus() == null) h.setStatus("DRAFT");
        headers.insert(h);
        return h;
    }
    public BomItem add(Long id, BomItem x) {
        BomHeader h = load(id);
        if (!"DRAFT".equals(h.getStatus())) throw new BizException("RELEASED BOM 行不可修改");
        if (x.getParentPartId() == null) x.setParentPartId(h.getRootPartId());
        if (Objects.equals(x.getParentPartId(), x.getChildPartId())) throw new BizException("不允许成环");
        if (parts.selectById(x.getChildPartId()) == null) throw new BizException("子零件不存在");
        if (wouldCycle(id, x.getParentPartId(), x.getChildPartId())) throw new BizException("不允许成环");
        x.setId(null);
        x.setBomId(id);
        if (x.getFindNo() == null) x.setFindNo(itemList(id).stream().map(BomItem :: getFindNo).filter(Objects :: nonNull).max(Integer :: compareTo).orElse(0) + 10);
        if (x.getQty() == null) x.setQty(BigDecimal.ONE);
        items.insert(x);
        return x;
    }
    private boolean wouldCycle(Long id, Long parent, Long child) {
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (BomItem item : itemList(id)) {
            childrenByParent
                    .computeIfAbsent(item.getParentPartId(), key -> new ArrayList<>())
                    .add(item.getChildPartId());
        }
        Set<Long> seen = new HashSet <>();
        Deque<Long> q = new ArrayDeque <>();
        q.add(child);
        while (!q.isEmpty()) {
            Long p = q.remove();
            if (!seen.add(p)) continue;
            if (Objects.equals(p, parent)) return true;
            for (Long childId : childrenByParent.getOrDefault(p, Collections.emptyList())) {
                q.add(childId);
            }
        }
        return false;
    }
    public BomItem updateItem(Long id, Long itemId, BomItem x) {
        BomHeader h = load(id);
        if (!"DRAFT".equals(h.getStatus())) throw new BizException("BOM不可修改");
        x.setId(itemId);
        x.setBomId(id);
        items.updateById(x);
        return items.selectById(itemId);
    }
    public BomHeader transition(Long id, String from, String to) {
        BomHeader h = load(id);
        if (!from.equals(h.getStatus())) throw new BizException("当前状态不允许操作");
        if ("RELEASED".equals(to)) {
            List<Part> bad = new ArrayList <>();
            for (BomItem i: itemList(id)) {
                Part p = parts.selectById(i.getChildPartId());
                if (p == null || !"RELEASED".equals(p.getLifecycle())) bad.add(p);
            }
            if (!bad.isEmpty()) throw new BizException("存在未发布子零件");
            h.setReleasedBy(CurrentUser.get() == null ? null: CurrentUser.get().getUsername());
            h.setReleasedAt(LocalDateTime.now());
        }
        h.setStatus(to);
        headers.updateById(h);
        return h;
    }
    @Transactional
public BomHeader newVersion(Long id) {
        BomHeader old = load(id);
        BomHeader h = new BomHeader();
        org.springframework.beans.BeanUtils.copyProperties(old, h);
        h.setId(null);
        h.setBomNo(codes.next("BOM").replace("-", ""));
        h.setVersion(old.getVersion() + 1);
        h.setStatus("DRAFT");
        headers.insert(h);
        for (BomItem i: itemList(id)) {
            BomItem n = new BomItem();
            org.springframework.beans.BeanUtils.copyProperties(i, n);
            n.setId(null);
            n.setBomId(h.getId());
            items.insert(n);
        }
        return h;
    }
    public List<Map<String, Object>> explode(Long id, Integer maxLevel, Map<String, String> selections) {
        BomHeader h = load(id);
        List<Map<String, Object>> out = new ArrayList <>();
        walk(id, h.getRootPartId(), BigDecimal.ONE, 0, "", out, maxLevel == null ? 99: maxLevel, selections);
        return out;
    }
    private void walk(Long id, Long parent, BigDecimal factor, int level, String path, List<Map<String, Object>> out, int max, Map<String, String> s) {
        if (level>= max) return;
        for (BomItem i: itemList(id)) {
            if (!Objects.equals(i.getParentPartId(), parent) || !UsageConditionEvaluator.evaluate(i.getUsageCondition(), s)) continue;
            Part p = parts.selectById(i.getChildPartId());
            Map<String, Object> m = new LinkedHashMap <>();
            m.put("level", level + 1);
            m.put("path", (path.isEmpty() ? "": path + "/") + i.getChildPartId());
            m.put("partId", i.getChildPartId());
            m.put("partNo", p == null ? null: p.getPartNo());
            m.put("partName", p == null ? null: p.getPartName());
            m.put("qty", i.getQty());
            m.put("extendedQty", factor.multiply(i.getQty()));
            m.put("item", i);
            out.add(m);
            walk(id, i.getChildPartId(), factor.multiply(i.getQty()), level + 1, (path.isEmpty() ? "": path + "/") + i.getChildPartId(), out, max, s);
        }
    }
    public List<Map<String, Object>> summarized(Long id, Map<String, String> s) {
        Map<Long, Map<String, Object>> m = new LinkedHashMap <>();
        for (Map<String, Object> x: explode(id, null, s)) {
            Long p = (Long) x.get("partId");
            if (!m.containsKey(p)) m.put(p, x);
            else {
                BigDecimal q = (BigDecimal) m.get(p).get("extendedQty");
                m.get(p).put("extendedQty", q.add((BigDecimal) x.get("extendedQty")));
            }
        }
        return new ArrayList <>(m.values());
    }
}
