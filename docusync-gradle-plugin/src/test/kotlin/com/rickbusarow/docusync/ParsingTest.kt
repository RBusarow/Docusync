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

import com.rickbusarow.docusync.markdown.MarkdownNode
import com.rickbusarow.docusync.markdown.depthFirst
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.LinkedList

class ParsingTest {

  @Test
  fun `canary things`() {

    val src = """

      I wonder what `fun foo() = TODO()` looks like

      [//]: # (this is a comment?)

      This is some normal markdown text.
      This is after a single line break.

      This is after a double line break.

      <!--docusync cats-to-dogs,dogs-to-cats-->

      ```kotlin sample=com.example.butt

      // this is some code


      // this is a comment after a bunch of line breaks
      // this is a comment on the very next line

      ```

      <!---/docusync-->
    """.trimIndent()
    val parsedTree = MarkdownNode.from(src)

    parsedTree
      .depthFirst()
      // TODO <Rick> delete me
      .forEach {

        println(
          "#######################################  -- ${it.type}  --  ${it::class.java.canonicalName}  --   ${it.parent?.type}\n"
        )
        println(it.text)
        println("#######################################\n")
      }
  }

  @Test
  fun `canary 2`() {

    val root = Node(
      name = "root",
      children = listOf(
        Node(
          "a1",
          listOf(
            Node("a2", listOf())
          )
        ),
        Node(
          "b1",
          listOf(
            Node("b2", listOf())
          )
        )
      )
    )

    root.depthFirst().map { it.name }.toList() shouldBe listOf("a1", "a2", "b1", "b2")

    root.depthFirst { it.name.startsWith("b") }
      .map { it.name }
      .toList() shouldBe listOf("b1", "b2")
  }
}

data class Node(val name: String, val children: List<Node>)

fun Node.depthFirst(): Sequence<Node> {

  val toVisit = LinkedList(children)

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    repeat(node.children.lastIndex + 1) {
      toVisit.addFirst(node.children[node.children.lastIndex - it])
    }
    toVisit.removeFirstOrNull()
  }
}

fun Node.depthFirst(predicate: (Node) -> Boolean): Sequence<Node> {

  val toVisit = LinkedList<Node>(children.filter(predicate))

  return generateSequence(toVisit::removeFirstOrNull) { node ->

    if (predicate(node)) {
      repeat(node.children.lastIndex + 1) {
        toVisit.addFirst(node.children[node.children.lastIndex - it])
      }
      toVisit.removeFirstOrNull()
    } else {
      null
    }
  }
}
