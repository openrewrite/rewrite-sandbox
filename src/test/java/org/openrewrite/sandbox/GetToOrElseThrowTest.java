package org.openrewrite.sandbox;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

public class GetToOrElseThrowTest implements RewriteTest {
    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new GetToOrElseThrow());
    }

    @Test
    void optionalGet() {
        rewriteRun(
          //language=java
          java(
            """
              import java.util.Optional;
                              
              class Test {
                  void test() {
                      String value = Optional.of("").get();
                  }
              }
              """,
            """
              import java.util.NoSuchElementException;
              import java.util.Optional;
                              
              class Test {
                  void test() {
                      String value = Optional.of("").orElseThrow(NoSuchElementException::new);
                  }
              }
              """
          )
        );
    }
}
