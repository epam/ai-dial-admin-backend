package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.PromptClient;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptMetadataIteratorTest {

    @Mock
    private PromptClient promptClient;

    @Test
    void testIteration_SinglePage_ReturnsAllItems() {
        String path = "test-path";
        boolean recursive = true;
        boolean permissions = true;
        int limit = 100;

        var item1 = new PromptMetadataDto();
        item1.setName("item1");
        var item2 = new PromptMetadataDto();
        item2.setName("item2");

        var response = new PromptMetadataDto();
        response.setItems(List.of(item1, item2));
        response.setNextToken(null);

        when(promptClient.getPromptsMetadata(path, recursive, null, limit, permissions)).thenReturn(response);

        var iterator = new PromptMetadataIterator(promptClient, path, recursive, limit, permissions);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(item1);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(item2);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void testIteration_MultiplePages_ReturnsAllItems() {
        String path = "test-path";
        boolean recursive = true;
        boolean permissions = true;
        int limit = 2;

        var item1 = new PromptMetadataDto();
        item1.setName("item1");
        var item2 = new PromptMetadataDto();
        item2.setName("item2");
        var item3 = new PromptMetadataDto();
        item3.setName("item3");

        var firstResponse = new PromptMetadataDto();
        firstResponse.setItems(List.of(item1, item2));
        firstResponse.setNextToken("next-token");
        var secondResponse = new PromptMetadataDto();
        secondResponse.setItems(List.of(item3));
        secondResponse.setNextToken(null);

        when(promptClient.getPromptsMetadata(path, recursive, null, limit, permissions)).thenReturn(firstResponse);
        when(promptClient.getPromptsMetadata(path, recursive, "next-token", limit, permissions)).thenReturn(secondResponse);

        var iterator = new PromptMetadataIterator(promptClient, path, recursive, limit, permissions);

        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(item1);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(item2);
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.next()).isEqualTo(item3);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void testHasNext_EmptyData_ReturnsFalse() {
        String path = "test-path";
        boolean recursive = true;
        boolean permissions = true;
        int limit = 100;

        var response = new PromptMetadataDto();
        response.setItems(List.of());
        response.setNextToken(null);

        when(promptClient.getPromptsMetadata(path, recursive, null, limit, permissions)).thenReturn(response);

        var iterator = new PromptMetadataIterator(promptClient, path, recursive, limit, permissions);

        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    void testNext_NoMoreElements_ThrowsException() {
        String path = "test-path";
        boolean recursive = true;
        boolean permissions = true;
        int limit = 100;

        var response = new PromptMetadataDto();
        response.setItems(List.of());
        response.setNextToken(null);

        when(promptClient.getPromptsMetadata(path, recursive, null, limit, permissions)).thenReturn(response);

        var iterator = new PromptMetadataIterator(promptClient, path, recursive, limit, permissions);

        assertThat(iterator.hasNext()).isFalse();
        assertThatThrownBy(iterator::next)
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("No more elements available");
    }

}
