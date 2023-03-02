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

package com.rickbusarow.docusync.psi

import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/**
 * prints a tree starting at any arbitrary psi element, showing all its children types and their text
 *
 * ex:
 * ```
 * ktFile.getKtProperty("foo").printEverything()
 * ```
 *
 * @since 0.10.0
 */
internal class EverythingPrinter : KtTreeVisitorVoid() {

  private val levels = mutableMapOf<PsiElement, Int>()
  private val dashes = "------------------------------------------------------------"

  private val parentNameMap = mutableMapOf<PsiElement, String>()

  override fun visitElement(element: PsiElement) {

    val thisName = element::class.java.simpleName // + element.extendedTypes()
    val parentName = element.parentName() ?: "----"

    val parentLevel = element.parent?.let { parent -> levels[parent] } ?: 0
    levels[element] = parentLevel + 1

    printNode(
      elementSimpleName = thisName,
      parentName = parentName,
      nodeText = element.text,
      level = parentLevel + 1
    )

    super.visitElement(element)
  }

  private fun printNode(
    elementSimpleName: String,
    parentName: String,
    nodeText: String,
    level: Int
  ) {
    println(
      """
      |   $dashes  $elementSimpleName    -- parent: $parentName
      |
      |   `$nodeText`
      """.trimMargin()
        .lines()
        .let {
          it.dropLast(1) + it.last().replaceFirst("  ", "└─")
        }
        .joinToString("\n")
        .prependIndent("│   ".repeat(level))
    )
  }

  private fun PsiElement.parentName() = parent?.let { parent ->

    parentNameMap.getOrPut(parent) {
      val typeCount = parentNameMap.keys.count { it::class == parent::class }

      val simpleName = parent::class.java.simpleName

      val start = if (typeCount == 0) {
        simpleName
      } else {
        "$simpleName (${typeCount + 1})"
      }

      start
    }
  }
}

internal fun PsiElement.printEverything() {
  accept(EverythingPrinter())
}
