package org.openrewrite.sandbox;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.Optional;

public class GetToOrElseThrow extends Recipe {
    public static final MethodMatcher IMMUTABLE_LIST_MATCHER = new MethodMatcher("java.util.Optional get()");

    @Override
    public String getDisplayName() { return "Replace Optional.get with Optional::orElseThrow"; }

    @Override
    protected TreeVisitor<?, ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            @Override
            public J vistMethodInnvocation(J.MethodInvocation method, ExecutionContext executionContext){
                if (IMMUTABLE_LIST_MATCHER.matches(method)) {

                    
                }
            }
        }
    }
}
