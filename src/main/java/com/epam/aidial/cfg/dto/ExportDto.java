package com.epam.aidial.cfg.dto;


import com.epam.aidial.cfg.web.validation.PublicPath;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ExportDto {

    @NotNull
    private List<@PublicPath String> paths;

}
