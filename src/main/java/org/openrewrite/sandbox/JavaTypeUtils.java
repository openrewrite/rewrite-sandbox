package org.openrewrite.sandbox;

import org.openrewrite.SourceFile;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTypeVisitor;
import org.openrewrite.java.UnsafeJavaTypeVisitor;
import org.openrewrite.java.tree.JavaType;

import java.util.*;

class JavaTypeUtils {
    public static long weigh(SourceFile sourceFile) {
        Set<Integer> ids = new HashSet<>();
        return sourceFile.getWeight(id -> ids.add(System.identityHashCode(id)));
    }

    public static SourceFile cloneTypes(SourceFile sourceFile) {
        return cloneTypes(sourceFile, new IdentityHashMap<>());
    }

    public static SourceFile cloneTypes(SourceFile sourceFile, IdentityHashMap<JavaType, JavaType> clones) {
        buildClones(sourceFile, clones);
        return replaceWithClones(sourceFile, clones);
    }

    public static SourceFile dedupTypes(SourceFile s) {
        return (SourceFile) new JavaTypeDeduplicationVisitor(new JavaTypeVariants())
                .visitNonNull(s, 0);
    }

    private static void buildClones(SourceFile sourceFile, IdentityHashMap<JavaType, JavaType> cloned) {
        new JavaIsoVisitor<Integer>() {
            @Override
            public JavaType visitType(@Nullable JavaType javaType, Integer p) {
                return new JavaTypeVisitor<Integer>() {
                    @Override
                    public JavaType visit(@Nullable JavaType javaType, Integer integer) {
                        if (javaType == null) {
                            //noinspection ConstantConditions
                            return null;
                        }
                        JavaType clone = cloned.get(javaType);
                        if (clone != null) {
                            return clone;
                        }
                        clone = javaType;
                        if (javaType instanceof JavaType.Array) {
                            JavaType.Array t = (JavaType.Array) javaType;
                            clone = new JavaType.Array(null, t.getElemType());
                        } else if (javaType instanceof JavaType.Class) {
                            JavaType.Class t = (JavaType.Class) javaType;
                            clone = new JavaType.Class(null, t.getFlagsBitMap(), t.getFullyQualifiedName(),
                                    t.getKind(), t.getTypeParameters(), t.getSupertype(), t.getOwningClass(), t.getAnnotations(),
                                    t.getInterfaces(), t.getMembers(), t.getMethods());
                        } else if (javaType instanceof JavaType.GenericTypeVariable) {
                            JavaType.GenericTypeVariable t = (JavaType.GenericTypeVariable) javaType;
                            clone = new JavaType.GenericTypeVariable(null, t.getName(), t.getVariance(), t.getBounds());
                        } else if (javaType instanceof JavaType.Method) {
                            JavaType.Method t = (JavaType.Method) javaType;
                            clone = new JavaType.Method(null, t.getFlagsBitMap(), t.getDeclaringType(), t.getName(),
                                    t.getReturnType(), t.getParameterNames(), t.getParameterTypes(), t.getThrownExceptions(),
                                    t.getAnnotations());
                        } else if (javaType instanceof JavaType.MultiCatch) {
                            JavaType.MultiCatch t = (JavaType.MultiCatch) javaType;
                            clone = new JavaType.MultiCatch(t.getThrowableTypes());
                        } else if (javaType instanceof JavaType.Parameterized) {
                            JavaType.Parameterized t = (JavaType.Parameterized) javaType;
                            clone = new JavaType.Parameterized(null, t.getType(), t.getTypeParameters());
                        } else if (javaType instanceof JavaType.Variable) {
                            JavaType.Variable t = (JavaType.Variable) javaType;
                            clone = new JavaType.Variable(null, t.getFlagsBitMap(), t.getName(),
                                    t.getOwner(), t.getType(), t.getAnnotations());
                        }

                        cloned.put(javaType, clone);
                        return super.visit(javaType, p);
                    }
                }.visit(javaType, p);
            }
        }.visit(sourceFile, 0);
    }

    static SourceFile replaceWithClones(SourceFile sourceFile, Map<JavaType, JavaType> clones) {
        final Set<JavaType> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        return (SourceFile) new JavaIsoVisitor<Integer>() {
            @Override
            public JavaType visitType(@Nullable JavaType javaType, Integer p) {
                return new UnsafeJavaTypeVisitor<Integer>() {
                    @Override
                    public JavaType visit(@Nullable JavaType javaType, Integer p) {
                        JavaType replace = clones.getOrDefault(javaType, javaType);
                        if (seen.add(replace)) {
                            return super.visit(replace, p);
                        }
                        return replace;
                    }
                }.visit(javaType, p);
            }
        }.visitNonNull(sourceFile, 0);
    }
}
