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

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.SourceFile;
import org.openrewrite.internal.ListUtils;
import org.openrewrite.internal.lang.Nullable;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.tree.*;
import org.openrewrite.sandbox.table.TypeReport;

import java.util.*;

public class JavaTypeDensityStudy extends Recipe {
    transient TypeReport report = new TypeReport(this);

    @Override
    public String getDisplayName() {
        return "Studying type variance";
    }

    @Override
    public String getDescription() {
        return "Testing hypotheses around reducing type variance and weight.";
    }

    @Override
    protected List<SourceFile> visit(List<SourceFile> before, ExecutionContext ctx) {
        Set<JavaType> privatePackageMethodsInSource = Collections.newSetFromMap(new IdentityHashMap<>());

        for (SourceFile sourceFile : before) {
            new JavaIsoVisitor<Integer>() {
                @Override
                public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, Integer integer) {
                    JavaType.Method t = method.getMethodType();
                    if (t != null && !t.hasFlags(Flag.Public) && !t.hasFlags(Flag.Protected)) {
                        privatePackageMethodsInSource.add(t);
                    }
                    return super.visitMethodDeclaration(method, integer);
                }

                @Override
                public J.ClassDeclaration visitClassDeclaration(J.ClassDeclaration classDecl, Integer integer) {
                    J.ClassDeclaration c = super.visitClassDeclaration(classDecl, integer);
                    for (Statement stat : c.getBody().getStatements()) {
                        if (stat instanceof J.VariableDeclarations) {
                            for (J.VariableDeclarations.NamedVariable variable : ((J.VariableDeclarations) stat).getVariables()) {
                                JavaType.Variable t = variable.getVariableType();
                                if (t != null && !t.hasFlags(Flag.Public) && !t.hasFlags(Flag.Protected)) {
                                    privatePackageMethodsInSource.add(t);
                                }
                            }
                        }
                    }
                    return c;
                }
            }.visit(sourceFile, 0);
        }

        for (SourceFile cu : before) {
            if (!(cu instanceof JavaSourceFile)) {
                continue;
            }

            SourceFile s = JavaTypeUtils.dedupTypes(cu);

            IdentityHashMap<JavaType, JavaType> typeClones = new IdentityHashMap<>();
            SourceFile sCloned = JavaTypeUtils.cloneTypes(s, typeClones);

            IdentityHashMap<JavaType, JavaType> invertedTypeClones = new IdentityHashMap<>();
            for (Map.Entry<JavaType, JavaType> entry : typeClones.entrySet()) {
                invertedTypeClones.put(entry.getValue(), entry.getKey());
            }
            typeClones.clear();

            SourceFile noPrivateMethodsAndFields = (SourceFile) new JavaIsoVisitor<Integer>() {
                @Override
                public JavaType visitType(@Nullable JavaType javaType, Integer p) {
                    return new SingleLoopUnsafeJavaTypeVisitor() {
                        @Override
                        public JavaType visitClass(JavaType.Class aClass, Integer integer) {
                            return aClass.unsafeSet(
                                    ListUtils.map(aClass.getTypeParameters(), t -> visit(t, p)),
                                    (JavaType.FullyQualified) visit(aClass.getSupertype(), p),
                                    (JavaType.FullyQualified) visit(aClass.getOwningClass(), p),
                                    ListUtils.map(aClass.getAnnotations(), a -> (JavaType.FullyQualified) visit(a, p)),
                                    ListUtils.map(aClass.getInterfaces(), i -> (JavaType.FullyQualified) visit(i, p)),
                                    ListUtils.map(aClass.getMembers(), m -> {
                                        if (!privatePackageMethodsInSource.contains(invertedTypeClones.get(m)) &&
                                            !m.hasFlags(Flag.Public) && !m.hasFlags(Flag.Protected)) {
                                            return null;
                                        }
                                        return (JavaType.Variable) visit(m, p);
                                    }),
                                    ListUtils.map(aClass.getMethods(), m -> {
                                        if (!privatePackageMethodsInSource.contains(invertedTypeClones.get(m)) &&
                                            !m.hasFlags(Flag.Public) && !m.hasFlags(Flag.Protected)) {
                                            return null;
                                        }
                                        return (JavaType.Method) visit(m, p);
                                    })
                            );
                        }
                    }.visit(javaType, p);
                }
            }.visitNonNull(sCloned, 0);

            report.insertRow(ctx, new TypeReport.Row(
                    cu.getSourcePath().toString(),
                    JavaTypeUtils.weigh(s),
                    JavaTypeUtils.weigh(noPrivateMethodsAndFields)
            ));
        }

        return before;
    }
}
