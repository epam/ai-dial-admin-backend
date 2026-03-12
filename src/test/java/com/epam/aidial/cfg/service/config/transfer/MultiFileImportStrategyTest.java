package com.epam.aidial.cfg.service.config.transfer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MultiFileImportStrategyTest {

    @Test
    void shouldHaveMergeJsonAndSequentialValues() {
        assertThat(MultiFileImportStrategy.values())
                .containsExactlyInAnyOrder(
                        MultiFileImportStrategy.MERGE_JSON,
                        MultiFileImportStrategy.SEQUENTIAL);
    }
}
