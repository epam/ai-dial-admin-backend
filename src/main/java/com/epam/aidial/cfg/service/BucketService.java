package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.BucketClient;
import com.epam.aidial.cfg.client.mapper.BucketClientMapper;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.UserBucket;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@LogExecution
@Slf4j
public class BucketService {
    private final BucketClient bucketClient;
    private final BucketClientMapper bucketClientMapper;

    public UserBucket getBucket() {
        return bucketClientMapper.toUserBucket(bucketClient.getBucket());
    }
}