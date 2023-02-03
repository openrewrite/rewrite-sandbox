package org.openrewrite.sandbox;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

import java.util.NoSuchElementException;
import java.util.Optional;

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
