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

package com.rickbusarow.docusync.markdown

import com.rickbusarow.docusync.Position
import com.rickbusarow.docusync.Position.Companion.positionOfSubstring
import com.rickbusarow.docusync.internal.RuleConfig

internal data class MarkdownGroup(
  val match: String,
  val openTagFull: String,
  val openTagStart: String,
  val openTagMatchersBlob: String,
  val openTagEnd: String,
  val body: String,
  val closeTag: String?,
  val afterCloseTag: String
) {
  val ruleConfigs: List<RuleConfig> by lazy(LazyThreadSafetyMode.NONE) {
    openTagMatchersBlob.split(',')
      .map { cfg ->

        val parts = cfg.split(':')

        val id = parts[0]
        val countSplit = parts.getOrNull(1)?.split('-')

        val minOrNull = countSplit?.firstOrNull()?.toInt()
        val maxOrNull = countSplit?.getOrNull(1)?.toInt() ?: minOrNull

        RuleConfig(
          name = id,
          minimumCount = minOrNull ?: 0,
          maximumCount = maxOrNull ?: Int.MAX_VALUE
        )
      }
  }

  fun position(
    leadingString: String,
    substring: String = match
  ): Position {

    val scopeString = leadingString + match

    return scopeString.positionOfSubstring(
      token = substring,
      startIndex = leadingString.lastIndex
    )
  }
}
