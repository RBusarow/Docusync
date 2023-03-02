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

package com.rickbusarow.docusync

import com.charleskorn.kaml.SingleLineStringStyle.Plain
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.rickbusarow.docusync.internal.existsOrNull
import com.rickbusarow.docusync.psi.SampleResult
import kotlinx.serialization.decodeFromString
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/** */
class ReplacersCache(
  private val samples: Map<Int, SampleResult>,
  globalReplacers: List<Replacer>
) : java.io.Serializable {

  private val globalReplacers = globalReplacers.associateBy { it.name }

  @delegate:Transient
  private val yaml: Yaml by lazy {
    Yaml(
      configuration = YamlConfiguration(encodingIndentationSize = 2, singleLineStringStyle = Plain)
    )
  }

  private val cache = ConcurrentHashMap<File, Lazy<Map<String, Replacer>>>()

  /**
   * Parses the file tree for all [Replacer]s defined in this directory and all parent directories.
   */
  fun get(file: File): Map<String, Replacer> {

    return if (file.isFile) {
      file.parentFile?.let { get(it) }.orEmpty()
    } else {
      cache.computeIfAbsent(file) {
        lazy {
          val here = file.resolve("docusync.yml")
            .existsOrNull()
            ?.readText()
            ?.let { yaml.decodeFromString<List<Replacer>>(it) }
            .orEmpty()
            .map {
              it.copy(
                replacement = it.replacement.replace("\u200B(-?\\d+)\u200B".toRegex()) { mr ->
                  samples.getValue(mr.groupValues[1].toInt()).content
                }
              )
            }
            .associateBy { it.name }

          val parentReplacers = file.parentFile?.let { get(it) }
            ?: globalReplacers

          parentReplacers + here
        }
      }.value
    }
  }
}
