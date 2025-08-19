package com.epam.aidial.cfg.dto.route;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RouteDto extends BaseRouteDto {
  private String displayName;
}
