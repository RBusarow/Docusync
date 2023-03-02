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

package com.rickbusarow.docusync.gradle

import com.rickbusarow.docusync.gradle.internal.dependsOn
import com.rickbusarow.docusync.gradle.internal.registerOnce
import com.rickbusarow.docusync.internal.capitalize
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import javax.inject.Inject

/** */
abstract class DocusyncExtension @Inject constructor(
  private val taskContainer: TaskContainer,
  private val objectFactory: ObjectFactory,
  private val layout: ProjectLayout
) : java.io.Serializable {

  /** */
  abstract val docsSets: NamedDomainObjectContainer<DocsSet>

  /** */
  fun docsSet(
    name: String = "main",
    action: Action<DocsSet>
  ): NamedDomainObjectProvider<DocsSet> {

    val parse = registerParseTask(name)

    val check = registerDocsTask(
      docSetName = name,
      autoCorrect = false,
      parseTask = parse
    )
    val fix = registerDocsTask(
      docSetName = name,
      autoCorrect = true,
      parseTask = parse
    )

    if (name != "main") {
      taskContainer.named("docusyncParse").dependsOn(parse)
      taskContainer.named("docusyncCheck").dependsOn(check)
      taskContainer.named("docusyncFix").dependsOn(fix)
    }

    return docsSets.register(name, action)
  }

  private fun registerParseTask(docSetName: String): TaskProvider<DocsSyncParseTask> {
    return taskContainer.registerOnce("docusyncParse", DocsSyncParseTask::class.java) { task ->

      task.sampleCode.from(
        docsSets.named(docSetName).map(DocsSet::sampleCode)
      )

      task.sampleRequests.addAll(
        docsSets.named(docSetName)
          .map { docsSet ->

            docsSet.replacers.flatMap { it.sampleRequests }
          }
      )

      task.samplesMapping.set(layout.buildDirectory.file("tmp/docusync/samples_$docSetName.json"))
    }
  }

  private fun registerDocsTask(
    docSetName: String,
    autoCorrect: Boolean,
    parseTask: TaskProvider<DocsSyncParseTask>
  ): TaskProvider<DocsSyncDocsTask> {

    val suffix = if (autoCorrect) "Fix" else "Check"

    val taskName = when (docSetName) {
      "main" -> "docusync$suffix"
      else -> "docusync${docSetName.capitalize()}$suffix"
    }

    return taskContainer.registerOnce(taskName, DocsSyncDocsTask::class.java) { task ->

      task.autoCorrect = autoCorrect

      task.group = "Docusync"
      task.description = if (autoCorrect) {
        "Automatically fixes any out-of-date documentation."
      } else {
        "Searches for any out-of-date documentation and fails if it finds any."
      }

      val docsSet = docsSets.getByName(docSetName)

      task.samplesMapping.set(parseTask.get().samplesMapping)

      task.docs.from(docsSet.docs)
      task.replacerBuilders.addAll(docsSet.replacers)

      task.outputs.files(docsSet.docs.files)
    }
  }
}
