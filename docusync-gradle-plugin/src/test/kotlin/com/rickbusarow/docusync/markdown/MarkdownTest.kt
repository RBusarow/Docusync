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

import com.rickbusarow.docusync.Replacer
import com.rickbusarow.docusync.internal.SEMVER_REGEX
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.matchers.shouldBe
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class MarkdownTest {

  val replacers = mapOf(
    "docusync-maven" to Replacer(
      name = "docusync-maven",
      regex = """(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)$SEMVER_REGEX""",
      replacement = "$11.2.3"
    ),
    "cats" to Replacer(
      name = "cats",
      regex = """cats""",
      replacement = "dogs"
    )
  )

  @Test
  fun `replacement works`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:1-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      <!--/docusync-->

      <!--docusync docusync-maven:1-->

      'com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT'

      <!--/docusync-->

      fin
      """
    )

    val new = original.markdown(
      absolutePath = "foo.md",
      replacers = replacers,
      autoCorrect = true
    )

    new shouldBe md(
      """
      # Title
      <!--docusync docusync-maven:1-->

      com.rickbusarow.docusync:docusync-cli:1.2.3

      <!--/docusync-->

      <!--docusync docusync-maven:1-->

      'com.rickbusarow.docusync:docusync-cli:1.2.3'

      <!--/docusync-->

      fin
      """
    )
  }

  @Test
  fun `replacement fails if autoCorrect is false`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:1-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      <!--/docusync-->

      <!--docusync docusync-maven:1-->

      'com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT'

      <!--/docusync-->

      fin
      """
    )

    shouldThrowWithMessage<IllegalStateException>(
      "Docusync - file://foo.md > text is out of date"
    ) {
      original.markdown(
        absolutePath = "foo.md",
        replacers = replacers,
        autoCorrect = false
      )
    }
  }

  @Test
  fun `a tag must be closed before opening another tag`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:1-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      <!--docusync docusync-maven:1-->

      'com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT'

      <!--/docusync-->

      fin
      """
    )

    shouldThrowWithMessage<IllegalStateException>(
      "Docusync - file://foo.md:1:0 > The tag '<!--docusync docusync-maven:1-->' " +
        "must be closed with `<!--/docusync-->` before the next docusync opening tag."
    ) {

      original.markdown(
        absolutePath = "foo.md",
        replacers = replacers,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a tag does not need to be closed if there are no more tags after it`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:1-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      fin
      """
    )

    shouldNotThrowAny {
      original.markdown(
        absolutePath = "foo.md",
        replacers = replacers,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a replacer with only one number must match that number of times`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:1-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT
      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      fin
      """
    )
    shouldThrowWithMessage<java.lang.IllegalStateException>(
      "The matcher 'docusync-maven' must find exactly 1 match, but it found 2."
    ) {
      original.markdown(
        absolutePath = "foo.md",
        replacers = replacers,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a replacer with a range cannot be above that range`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:1-2-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT
      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT
      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      fin
      """
    )
    shouldThrowWithMessage<java.lang.IllegalStateException>(
      "The matcher 'docusync-maven' must find a maximum of 2 matches, but it found 3."
    ) {
      original.markdown(
        absolutePath = "foo.md",
        replacers = replacers,
        autoCorrect = true
      )
    }
  }

  @Test
  fun `a replacer with a range cannot be below that range`() {

    val original = md(
      """
      # Title
      <!--docusync docusync-maven:2-3-->

      com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT

      fin
      """
    )
    shouldThrowWithMessage<java.lang.IllegalStateException>(
      "The matcher 'docusync-maven' must find a minimum of 2 matches, but it found 1."
    ) {
      original.markdown(
        absolutePath = "foo.md",
        replacers = replacers,
        autoCorrect = true
      )
    }
  }

  fun md(@Language("markdown") content: String): String = content.trimIndent()
}
