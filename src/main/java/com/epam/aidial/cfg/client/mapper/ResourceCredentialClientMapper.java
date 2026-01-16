package com.epam.aidial.cfg.client.mapper;

import com.epam.aidial.cfg.client.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.client.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.model.ResourceSignInRequest;
import com.epam.aidial.cfg.model.ResourceSignOutRequest;
import lombok.extern.slf4j.Slf4j;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
@Slf4j
public abstract class ResourceCredentialClientMapper {

    public abstract ResourceSignInRequestDto toResourceSignInRequestDto(ResourceSignInRequest request);

    public abstract ResourceSignOutRequestDto toResourceSignOutRequestDto(ResourceSignOutRequest request);

}