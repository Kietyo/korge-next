
import com.soywiz.klock.TimeProvider
import com.soywiz.korev.MouseButton
import com.soywiz.korev.MouseEvent
import com.soywiz.korge.baseview.BaseView
import com.soywiz.korge.component.MouseComponent
import com.soywiz.korge.input.DraggableInfo
import com.soywiz.korge.input.onClick
import com.soywiz.korge.ui.uiButton
import com.soywiz.korge.view.Container
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.Text
import com.soywiz.korge.view.View
import com.soywiz.korge.view.Views
import com.soywiz.korge.view.addTo
import com.soywiz.korge.view.alpha
import com.soywiz.korge.view.centerOnStage
import com.soywiz.korge.view.circle
import com.soywiz.korge.view.container
import com.soywiz.korge.view.sgraphics
import com.soywiz.korge.view.size
import com.soywiz.korge.view.solidRect
import com.soywiz.korge.view.visible
import com.soywiz.korge.view.xy
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.MaterialColors
import com.soywiz.korma.geom.Point
import kotlinx.coroutines.sync.Mutex
import kotlin.random.Random
import kotlin.random.nextInt

fun DraggableInfo.asString(): String {
    return """
        DraggableInfo(
        viewStartXY: $viewStartXY
        viewPrevXY: $viewPrevXY
        viewNextXY: $viewNextXY
        viewDeltaXY: $viewDeltaXY
        )
    """.trimIndent()
}

enum class MouseDragState {
    UNKNOWN,
    START,
    DRAG,
    END
}

data class MouseDragKomponentSettings(
    var isEnabled: Boolean = true,
    var allowLeftClickDragging: Boolean = true,
    var allowRightClickDragging: Boolean = true,
    var allowMiddleClickDragging: Boolean = true,
)

data class MouseDragKomponent(
    override val view: View,
    val settings: MouseDragKomponentSettings = MouseDragKomponentSettings()
) : MouseComponent {
    val autoMove = true
    val info = DraggableInfo(view)

    private val ALLOWED_EVENTS = setOf(
        MouseEvent.Type.DOWN,
        MouseEvent.Type.DRAG,
        MouseEvent.Type.UP,
    )

    fun adjustSettings(fn: MouseDragKomponentSettings.() -> Unit) {
        fn(settings)
        reset()
    }

    // This is used to prevent other buttons from accidentally "closing" the current
    // drag state. For example, right clicking while doing a left click drag.
    private var activeButton = MouseButton.LEFT

    private fun getState(event: MouseEvent): MouseDragState {
        when (event.type) {
            MouseEvent.Type.DOWN -> {
                if (event.button == MouseButton.LEFT) {
                    return MouseDragState.START
                }
                if (event.button == MouseButton.MIDDLE) {
                    return MouseDragState.START
                }
                if (event.button == MouseButton.RIGHT) {
                    return MouseDragState.START
                }
                return MouseDragState.UNKNOWN
            }
            MouseEvent.Type.UP -> {
                if (event.button == MouseButton.LEFT) {
                    return MouseDragState.END
                }
                if (event.button == MouseButton.MIDDLE) {
                    return MouseDragState.END
                }
                if (event.button == MouseButton.RIGHT) {
                    return MouseDragState.END
                }
                return MouseDragState.UNKNOWN
            }
            MouseEvent.Type.DRAG -> return MouseDragState.DRAG
            else -> return MouseDragState.UNKNOWN
        }
    }

    private val currentPosition = Point()

    var startX = 0.0
    var startY = 0.0

    private var dragging = false

    private fun reset() {
        dragging = false
    }

    override fun onMouseEvent(views: Views, event: MouseEvent) {
        if (!settings.isEnabled) return
        if (event.type !in ALLOWED_EVENTS) {
            return
        }

        if (!settings.allowLeftClickDragging && event.button == MouseButton.LEFT) {
            return
        }

        if (!settings.allowRightClickDragging && event.button == MouseButton.RIGHT) {
            return
        }

        if (dragging) {
            if (event.type != MouseEvent.Type.DRAG &&
                event.button != activeButton) {
                return
            }
        }

        val state = getState(event)

        println(
            """
            dragging: $dragging,
            settings: $settings
            event: $event,
            state: $state
            info: ${info.asString()}
        """.trimIndent()
        )

        if (state == MouseDragState.UNKNOWN) {
            require(!dragging)
            return
        }

        require(state != MouseDragState.UNKNOWN)

        currentPosition.copyFrom(views.globalMouseXY)

        when (state) {
            MouseDragState.START -> {
                require(!dragging)
                activeButton = event.button
                dragging = true
                startX = currentPosition.x
                startY = currentPosition.y
                info.reset()
            }
            MouseDragState.END -> {
                dragging = false
            }
            else -> Unit
        }

        val deltaX = currentPosition.x - startX
        val deltaY = currentPosition.y - startY

        info.set(
            deltaX,
            deltaY,
            state == MouseDragState.START,
            state == MouseDragState.END,
            TimeProvider.now()
        )

        if (dragging) {
            handle(event)
        }
    }

    fun handle(event: MouseEvent) {
        val state = getState(event)
        val view = view
        if (state == MouseDragState.START) {
            info.viewStartXY.copyFrom(view.pos)
        }
        //println("localDXY=${info.localDX(view)},${info.localDY(view)}")
        info.viewPrevXY.copyFrom(view.pos)
        info.viewNextXY.setTo(
            info.viewStartX + info.localDX(view),
            info.viewStartY + info.localDY(view)
        )
        info.viewDeltaXY.setTo(info.viewNextX - info.viewPrevX, info.viewNextY - info.viewPrevY)
        if (autoMove) {
            view.xy(info.viewNextXY)
        }
    }
}

