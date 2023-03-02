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

import com.rickbusarow.docusync.internal.SEMVER_REGEX
import com.rickbusarow.docusync.psi.SampleRequest
import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Transformer
import org.gradle.api.file.ConfigurableFileCollection
import org.intellij.lang.annotations.Language

/** */
abstract class DocsSet : Named, java.io.Serializable {

  /** */
  abstract val docs: ConfigurableFileCollection

  /** */
  abstract val sampleCode: ConfigurableFileCollection

  /** */
  abstract val replacers: NamedDomainObjectContainer<ReplacerBuilderScope>

  /**
   * Adds a set of document paths to this source set. The given paths are evaluated as per [Project.files].
   *
   * @param paths The files to add.
   * @return this
   */
  fun docs(vararg paths: Any) {
    docs.from(*paths)
  }

  /** */
  fun replacer(
    name: String,
    action: Action<ReplacerBuilderScope>
  ): NamedDomainObjectProvider<ReplacerBuilderScope> {
    return replacers.register(name) {
      // it.sampleRequests.set(emptyList())
      action.execute(it)
    }
  }

  /** */
  fun replacer(
    name: String,
    @Language("regexp") regex: String,
    replacement: String
  ): NamedDomainObjectProvider<ReplacerBuilderScope> {
    return replacers.register(name) {
      it.regex = regex
      it.replacement = replacement
    }
  }
}

/** */
abstract class ReplacerBuilderScope : Named, java.io.Serializable {

  @PublishedApi internal val sourceDelim = '​'

  val GROUP_ID: String
    get() = "([\\w\\.]+)"

  val ARTIFACT_ID: String
    get() = "([\\w\\-]+)"

  val PACKAGING: String
    get() = "(\\w+)"

  val SEMVER: String
    get() = "($SEMVER_REGEX)"

  /** */
  abstract var regex: String

  /** */
  abstract var replacement: String

  /** */
  private val _sampleRequests: MutableList<SampleRequest> = mutableListOf()
  val sampleRequests: List<SampleRequest>
    get() = _sampleRequests

  // var transform: Transformer<String, Pair<String, String>>? = null

  /** */
  fun sourceCode(
    request: SampleRequest,
    replacementBuilder: Transformer<String, String>
  ): String {

    _sampleRequests.add(request)

    return replacementBuilder.transform("$sourceDelim${request.hashCode()}$sourceDelim")
  }

  /** */
  // fun sourceCode(
  //   vararg requests: SampleRequest,
  //   action: Transformer<String, Pair<String, String>>
  // ) {
  //
  //   // transform = action
  //   // action.transform("a" to "b")
  // }

  /**
   * Builds a [Regex] to match a maven artifact as defined in a Gradle project.
   *
   * An example match:
   * `com.example.foo:foo-utils:1.2.3-SNAPSHOT`
   *
   * The resultant regex is wrapped in a capturing group, so `$1` would refer to the entire match.
   * This is done in case this pattern is just the component of a larger regex.
   *
   * @param group
   * @param artifactId
   * @param version
   * @return
   */
  fun maven(
    group: String = GROUP_ID,
    artifactId: String = ARTIFACT_ID,
    version: String = SEMVER
  ): String = "($group:$artifactId:$version)"
}
