package com.soywiz.korge.view

import com.soywiz.korge.test.assertEqualsFileReference
import com.soywiz.korge.tests.ViewsForTesting
import com.soywiz.korge.view.filter.ColorMatrixFilter
import com.soywiz.korge.view.filter.Convolute3Filter
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.readFont
import com.soywiz.korim.font.renderTextToBitmap
import com.soywiz.korim.format.readBitmap
import com.soywiz.korim.text.DefaultStringTextRenderer
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korma.geom.Rectangle
import com.soywiz.korma.geom.RectangleInt
import com.soywiz.korma.geom.Size
import kotlin.test.Test
import kotlin.test.assertEquals

class ViewsJvmTest : ViewsForTesting(log = true) {
	val tex = Bitmap32(10, 10)

	@Test
	fun name() {
		views.stage += Container().apply {
			this += Image(tex)
		}
		assertEquals(
			"""
				Stage
				 Container
				  Image:bitmap=BitmapSlice(null:SizeInt(width=10, height=10))
			""".trimIndent(),
			views.stage.dumpToString()
		)
		views.render()
        assertEqualsFileReference("korge/render/ViewsJvmTest1.log", logAg.getLogAsString())
	}

	@Test
	fun textGetBounds1() = viewsTest {
		val font = views.debugBmpFont
		assertEquals(Rectangle(0, 0, 77, 8), TextOld("Hello World", font = font, textSize = 8.0).globalBounds)
	}

    @Test
    fun textGetBounds2() = viewsTest {
        val font = views.debugBmpFont
        assertEquals(Rectangle(0, 0, 154, 16), TextOld("Hello World", font = font, textSize = 16.0).globalBounds)
    }

    @Test
    fun testFilter() {
        views.stage += Container().apply {
            this += Image(tex).also {
                it.addFilter(ColorMatrixFilter(ColorMatrixFilter.GRAYSCALE_MATRIX))
                it.addFilter(Convolute3Filter(Convolute3Filter.KERNEL_EDGE_DETECTION))
            }
        }
        assertEquals(
            """
				Stage
				 Container
				  Image:bitmap=BitmapSlice(null:SizeInt(width=10, height=10))
			""".trimIndent(),
            views.stage.dumpToString()
        )
        views.render()
        assertEqualsFileReference("korge/render/ViewsJvmTestFilter.log", logAg.getLogAsString())
    }

    @Test
    fun testTextBounds() = viewsTest {
        val font = resourcesVfs["Pacifico.ttf"].readFont()
        run {
            val text = text("WTF is going on", 64.0)
            assertEquals(RectangleInt(0, 0, 417, 71), text.globalBounds.toInt())
        }

        run {
            val realTextSize = 64.0
            //val text = "WTF is going on\nWTF is going on"
            val text = "WTF is going on"
            val renderer = DefaultStringTextRenderer
            val useNativeRendering = true
            val textResult = font.renderTextToBitmap(
                realTextSize, text,
                paint = Colors.WHITE, fill = true, renderer = renderer,
                //background = Colors.RED,
                nativeRendering = useNativeRendering,
                drawBorder = true
            )
            assertEquals(Size(450, 121), textResult.bmp.size)
        }
    }

    @Test
    fun testRunBlockingNoJs() = viewsTest {
        val bitmap = runBlockingNoJs {
            resourcesVfs["texture.png"].readBitmap()
        }
        assertEquals(Size(64, 64), bitmap.size)
    }
}
