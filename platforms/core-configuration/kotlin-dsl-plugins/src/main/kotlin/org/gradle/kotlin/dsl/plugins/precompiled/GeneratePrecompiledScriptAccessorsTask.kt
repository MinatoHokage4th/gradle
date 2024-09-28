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

package org.gradle.kotlin.dsl.plugins.precompiled

import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.catalog.DefaultVersionCatalog
import org.gradle.api.internal.catalog.LibrariesSourceGenerator
import org.gradle.api.problems.Problems
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.support.uppercaseFirstChar
import java.nio.file.Files

@CacheableTask
abstract class GeneratePrecompiledScriptAccessorsTask : DefaultTask() {
    @get:Input
    abstract val versionCatalogs: SetProperty<VersionCatalog>

    @get:Input
    abstract val targetPackage: Property<String>

    @get:OutputDirectory
    abstract val outputSrcDirectory: DirectoryProperty

    @get:Input
    internal abstract val problems: Problems

    @TaskAction
    fun generateAccessors() {
        val output = outputSrcDirectory.get().asFile
        Files.createDirectories(output.toPath())
        versionCatalogs.get().forEach { catalog ->
            val className = "LibrariesFor${catalog.name.uppercaseFirstChar()}"
            output.resolve("$className.java").writer().use {
                LibrariesSourceGenerator.generateSource(
                    it,
                    catalog as DefaultVersionCatalog,
                    targetPackage.get(),
                    className,
                    problems
                )
            }
        }
    }
}
