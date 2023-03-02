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
import io.kotest.matchers.shouldBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.junit.jupiter.api.Test

class ReplacerYamlSerializationTest {

  val yaml = Yaml(
    configuration = YamlConfiguration(encodingIndentationSize = 2, singleLineStringStyle = Plain)
  )

  @Test
  fun `deserializes from yaml`() {

    //language=yaml
    val yamlString = """
      - name: some-replacer
        regex: (com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)
        replacement: foo
      - name: some-other-replacer
        regex: cat
        replacement: dog
    """.trimIndent()

    yaml.decodeFromString<List<Replacer>>(yamlString) shouldBe listOf(
      Replacer(
        name = "some-replacer",
        regex = "(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)",
        replacement = "foo"
      ),
      Replacer(
        name = "some-other-replacer",
        regex = "cat",
        replacement = "dog"
      )
    )
  }

  @Test
  fun `serializes to yaml`() {

    val replacers = listOf(
      Replacer(
        name = "some-replacer",
        regex = "(com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)",
        replacement = "foo"
      ),
      Replacer(
        name = "some-other-replacer",
        regex = "cat",
        replacement = "dog"
      )
    )

    //language=yaml
    yaml.encodeToString(replacers) shouldBe """
      - name: some-replacer
        regex: (com.rickbusarow.docusync:[^:]*?docusync[^:]*?:)
        replacement: foo
      - name: some-other-replacer
        regex: cat
        replacement: dog
    """.trimIndent()
  }
}
