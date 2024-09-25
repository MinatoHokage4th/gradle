/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.buildinit.projectspecs.internal;

import com.google.common.annotations.VisibleForTesting;
import org.gradle.buildinit.projectspecs.InitProjectGenerator;
import org.gradle.buildinit.projectspecs.InitProjectSpec;
import org.gradle.internal.service.scopes.Scope;
import org.gradle.internal.service.scopes.ServiceScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A registry of the available {@link InitProjectSpec}s that can be used to generate new projects via the {@code init} task.
 */
@ServiceScope(Scope.Project.class)
public final class InitProjectSpecRegistry {
    private final Map<Class<? extends InitProjectGenerator>, List<InitProjectSpec>> specsByGeneratorType = new HashMap<>();

    /**
     * Loads and adds mappings (from generator to specs that can be generated) to this registry.
     * <p>
     * This does not replace existing mappings for the same generator class, but appends to any that are already
     * present in the registry.  Attempting to register a spec for the same type with multiple generators will
     * produce an exception.
     *
     * @param loader source that will load mappings to add to this registry
     */
    public void register(InitProjectSpecLoader loader) {
        register(loader.loadProjectSpecs());
    }

    /**
     * Adds the given mappings (from generator to specs that can be generated) to this registry.
     * <p>
     * This does not replace existing mappings for the same generator class, but appends to any that are already
     * present in the registry.  Attempting to register a spec for the same type with multiple generators will
     * produce an exception.
     *
     * @param newSpecsByGeneratorType map from generator class to list of specs it can generate
     */
    @VisibleForTesting
    void register(Map<Class<? extends InitProjectGenerator>, List<InitProjectSpec>> newSpecsByGeneratorType) {
        newSpecsByGeneratorType.forEach((generator, newSpecs) -> {
            List<InitProjectSpec> currentSpecsForGenerator = specsByGeneratorType.computeIfAbsent(generator, k -> new ArrayList<>());
            newSpecs.forEach(newSpec -> {
                doGetGeneratorForSpec(newSpec).ifPresent(generatorType -> {
                    throw new IllegalStateException(String.format("Spec: '%s' with type: '%s' cannot use same type as another spec already registered!", newSpec.getDisplayName(), newSpec.getType()));
                });
                currentSpecsForGenerator.add(newSpec);
            });
        });
    }

    public List<InitProjectSpec> getAllSpecs() {
        return specsByGeneratorType.values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return specsByGeneratorType.isEmpty();
    }

    /**
     * Returns the {@link InitProjectSpec} in the registry with the given type; throwing
     * an exception if there is not exactly one such spec.
     *
     * @param type the type of the project spec to find
     * @return the project spec with the given type
     */
    public InitProjectSpec getSpecByType(String type) {
        List<InitProjectSpec> matchingSpecs = specsByGeneratorType.values().stream()
            .flatMap(List::stream)
            .filter(spec -> Objects.equals(spec.getType(), type))
            .collect(Collectors.toList());

        switch (matchingSpecs.size()) {
            case 0:
                throw new IllegalStateException("Project spec with type: '" + type + "' was not found!" + System.lineSeparator() +
                    "Known types:" + System.lineSeparator() +
                    getAllSpecs().stream()
                        .map(InitProjectSpec::getType)
                        .map(t -> " - " + t)
                        .collect(Collectors.joining(System.lineSeparator()))
                );
            case 1:
                return matchingSpecs.get(0);
            default:
                throw new IllegalStateException("Multiple project specs: " + matchingSpecs.stream().map(InitProjectSpec::getDisplayName).collect(Collectors.joining(", ")) + " with type: '" + type + "' were found!");
        }
    }

    /**
     * Returns the {@link InitProjectGenerator} type that can be used to generate a project
     * with the given {@link InitProjectSpec}.
     * <p>
     * This searches by project type.
     *
     * @param spec the project spec to find the generator for
     * @return the type of generator that can be used to generate a project with the given spec
     */
    public Class<? extends InitProjectGenerator> getGeneratorForSpec(InitProjectSpec spec) {
        return doGetGeneratorForSpec(spec).orElseThrow(() -> new IllegalStateException("Spec: '" + spec.getDisplayName() + "' with type: '" + spec.getType() + "' is not registered!"));
    }

    private Optional<Class<? extends InitProjectGenerator>> doGetGeneratorForSpec(InitProjectSpec spec) {
        return specsByGeneratorType.entrySet().stream()
            .filter(entry -> isSpecWithTypePresent(spec, entry.getValue()))
            .findFirst()
            .map(Map.Entry::getKey);
    }

    private boolean isSpecWithTypePresent(InitProjectSpec target, List<InitProjectSpec> toSearch) {
        return toSearch.stream().anyMatch(s -> isSameType(s, target));
    }

    private boolean isSameType(InitProjectSpec s1, InitProjectSpec s2) {
        return Objects.equals(s1.getType(), s2.getType());
    }
}
