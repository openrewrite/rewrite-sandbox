package org.openrewrite.sandbox;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.Optional;

public class GetToOrElseThrow extends Recipe {
    @Override
    public String getDisplayName() { return "Replace Optional.get with Optional::orElseThrow"; }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        final MethodMatcher IMMUTABLE_LIST_MATCHER = new MethodMatcher("java.util.Optional get()");
        final JavaTemplate orElseThrowTemplate  =
                JavaTemplate.builder(this::getCursor,"Optional.of("").orElseThrow(NoSuchElementException::new)")
                        .build();
        return new JavaVisitor<ExecutionContext>() {
            public J visitMethodInvocation(J.MethodInvocation method, ExecutionContext executionContext){
                if (IMMUTABLE_LIST_MATCHER.matches(method)) {


                }
                return method;
            }
        };
    }
}
