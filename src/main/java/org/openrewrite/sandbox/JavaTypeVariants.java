package org.openrewrite.sandbox;

import org.openrewrite.java.tree.JavaType;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JavaTypeVariants {
    private final Map<String, Set<? extends JavaType>> variants;

    public JavaTypeVariants() {
        this(new ConcurrentHashMap<>());
    }

    public JavaTypeVariants(Map<String, Set<? extends JavaType>> variants) {
        this.variants = variants;
    }

    @SuppressWarnings("unchecked")
    public <T extends JavaType> Set<T> variantsOf(T t) {
        return (Set<T>) variants.computeIfAbsent(t.toString(), it -> Collections.synchronizedSet(
                Collections.newSetFromMap(new IdentityHashMap<>())));
    }
}
