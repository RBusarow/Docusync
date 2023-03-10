/*
 * Copyright (C) 2023 Rick Busarow
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

package builds

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.DetektCreateBaselineTask
import io.gitlab.arturbosch.detekt.DetektGenerateConfigTask
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import io.gitlab.arturbosch.detekt.report.ReportMergeTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection
import org.gradle.language.base.plugins.LifecycleBasePlugin

abstract class DetektConventionPlugin : Plugin<Project> {
  override fun apply(target: Project) {

    target.plugins.applyOnce("io.gitlab.arturbosch.detekt")

    val detektExcludes = listOf(
      "**/resources/**",
      "**/build/**"
    )

    val reportMerge = target.tasks
      .register("reportMerge", ReportMergeTask::class.java) {
        it.output.set(target.rootProject.buildDir.resolve("reports/detekt/merged.sarif"))
      }

    target.dependencies.add("detektPlugins", target.libsCatalog.dependency("detekt-rules-libraries"))

    target.extensions.configure(DetektExtension::class.java) { extension ->

      extension.autoCorrect = false
      extension.config = target.files("${target.rootDir}/detekt/detekt-config.yml")
      extension.buildUponDefaultConfig = true

      extension.source = target.files(
        "src/main/java",
        "src/test/java",
        "src/main/kotlin",
        "src/test/kotlin"
      )

      extension.parallel = true
    }

    target.tasks.withType(Detekt::class.java) { task ->

      task.autoCorrect = false
      task.parallel = true
      task.config.from(target.files("${target.rootDir}/detekt/detekt-config.yml"))
      task.buildUponDefaultConfig = true

      // If in CI, merge sarif reports.  Skip this locally because it's not worth looking at
      // and the task is unnecessarily chatty.
      if (inCI()) {
        task.finalizedBy(reportMerge)
        reportMerge.configure {
          it.input.from(task.sarifReportFile)
        }
      }

      task.reports {
        it.xml.required.set(true)
        it.html.required.set(true)
        it.txt.required.set(false)
        it.sarif.required.set(true)
      }

      task.exclude(detektExcludes)
      target.subprojects.forEach { sub ->
        task.exclude("**/${sub.projectDir.relativeTo(target.rootDir)}/**")
      }

      // https://github.com/detekt/detekt/issues/4127
      task.exclude { "/build/generated/" in it.file.absolutePath }

      task.dependsOn(target.tasks.withType(BuildCodeGeneratorLogicTask::class.java))
      task.dependsOn(target.tasks.matchingName("generateMainProtos"))
    }

    target.tasks.register("detektAll", Detekt::class.java) {
      it.description = "runs the standard PSI Detekt as well as all type resolution tasks"
      it.dependsOn(target.otherDetektTasks(it, withAutoCorrect = false))
    }

    // Make all tasks from Detekt part of the 'detekt' task group.  Default is 'verification'.
    sequenceOf(
      Detekt::class.java,
      DetektCreateBaselineTask::class.java,
      DetektGenerateConfigTask::class.java
    ).forEach { type ->
      target.tasks.withType(type) { it.group = "detekt" }
    }

    // By default, `check` only handles the PSI Detekt task.  This adds the type resolution tasks.
    target.tasks.named(LifecycleBasePlugin.CHECK_TASK_NAME) {
      it.dependsOn(target.otherDetektTasks(it, withAutoCorrect = false))
    }
  }

  private fun Project.otherDetektTasks(
    targetTask: Task,
    withAutoCorrect: Boolean
  ): TaskCollection<Detekt> {
    return tasks.withType(Detekt::class.java)
      .matching { it.autoCorrect == withAutoCorrect && it != targetTask }
  }
}
