package com.epam.aidial.cfg.utils;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderUtilsTest {
    private static final String TEST_ETAG = "etag";

    @Test
    void testCreateHeadersForCreate_whenOverrideNotAllowed() {
        Map<String, String> headers = HeaderUtils.createHeadersForCreate(false, TEST_ETAG);
        assertThat(headers)
                .containsEntry(HeaderUtils.IF_NONE_MATCH_HEADER_NAME, "*")
                .hasSize(1);
    }

    @Test
    void testCreateHeadersForCreate_whenOverrideAllowed_andEtagProvided() {
        Map<String, String> headers = HeaderUtils.createHeadersForCreate(true, TEST_ETAG);
        assertThat(headers)
                .containsEntry(HeaderUtils.IF_MATCH_HEADER_NAME, TEST_ETAG)
                .hasSize(1);
    }

    @Test
    void testCreateHeadersForCreate_whenOverrideAllowed_andEtagNull() {
        Map<String, String> headers = HeaderUtils.createHeadersForCreate(true, null);
        assertThat(headers).isEmpty();
    }

    @Test
    void testCreateIfMatchHeaders_whenEtagProvided() {
        Map<String, String> headers = HeaderUtils.createIfMatchHeaders(TEST_ETAG);
        assertThat(headers)
                .containsEntry(HeaderUtils.IF_MATCH_HEADER_NAME, TEST_ETAG)
                .hasSize(1);
    }

    @Test
    void testCreateIfMatchHeaders_whenEtagNull() {
        Map<String, String> headers = HeaderUtils.createIfMatchHeaders(null);
        assertThat(headers).isEmpty();
    }
}