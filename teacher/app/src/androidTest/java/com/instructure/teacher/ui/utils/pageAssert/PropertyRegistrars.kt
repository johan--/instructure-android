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
 */
package com.instructure.teacher.ui.utils.pageAssert

import android.support.test.espresso.ViewInteraction
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


interface PropertyRegistry {
    fun registerPropertyInfo(info: Pair<ReadOnlyProperty<Any, ViewInteraction>, KProperty<*>>)
    fun getRegisteredProperties(): List<ViewInteraction>
}

class SkipPropertyRegistry : PropertyRegistry {
    override fun registerPropertyInfo(info: Pair<ReadOnlyProperty<Any, ViewInteraction>, KProperty<*>>) {}
    override fun getRegisteredProperties(): List<ViewInteraction> = emptyList()
}

class SimplePropertyRegistry : PropertyRegistry {
    val assertPropertiesInfo = arrayListOf<Pair<ReadOnlyProperty<Any, ViewInteraction>, KProperty<*>>>()

    override fun getRegisteredProperties(): List<ViewInteraction> {
        return assertPropertiesInfo.map { (prop, kProp) -> prop.getValue(this, kProp) }
    }

    override fun registerPropertyInfo(info: Pair<ReadOnlyProperty<Any, ViewInteraction>, KProperty<*>>) {
        assertPropertiesInfo += info
    }
}