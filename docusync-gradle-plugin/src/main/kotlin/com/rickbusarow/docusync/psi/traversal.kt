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
import java.util.LinkedList

/**
 * @return a breadth-first traversal of all nested elements of type [T], including the nested elements of
 *     children, their children, etc.
 * @since 0.10.0
 */
inline fun <reified T : PsiElement> PsiElement.getChildrenOfTypeRecursive(): Sequence<T> {

  return generateSequence(sequenceOf(this)) { elements ->

    elements
      .flatMap { it.children.asSequence() }
      .takeIf<Sequence<PsiElement>> { it.iterator().hasNext() }
  }
    .flatten()
    .drop(1)
    .filterIsInstance<T>()
}

/**
 * @return a depth-first traversal of all nested elements of type [T], including the nested elements of
 *     children, their children, etc.
 * @since 0.10.0
 */
inline fun <reified T : PsiElement> PsiElement.getChildrenOfTypeRecursive(
  noinline predicate: (PsiElement) -> Boolean = { true }
): Sequence<T> {
  val toVisit = LinkedList(children.filter(predicate))

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    if (predicate(node)) {
      val filtered = node.children.filter(predicate)

      repeat(filtered.lastIndex + 1) {
        toVisit.addFirst(filtered[filtered.lastIndex - it])
      }

      toVisit.removeFirstOrNull()
    } else {
      null
    }
  }
    .filterIsInstance<T>()
}
