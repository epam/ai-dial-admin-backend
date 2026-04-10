package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import org.hibernate.envers.Audited;

@Data
@Audited
@Embeddable
public class ToolSetMcpRegistryEntity {

    @Column(name = "mcp_server_name")
    private String serverName;

    @Column(name = "mcp_server_version")
    private String serverVersion;
}
