import com.soywiz.korge.scene.MaskTransition
import com.soywiz.korge.scene.TransitionView
import com.soywiz.korge.view.SolidRect
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.filter.DropshadowFilter
import com.soywiz.korge.view.filter.TransitionFilter
import com.soywiz.korge.view.filters
import com.soywiz.korge.view.solidRect
import com.soywiz.korge.view.xy
import com.soywiz.korim.color.Colors

suspend fun Stage.mainTransition() {
    val transition = TransitionView().addTo(this).xy(300, 100)
    transition.startNewTransition(SolidRect(100, 100, Colors.RED))
    transition.startNewTransition(SolidRect(100, 100, Colors.BLUE), MaskTransition(
        TransitionFilter.Transition.CIRCULAR
    ))
    transition.ratio = 0.5
    transition.filters(DropshadowFilter(shadowColor = Colors.PURPLE))

    solidRect(100, 100, Colors.GREEN).filters(DropshadowFilter(shadowColor = Colors.PURPLE))
}
