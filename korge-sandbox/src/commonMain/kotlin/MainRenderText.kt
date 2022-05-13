import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.image
import com.soywiz.korim.color.Colors
import com.soywiz.korim.font.readFont
import com.soywiz.korim.font.renderTextToBitmap
import com.soywiz.korim.text.DefaultStringTextRenderer
import com.soywiz.korio.file.std.resourcesVfs

suspend fun Stage.mainRenderText() {
    val font = resourcesVfs["Pacifico.ttf"].readFont()
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
    image(textResult.bmp) {
        debugAnnotate = true
    }
    println("BITMAP SIZE: ${textResult.bmp.size}")
    println("FONT METRICS: ${textResult.fmetrics}")
    println("TEXT METRICS: ${textResult.metrics}")
    println("GLYPHS PLACED:")
    for (g in textResult.glyphs) {
        println(" - $g")
    }
}
