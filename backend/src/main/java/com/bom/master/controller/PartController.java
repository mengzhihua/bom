package com.bom.master.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bom.common.R;
import com.bom.master.entity.Part;
import com.bom.master.entity.PartDocument;
import com.bom.master.entity.PartRevision;
import com.bom.master.mapper.PartMapper;
import com.bom.master.mapper.PartRevisionMapper;
import com.bom.master.service.PartService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

@RestController
@RequestMapping("/api/parts")
@RequiredArgsConstructor
public class PartController {

    private final PartMapper parts;
    private final PartRevisionMapper revisions;
    private final PartService service;

    @GetMapping
    public R<Page<Part>> page(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "20") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String partType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String lifecycle,
            @RequestParam(required = false) String makeBuy) {
        QueryWrapper<Part> query = new QueryWrapper<>();
        query.and(StringUtils.isNotBlank(keyword),
                wrapper -> wrapper.like("part_no", keyword)
                        .or()
                        .like("part_name", keyword))
                .eq(StringUtils.isNotBlank(partType), "part_type", partType)
                .eq(StringUtils.isNotBlank(category), "category", category)
                .eq(StringUtils.isNotBlank(lifecycle), "lifecycle", lifecycle)
                .eq(StringUtils.isNotBlank(makeBuy), "make_buy", makeBuy)
                .orderByDesc("id");
        return R.ok(parts.selectPage(new Page<>(current, Math.min(size, 200)), query));
    }

    @GetMapping("/{id}")
    public R<Part> get(@PathVariable Long id) {
        Part part = parts.selectById(id);
        if (part == null) {
            return R.ok(null);
        }
        List<Part> samePartNo = parts.selectList(new QueryWrapper<Part>()
                .eq("part_no", part.getPartNo()));
        Set<Long> partIds = new HashSet<>();
        for (Part value : samePartNo) {
            partIds.add(value.getId());
        }
        List<PartRevision> history = revisions.selectList(
                new QueryWrapper<PartRevision>()
                        .in(!partIds.isEmpty(), "part_id", partIds)
                        .orderByDesc("released_at"));
        Set<String> recorded = new HashSet<>();
        for (PartRevision revision : history) {
            recorded.add(revision.getPartId() + ":" + revision.getRevision());
        }
        for (Part value : samePartNo) {
            String key = value.getId() + ":" + value.getRevision();
            if (!recorded.contains(key)) {
                PartRevision revision = new PartRevision();
                revision.setPartId(value.getId());
                revision.setRevision(value.getRevision());
                history.add(revision);
            }
        }
        history.sort(Comparator.comparing(
                PartRevision::getReleasedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        part.setRevisions(history);
        return R.ok(part);
    }

    @PostMapping
    public R<Part> create(@RequestBody Part part) {
        return R.ok(service.create(part));
    }

    @PutMapping("/{id}")
    public R<Part> update(@PathVariable Long id, @RequestBody Part part) {
        return R.ok(service.update(id, part));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/submit")
    public R<Part> submit(@PathVariable Long id) {
        return R.ok(service.submit(id));
    }

    @PostMapping("/{id}/release")
    public R<Part> release(@PathVariable Long id) {
        return R.ok(service.release(id));
    }

    @PostMapping("/{id}/obsolete")
    public R<Part> obsolete(@PathVariable Long id) {
        return R.ok(service.obsolete(id));
    }

    @PostMapping("/{id}/revise")
    public R<Part> revise(@PathVariable Long id) {
        return R.ok(service.revise(id));
    }

    @PostMapping("/{id}/sync-sap")
    public R<Part> syncSap(@PathVariable Long id) {
        return R.ok(service.syncSap(id));
    }

    @GetMapping("/{id}/documents")
    public R<List<PartDocument>> documents(@PathVariable Long id) {
        return R.ok(service.documents(id));
    }

    @PostMapping("/{id}/documents")
    public R<PartDocument> addDocument(
            @PathVariable Long id,
            @RequestBody PartDocument document) {
        return R.ok(service.addDocument(id, document));
    }

    @DeleteMapping("/documents/{id}")
    public R<Void> deleteDocument(@PathVariable Long id) {
        service.deleteDocument(id);
        return R.ok();
    }

    @PostMapping("/import")
    public R<Integer> importCsv(@RequestParam("file") MultipartFile file)
            throws IOException {
        return R.ok(service.importCsv(file));
    }

    @GetMapping("/export")
    public void exportCsv(HttpServletResponse response) throws IOException {
        service.exportCsv(response);
    }
}
