package com.epam.aidial.cfg.domain.mapper;

import com.epam.aidial.cfg.domain.model.Route;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreUpstream;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.regex.Pattern;

@Mapper(
        componentModel = "spring",
        uses = {
                DeploymentCoreMapper.class,
        }
)
public abstract class RouteCoreMapper {

    @Mapping(target = "name", source = "deployment.name")
    @Mapping(target = "userRoles", source = "deployment")
    public abstract CoreRoute mapRoute(Route route);

    @Mapping(target = "deployment", source = "route")
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    public abstract Route mapRoute(CoreRoute route);

    @Mapping(target = "id", ignore = true)
    public abstract Upstream mapUpstream(CoreUpstream upstream);

    public abstract List<Pattern> mapPaths(List<String> paths);

    public Pattern mapPath(String path) {
        return Pattern.compile(path);
    }

    public String mapToString(Pattern path) {
        if (path == null) {
            return null;
        }
        return path.pattern();
    }

}
