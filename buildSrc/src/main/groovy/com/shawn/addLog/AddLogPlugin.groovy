package com.shawn.addLog

import org.gradle.api.Plugin
import org.gradle.api.Project;

public class AddLogPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        println "task 运行成功了"
    }
}
