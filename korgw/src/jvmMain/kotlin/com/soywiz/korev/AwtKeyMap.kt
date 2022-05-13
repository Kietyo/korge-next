package com.soywiz.korev

import java.awt.event.KeyEvent

fun awtKeyCodeToKey(keyCode: Int): Key {
    return AwtKeyMap[keyCode] ?: Key.UNKNOWN
}

internal val AwtKeyMap = mapOf(
    KeyEvent.VK_ENTER to Key.ENTER,
    KeyEvent.VK_BACK_SPACE to Key.BACKSPACE,
    KeyEvent.VK_TAB to Key.TAB,
    KeyEvent.VK_CANCEL to Key.CANCEL,
    KeyEvent.VK_CLEAR to Key.CLEAR,
    KeyEvent.VK_SHIFT to Key.LEFT_SHIFT,
    KeyEvent.VK_CONTROL to Key.LEFT_CONTROL,
    KeyEvent.VK_ALT to Key.LEFT_ALT,
    KeyEvent.VK_PAUSE to Key.PAUSE,
    KeyEvent.VK_CAPS_LOCK to Key.CAPS_LOCK,
    KeyEvent.VK_ESCAPE to Key.ESCAPE,
    KeyEvent.VK_SPACE to Key.SPACE,
    KeyEvent.VK_PAGE_UP to Key.PAGE_UP,
    KeyEvent.VK_PAGE_DOWN to Key.PAGE_DOWN,
    KeyEvent.VK_END to Key.END,
    KeyEvent.VK_HOME to Key.HOME,
    KeyEvent.VK_LEFT to Key.LEFT,
    KeyEvent.VK_UP to Key.UP,
    KeyEvent.VK_RIGHT to Key.RIGHT,
    KeyEvent.VK_DOWN to Key.DOWN,
    KeyEvent.VK_COMMA to Key.COMMA,
    KeyEvent.VK_MINUS to Key.MINUS,
    KeyEvent.VK_PLUS to Key.PLUS,
    KeyEvent.VK_PERIOD to Key.PERIOD,
    KeyEvent.VK_SLASH to Key.SLASH,
    KeyEvent.VK_0 to Key.N0,
    KeyEvent.VK_1 to Key.N1,
    KeyEvent.VK_2 to Key.N2,
    KeyEvent.VK_3 to Key.N3,
    KeyEvent.VK_4 to Key.N4,
    KeyEvent.VK_5 to Key.N5,
    KeyEvent.VK_6 to Key.N6,
    KeyEvent.VK_7 to Key.N7,
    KeyEvent.VK_8 to Key.N8,
    KeyEvent.VK_9 to Key.N9,
    KeyEvent.VK_SEMICOLON to Key.SEMICOLON,
    KeyEvent.VK_EQUALS to Key.EQUAL,
    KeyEvent.VK_A to Key.A,
    KeyEvent.VK_B to Key.B,
    KeyEvent.VK_C to Key.C,
    KeyEvent.VK_D to Key.D,
    KeyEvent.VK_E to Key.E,
    KeyEvent.VK_F to Key.F,
    KeyEvent.VK_G to Key.G,
    KeyEvent.VK_H to Key.H,
    KeyEvent.VK_I to Key.I,
    KeyEvent.VK_J to Key.J,
    KeyEvent.VK_K to Key.K,
    KeyEvent.VK_L to Key.L,
    KeyEvent.VK_M to Key.M,
    KeyEvent.VK_N to Key.N,
    KeyEvent.VK_O to Key.O,
    KeyEvent.VK_P to Key.P,
    KeyEvent.VK_Q to Key.Q,
    KeyEvent.VK_R to Key.R,
    KeyEvent.VK_S to Key.S,
    KeyEvent.VK_T to Key.T,
    KeyEvent.VK_U to Key.U,
    KeyEvent.VK_V to Key.V,
    KeyEvent.VK_W to Key.W,
    KeyEvent.VK_X to Key.X,
    KeyEvent.VK_Y to Key.Y,
    KeyEvent.VK_Z to Key.Z,
    KeyEvent.VK_OPEN_BRACKET to Key.OPEN_BRACKET,
    KeyEvent.VK_BACK_SLASH to Key.BACKSLASH,
    KeyEvent.VK_CLOSE_BRACKET to Key.CLOSE_BRACKET,
    KeyEvent.VK_NUMPAD0 to Key.NUMPAD0,
    KeyEvent.VK_NUMPAD1 to Key.NUMPAD1,
    KeyEvent.VK_NUMPAD2 to Key.NUMPAD2,
    KeyEvent.VK_NUMPAD3 to Key.NUMPAD3,
    KeyEvent.VK_NUMPAD4 to Key.NUMPAD4,
    KeyEvent.VK_NUMPAD5 to Key.NUMPAD5,
    KeyEvent.VK_NUMPAD6 to Key.NUMPAD6,
    KeyEvent.VK_NUMPAD7 to Key.NUMPAD7,
    KeyEvent.VK_NUMPAD8 to Key.NUMPAD8,
    KeyEvent.VK_NUMPAD9 to Key.NUMPAD9,
    KeyEvent.VK_MULTIPLY to Key.KP_MULTIPLY,
    KeyEvent.VK_ADD to Key.KP_ADD,
    KeyEvent.VK_SEPARATER to Key.KP_SEPARATOR,
    KeyEvent.VK_SUBTRACT to Key.KP_SUBTRACT,
    KeyEvent.VK_DECIMAL to Key.KP_DECIMAL,
    KeyEvent.VK_DIVIDE to Key.KP_DIVIDE,
    KeyEvent.VK_DELETE to Key.DELETE,
    KeyEvent.VK_NUM_LOCK to Key.NUM_LOCK,
    KeyEvent.VK_SCROLL_LOCK to Key.SCROLL_LOCK,
    KeyEvent.VK_F1 to Key.F1,
    KeyEvent.VK_F2 to Key.F2,
    KeyEvent.VK_F3 to Key.F3,
    KeyEvent.VK_F4 to Key.F4,
    KeyEvent.VK_F5 to Key.F5,
    KeyEvent.VK_F6 to Key.F6,
    KeyEvent.VK_F7 to Key.F7,
    KeyEvent.VK_F8 to Key.F8,
    KeyEvent.VK_F9 to Key.F9,
    KeyEvent.VK_F10 to Key.F10,
    KeyEvent.VK_F11 to Key.F11,
    KeyEvent.VK_F12 to Key.F12,
    KeyEvent.VK_F13 to Key.F13,
    KeyEvent.VK_F14 to Key.F14,
    KeyEvent.VK_F15 to Key.F15,
    KeyEvent.VK_F16 to Key.F16,
    KeyEvent.VK_F17 to Key.F17,
    KeyEvent.VK_F18 to Key.F18,
    KeyEvent.VK_F19 to Key.F19,
    KeyEvent.VK_F20 to Key.F20,
    KeyEvent.VK_F21 to Key.F21,
    KeyEvent.VK_F22 to Key.F22,
    KeyEvent.VK_F23 to Key.F23,
    KeyEvent.VK_F24 to Key.F24,
    KeyEvent.VK_PRINTSCREEN to Key.PRINT_SCREEN,
    KeyEvent.VK_INSERT to Key.INSERT,
    KeyEvent.VK_HELP to Key.HELP,
    KeyEvent.VK_META to Key.META,
    KeyEvent.VK_BACK_QUOTE to Key.BACKQUOTE,
    KeyEvent.VK_QUOTE to Key.QUOTE,
    KeyEvent.VK_KP_UP to Key.KP_UP,
    KeyEvent.VK_KP_DOWN to Key.KP_DOWN,
    KeyEvent.VK_KP_LEFT to Key.KP_LEFT,
    KeyEvent.VK_KP_RIGHT to Key.KP_RIGHT,
    KeyEvent.VK_UNDEFINED to Key.UNDEFINED
)
