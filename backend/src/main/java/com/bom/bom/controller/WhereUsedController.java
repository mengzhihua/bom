package com.bom.bom.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.bom.entity.*;
import com.bom.bom.mapper.*;
import com.bom.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class WhereUsedController {
    private final BomItemMapper items;
    private final BomHeaderMapper headers;
    @GetMapping("/api/parts/{partId}/where-used")
public R<List<Map<String, Object>>> where(
    @PathVariable Long partId,
    @RequestParam(defaultValue = "true") boolean recursive) {
        List<Map<String, Object>> out = new ArrayList <>();
        for (BomItem i: items.selectList(new QueryWrapper<BomItem>().eq("child_part_id", partId))) {
            BomHeader h = headers.selectById(i.getBomId());
            Map<String, Object> m = new LinkedHashMap <>();
            m.put("bomNo", h.getBomNo());
            m.put("bomType", h.getBomType());
            m.put("parentPartId", i.getParentPartId());
            m.put("rootPartId", h.getRootPartId());
            m.put("qty", i.getQty());
            m.put("level", 1);
            out.add(m);
        }
        return R.ok(out);
    }
}
