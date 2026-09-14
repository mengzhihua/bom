package com.bom.integration.service;

import com.bom.bom.entity.*;
import com.bom.bom.mapper.*;
import com.bom.integration.entity.IntegrationLog;
import com.bom.integration.mapper.IntegrationLogMapper;
import com.bom.master.entity.Part;
import com.bom.master.mapper.PartMapper;
import com.bom.common.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.concurrent.atomic.AtomicLong;

@Service
@RequiredArgsConstructor
public class SapIntegrationService {
    private final PartMapper parts;
    private final BomHeaderMapper boms;
    private final BomItemMapper items;
    private final IntegrationLogMapper logs;
    private final AtomicLong seq = new AtomicLong(0);
    public Part syncPart(Long id) {
        Part p = parts.selectById(id);
        if (p == null) throw new BizException("零件不存在");
        try {
            p.setSapMaterial(p.getSapMaterial() == null ? "1000" + String.format("%06d", seq.incrementAndGet()): p.getSapMaterial());
            parts.updateById(p);
            log("CREATE_MATERIAL", id, p.getPartNo(), "SUCCESS", null);
            return p;
        } catch (RuntimeException e) {
            log("CREATE_MATERIAL", id, p.getPartNo(), "FAILED", e.getMessage());
            throw e;
        }
    }
    public BomHeader syncBom(Long id) {
        BomHeader h = boms.selectById(id);
        if (h == null || !"RELEASED".equals(h.getStatus())) throw new BizException("仅 RELEASED BOM 可同步 SAP");
        h.setSapBomNo("CS" + String.format("%08d", seq.incrementAndGet()));
        boms.updateById(h);
        log("SEND_BOM", id, h.getBomNo(), "SUCCESS", h.getSapBomNo());
        return h;
    }
    private void log(String a, Long id, String code, String status, String response) {
        IntegrationLog l = new IntegrationLog();
        l.setDirection("OUT");
        l.setSystem("SAP");
        l.setAction(a);
        l.setBizType("BOM");
        l.setBizId(id);
        l.setBizCode(code);
        l.setStatus(status);
        l.setResponse(response);
        logs.insert(l);
    }
}
