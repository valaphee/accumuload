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

package com.valaphee.tead.explorer

import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import tornadofx.Dimension
import tornadofx.add
import tornadofx.combobox
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.onChange
import tornadofx.style
import tornadofx.textfield
import java.io.File

/**
 * @author Kevin Ludwig
 */
class TreePane<T : Entry<T>>(
    private val entry: Entry<T>,
) : VBox() {
    private val root = SimpleStringProperty(File("").absolutePath).onChange {
        tree.root = entry.item

        tree.populate(tree.root)
    }
    private val tree = Tree(entry, Dispatchers.IO + SupervisorJob()).also { it.startUpdates() }

    init {
        hgrow = Priority.ALWAYS

        hbox {
            combobox<String> { items = FXCollections.observableArrayList("Local", "root@127.0.0.1") }
            textfield(root) {
                hgrow = Priority.ALWAYS

                style(append = true) { prefHeight = Dimension(27.0, Dimension.LinearUnits.px) }
            }
        }
        add(tree)
    }
}
