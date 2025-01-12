package com.example.buildplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildPlugin: Plugin<Project> {
    override fun apply(project: Project) {
//        project.tasks.register("buildPlugin") {
//            group = "CustomTasks"
//            doLast {
//                println("Hello from the build plugin!")
//            }
//        }
//        throw Exception("meow")
        project.tasks.register("meow") {
            group = "test"
            doLast {
                println("meow")
            }
        }
    }
}