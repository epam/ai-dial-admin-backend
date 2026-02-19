package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBucketDto {
    private String bucket;
    private String appdata;
}