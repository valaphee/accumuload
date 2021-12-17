/*
 * MIT License
 *
 * Copyright (c) 2021, Valaphee.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.valaphee.blit.data.locale

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.valaphee.blit.data.DataType
import com.valaphee.blit.data.KeyedData
import java.text.MessageFormat
import java.util.regex.Pattern

/**
 * @author Kevin Ludwig
 */
@DataType("locale")
class Locale(
    @get:JsonProperty("key") override val key: String,
    @get:JsonProperty("entries") val entries: Map<String, Any>
) : KeyedData() {
    @JsonIgnore private val entriesFlat = entries.flatMap { flatten(it.key, it.value) }.toMap()
    @JsonIgnore private val entryFormats = mutableMapOf<String, MessageFormat>()

    operator fun get(key: String, vararg arguments: Any?) = entriesFlat[key]?.let {
        (entryFormats[key] ?: (try {
            MessageFormat(it)
        } catch (_: IllegalArgumentException) {
            MessageFormat(pattern.matcher(it).replaceAll("\\[$1\\]"))
        }).also { entryFormats[key] = it }).format(arguments)
    } ?: key

    companion object {
        private val pattern = Pattern.compile("\\{(\\D*?)}")

        private fun flatten(key: String, value: Any): Iterable<Pair<String, String>> = if (value !is Map<*, *>) listOf(key to value.toString()) else value.flatMap { flatten("$key.${(it.key as String)}", it.value!!) }
    }
}