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

@file:UseSerializers(RegexAsStringSerializer::class)

package com.rickbusarow.docusync

import com.rickbusarow.docusync.internal.RegexAsStringSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.intellij.lang.annotations.Language

/**
 * Models a single replacement action very much like the [Regex] version of [String.replace]
 *
 * @property name a unique identifier for this replacement.  It can be any arbitrary string.
 * @property regex supports normal Regex semantics including capturing groups like `(.*)`
 * @property replacement any combination of literal text and $-substitutions
 */
@Serializable
data class Rule(
  val name: String,
  val regex: Regex,
  val replacement: String
) : java.io.Serializable {

  constructor(
    name: String,
    @Language("RegExp")
    regex: String,
    replacement: String
  ) : this(name, regex.toRegex(), replacement)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is Rule) return false

    if (name != other.name) return false
    if (regex.pattern != other.regex.pattern) return false
    if (replacement != other.replacement) return false

    return true
  }

  override fun hashCode(): Int {
    var result = name.hashCode()
    result = 31 * result + regex.pattern.hashCode()
    result = 31 * result + replacement.hashCode()
    return result
  }
}
