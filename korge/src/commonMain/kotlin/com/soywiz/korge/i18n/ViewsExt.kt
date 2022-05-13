package com.soywiz.korge.i18n

import com.soywiz.kds.*
import com.soywiz.korge.view.*
import com.soywiz.korio.util.i18n.*

private var Views.extraLanguage: Language
    get() = getExtra("extraLanguage") as? Language ?: Language.CURRENT
    set(value) { setExtra("extraLanguage", value) }

var Views.language: Language
	get() = this.extraLanguage
	set(value) {
		this.extraLanguage = value
		this.stage.foreachDescendant {
			if (it is TextContainer) it.updateText(value)
		}
	}
