package com.epam.aidial.cfg.client.dto;

/**
 * System-detected inference task category reported by the deployment manager for an inference
 * deployment. Deserialized case-insensitively from the lowercase wire values
 * ({@code text_classification} / {@code text_generation} / {@code none}).
 */
public enum InferenceTask {
    TEXT_CLASSIFICATION,
    TEXT_GENERATION,
    NONE
}
