package com.soywiz.korge.spike

import com.soywiz.korge.*
import com.soywiz.korge.view.*
import com.soywiz.korim.color.*

object Spike1 {
	@JvmStatic suspend fun main(args: Array<String>) {
		Korge {
			solidRect(100, 100, Colors.RED) {
				x = 100.0
				y = 100.0
			}
		}
	}
}
