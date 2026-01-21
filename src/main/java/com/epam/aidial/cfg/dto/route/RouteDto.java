package com.epam.aidial.cfg.dto.route;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.TreeSet;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RouteDto extends BaseRouteDto {
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    private TreeSet<String> topics;
}