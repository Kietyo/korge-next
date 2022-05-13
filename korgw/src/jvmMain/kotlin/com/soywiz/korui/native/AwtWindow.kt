package com.soywiz.korui.native

import com.soywiz.korma.geom.RectangleInt
import com.soywiz.korui.UiMenu
import com.soywiz.korui.native.util.toJMenuBar
import java.awt.Rectangle
import javax.swing.JFrame
import javax.swing.WindowConstants

open class AwtWindow(factory: BaseAwtUiFactory, val frame: JFrame = JFrame()) : AwtContainer(factory, frame, frame.contentPane), NativeUiFactory.NativeWindow {
    init {
        frame.contentPane.layout = null
        frame.defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
        frame.setLocationRelativeTo(null)
    }

    override val componentPane get() = frame.contentPane

    override var bounds: RectangleInt
        get() {
            val b = frame.contentPane.bounds
            return RectangleInt(b.x, b.y, b.width, b.height)
        }
        set(value) {
            frame.contentPane.bounds = Rectangle(value.x, value.y, value.width, value.height)
            frame.bounds = Rectangle(value.x, value.y, value.width, value.height)
        }

    override var visible: Boolean
        get() = super<AwtContainer>.visible
        set(value) {
            super<AwtContainer>.visible = value
            frame.setLocationRelativeTo(null)
        }
    override var title: String
        get() = frame.title
        set(value) {
            frame.title = value
        }

    override var menu: UiMenu? = null
        set(value) {
            field = value
            frame.jMenuBar = value?.toJMenuBar(factory)
        }

    override var focusable: Boolean
        get() = frame.contentPane.isFocusable
        set(value) {
            frame.contentPane.isFocusable = value
        }
}
