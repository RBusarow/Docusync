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

package builds.ktlint.rules

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

fun ASTNode.isBlank() = text.isBlank()

private val copyRightCommentStart = Regex(
  """(?:\/\*{1,2}\s+(?:\*\s)?|\/\/ *)Copyright [\s\S]*"""
)

fun ASTNode.isCopyrightHeader(): Boolean {
  if (elementType != ElementType.BLOCK_COMMENT) return false

  return text.matches(copyRightCommentStart)
}
