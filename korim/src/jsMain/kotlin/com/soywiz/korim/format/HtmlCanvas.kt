package com.soywiz.korim.format

import com.soywiz.korio.util.OS
import kotlinx.browser.document
import org.khronos.webgl.TexImageSource
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.RenderingContext
import org.w3c.files.Blob

external interface HTMLCanvasElementLike : TexImageSource {
    val width: Int
    val height: Int
    fun getContext(contextId: String, vararg arguments: Any?): RenderingContext?
    fun toDataURL(type: String = definedExternally, quality: Any? = definedExternally): String
    fun toBlob(_callback: (Blob?) -> Unit, type: String = definedExternally, quality: Any? = definedExternally): Unit
}

external interface HTMLImageElementLike : TexImageSource {
    val width: Int
    val height: Int
    val src: String
}

object HtmlCanvas {
	fun createCanvas(width: Int, height: Int): HTMLCanvasElementLike {
		if (OS.isJsNodeJs) error("Canvas not available on Node.JS")
        val out = document.createElement("canvas").unsafeCast<HTMLCanvasElement>()
        out.width = width
        out.height = height
        return out.toLike()
	}
}

fun HTMLCanvasElement.toLike() = this.unsafeCast<HTMLCanvasElementLike>()
