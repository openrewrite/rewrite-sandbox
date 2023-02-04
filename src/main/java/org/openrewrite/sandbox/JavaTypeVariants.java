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
