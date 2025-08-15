package com.epam.aidial.cfg.domain.model.route;

import com.epam.aidial.cfg.domain.model.AttachmentPath;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DependentRoute extends BaseRoute {

    private Set<ResourceAccessType> permissions;
    private AttachmentPath attachmentPaths;

    public enum ResourceAccessType {
        READ,
        WRITE,
    }
}
