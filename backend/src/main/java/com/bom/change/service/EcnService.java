package com.bom.change.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.bom.bom.entity.BomHeader;
import com.bom.bom.entity.BomItem;
import com.bom.bom.mapper.BomHeaderMapper;
import com.bom.bom.mapper.BomItemMapper;
import com.bom.bom.service.BomService;
import com.bom.change.entity.Ecn;
import com.bom.change.entity.EcnItem;
import com.bom.change.mapper.EcnItemMapper;
import com.bom.change.mapper.EcnMapper;
import com.bom.common.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EcnService {

    private final EcnMapper ecns;
    private final EcnItemMapper ecnItems;
    private final BomHeaderMapper boms;
    private final BomItemMapper bomItems;
    private final BomService bomService;

    public List<Ecn> list() {
        return ecns.selectList(null);
    }

    public Ecn get(Long id) {
        return ecns.selectById(id);
    }

    public Ecn update(Long id, Ecn ecn) {
        ecn.setId(id);
        ecns.updateById(ecn);
        return ecns.selectById(id);
    }

    public EcnItem addItem(Long id, EcnItem item) {
        item.setId(null);
        item.setEcnId(id);
        item.setOldUsageCondition(trimToNull(item.getOldUsageCondition()));
        item.setUsageCondition(trimToNull(item.getUsageCondition()));
        item.setStationCode(trimToNull(item.getStationCode()));
        ecnItems.insert(item);
        return item;
    }

    public List<EcnItem> items(Long id) {
        return ecnItems.selectList(new QueryWrapper<EcnItem>()
                .eq("ecn_id", id));
    }

    public Ecn submit(Long id) {
        return transition(id, "DRAFT", "SUBMITTED");
    }

    public Ecn approve(Long id) {
        return transition(id, "SUBMITTED", "APPROVED");
    }

    @Transactional
    public Ecn implement(Long id) {
        Ecn ecn = require(id);
        if (!"APPROVED".equals(ecn.getStatus())) {
            throw new BizException("仅 APPROVED ECN 可实施");
        }
        if (ecn.getBomId() == null) {
            throw new BizException("ECN目标 BOM 不能为空");
        }

        BomHeader newBom = bomService.newVersion(ecn.getBomId());
        for (EcnItem change : items(id)) {
            applyChange(newBom.getId(), change);
        }
        bomService.transition(newBom.getId(), "DRAFT", "RELEASED");

        BomHeader oldBom = boms.selectById(ecn.getBomId());
        oldBom.setStatus("OBSOLETE");
        boms.updateById(oldBom);

        ecn.setImplementedBomId(newBom.getId());
        ecn.setStatus("IMPLEMENTED");
        ecns.updateById(ecn);
        return ecn;
    }

    int applyChange(Long bomId, EcnItem change) {
        List<BomItem> currentItems = bomService.itemList(bomId);
        if ("ADD".equals(change.getAction())) {
            BomItem item = new BomItem();
            item.setParentPartId(change.getParentPartId());
            item.setChildPartId(change.getNewChildPartId());
            item.setQty(change.getNewQty());
            item.setFindNo(change.getFindNo());
            item.setUsageCondition(change.getUsageCondition());
            item.setStationCode(change.getStationCode());
            bomService.add(bomId, item);
            return 1;
        }
        if (!"REPLACE".equals(change.getAction())
                && !"REMOVE".equals(change.getAction())
                && !"MODIFY".equals(change.getAction())) {
            throw new BizException("不支持的 ECN 动作: " + change.getAction());
        }
        List<BomItem> matches = currentItems.stream()
                .filter(item -> sameItem(item, change))
                .collect(Collectors.toList());
        if (matches.isEmpty()) {
            throw new BizException("ECN 明细未匹配到 BOM 行");
        }
        if (matches.size() > 1) {
            throw new BizException("匹配到多行，请指定序号/配置条件");
        }
        BomItem item = matches.get(0);
        if ("REPLACE".equals(change.getAction())) {
            item.setChildPartId(change.getNewChildPartId());
            if (change.getNewQty() != null) {
                item.setQty(change.getNewQty());
            }
            bomItems.updateById(item);
        } else if ("REMOVE".equals(change.getAction())) {
            bomItems.deleteById(item.getId());
        } else {
            LambdaUpdateWrapper<BomItem> update = new LambdaUpdateWrapper<BomItem>()
                    .eq(BomItem::getId, item.getId());
            boolean changed = false;
            if (change.getNewQty() != null) {
                update.set(BomItem::getQty, change.getNewQty());
                changed = true;
            }
            if (change.getFindNo() != null) {
                update.set(BomItem::getFindNo, change.getFindNo());
                changed = true;
            }
            if (Boolean.TRUE.equals(change.getClearUsageCondition())) {
                update.set(BomItem::getUsageCondition, null);
                changed = true;
            } else if (change.getUsageCondition() != null) {
                update.set(BomItem::getUsageCondition,
                        change.getUsageCondition());
                changed = true;
            }
            if (Boolean.TRUE.equals(change.getClearStationCode())) {
                update.set(BomItem::getStationCode, null);
                changed = true;
            } else if (change.getStationCode() != null) {
                update.set(BomItem::getStationCode, change.getStationCode());
                changed = true;
            }
            if (changed) {
                bomItems.update(null, update);
            }
        }
        return 1;
    }

    private boolean sameItem(BomItem item, EcnItem change) {
        boolean sameBase = Objects.equals(item.getParentPartId(),
                change.getParentPartId())
                && Objects.equals(item.getChildPartId(),
                change.getOldChildPartId());
        boolean sameFindNo = change.getFindNo() == null
                || Objects.equals(item.getFindNo(), change.getFindNo());
        return sameBase
                && sameFindNo
                && (change.getOldUsageCondition() == null
                || Objects.equals(item.getUsageCondition(),
                change.getOldUsageCondition()));
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private Ecn transition(Long id, String from, String to) {
        Ecn ecn = require(id);
        if (!from.equals(ecn.getStatus())) {
            throw new BizException("ECN状态不允许操作");
        }
        ecn.setStatus(to);
        ecns.updateById(ecn);
        return ecn;
    }

    private Ecn require(Long id) {
        Ecn ecn = ecns.selectById(id);
        if (ecn == null) {
            throw new BizException("ECN不存在");
        }
        return ecn;
    }
}
