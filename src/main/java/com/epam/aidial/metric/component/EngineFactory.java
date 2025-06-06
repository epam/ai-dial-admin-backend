package com.epam.aidial.metric.component;

import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import com.epam.aidial.ql.Engine;

public interface EngineFactory {

    boolean supports(DatasetDeclaration dataset);

    Engine createEngine(DatasetDeclaration dataset);

}
