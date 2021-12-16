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

package com.valaphee.blit.app.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.valaphee.blit.Source
import com.valaphee.blit.local.LocalSource
import javafx.collections.FXCollections
import javafx.collections.ObservableList

/**
 * @author Kevin Ludwig
 */
class Config(
    @JsonDeserialize(using = SourceObservableListDeserializer::class)
    @JsonProperty("sources") val sources: ObservableList<Source<*>> = FXCollections.observableArrayList(LocalSource("local"))
) {
    companion object {
        class SourceObservableListDeserializer : JsonDeserializer<ObservableList<Source<*>>>() {
            override fun deserialize(parser: JsonParser, context: DeserializationContext): ObservableList<Source<*>> = FXCollections.observableList(parser.readValueAs<List<Source<*>>>(object : TypeReference<List<Source<*>>>() {}))
        }
    }
}


