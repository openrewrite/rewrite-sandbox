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
package org.openrewrite.sandbox.table;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import lombok.Value;
import org.openrewrite.Column;
import org.openrewrite.DataTable;
import org.openrewrite.Recipe;

@JsonIgnoreType
public class TypeReport extends DataTable<TypeReport.Row> {

    public TypeReport(Recipe recipe) {
        super(recipe,
                "Java type report",
                "Contains a report about Java type weight.");
    }

    @Value
    public static class Row {
        @Column(displayName = "Source file",
                description = "A sourc file path.")
        String sourceFile;

        @Column(displayName = "Weight",
                description = "Existing weight.")
        Long weight;

        @Column(displayName = "Weight without private visibility",
                description = "Weight without private method names and fields.")
        Long weightWithoutPrivateMethodNamesAndFields;
    }
}