const val GRID_SIZE = 25.0

// The border will be a ratio of the grid size.
const val BORDER_RATIO = 0.5

// Grid lines width based on ratio of the grid size.
const val GRID_LINES_RATIO = 0.04

// Size of the grid number text based on the ratio of the grid size.
const val GRID_NUMBERS_RATIO = 0.5

// Path lines width based on ratio of the grid size.
const val PATH_LINES_RATIO = 0.125

// The width of the line based on the ratio of the grid size.
const val LINE_WIDTH_RATIO = 0.035

data class UIMapSettings(
    val gridSize: Double = GRID_SIZE,
    val borderRatio: Double = BORDER_RATIO,
    val gridLinesRatio: Double = GRID_LINES_RATIO,
    val gridNumbersRatio: Double = GRID_NUMBERS_RATIO,
    val pathLinesRatio: Double = PATH_LINES_RATIO,
    val drawGridNumbers: Boolean = true,
) {
    val borderSize = gridSize * borderRatio
    val gridLineSize = gridSize * gridLinesRatio
    val gridNumberFontSize = gridSize * gridNumbersRatio
    val pathLinesWidth = gridSize * pathLinesRatio
}

data class GameMap(
    val width: Int = 10,
    val height: Int = 10)

class UIMap(
    val gameMap: GameMap = GameMap(),
    val uiMapSettings: UIMapSettings = UIMapSettings()): Container(), View.Reference {
    val _gridSize = uiMapSettings.gridSize
    val _borderSize = uiMapSettings.borderSize
    val _gridLineSize = uiMapSettings.gridLineSize
    val _gridNumberFontSize = uiMapSettings.gridNumberFontSize
    val _pathLinesWidth = uiMapSettings.pathLinesWidth

    val _drawnRockCounters = mutableMapOf<Pair<Int, Int>, Text>()

    val _boardBG = solidRect(
        _gridSize * gameMap.width, _gridSize * gameMap.height,
        MaterialColors.GREEN_600
    )

    // Note that the order in which layers are initialized mattes here.
    val _boardLayer = this.container() {
        //        this.propagateEvents = false
        //        this.mouseChildren = false
        //        this.hitTestEnabled = false
    }
    val _gridNumberLayer = this.container()

    val _gridLinesLayer = this.container()
    val _gridLinesGraphics = _gridLinesLayer.sgraphics {
        useNativeRendering = false
        visible(false)
    }

    val _speedAreaLayer = this.container()

    val _entityLayer = this.container()

    val _rockCountersLayer = this.container()
    val _rockCountersLayerMutex = Mutex()

    val _entityLabelLayer = this.container()

    val _pathingLinesLayer = this.container()
    val _pathingLinesGraphics = _pathingLinesLayer.sgraphics {
        useNativeRendering = false
    }

    val _highlightLayer = this.container()
    val _highlightRectangle = _highlightLayer
        .solidRect(0, 0, Colors.YELLOW).alpha(0.5).visible(false)

    val mapHeight: Int
        get() = gameMap.height
    val mapWidth: Int
        get() = gameMap.width

    fun renderHighlightRectangle() {
        _highlightRectangle
            .size(100, 100)
            .xy(Random.nextInt(0..400), Random.nextInt(0..400))
            .visible(true)
    }
}

class PlacementComponent(
    override val view: BaseView,
    val uiMap: UIMap
) : MouseComponent {
    override fun onMouseEvent(views: Views, event: MouseEvent) {
//        uiMap.renderHighlightRectangle()
//        if (event.type == MouseEvent.Type.UP) {
//            uiMap._entityLayer.circle(50.0, Colors.MAGENTA).xy(50, 50)
//        }
    }
}

suspend fun Stage.mainBuggy() {

    val stage = this

    val uiMap = UIMap().addTo(this).apply {
        centerOnStage()
    }

    val placementComponent = PlacementComponent(this, uiMap)
    addComponent(MouseDragKomponent(uiMap))
    addComponent(placementComponent)

    uiButton("Add Circle") {
        onClick {
            uiMap._entityLayer.circle(50.0, Colors.MAGENTA).xy(50, 50)
        }
    }
}
