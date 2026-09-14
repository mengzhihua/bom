package com.bom.change.service;

import com.bom.bom.service.BomService;
import com.bom.change.entity.EcnItem;
import org.junit.jupiter.api.Test;

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
}
