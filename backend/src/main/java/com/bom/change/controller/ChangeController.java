package com.bom.change.controller;

import com.bom.change.entity.Ecr;
import com.bom.change.entity.Ecn;
import com.bom.change.entity.EcnItem;
import com.bom.change.service.EcnService;
import com.bom.change.service.EcrService;
import com.bom.common.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ChangeController {

    private final EcrService ecrService;
    private final EcnService ecnService;

    @GetMapping("/api/ecrs")
    public R<List<Ecr>> ecrList() {
        return R.ok(ecrService.list());
    }

    @PostMapping("/api/ecrs")
    public R<Ecr> createEcr(@RequestBody Ecr ecr) {
        return R.ok(ecrService.create(ecr));
    }

    @PostMapping("/api/ecrs/{id}/submit")
    public R<Ecr> submitEcr(@PathVariable Long id) {
        return R.ok(ecrService.submit(id));
    }

    @PostMapping("/api/ecrs/{id}/approve")
    public R<Ecr> approveEcr(@PathVariable Long id) {
        return R.ok(ecrService.approve(id));
    }

    @PostMapping("/api/ecrs/{id}/reject")
    public R<Ecr> rejectEcr(@PathVariable Long id) {
        return R.ok(ecrService.reject(id));
    }

    @PostMapping("/api/ecrs/{id}/close")
    public R<Ecr> closeEcr(@PathVariable Long id) {
        return R.ok(ecrService.close(id));
    }

    @PostMapping("/api/ecrs/{id}/to-ecn")
    public R<Ecn> toEcn(@PathVariable Long id) {
        return R.ok(ecrService.toEcn(id));
    }

    @GetMapping("/api/ecns")
    public R<List<Ecn>> ecnList() {
        return R.ok(ecnService.list());
    }

    @GetMapping("/api/ecns/{id}")
    public R<Ecn> getEcn(@PathVariable Long id) {
        return R.ok(ecnService.get(id));
    }

    @PutMapping("/api/ecns/{id}")
    public R<Ecn> updateEcn(@PathVariable Long id, @RequestBody Ecn ecn) {
        return R.ok(ecnService.update(id, ecn));
    }

    @PostMapping("/api/ecns/{id}/items")
    public R<EcnItem> addEcnItem(
            @PathVariable Long id,
            @RequestBody EcnItem item) {
        return R.ok(ecnService.addItem(id, item));
    }

    @GetMapping("/api/ecns/{id}/items")
    public R<List<EcnItem>> listEcnItems(@PathVariable Long id) {
        return R.ok(ecnService.items(id));
    }

    @PostMapping("/api/ecns/{id}/submit")
    public R<Ecn> submitEcn(@PathVariable Long id) {
        return R.ok(ecnService.submit(id));
    }

    @PostMapping("/api/ecns/{id}/approve")
    public R<Ecn> approveEcn(@PathVariable Long id) {
        return R.ok(ecnService.approve(id));
    }

    @PostMapping("/api/ecns/{id}/implement")
    public R<Ecn> implementEcn(@PathVariable Long id) {
        return R.ok(ecnService.implement(id));
    }
}
