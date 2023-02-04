package org.openrewrite.sandbox;

import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.UnsafeJavaTypeVisitor;
import org.openrewrite.java.tree.JavaType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

public class SingleLoopUnsafeJavaTypeVisitor extends UnsafeJavaTypeVisitor<Integer> {
    Set<JavaType> seen = Collections.newSetFromMap(new IdentityHashMap<>());

    @Override
    public JavaType visit(@Nullable JavaType javaType, Integer p) {
        if (javaType == null) {
            //noinspection ConstantConditions
            return null;
        }

        if (seen.add(javaType)) {
            return super.visit(javaType, p);
        }
        return javaType;
    }
}
