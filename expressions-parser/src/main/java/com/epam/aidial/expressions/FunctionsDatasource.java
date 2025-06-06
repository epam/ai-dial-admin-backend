package com.epam.aidial.expressions;


import com.epam.aidial.datasource.definition.InterfaceMethodDef;

import java.util.List;
import java.util.Map;

public interface FunctionsDatasource {
    Map<String, List<InterfaceMethodDef>> getMethodsByName();
    Map<String, List<InterfaceMethodDef>> getAggregationMethodsByName();
    Map<String, List<InterfaceMethodDef>> getGroupMethodsByName();
    List<InterfaceMethodDef> getNegateMethods();
    List<InterfaceMethodDef> getAndMethods();
    List<InterfaceMethodDef> getOrMethods();
    List<InterfaceMethodDef> getNotMethods();
    List<InterfaceMethodDef> getIfMethods();
    List<InterfaceMethodDef> getPlusMethods();
    List<InterfaceMethodDef> getMinusMethods();
    List<InterfaceMethodDef> getMultiplyMethods();
    List<InterfaceMethodDef> getDivideMethods();
    List<InterfaceMethodDef> getModuloMethods();
    List<InterfaceMethodDef> getEqualsMethods();
    List<InterfaceMethodDef> getNotEqualsMethods();
    List<InterfaceMethodDef> getLessMethods();
    List<InterfaceMethodDef> getGreaterMethods();
    List<InterfaceMethodDef> getLessOrEqualsMethods();
    List<InterfaceMethodDef> getGreaterOrEqualsMethods();
}
