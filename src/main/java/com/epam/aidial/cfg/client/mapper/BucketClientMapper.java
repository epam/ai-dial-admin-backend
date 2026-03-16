package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.UserBucketDto;
import com.epam.aidial.cfg.model.UserBucket;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BucketClientMapper {

    UserBucket toUserBucket(UserBucketDto bucketDto);

}