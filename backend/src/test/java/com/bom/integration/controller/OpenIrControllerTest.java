package com.bom.integration.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
public class OpenIrControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void implementApprovedEcn() throws Exception {
        String snapshots = mockMvc.perform(get("/api/open/ir/snapshots")
                        .header("X-Api-Key", "bom-open-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.system").value("BOM"))
                .andReturn().getResponse().getContentAsString();
        JsonNode ecn = null;
        for (JsonNode row : objectMapper.readTree(snapshots).get("data").get("snapshots")) {
            if ("ECN".equals(row.path("dataType").asText())
                    && "ECN-DEMO-001".equals(row.path("bizKey").asText())) {
                ecn = row;
                break;
            }
        }
        assertNotNull(ecn, "应包含演示 ECN");
        org.junit.jupiter.api.Assertions.assertEquals("P001", ecn.path("plantCode").asText(),
                "ECN 快照应带出关联 BOM 工厂");

        if ("DRAFT".equals(ecn.path("status").asText())) {
            mockMvc.perform(post("/api/open/ir/actions")
                            .header("X-Api-Key", "bom-open-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"BOM_SUBMIT_ECN\",\"targetKey\":\"ECN-DEMO-001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
            mockMvc.perform(post("/api/open/ir/actions")
                            .header("X-Api-Key", "bom-open-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"BOM_APPROVE_ECN\",\"targetKey\":\"ECN-DEMO-001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        } else if ("SUBMITTED".equals(ecn.path("status").asText())) {
            mockMvc.perform(post("/api/open/ir/actions")
                            .header("X-Api-Key", "bom-open-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"type\":\"BOM_APPROVE_ECN\",\"targetKey\":\"ECN-DEMO-001\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0));
        }

        if (!"IMPLEMENTED".equals(ecn.path("status").asText())) {
            String impl = "{\"type\":\"BOM_IMPLEMENT_ECN\",\"targetKey\":\"ECN-DEMO-001\","
                    + "\"idempotencyKey\":\"BOM-IMPL-1\"}";
            mockMvc.perform(post("/api/open/ir/actions")
                            .header("X-Api-Key", "bom-open-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(impl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("IMPLEMENTED"));
            mockMvc.perform(post("/api/open/ir/actions")
                            .header("X-Api-Key", "bom-open-key")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(impl))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("IMPLEMENTED"));
        }

        String after = mockMvc.perform(get("/api/open/ir/snapshots")
                        .header("X-Api-Key", "bom-open-key"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode implemented = null;
        for (JsonNode row : objectMapper.readTree(after).get("data").get("snapshots")) {
            if ("ECN-DEMO-001".equals(row.path("bizKey").asText())) {
                implemented = row;
            }
        }
        assertNotNull(implemented);
        assertEquals("IMPLEMENTED", implemented.path("status").asText());
    }
}
