package com.bom.bom;

import com.bom.bom.service.UsageConditionEvaluator;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UsageConditionEvaluatorTest {

    private final UsageConditionEvaluator evaluator = new UsageConditionEvaluator();

    @Test
    void emptyConditionMatches() {
        assertTrue(evaluator.matches("", selections()));
    }

    @Test
    void andExpressionMatches() {
        assertTrue(evaluator.matches(
                "ENGINE=1.5T & TRANS=AT",
                selections("ENGINE", "1.5T", "TRANS", "AT")));
    }

    @Test
    void orExpressionMatches() {
        assertTrue(evaluator.matches(
                "COLOR=RED | COLOR=BLK",
                selections("COLOR", "BLK")));
    }

    @Test
    void notEqualsExpressionMatches() {
        assertTrue(evaluator.matches(
                "ENGINE!=2.0T",
                selections("ENGINE", "1.5T")));
    }

    @Test
    void unknownFeatureDoesNotMatch() {
        assertFalse(evaluator.matches(
                "UNKNOWN=X",
                selections("ENGINE", "1.5T")));
    }

    @Test
    void missingSelectionDoesNotMatch() {
        assertFalse(evaluator.matches(
                "ENGINE=1.5T",
                selections("TRANS", "AT")));
    }

    private Map<String, String> selections(String... values) {
        Map<String, String> selections = new HashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            selections.put(values[i], values[i + 1]);
        }
        return selections;
    }
}
