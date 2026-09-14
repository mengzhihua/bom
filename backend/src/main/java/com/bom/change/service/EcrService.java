package com.bom.change.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.change.entity.Ecr;
import com.bom.change.entity.Ecn;
import com.bom.change.mapper.EcrMapper;
import com.bom.change.mapper.EcnMapper;
import com.bom.common.BizException;
import com.bom.common.CodeGenerator;
import com.bom.system.auth.CurrentUser;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EcrService {

    private final EcrMapper ecrs;
    private final EcnMapper ecns;
    private final CodeGenerator codes;

    public List<Ecr> list() {
        return ecrs.selectList(null);
    }

    public Ecr create(Ecr ecr) {
        ecr.setId(null);
        ecr.setEcrNo(codes.next("ECR").replace("-", ""));
        ecr.setStatus("DRAFT");
        ecr.setRequester(CurrentUser.get() == null
                ? null
                : CurrentUser.get().getUsername());
        ecrs.insert(ecr);
        return ecr;
    }

    public Ecr submit(Long id) {
        return transition(id, "DRAFT", "SUBMITTED");
    }

    public Ecr approve(Long id) {
        Ecr ecr = transition(id, "SUBMITTED", "APPROVED");
        ecr.setApprovedBy(CurrentUser.get() == null
                ? null
                : CurrentUser.get().getUsername());
        ecr.setApprovedAt(LocalDateTime.now());
        ecrs.updateById(ecr);
        return ecr;
    }

    public Ecr reject(Long id, String rejectReason) {
        Ecr ecr = transition(id, "SUBMITTED", "REJECTED");
        ecr.setRejectReason(rejectReason);
        ecrs.updateById(ecr);
        return ecr;
    }

    public Ecr close(Long id) {
        return transition(id, "APPROVED", "CLOSED");
    }

    public Ecn toEcn(Long id) {
        Ecr ecr = require(id);
        if (!"APPROVED".equals(ecr.getStatus())) {
            throw new BizException("仅 APPROVED ECR 可生成 ECN");
        }
        Ecn ecn = new Ecn();
        ecn.setEcrId(id);
        ecn.setEcnNo(codes.next("ECN").replace("-", ""));
        ecn.setTitle(ecr.getTitle());
        ecn.setStatus("DRAFT");
        ecn.setEffectiveType("IMMEDIATE");
        ecns.insert(ecn);
        return ecn;
    }

    private Ecr transition(Long id, String from, String to) {
        Ecr ecr = require(id);
        if (!from.equals(ecr.getStatus())) {
            throw new BizException("ECR状态不允许操作");
        }
        ecr.setStatus(to);
        ecrs.updateById(ecr);
        return ecr;
    }

    private Ecr require(Long id) {
        Ecr ecr = ecrs.selectById(id);
        if (ecr == null) {
            throw new BizException("ECR不存在");
        }
        return ecr;
    }
}
