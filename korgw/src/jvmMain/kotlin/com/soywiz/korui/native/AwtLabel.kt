package com.soywiz.korui.native

import com.soywiz.korim.bitmap.Bitmap
import com.soywiz.korui.native.util.toAwtIcon
import javax.swing.JLabel

open class AwtLabel(factory: BaseAwtUiFactory, val label: JLabel = JLabel()) : AwtComponent(factory, label), NativeUiFactory.NativeLabel {
    override var text: String
        get() = label.text
        set(value) { label.text = value }

    override var icon: Bitmap? = null
        set(value) {
            field = value
            label.icon = value?.toAwtIcon()
        }
}
