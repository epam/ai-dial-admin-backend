package com.epam.aidial.cfg.domain.utils;

import com.epam.aidial.cfg.dao.model.ModelTypeEntity;
import com.epam.aidial.cfg.domain.model.Model;
import com.epam.aidial.cfg.domain.model.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ModelEndpointUtils {

    public String createEndpoint(Model model) {
        var adapter = model.getAdapter();
        if (adapter == null) {
            return null;
        }
        String baseEndpoint = adapter.getBaseEndpoint();
        String modelName = model.getDeployment().getName();
        return createEndpoint(baseEndpoint, modelName, model.getType());
    }

    public String createEndpoint(String baseEndpoint, String modelName, ModelType type) {
        String suffix = modelPath(modelName, type);
        return StringUtils.appendIfMissing(baseEndpoint, "/") + suffix;
    }

    public String createEndpoint(String baseEndpoint, String modelName, com.epam.aidial.core.config.ModelType type) {
        String suffix = modelPath(modelName, type);
        return baseEndpoint + suffix;
    }

    public String extractAdapterEndpoint(String modelEndpoint, String name, com.epam.aidial.core.config.ModelType type) {
        String modelPath = modelPath(name, type);
        return extractAdapterEndpoint(modelEndpoint, modelPath);
    }

    public String extractAdapterEndpoint(String modelEndpoint, String name, ModelType type) {
        String modelPath = modelPath(name, type);
        return extractAdapterEndpoint(modelEndpoint, modelPath);
    }

    public String extractAdapterEndpoint(String modelEndpoint, String name, ModelTypeEntity type) {
        String modelPath = modelPath(name, type);
        return extractAdapterEndpoint(modelEndpoint, modelPath);
    }

    private String extractAdapterEndpoint(String modelEndpoint, String modelPath) {
        if (!StringUtils.endsWith(modelEndpoint, modelPath)) {
            throw new IllegalArgumentException("Model endpoint " + modelEndpoint + " must ends with " + modelPath);
        }
        return StringUtils.removeEnd(modelEndpoint, modelPath);
    }

    private boolean isChat(ModelType type) {
        return type == ModelType.CHAT || type == null;
    }

    private boolean isChat(com.epam.aidial.core.config.ModelType type) {
        return type == com.epam.aidial.core.config.ModelType.CHAT || type == null;
    }

    private boolean isChat(ModelTypeEntity type) {
        return type == ModelTypeEntity.CHAT || type == null;
    }

    private String getEndpointByType(ModelType type) {
        return getEndpointByType(isChat(type));
    }

    private String getEndpointByType(com.epam.aidial.core.config.ModelType type) {
        return getEndpointByType(isChat(type));
    }

    private String getEndpointByType(ModelTypeEntity type) {
        return getEndpointByType(isChat(type));
    }

    private String getEndpointByType(boolean chat) {
        return chat
                ? "chat/completions"
                : "embeddings";
    }

    private String modelPath(String modelName, ModelType type) {
        return modelName + "/" + getEndpointByType(type);
    }

    private String modelPath(String modelName, com.epam.aidial.core.config.ModelType type) {
        return modelName + "/" + getEndpointByType(type);
    }

    private String modelPath(String modelName, ModelTypeEntity type) {
        return modelName + "/" + getEndpointByType(type);
    }
}
