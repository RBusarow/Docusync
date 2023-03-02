## My File 2

<!---docusync cats-to-dogs,dogs-to-cats-->

cat

dogs

dogegory

api 'cat'

dog

<!---/docusync-->

<!---docusync code-->

```kotlin
abstract class DocsSyncTask : DefaultTask() {

  @delegate:Transient
  @get:Internal
  protected val json by lazy {
    Json {
      prettyPrint = true
      allowStructuredMapKeys = true
    }
  }
}
```

<!---/docusync-->
