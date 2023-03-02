[![Maven Central](https://img.shields.io/maven-central/v/com.rickbusarow.docusync/docusync-gradle-plugin?style=flat-square)](https://search.maven.org/search?q=com.rickbusarow.docusync)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.rickbusarow.docusync?style=flat-square)](https://plugins.gradle.org/plugin/com.rickbusarow.docusync)
[![Sonatype Nexus (Snapshots)](https://img.shields.io/nexus/s/com.rickbusarow.docusync/docusync-gradle-plugin?label=snapshots&server=https%3A%2F%2Foss.sonatype.org&style=flat-square)](https://oss.sonatype.org/#nexus-search;quick~com.rickbusarow.docusync)
[![License](https://img.shields.io/badge/license-apache2.0-blue?style=flat-square.svg)](https://opensource.org/licenses/Apache-2.0)

In markdown files, use the `<!---docusync ____-->` tags to tell Docusync where to find replacements.

```markdown
<!---docusync docusync-maven-->

classpath 'com.rickbusarow.docusync:docusync-gradle-plugin:0.0.1-SNAPSHOT'

<!---/docusync-->

<!---docusync docusync-plugin-->

classpath 'com.rickbusarow.docusync:docusync-gradle-plugin:0.0.1-SNAPSHOT'

<!---/docusync-->
```

### Getting Started

```kotlin
// settings.gradle.kts

pluginManagement {
  repositories {
    gradlePluginPortal()
    // Add for SNAPSHOT builds
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
  }
}
```

```kotlin
// build.gradle.kts

plugins {
  id("com.rickbusarow.docusync") version "0.0.1-SNAPSHOT"
}

// configure the plugin
docusync.sourceSet {
  // tell assign files to the sourceSet -- in this case, all markdown and MDX files.
  docs("**/*.md", "**/*.mdx")

  replacer("docusync-maven") {
    // ex: com.rickbusarow.docusync:docusync-cli:0.0.1-SNAPSHOT
    regex = """($GROUP:docusync-[^:]*?:)$SEMVER(["'].*)"""
    replacement = "$1$CURRENT_VERSION$2"
  }
}
```

### No Silent Failures

In truth,
