package com.soywiz.korim.format

import com.soywiz.klogger.*
import com.soywiz.kmem.*
import com.soywiz.korio.async.*
import com.soywiz.korio.file.*
import com.soywiz.korio.lang.Charsets
import com.soywiz.korio.stream.*
import com.soywiz.krypto.encoding.*

// https://zpl.fi/exif-orientation-in-different-formats/
// https://exiftool.org/TagNames/EXIF.html
object EXIF {
    suspend fun readExifFromJpeg(s: VfsFile, info: ImageInfo = ImageInfo(), debug: Boolean = false): ImageInfo = s.openUse(VfsOpenMode.READ) {
        readExifFromJpeg(this, info, debug)
    }

    suspend fun readExifFromJpeg(s: AsyncStream, info: ImageInfo = ImageInfo(), debug: Boolean = false): ImageInfo {
        val jpegHeader = s.readU16BE()
        if (jpegHeader != 0xFFD8) error("Not a JPEG file ${jpegHeader.hex}")
        while (!s.eof()) {
            val sectionType = s.readU16BE()
            val sectionSize = s.readU16BE()
            //Console.error("sectionType=${sectionType.hex}, sectionSize=${sectionSize.hex}")
            if ((sectionType and 0xFF00) != 0xFF00) error("Probably an invalid JPEG file? ${sectionType.hex}")
            val ss = s.readStream(sectionSize - 2)
            when (sectionType) {
                0xFFE1 -> { // APP1
                    readExif(ss.readAllAsFastStream(), info, debug)
                }
                0xFFC0 -> { // SOF0
                    val precision = ss.readU8()
                    info.width = ss.readU16BE()
                    info.height = ss.readU16BE()
                    info.bitsPerPixel = 24
                }
                0xFFDA -> { // SOS (starts data)
                    // END HERE, we don't want to read the data itself
                    return info
                }
            }
        }
        error("Couldn't find EXIF information")
    }

    fun readExif(s: FastByteArrayInputStream, info: ImageInfo = ImageInfo(), debug: Boolean = false): ImageInfo {
        return runBlockingNoSuspensions { readExif(s.toAsyncStream(), info, debug) }
    }

    suspend fun readExif(s: AsyncStream, info: ImageInfo = ImageInfo(), debug: Boolean = false): ImageInfo {
        if (s.readString(4, Charsets.LATIN1) != "Exif") error("Not an Exif section")
        s.skip(2)
        return readExifBase(s, info, debug)
    }

    // @TODO: We are missing some data from other FIDs, but for orientation it should be on the first FID
    suspend fun readExifBase(ss: AsyncStream, info: ImageInfo = ImageInfo(), debug: Boolean = false): ImageInfo {
        val s = ss.sliceHere()
        val endian = when (val start = s.readString(2, Charsets.LATIN1)) {
            "MM" -> Endian.BIG_ENDIAN
            "II" -> Endian.LITTLE_ENDIAN
            else -> error("Not Exif data (not starting with MM or II but '$start')")
        }
        val tagMark = s.readU16(endian)
        val offsetFirstIFD = s.readS32(endian) // @TODO: do we need to use this somehow?

        val nDirEntry = s.readU16(endian)
        if (debug) {
            Console.error("nDirEntry=$nDirEntry, tagMark=$tagMark, offsetFirstFID=$offsetFirstIFD")
        }
        for (n in 0 until nDirEntry) {
            val tagPos = s.position.toInt()
            val tagNumber = s.readU16(endian)
            val dataFormat = DataFormat[s.readU16(endian)]
            val nComponent = s.readS32(endian)
            if (debug) {
                Console.error("tagPos=${tagPos.hex}, tagNumber=${tagNumber.hex}, dataFormat=$dataFormat, nComponent=$nComponent, size=${dataFormat.indexBytes(nComponent)}")
            }
            val data: ByteArray = s.readBytesExact(dataFormat.indexBytes(nComponent))

            fun readUShort(index: Int): Int = data.readU16(index * 2, little = endian.isLittle)
            fun readInt(index: Int): Int = data.readS32(index * 4, little = endian.isLittle)

            if (debug) {
                if (dataFormat == DataFormat.STRING) {
                    Console.error("  - '${s.sliceStart(readInt(0).toLong()).readStringz(nComponent)}'")
                }
            }

            when (tagNumber) {
                0x112 -> { // Orientation
                    info.orientation = when (readUShort(0)) {
                        1 -> ImageOrientation.ORIGINAL
                        2 -> ImageOrientation.MIRROR_HORIZONTAL
                        3 -> ImageOrientation.ROTATE_180
                        4 -> ImageOrientation.MIRROR_VERTICAL
                        5 -> ImageOrientation.MIRROR_HORIZONTAL_ROTATE_270
                        6 -> ImageOrientation.ROTATE_90
                        7 -> ImageOrientation.MIRROR_HORIZONTAL_ROTATE_90
                        8 -> ImageOrientation.ROTATE_270
                        else -> ImageOrientation.ORIGINAL
                    }
                }
            }
            //AsyncStream().skipToAlign()
            s.skipToAlign(4, offset = 2)
        }
        return info
    }

    enum class DataFormat(
        val id: Int,
        val indexBytes: (Int) -> Int,
    ) {
        UBYTE(1, { it }),
        STRING(2, { 4 }),
        USHORT(3, { it * 2 }),
        ULONG(4, { it * 4 }),
        URATIO(5, { it * 4 }),
        SBYTE(6, { it }),
        UNDEFINED(7, { it }),
        SSHORT(8, { it * 2 }),
        SLONG(9, { it * 4 }),
        SRATIO(10, { it * 4 }),
        SFLOAT(11, { it * 4 }),
        DFLOAT(12, { it * 8 }),
        UNKNOWN(-1, { 0 });

        companion object {
            val BY_ID = values().associateBy { it.id }
            operator fun get(index: Int): DataFormat =
                BY_ID[index] ?: UNKNOWN
        }
    }
}
