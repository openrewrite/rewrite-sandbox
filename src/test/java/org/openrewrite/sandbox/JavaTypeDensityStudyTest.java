package org.openrewrite.sandbox;

import org.junit.jupiter.api.Test;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.junit.jupiter.api.Assertions.fail;
import static org.openrewrite.java.Assertions.java;

public class JavaTypeDensityStudyTest implements RewriteTest {

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipe(new JavaTypeDensityStudy());
    }

    @Test
    void weights() {
        rewriteRun(
          spec -> spec.dataTableAsCsv("org.openrewrite.sandbox.table.TypeReport", """
            sourceFile,weight,"weightWithoutPrivateMethodNamesAndFields"
            "org/openrewrite/java/is.java",18137,6516
            """),
          //language=java
          java(
            """
              package org.openrewrite.java;
                         
              import java.lang.annotation.Retention;
              import java.lang.annotation.RetentionPolicy;
                         
              // Whenever this class is changed, make a corresponding change in JavaTypeGoat in the main resources folder.
              @AnnotationWithRuntimeRetention
              @AnnotationWithSourceRetention
              abstract class JavaTypeGoat<T, S extends PT<S> & C> {
                         
                  public static final PT<TypeA> parameterizedField = new PT<TypeA>() {
                  };
                         
                  public static abstract class InheritedJavaTypeGoat<T, U extends PT<U> & C> extends JavaTypeGoat<T, U> {
                      public InheritedJavaTypeGoat() {
                          super();
                      }
                  }
                         
                  public enum EnumTypeA {
                      FOO, BAR(),
                      @AnnotationWithRuntimeRetention
                      FUZ
                  }
                         
                  public enum EnumTypeB {
                      FOO(null);
                      private TypeA label;
                      EnumTypeB(TypeA label) {
                          this.label = label;
                      }
                  }
                         
                  public abstract class ExtendsJavaTypeGoat extends JavaTypeGoat<T, S> {
                  }
                         
                  public static abstract class Extension<U extends Extension<U>> {}
                         
                  public static class TypeA {}
                  public static class TypeB {}
                         
                  @AnnotationWithRuntimeRetention
                  @AnnotationWithSourceRetention
                  public abstract void clazz(C n);
                  public abstract void primitive(int n);
                  public abstract void array(C[][] n);
                  public abstract PT<C> parameterized(PT<C> n);
                  public abstract PT<PT<C>> parameterizedRecursive(PT<PT<C>> n);
                  public abstract PT<? extends C> generic(PT<? extends C> n);
                  public abstract PT<? super C> genericContravariant(PT<? super C> n);
                  public abstract <U extends JavaTypeGoat<U, ?>> JavaTypeGoat<? extends U[], ?> genericRecursive(JavaTypeGoat<? extends U[], ?> n);
                  public abstract <U> PT<U> genericUnbounded(PT<U> n);
                  public abstract void genericArray(PT<C>[] n);
                  public abstract void inner(C.Inner n);
                  public abstract void enumTypeA(EnumTypeA n);
                  public abstract void enumTypeB(EnumTypeB n);
                  public abstract <U extends PT<U> & C> InheritedJavaTypeGoat<T, U> inheritedJavaTypeGoat(InheritedJavaTypeGoat<T, U> n);
                  public abstract <U extends TypeA & PT<U> & C> U genericIntersection(U n);
                  public abstract T genericT(T n); // remove after signatures are common.
                  public abstract <U extends Extension<U> & Intersection<U>> void recursiveIntersection(U n);
              }
                         
              interface C {
                  class Inner {
                  }
              }
                         
              interface PT<T> {
              }
                         
              interface Intersection<T extends JavaTypeGoat.Extension<T> & Intersection<T>> {
                  T getIntersectionType();
              }
                         
              @Retention(RetentionPolicy.SOURCE)
              @interface AnnotationWithSourceRetention {}
                         
              @Retention(RetentionPolicy.RUNTIME)
              @interface AnnotationWithRuntimeRetention {}
              """
          )
        );
    }
}
