package com.epam.aidial.ql.common.deserializer;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;

import java.io.IOException;

public interface Deserializer<T> {
    T deserialize(TreeNode treeNode, ObjectCodec codec) throws IOException;
}
