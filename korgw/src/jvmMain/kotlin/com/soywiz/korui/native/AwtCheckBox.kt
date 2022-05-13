package com.soywiz.korui.native

import com.soywiz.korio.lang.Disposable
import javax.swing.JCheckBox
import javax.swing.event.ChangeListener

open class AwtCheckBox(factory: BaseAwtUiFactory, val checkBox: JCheckBox = JCheckBox()) : AwtComponent(factory, checkBox), NativeUiFactory.NativeCheckBox {
    override var text: String
        get() = checkBox.text
        set(value) { checkBox.text = value }
    override var checked: Boolean
        get() = checkBox.isSelected
        set(value) { checkBox.isSelected = value }

    override fun onChange(block: () -> Unit): Disposable {
        val listener = ChangeListener { block() }
        checkBox.addChangeListener(listener)
        return Disposable {
            checkBox.removeChangeListener(listener)
        }
    }
}
