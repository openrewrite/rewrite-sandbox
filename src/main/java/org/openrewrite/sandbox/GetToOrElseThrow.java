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
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.search.UsesMethod;
import org.openrewrite.java.tree.J;

public class GetToOrElseThrow extends Recipe {
    public static final MethodMatcher OPTIONAL_GET = new MethodMatcher("java.util.Optional get()");

    @Override
    public String getDisplayName() {
        return "Replace `Optional::get` with `Optional::orElseThrow`";
    }

    @Override
    public String getDescription() {
        return "The latter explicitly handles the empty case.";
    }

    @Override
    protected TreeVisitor<?, ExecutionContext> getSingleSourceApplicableTest() {
        return new UsesMethod<ExecutionContext>(OPTIONAL_GET);
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            final JavaTemplate template = JavaTemplate
                    .builder(this::getCursor, "#{any(java.util.Optional)}.orElseThrow(NoSuchElementException::new)")
                    .imports("java.util.NoSuchElementException")
                    .build();

            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext ctx){
                if (OPTIONAL_GET.matches(method)) {
                    maybeAddImport("java.util.NoSuchElementException");
                    return method.withTemplate(template,
                            method.getCoordinates().replace(),
                            method.getSelect());
                }
                return super.visitMethodInvocation(method, ctx);
            }
        };
    }
}
