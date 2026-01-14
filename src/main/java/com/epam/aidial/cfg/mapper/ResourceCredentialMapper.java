package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.model.ResourceSignInRequest;
import com.epam.aidial.cfg.model.ResourceSignOutRequest;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ResourceCredentialMapper {

    ResourceSignInRequest toResourceSignInRequest(ResourceSignInRequestDto dto);

    ResourceSignOutRequest toResourceSignOutRequest(ResourceSignOutRequestDto dto);
}