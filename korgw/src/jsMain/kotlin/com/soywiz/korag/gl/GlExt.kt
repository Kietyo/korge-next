package com.soywiz.korag.gl

import com.soywiz.kgl.KmlGl
import com.soywiz.kgl.KmlGlJsCanvas
import com.soywiz.korag.AG
import com.soywiz.korag.AGConfig
import com.soywiz.korag.AGContainer
import com.soywiz.korag.AGFactory
import com.soywiz.korag.AGWindow
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLCanvasElement

object AGFactoryWebgl : AGFactory {
	override val supportsNativeFrame: Boolean = true
	override fun create(nativeControl: Any?, config: AGConfig): AG = AGWebgl(config)
	override fun createFastWindow(title: String, width: Int, height: Int): AGWindow {
		TODO()
	}
}

fun jsEmptyObject(): dynamic = js("({})")

fun jsObject(vararg pairs: Pair<String, Any?>): dynamic {
	val out = jsEmptyObject()
	for ((k, v) in pairs) if (v != null) out[k] = v
	//for ((k, v) in pairs) out[k] = v
	return out
}

val korgwCanvasQuery: String? by lazy { window.asDynamic().korgwCanvasQuery.unsafeCast<String?>() }
val isCanvasCreatedAndHandled get() = korgwCanvasQuery == null

open class AGWebgl(val config: AGConfig, val glDecorator: (KmlGl) -> KmlGl = { it }) : AGOpengl(), AGContainer {
	override val ag: AG = this

    open fun getCanvas(): HTMLCanvasElement {
        return (korgwCanvasQuery?.let { document.querySelector(it) as HTMLCanvasElement })
            ?: (document.createElement("canvas") as HTMLCanvasElement)
    }

	val canvas by lazy { getCanvas() }

	val glOpts = jsObject(
		"premultipliedAlpha" to true,
		"alpha" to false,
		"stencil" to true,
        "antialias" to config.antialiasHint
	)
	//val gl: GL = (canvas.getContext("webgl", glOpts) ?: canvas.getContext("experimental-webgl", glOpts)) as GL
	//override val gl = KmlGlCached(KmlGlJsCanvas(canvas, glOpts))
    val baseGl = KmlGlJsCanvas(canvas, glOpts)
    override val gl = glDecorator(baseGl)

    init {
		(window.asDynamic()).ag = this
		//(window.asDynamic()).gl = gl
	}

	override val nativeComponent: Any = canvas
	val tDevicePixelRatio get() = window.devicePixelRatio.toDouble()
	override val devicePixelRatio get() = when {
		tDevicePixelRatio <= 0.0 -> 1.0
		tDevicePixelRatio.isNaN() -> 1.0
		tDevicePixelRatio.isInfinite() -> 1.0
		else -> tDevicePixelRatio
	}
    // @TODO: Improve this: https://gist.github.com/scryptonite/5242987
    override val pixelsPerInch: Double get() = 96.0 * devicePixelRatio

    init {
		canvas.addEventListener("webglcontextlost", { e ->
			//contextVersion++
			e.preventDefault()
		}, false)

		canvas.addEventListener("webglcontextrestored", { e ->
			contextVersion++
			//e.preventDefault()
		}, false)

		//fun handleOnResized() {
		//	ag.resized(canvas.width, canvas.height)
		//}
//
		//window.addEventListener("resize", { e ->
		//	handleOnResized()
		//	//e.preventDefault()
		//}, false)
//
		//handleOnResized()
	}

	override fun repaint() {
	}

	override fun dispose() {
		// https://www.khronos.org/webgl/wiki/HandlingContextLost
		// https://gist.github.com/mattdesl/9995467
	}
}
