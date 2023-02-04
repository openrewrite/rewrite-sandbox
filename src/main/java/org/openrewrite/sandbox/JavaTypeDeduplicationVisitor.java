/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.sandbox;

import lombok.RequiredArgsConstructor;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.UnsafeJavaTypeVisitor;
import org.openrewrite.java.marker.JavaSourceSet;
import org.openrewrite.java.tree.JavaType;
import org.openrewrite.marker.Marker;

import java.util.*;

public class JavaTypeDeduplicationVisitor extends JavaVisitor<Integer> {
    private final JavaTypeDeduplicationTypeVisitor typeVisitor;

    public JavaTypeDeduplicationVisitor(JavaTypeVariants multiRepositoryJavaTypeCache) {
        this.typeVisitor = new JavaTypeDeduplicationTypeVisitor(multiRepositoryJavaTypeCache);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <M extends Marker> M visitMarker(Marker marker, Integer p) {
        if (marker instanceof JavaSourceSet) {
            JavaSourceSet sourceSet = (JavaSourceSet) marker;
            List<JavaType.FullyQualified> classpath = ListUtils.map(sourceSet.getClasspath(), c ->
                    (JavaType.FullyQualified) typeVisitor.visit(c, 0));
            return (M) sourceSet.withClasspath(classpath);
        }

        return (M) marker;
    }

    @Nullable
    @Override
    public JavaType visitType(@Nullable JavaType javaType, Integer p) {
        return typeVisitor.visit(javaType, 0);
    }
}

@RequiredArgsConstructor
class JavaTypeDeduplicationTypeVisitor extends UnsafeJavaTypeVisitor<Integer> {
    private final Map<JavaType, JavaType> provenEquivalencies = new IdentityHashMap<>();
    private final Map<JavaType, JavaType> proposedEquivalencies = new IdentityHashMap<>();

    private final JavaTypeVariants multiRepoCache;

    @Override
    public JavaType visit(@Nullable JavaType javaType, Integer p) {
        if (javaType == null) {
            //noinspection ConstantConditions
            return null;
        }

        JavaType proven = provenEquivalencies.get(javaType);
        if (proven != null) {
            return proven;
        }

        JavaType proposed = proposedEquivalencies.get(javaType);
        if (proposed != null) {
            return proposed;
        }

        Set<JavaType> variants = multiRepoCache.variantsOf(javaType);
        for (JavaType variant : new ArrayList<>(variants)) {
            proposedEquivalencies.put(javaType, variant);
            boolean isSame = isEqual(javaType, variant);
            proposedEquivalencies.remove(javaType);
            if (isSame) {
                provenEquivalencies.put(javaType, variant);
                return variant;
            }
        }

        proposedEquivalencies.put(javaType, javaType);
        JavaType jt = super.visit(javaType, p);
        proposedEquivalencies.remove(javaType);

        variants.add(jt);
        provenEquivalencies.put(javaType, jt);
        return jt;
    }

    private boolean isEqual(JavaType jt, JavaType variant) {
        if (jt == variant) {
            return true;
        }

        if (!jt.getClass().isAssignableFrom(variant.getClass()) &&
                !variant.getClass().isAssignableFrom(jt.getClass())) {
            return false;
        }

        if (jt instanceof JavaType.Class) {
            JavaType.Class c = (JavaType.Class) jt;
            JavaType.Class varc = (JavaType.Class) variant;
            return c.getFlagsBitMap() == varc.getFlagsBitMap() &&
                    c.getFullyQualifiedName().equals(varc.getFullyQualifiedName()) &&
                    c.getKind() == varc.getKind() &&
                    isSame(c.getOwningClass(), varc.getOwningClass()) &&
                    isSame(c.getSupertype(), varc.getSupertype()) &&
                    isSame(c.getInterfaces(), varc.getInterfaces()) &&
                    isSame(c.getMethods(), varc.getMethods()) &&
                    isSame(c.getMembers(), varc.getMembers()) &&
                    isSame(c.getAnnotations(), varc.getAnnotations()) &&
                    isSame(c.getTypeParameters(), varc.getTypeParameters());
        } else if (jt instanceof JavaType.Parameterized) {
            JavaType.Parameterized p = (JavaType.Parameterized) jt;
            JavaType.Parameterized varp = (JavaType.Parameterized) variant;
            return isSame(p.getType(), varp.getType()) &&
                    isSame(p.getTypeParameters(), varp.getTypeParameters());
        } else if (jt instanceof JavaType.Array) {
            JavaType.Array a = (JavaType.Array) jt;
            JavaType.Array vara = (JavaType.Array) variant;
            return isSame(a.getElemType(), vara.getElemType());
        } else if (jt instanceof JavaType.GenericTypeVariable) {
            JavaType.GenericTypeVariable g = (JavaType.GenericTypeVariable) jt;
            JavaType.GenericTypeVariable varg = (JavaType.GenericTypeVariable) variant;
            return g.getName().equals(varg.getName()) &&
                    g.getVariance() == varg.getVariance() &&
                    isSame(g.getBounds(), varg.getBounds());
        } else if (jt instanceof JavaType.Method) {
            JavaType.Method m = (JavaType.Method) jt;
            JavaType.Method varm = (JavaType.Method) variant;
            return m.getName().equals(varm.getName()) &&
                    m.getFlagsBitMap() == varm.getFlagsBitMap() &&
                    isSame(m.getAnnotations(), varm.getAnnotations()) &&
                    isSame(m.getParameterTypes(), varm.getParameterTypes()) &&
                    isSame(m.getReturnType(), varm.getReturnType()) &&
                    isSame(m.getThrownExceptions(), varm.getThrownExceptions()) &&
                    isSame(m.getDeclaringType(), varm.getDeclaringType());
        } else if (jt instanceof JavaType.Variable) {
            JavaType.Variable v = (JavaType.Variable) jt;
            JavaType.Variable varv = (JavaType.Variable) variant;
            return v.getName().equals(varv.getName()) &&
                    isSame(v.getOwner(), varv.getOwner());
        } else if (jt instanceof JavaType.MultiCatch) {
            JavaType.MultiCatch m = (JavaType.MultiCatch) jt;
            JavaType.MultiCatch varm = (JavaType.MultiCatch) variant;
            return isSame(m.getThrowableTypes(), varm.getThrowableTypes());
        }

        return true;
    }

    private boolean isSame(@Nullable JavaType test, @Nullable JavaType variant) {
        return visit(test, 0) == variant;
    }

    private boolean isSame(@Nullable List<? extends JavaType> test, @Nullable List<? extends JavaType> variant) {
        if (test == null && variant == null) {
            return true;
        } else if (test == null || variant == null || test.size() != variant.size()) {
            return false;
        }
        for (int i = 0; i < test.size(); i++) {
            if (visit(test.get(i), 0) != variant.get(i)) {
                return false;
            }
        }
        return true;
    }
}
