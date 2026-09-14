package com.bom.change.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    private void applyChange(Long bomId, EcnItem change) {
        List<BomItem> currentItems = bomService.itemList(bomId);
        if ("REPLACE".equals(change.getAction())) {
            for (BomItem item : currentItems) {
                if (sameItem(item, change)) {
                    item.setChildPartId(change.getNewChildPartId());
                    if (change.getNewQty() != null) {
                        item.setQty(change.getNewQty());
                    }
                    bomItems.updateById(item);
                }
            }
        } else if ("REMOVE".equals(change.getAction())) {
            for (BomItem item : currentItems) {
                if (sameItem(item, change)) {
                    bomItems.deleteById(item.getId());
                }
            }
        } else if ("ADD".equals(change.getAction())) {
            BomItem item = new BomItem();
            item.setParentPartId(change.getParentPartId());
            item.setChildPartId(change.getNewChildPartId());
            item.setQty(change.getNewQty());
            item.setFindNo(change.getFindNo());
            item.setUsageCondition(change.getUsageCondition());
            item.setStationCode(change.getStationCode());
            bomService.add(bomId, item);
        } else if ("MODIFY".equals(change.getAction())) {
            for (BomItem item : currentItems) {
                if (sameItem(item, change)) {
                    item.setQty(change.getNewQty());
                    item.setUsageCondition(change.getUsageCondition());
                    item.setStationCode(change.getStationCode());
                    bomItems.updateById(item);
                }
            }
        }
    }

    private boolean sameItem(BomItem item, EcnItem change) {
        return Objects.equals(item.getParentPartId(), change.getParentPartId())
                && Objects.equals(item.getChildPartId(), change.getOldChildPartId());
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
