package com.bom.change.service;

import com.bom.bom.service.BomService;
import com.bom.bom.entity.BomItem;
import com.bom.change.entity.EcnItem;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EcnServiceTest {
    @Test
    void unmatchedChangeFails() {
        BomService bomService = mock(BomService.class);
        when(bomService.itemList(10L)).thenReturn(Collections.emptyList());
        EcnService service = new EcnService(
                null,
                null,
                null,
                null,
                bomService);
        EcnItem change = new EcnItem();
        change.setAction("REMOVE");
        assertThrows(RuntimeException.class, () -> service.applyChange(10L, change));
    }

    @Test
    void unknownActionFails() {
        BomService bomService = mock(BomService.class);
        when(bomService.itemList(10L)).thenReturn(Collections.emptyList());
        EcnService service = new EcnService(
                null,
                null,
                null,
                null,
                bomService);
        EcnItem change = new EcnItem();
        change.setAction("UNKNOWN");
        assertThrows(RuntimeException.class, () -> service.applyChange(10L, change));
    }

    @Test
    void ambiguousChangeFails() {
        BomService bomService = mock(BomService.class);
        BomItem first = new BomItem();
        first.setId(1L);
        first.setParentPartId(2L);
        first.setChildPartId(3L);
        first.setUsageCondition(null);
        BomItem second = new BomItem();
        second.setId(2L);
        second.setParentPartId(2L);
        second.setChildPartId(3L);
        second.setUsageCondition(null);
        when(bomService.itemList(10L)).thenReturn(Arrays.asList(first, second));
        EcnService service = new EcnService(
                null,
                null,
                null,
                null,
                bomService);
        EcnItem change = new EcnItem();
        change.setAction("REMOVE");
        change.setParentPartId(2L);
        change.setOldChildPartId(3L);
        assertThrows(RuntimeException.class, () -> service.applyChange(10L, change));
    }
}
