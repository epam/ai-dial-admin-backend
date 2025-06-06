package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import lombok.Data;

import java.util.List;

@Data
public class CoreKey {
    private String key;
    private String project;
    private String role;
    private boolean secured;
    private List<String> roles;

    public String toString() {
        return "Key(key=" + SecretUtils.mask(this.getKey())
                + ", project=" + this.getProject()
                + ", role=" + this.getRole()
                + ", secured=" + this.isSecured()
                + ", roles=" + this.getRoles()
                + ")";
    }
}
