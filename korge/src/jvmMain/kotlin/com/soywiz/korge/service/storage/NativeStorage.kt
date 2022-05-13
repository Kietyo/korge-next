package com.soywiz.korge.service.storage

import com.soywiz.korge.view.Views
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.List
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.toList

actual class NativeStorage actual constructor(val views: Views) : IStorage {
	val props = Properties()
    val folder = File(views.realSettingsFolder).also { kotlin.runCatching { it.mkdirs() } }
    val file = File(folder, "game.jvm.storage")

	init {
		load()
	}

    override fun toString(): String = "NativeStorage(${toMap()})"

    actual fun keys(): List<String> = props.keys.toList().map { it.toString() }

	private fun load() {
        if (!file.exists()) return

        try {
            FileInputStream(file).use { fis -> props.load(fis) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

	private fun save() {
		try {
			FileOutputStream(file).use { fout -> props.store(fout, "") }
		} catch (e: IOException) {
			e.printStackTrace()
		}
	}

	actual override fun set(key: String, value: String) {
		props[key] = value
		save()
	}

	actual override fun getOrNull(key: String): String? {
		return props[key]?.toString()
	}

	actual override fun remove(key: String) {
		props.remove(key)
		save()
	}

	actual override fun removeAll() {
		props.clear()
		save()
	}
}
