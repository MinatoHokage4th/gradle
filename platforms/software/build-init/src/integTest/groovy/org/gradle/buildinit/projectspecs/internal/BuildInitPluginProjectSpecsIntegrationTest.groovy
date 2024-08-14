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

package org.gradle.buildinit.projectspecs.internal

import org.gradle.buildinit.plugins.AbstractInitIntegrationSpec
import org.gradle.buildinit.plugins.TestsInitProjectSpecsViaPlugin
import org.gradle.plugin.management.internal.autoapply.AutoAppliedPluginHandler
import org.gradle.test.fixtures.file.LeaksFileHandles
import spock.lang.Ignore

@Ignore // TODO: Fails on CI where D-G included build is not available, remove when depending upon published version and not included build
class BuildInitPluginProjectSpecsIntegrationTest extends AbstractInitIntegrationSpec implements TestsInitProjectSpecsViaPlugin {
    def "can specify 3rd party plugin using argument to init"() {
        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build file present"() {
        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root KTS build file present"() {
        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with settings file present"() {
        groovyFile("new-project/settings.gradle","""
            rootProject.name = "rootProject"
        """)

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with settings KTS file present"() {
        kotlinFile("new-project/settings.gradle.kts","""
            rootProject.name = "rootProject"
        """)

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build and settings files present"() {
        groovyFile("new-project/settings.gradle","""
            rootProject.name = "rootProject"
        """)

        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build and settings KTS files present"() {
        kotlinFile("new-project/settings.gradle.kts","""
            rootProject.name = "rootProject"
        """)

        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
    }

    def "can specify plugin using argument to init with root build and settings files present in multiproject build"() {
        groovyFile("new-project/settings.gradle", """
            rootProject.name = "rootProject"
            include("subproject")
        """)

        groovyFile("new-project/build.gradle", """
            plugins {
                id 'java-library'
            }
        """)

        groovyFile("new-project/subproject/build.gradle",
            """
                plugins {
                    id 'java-library'
                }
            """
        )

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
        // TODO: should appear exactly once, but no way to automatically verify this currently.  Looking at the output, it is true currently
    }

    def "can specify plugin using argument to init with root build and settings KTS files present in multiproject build"() {
        kotlinFile("new-project/settings.gradle.kts", """
            rootProject.name = "rootProject"
            include("subproject")
        """)

        kotlinFile("new-project/build.gradle.kts", """
            plugins {
                `java-library`
            }
        """)

        kotlinFile("new-project/subproject/build.gradle.kts",
            """
                plugins {
                    `java-library`
                }
            """
        )

        when:
        initSucceedsWithPluginSupplyingSpec("org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
        // TODO: should appear exactly once, but no way to automatically verify this currently.  Looking at the output, it is true currently
    }

    @LeaksFileHandles
    def "can specify custom plugin using argument to init"() {
        given:
        publishTestPlugin()

        when:
        initSucceedsWithPluginSupplyingSpec("org.example.myplugin:1.0")

        then:
        assertResolvedPlugin("org.example.myplugin", "1.0")
        outputDoesNotContain("MyPlugin applied.")
        assertLoadedSpec("Custom Project Type")
        assertLoadedSpec("Custom Project Type 2")

        // Note: If running in non-interactive mode, first template is automatically used
        assertProjectFileGenerated("project.output", "MyGenerator created this Custom Project Type project.")
        assertWrapperGenerated()
    }

    @LeaksFileHandles
    def "can specify multiple plugins using argument to init"() {
        given:
        publishTestPlugin()

        when:
        initSucceedsWithPluginSupplyingSpec("org.example.myplugin:1.0,org.barfuin.gradle.taskinfo:2.2.0")

        then:
        assertResolvedPlugin("org.example.myplugin", "1.0")
        assertResolvedPlugin("org.barfuin.gradle.taskinfo", "2.2.0")
        outputDoesNotContain("MyPlugin applied.")
        assertLoadedSpec("Custom Project Type")
        assertLoadedSpec("Custom Project Type 2")

        // Note: If running in non-interactive mode, first template is used
        assertProjectFileGenerated("project.output", "MyGenerator created this Custom Project Type project.")
        assertWrapperGenerated()
    }

    @LeaksFileHandles
    def "can specify declarative plugin using argument to init"() {
        when:
        initSucceedsWithPluginSupplyingSpec("org.gradle.experimental.jvm-ecosystem:0.1.10")

        then:
        assertResolvedPlugin("org.gradle.experimental.jvm-ecosystem", "0.1.10")
        assertLoadedSpec("Declarative Java Library Project")
        assertLoadedSpec("Declarative Java Application Project")

        // Note: If running in non-interactive mode, first template is used
        assertProjectFileGenerated("settings.gradle.dcl", """pluginManagement {
    repositories {
        google() // for Android plugin
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.experimental.jvm-ecosystem") version "0.1.10"
}
""")

        assertProjectFileGenerated("build.gradle.dcl", """javaLibrary {
    javaVersion = 21

    dependencies {
        implementation("com.google.guava:guava:32.1.3-jre")
    }
}
""")

        assertProjectFileGenerated("src/main/java/com/example/lib/Library.java", """package com.example.lib;

import com.google.common.collect.ImmutableList;

public class Library {
    public Iterable<String> getMessages() {
        // Verify that Guava is available
        ImmutableList.Builder<String> builder = ImmutableList.builder();
        builder.add("Hello from Java " + System.getProperty("java.version"));

        return builder.build();
    }
}
""")
        assertWrapperGenerated()
    }

    private void initSucceedsWithPluginSupplyingSpec(String pluginsProp = null) {
        targetDir = file("new-project").with { createDir() }

        def args = ["init"]
        if (pluginsProp) {
            args << "-D${AutoAppliedPluginHandler.INIT_PROJECT_SPEC_SUPPLIERS_PROP}=$pluginsProp".toString()
        }
        args << "--overwrite"
        args << "--info"
        args << "--init-script" << "../init.gradle"

        println "Executing: '${args.join(" ")}')"
        println "Working Dir: '$targetDir'"

        executer.noDeprecationChecks() // TODO: We don't care about these here, they are from the declarative-prototype build, remove when depending upon published version and not included build
        succeeds args
    }

    @Override
    String subprojectName() {
        return "new-project"
    }
}
