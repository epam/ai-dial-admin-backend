package com.epam.aidial.cfg.domain.model;

import java.util.HashSet;
import java.util.Set;

public enum ExportConfigComponentType {

    MODEL {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            Set<ExportConfigComponentType> result = new HashSet<>(2);
            if (exportFormat == ExportFormat.ADMIN) {
                result.add(ADAPTER);
            }
            result.add(INTERCEPTOR);
            return result;
        }
    },

    APPLICATION {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of(INTERCEPTOR, APPLICATION_TYPE_SCHEMA);
        }
    },

    ROUTE {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    TOOL_SET {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    ROLE {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of(MODEL, APPLICATION, TOOL_SET, ROUTE, APPLICATION_TYPE_SCHEMA, INTERCEPTOR);
        }
    },

    KEY {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of(ROLE, MODEL, APPLICATION, APPLICATION_TYPE_SCHEMA, INTERCEPTOR);
        }
    },

    INTERCEPTOR {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            Set<ExportConfigComponentType> result = new HashSet<>(1);
            if (exportFormat == ExportFormat.ADMIN) {
                result.add(INTERCEPTOR_RUNNER);
            }
            return result;
        }
    },

    INTERCEPTOR_RUNNER {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    APPLICATION_TYPE_SCHEMA {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    ADAPTER {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    GLOBAL_INTERCEPTOR {
        @Override
        public Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat) {
            Set<ExportConfigComponentType> result = new HashSet<>(1);
            if (exportFormat == ExportFormat.ADMIN) {
                result.add(INTERCEPTOR_RUNNER);
            }
            return result;
        }
    };

    public abstract Set<ExportConfigComponentType> getDependencies(ExportFormat exportFormat);

}