package org.openrewrite.sandbox;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.JavaVisitor;
import org.openrewrite.java.MethodMatcher;
import org.openrewrite.java.tree.J;

public class GetToOrElseThrow extends Recipe {
    public static final MethodMatcher OPTIONAL_GET = new MethodMatcher("java.util.Optional get()");

    @Override
    public String getDisplayName() {
        return "Replace Optional.get with Optional::orElseThrow";
    }

    @Override
    public String getDescription() {
        return "Replace Optional.get with Optional::orElseThrow.";
    }

    @Override
    protected JavaVisitor<ExecutionContext> getVisitor() {
        return new JavaVisitor<ExecutionContext>() {
            final JavaTemplate template = JavaTemplate.builder(this::getCursor, "").build();
            public J vistMethodInnvocation(J.MethodInvocation method, ExecutionContext ctx){
                System.out.println("GETTING HERE");
                if (OPTIONAL_GET.matches(method)) {
                    // see what both .replaceMethod() and .replace() do
                    return method.withTemplate(template, method.getCoordinates().replaceMethod(), method.getSelect());
                }
                return super.visitMethodInvocation(method, ctx);
            }
        };
    }
}
