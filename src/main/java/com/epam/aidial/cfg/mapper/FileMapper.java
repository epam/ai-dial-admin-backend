package com.epam.aidial.cfg.mapper;

import com.epam.aidial.cfg.dto.FileNodeInfoDto;
import com.epam.aidial.cfg.model.FileNodeInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FileMapper {

    FileNodeInfoDto toFilesInfoDto(FileNodeInfo filesInfo);

}
