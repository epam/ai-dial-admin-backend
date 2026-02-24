package com.epam.aidial.cfg.domain.model;

import java.util.HashSet;
import java.util.Set;

public enum ExportConfigComponentType {

    MODEL {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return switch (exportFormat) {
                case ADMIN -> Set.of(ADAPTER, INTERCEPTOR);
                case CORE -> Set.of(INTERCEPTOR);
            };
        }
    },

    APPLICATION {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of(INTERCEPTOR, APPLICATION_TYPE_SCHEMA);
        }
    },

    ROUTE {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    TOOL_SET {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    ROLE {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of(MODEL, APPLICATION, TOOL_SET, ROUTE);
        }
    },

    KEY {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of(ROLE);
        }
    },

    INTERCEPTOR {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return switch (exportFormat) {
                case ADMIN -> Set.of(INTERCEPTOR_RUNNER);
                case CORE -> Set.of();
            };
        }
    },

    INTERCEPTOR_RUNNER {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return switch (exportFormat) {
                case CORE -> false;
                case ADMIN -> true;
            };
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    APPLICATION_TYPE_SCHEMA {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return true;
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of(INTERCEPTOR);
        }
    },

    ADAPTER {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return switch (exportFormat) {
                case CORE -> false;
                case ADMIN -> true;
            };
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of();
        }
    },

    GLOBAL_INTERCEPTOR {
        @Override
        public boolean supports(ExportFormat exportFormat) {
            return switch (exportFormat) {
                case CORE -> false;
                case ADMIN -> true;
            };
        }

        @Override
        Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat) {
            return Set.of(INTERCEPTOR);
        }
    };

    public abstract boolean supports(ExportFormat exportFormat);

    abstract Set<ExportConfigComponentType> getDirectDependencies(ExportFormat exportFormat);

    public Set<ExportConfigComponentType> getAllDependencies(ExportFormat exportFormat) {
        if (!supports(exportFormat)) {
            throw new IllegalArgumentException("Export format: " + exportFormat + " is not supported for: " + this);
        }

        Set<ExportConfigComponentType> directDependencies = getDirectDependencies(exportFormat);
        Set<ExportConfigComponentType> result = new HashSet<>(directDependencies);

        for (var directDependency : directDependencies) {
            result.addAll(directDependency.getAllDependencies(exportFormat));
        }

        return result;
    }
}