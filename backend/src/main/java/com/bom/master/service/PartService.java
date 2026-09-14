package com.bom.master.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bom.common.BizException;
import com.bom.common.CodeGenerator;
import com.bom.integration.service.SapIntegrationService;
import com.bom.master.entity.Part;
import com.bom.master.entity.PartDocument;
import com.bom.master.entity.PartRevision;
import com.bom.master.mapper.PartDocumentMapper;
import com.bom.master.mapper.PartMapper;
import com.bom.master.mapper.PartRevisionMapper;
import com.bom.system.auth.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PartService {

    private final PartMapper parts;
    private final PartRevisionMapper revisions;
    private final PartDocumentMapper documents;
    private final CodeGenerator codes;
    private final SapIntegrationService sap;

    public Part create(Part part) {
        part.setId(null);
        if (part.getPartNo() == null || part.getPartNo().trim().isEmpty()) {
            part.setPartNo(codes.next("P").replace("-", "").substring(0, 10));
        }
        if (part.getRevision() == null) {
            part.setRevision("A");
        }
        if (part.getLifecycle() == null) {
            part.setLifecycle("DRAFT");
        }
        parts.insert(part);
        return part;
    }

    public Part update(Long id, Part part) {
        part.setId(id);
        parts.updateById(part);
        return parts.selectById(id);
    }

    public void delete(Long id) {
        parts.deleteById(id);
    }

    public Part submit(Long id) {
        return transition(id, "DRAFT", "IN_REVIEW");
    }

    public Part release(Long id) {
        Part part = require(id);
        if (!"IN_REVIEW".equals(part.getLifecycle())) {
            throw new BizException("零件必须处于 IN_REVIEW");
        }
        part.setLifecycle("RELEASED");
        parts.updateById(part);

        PartRevision revision = new PartRevision();
        revision.setPartId(id);
        revision.setRevision(part.getRevision());
        revision.setReleasedBy(CurrentUser.get() == null
                ? null
                : CurrentUser.get().getUsername());
        revision.setReleasedAt(LocalDateTime.now());
        revisions.insert(revision);
        return part;
    }

    public Part obsolete(Long id) {
        return transition(id, "RELEASED", "OBSOLETE");
    }

    public Part revise(Long id) {
        Part old = require(id);
        if (!"RELEASED".equals(old.getLifecycle())) {
            throw new BizException("仅 RELEASED 可修订");
        }
        Part revised = new Part();
        BeanUtils.copyProperties(old, revised);
        revised.setId(null);
        revised.setRevision(nextRevision(old.getRevision()));
        revised.setLifecycle("DRAFT");
        parts.insert(revised);
        return revised;
    }

    public Part syncSap(Long id) {
        return sap.syncPart(id);
    }

    public List<PartDocument> documents(Long partId) {
        return documents.selectList(new QueryWrapper<PartDocument>()
                .eq("part_id", partId));
    }

    public PartDocument addDocument(Long partId, PartDocument document) {
        document.setId(null);
        document.setPartId(partId);
        documents.insert(document);
        return document;
    }

    public void deleteDocument(Long id) {
        documents.deleteById(id);
    }

    public int importCsv(MultipartFile file) throws IOException {
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                file.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (header) {
                    header = false;
                    continue;
                }
                String[] columns = line.split(",", -1);
                if (columns.length < 3) {
                    continue;
                }
                Part part = new Part();
                part.setPartNo(columns[0]);
                part.setRevision(columns[1]);
                part.setPartName(columns[2]);
                part.setLifecycle(columns.length > 3 ? columns[3] : "DRAFT");
                parts.insert(part);
                count++;
            }
        }
        return count;
    }

    public void exportCsv(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv;charset=UTF-8");
        response.getWriter().println("partNo,revision,partName,lifecycle");
        for (Part part : parts.selectList(null)) {
            response.getWriter().println(String.join(",",
                    safe(part.getPartNo()),
                    safe(part.getRevision()),
                    safe(part.getPartName()),
                    safe(part.getLifecycle())));
        }
    }

    private Part require(Long id) {
        Part part = parts.selectById(id);
        if (part == null) {
            throw new BizException("零件不存在");
        }
        return part;
    }

    private Part transition(Long id, String from, String to) {
        Part part = require(id);
        if (!from.equals(part.getLifecycle())) {
            throw new BizException("当前状态不允许操作");
        }
        part.setLifecycle(to);
        parts.updateById(part);
        return part;
    }

    private static String nextRevision(String revision) {
        if (revision == null || revision.isEmpty()) {
            return "A";
        }
        return String.valueOf((char) (revision.charAt(0) + 1));
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace(",", " ");
    }
}
