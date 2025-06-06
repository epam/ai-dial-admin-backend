package com.epam.aidial.cfg.service.prompt;

import com.epam.aidial.cfg.client.PromptClient;
import com.epam.aidial.cfg.client.dto.PromptMetadataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
class PromptMetadataIterator implements Iterator<PromptMetadataDto> {

    private final PromptClient promptClient;
    private final String path;
    private final boolean recursive;

    private List<PromptMetadataDto> items = Collections.emptyList();
    private String nextToken = null;
    private int currentIndex = 0;
    private boolean firstRequest = true;

    private void fetchNextData() {
        if (!firstRequest && nextToken == null) {
            log.debug("All prompt metadata has been fetched in iterator");
            items = Collections.emptyList();
            return;
        }
        firstRequest = false;

        var data = promptClient.getPromptsMetadata(path, recursive, nextToken);
        log.debug("New batch of prompt metadata is fetched in iterator: data={}", data);
        items = data.getItems() != null ? data.getItems() : Collections.emptyList();
        nextToken = data.getNextToken();
        currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        if (currentIndex >= items.size()) {
            fetchNextData();
        }
        return currentIndex < items.size();
    }

    @Override
    public PromptMetadataDto next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more elements available");
        }
        return items.get(currentIndex++);
    }
}
