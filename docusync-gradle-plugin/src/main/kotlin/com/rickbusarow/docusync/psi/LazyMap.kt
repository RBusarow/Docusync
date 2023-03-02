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

/**
 * Creates a [LazyMap] which will invoke [compute] each time a *new* key is requested. Repeated
 * lookups will not invoke the [compute] action again.
 *
 * This is not a thread-safe map.
 *
 * usage:
 * ```
 * val cache = LazyMap<String, Data> { key ->
 *   // some expensive operation,
 *   // only performed when a key is requested for the first time
 *   someDataSource.get(key)
 * }
 * ```
 */
class LazyMap<K, V>(
  private val compute: LazyMap<K, V>.(K) -> V
) {

  private val map: MutableMap<K, Lazy<V>> = mutableMapOf()

  /**
   * size of the cached data
   */
  val size: Int get() = map.size

  /**
   * @return true if [key] is **already cached**
   */
  fun containsKey(key: K): Boolean = map.containsKey(key)

  /**
   * Tells the map to invoke [value] lazily the next time a value for [key] is requested. [value] will
   * not be invoked if [key] is not requested.
   *
   * @return the old [V] if one is already cached, else `null`
   */
  fun set(key: K, value: () -> V): V? = put(key, lazy(value))

  /**
   * Tells the map to invoke [value] lazily the next time a value for [key] is requested. [value] will
   * not be invoked if [key] is not requested.
   *
   * @return the old [V] if one is already cached, else `null`
   */
  fun set(key: K, value: Lazy<V>): V? = put(key, value)

  /**
   * Tells the map to invoke [value] lazily the next time a value for [key] is requested. [value] will
   * not be invoked if [key] is not requested.
   *
   * @return the old [V] if one is already cached, else `null`
   */
  fun put(key: K, value: Lazy<V>): V? = map.put(key, value)?.value

  /**
   * Tells the map to immediately store [value] for [key].
   *
   * @return the old [V] if one is already cached, else `null`
   */
  fun set(key: K, value: V): V? = put(key, value)

  /**
   * Tells the map to immediately store [value] for [key].
   *
   * @return the old [V] if one is already cached, else `null`
   */
  fun put(key: K, value: V): V? = map.put(key, lazy { value })?.value

  /**
   * Fetches the value for [key] or throws if the value cannot be computed.
   */
  operator fun get(key: K): V {

    val lazyValue = map.getOrPut(key) { lazy { compute(this@LazyMap, key) } }

    return lazyValue.value
  }

  companion object {

    /**
     * Creates a [LazyMap] which is essentially a lazy [Sequence.associate].
     *
     * When a key is requested which has not already been cached, the [LazyMap] will iterate over the
     * receiver [Sequence], caching each pair along the way. It will stop iterating as soon as it
     * arrives at the requested key. A subsequent request will check the cache and if there's a miss,
     * it will resume iterating at the point where it left off.
     *
     * Because the receiver is a [Sequence], the [Pair]s are not computed until the iterator reaches
     * them.
     *
     * This is not a thread-safe map.
     *
     * usage:
     * ```
     * someList
     *   .asSequence()
     *   // this map will be performed lazily while searching
     *   .map { someData -> someData to someData.otherValue() }
     *   .toLazyMap()
     * ```
     */
    fun <K, V> Sequence<Pair<K, V>>.toLazyMap(): LazyMap<K, V?> {
      val dataSource = stateful()

      return LazyMap compute@{ requestedKey ->
        dataSource.firstNotNullOfOrNull { (key, value) ->

          if (!containsKey(key)) {
            put(key, value)
          }
          value.takeIf { key == requestedKey }
        }
      }
    }
  }
}

/**
 * A Sequence which only yields each element once, but is **not** constrained to only one consumer via
 * [constrainOnce].
 *
 * Given this code:
 * ```
 * val seq = sequence {
 *   repeat(5) { num ->
 *     println("    yield $num")
 *     yield(num)
 *   }
 * }
 *   .stateful()
 *
 * repeat(3) {
 *   println(seq.firstOrNull { num -> num % 3 == 0 })
 * }
 * ```
 *
 * output:
 * ```text
 *      yield 0
 * 0
 *      yield 1
 *      yield 2
 *      yield 3
 * 3
 *      yield 4
 * null
 * ```
 */
fun <T> Sequence<T>.stateful(): Sequence<T> {
  val iterator = iterator()
  return Sequence { iterator }
}
