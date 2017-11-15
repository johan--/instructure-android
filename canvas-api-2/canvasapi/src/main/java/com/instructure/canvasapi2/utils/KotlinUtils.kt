/*
 * Copyright (C) 2017 - present Instructure, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
@file:Suppress("unused")

package com.instructure.canvasapi2.utils

import java.util.*

/** Returns true if this string is non-null and non-blank, otherwise returns false */
@Suppress("NOTHING_TO_INLINE")
inline fun String?.isValid(): Boolean = !isNullOrBlank()

/** Returns this string if is it non-null and non-blank, otherwise returns null */
@Suppress("NOTHING_TO_INLINE")
inline fun String?.validOrNull(): String? = this?.takeIf { it.isValid() }

inline fun <T> List<T>.intersectBy(other: List<T>, unique: (T) -> Any): List<T> {
    val list = this.toMutableList()
    val iterator = list.iterator()
    while (iterator.hasNext()) {
        val compareValue = unique(iterator.next())
        if (other.indexOfFirst { compareValue == unique(it) } == -1) {
            iterator.remove()
        }
    }
    return list
}

/**
 * Randomly shuffles elements in this mutable list.
 */
fun <T> MutableList<T>.shuffle() = Collections.shuffle(this)

/**
 * Randomly shuffles elements in this mutable list using the specified [random] instance as the source of randomness.
 */
fun <T> MutableList<T>.shuffle(random: Random) = Collections.shuffle(this, random)

/**
 * Returns a new list with the elements of this list randomly shuffled.
 */
fun <T> Iterable<T>.shuffled(): List<T> = toMutableList().apply { shuffle() }

/**
 * Returns a new list with the elements of this list randomly shuffled
 * using the specified [random] instance as the source of randomness.
 */
fun <T> Iterable<T>.shuffled(random: Random): List<T> = toMutableList().apply { shuffle(random) }

fun <T> tryOrNull(block: () -> T?): T? {
    return try {
        block()
    } catch (ignore: Throwable) {
        null
    }
}

/** Creates a range surrounding this number */
fun Int.rangeWithin(scope: Int): IntRange = (this - scope)..(this + scope)

/**
 * Ensures the starting value of this range is not less than the specified [minValue].
 *
 * @return This range if the starting value is greater than or equal to [minValue], otherwise a
 * new range of equal length with a starting value of [minValue].
 */
fun IntRange.coerceAtLeast(minValue: Int) = if (start > minValue) this else minValue..(minValue + endInclusive - start)

/**
 * A property whose sole purpose is to force *when* expressions to be exhaustive. This is useful
 * in *when* expressions that switch on sealed classes, do not require a return value, and must
 * guarantee that all current and future branches are covered.
 *
 * For example, imagine a sealed class **Fruit** with inheritors **Apple** and **Orange**. Let's
 * say that the following code must accept any **Fruit** and prepare it for use in a fruit salad by
 * processing it in a manner unique to that inheritor - i.e. **Apples** must be sliced and
 * **Oranges** must be peeled:
 *
 * ```
 *
 *     val fruit: Fruit = ...
 *
 *     when(fruit) {
 *         is Apple -> AppleSlicer.slice(fruit)
 *         is Orange -> OrangePeeler.peel(fruit)
 *     }
 * ```
 *
 * Imagine this code is tucked away in a remote corner of the project and is quickly forgotten. What
 * happens a few months later when we add a new inheritor, **Banana**, but neglect to update this
 * code? In the best case all the **Bananas** will go into the fruit salad whole and unwashed,
 * hopefully giving our guests food poisoning. In the worst case it tries to add a null **Banana**
 * to the salad, causing a tear in the fabric of time and space which ripples out into the infinite
 * void, destroying everything in its wake and coming to an end only when the entire universe has
 * been obliterated by an unstoppable cascade of [NullPointerException]s.
 *
 * By adding `.exhaustive` to the end of the *when* expression the code will no longer compile
 * unless all branches have been exhausted:
 *
 * ```
 *
 *     val fruit: Fruit = ...
 *
 *     when(fruit) { // COMPILER ERROR
 *         is Apple -> AppleSlicer.slice(fruit)
 *         is Orange -> OrangePeeler.peel(fruit)
 *     }.exhaustive
 * ```
 *
 * This allows us to fail fast when adding new features and reduces the likelihood of introducing
 * additional bugs during future development.
 */
@Suppress("unused")
val Any?.exhaustive get() = Unit
