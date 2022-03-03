// @WARNING: File AUTOGENERATED by `korlibs-generator-jvm/src/com/soywiz/korlibs` @ korlibs/kmem do not modify manually!
// @TODO: USELESS_CAST is required since it requires a cast to work, but IDE says that that cast is not necessary
@file:Suppress("NOTHING_TO_INLINE", "EXTENSION_SHADOWED_BY_MEMBER", "RedundantUnitReturnType", "FunctionName", "USELESS_CAST")
package com.soywiz.kmem

public actual class MemBuffer(public val data: ByteArray)
public actual fun MemBufferAlloc(size: Int): MemBuffer = MemBuffer(ByteArray(size))
public actual fun MemBufferAllocNoDirect(size: Int): MemBuffer = MemBuffer(ByteArray(size))
public actual fun MemBufferWrap(array: ByteArray): MemBuffer = MemBuffer(array)
public actual inline val MemBuffer.size: Int get() = data.size

public actual fun MemBuffer._sliceInt8Buffer(offset: Int, size: Int): Int8Buffer = Int8Buffer(this, offset * 1, size)
public actual fun MemBuffer._sliceInt16Buffer(offset: Int, size: Int): Int16Buffer = Int16Buffer(this, offset * 2, size)
public actual fun MemBuffer._sliceInt32Buffer(offset: Int, size: Int): Int32Buffer = Int32Buffer(this, offset * 4, size)
public actual fun MemBuffer._sliceFloat32Buffer(offset: Int, size: Int): Float32Buffer = Float32Buffer(this, offset * 4, size)
public actual fun MemBuffer._sliceFloat64Buffer(offset: Int, size: Int): Float64Buffer = Float64Buffer(this, offset * 8, size)

// @TODO: https://youtrack.jetbrains.com/issue/KT-46427
//@SymbolName("Kotlin_ByteArray_setFloatAtUnsafe") public external fun ByteArray.setFloatAtUnsafe(index: Int, value: Float)
//@SymbolName("Kotlin_ByteArray_getFloatAtUnsafe") public external fun ByteArray.getFloatAtUnsafe(index: Int): Float

private inline fun ByteArray.setFloatAtUnsafe(index: Int, value: Float) = setFloatAt(index, value)
private inline fun ByteArray.getFloatAtUnsafe(index: Int): Float = getFloatAt(index)

public actual typealias DataBuffer = MemBuffer
public actual val DataBuffer.mem: MemBuffer get() = this
public actual fun MemBuffer.getData(): DataBuffer = this
public actual fun DataBuffer.getByte(index: Int): Byte = data.get(index)
public actual fun DataBuffer.getShort(index: Int): Short = data.getShortAt(index)
public actual fun DataBuffer.getInt(index: Int): Int = data.getIntAt(index)
public actual fun DataBuffer.getFloat(index: Int): Float = data.getFloatAtUnsafe(index)
public actual fun DataBuffer.getDouble(index: Int): Double = data.getDoubleAt(index)
public actual fun DataBuffer.setByte(index: Int, value: Byte): Unit = data.set(index, value)
public actual fun DataBuffer.setShort(index: Int, value: Short): Unit = data.setShortAt(index, value)
public actual fun DataBuffer.setInt(index: Int, value: Int): Unit = data.setIntAt(index, value)
public actual fun DataBuffer.setFloat(index: Int, value: Float): Unit = data.setFloatAtUnsafe(index, value)
public actual fun DataBuffer.setDouble(index: Int, value: Double): Unit = data.setDoubleAt(index, value)

@PublishedApi
internal const val BYTE_SIZE: Int = 1

public actual class Int8Buffer(public val mbuffer: MemBuffer, public val byteOffset: Int, public val size: Int) {
    public val MEM_OFFSET: Int = byteOffset / BYTE_SIZE
    public val MEM_SIZE: Int = size / BYTE_SIZE
    public inline fun getByteIndex(index: Int): Int = byteOffset + index * BYTE_SIZE
}
public actual inline val Int8Buffer.mem: MemBuffer get() = mbuffer
public actual inline val Int8Buffer.offset: Int get() = MEM_OFFSET
public actual inline val Int8Buffer.size: Int get() = MEM_SIZE
public actual operator fun Int8Buffer.get(index: Int): Byte = mbuffer.getByte(getByteIndex(index))
public actual operator fun Int8Buffer.set(index: Int, value: Byte): Unit = mbuffer.setByte(getByteIndex(index), value)

@PublishedApi
internal const val SHORT_SIZE: Int = 2

public actual class Int16Buffer(public val mbuffer: MemBuffer, public val byteOffset: Int, public val size: Int) {
    public val MEM_OFFSET: Int = byteOffset / SHORT_SIZE
    public val MEM_SIZE: Int = size / SHORT_SIZE
    public inline fun getByteIndex(index: Int): Int = byteOffset + index * SHORT_SIZE
}
public actual inline val Int16Buffer.mem: MemBuffer get() = mbuffer
public actual inline val Int16Buffer.offset: Int get() = MEM_OFFSET
public actual inline val Int16Buffer.size: Int get() = MEM_SIZE
public actual operator fun Int16Buffer.get(index: Int): Short = mbuffer.getShort(getByteIndex(index))
public actual operator fun Int16Buffer.set(index: Int, value: Short): Unit = mbuffer.setShort(getByteIndex(index), value)

@PublishedApi
internal const val INT_SIZE: Int = 4

public actual class Int32Buffer(public val mbuffer: MemBuffer, public val byteOffset: Int, public val size: Int) {
    public val MEM_OFFSET: Int = byteOffset / INT_SIZE
    public val MEM_SIZE: Int = size / INT_SIZE
    public inline fun getByteIndex(index: Int): Int = byteOffset + index * INT_SIZE
}
public actual inline val Int32Buffer.mem: MemBuffer get() = mbuffer
public actual inline val Int32Buffer.offset: Int get() = MEM_OFFSET
public actual inline val Int32Buffer.size: Int get() = MEM_SIZE
public actual operator fun Int32Buffer.get(index: Int): Int = mbuffer.getInt(getByteIndex(index))
public actual operator fun Int32Buffer.set(index: Int, value: Int): Unit = mbuffer.setInt(getByteIndex(index), value)

@PublishedApi
internal const val FLOAT_SIZE: Int = 4

public actual class Float32Buffer(public val mbuffer: MemBuffer, public val byteOffset: Int, public val size: Int) {
    public val MEM_OFFSET: Int = byteOffset / FLOAT_SIZE
    public val MEM_SIZE: Int = size / FLOAT_SIZE
    public inline fun getByteIndex(index: Int): Int = byteOffset + index * FLOAT_SIZE
}
public actual inline val Float32Buffer.mem: MemBuffer get() = mbuffer
public actual inline val Float32Buffer.offset: Int get() = MEM_OFFSET
public actual inline val Float32Buffer.size: Int get() = MEM_SIZE
public actual operator fun Float32Buffer.get(index: Int): Float = mbuffer.getFloat(getByteIndex(index))
public actual operator fun Float32Buffer.set(index: Int, value: Float): Unit = mbuffer.setFloat(getByteIndex(index), value)

@PublishedApi
internal const val DOUBLE_SIZE: Int = 8

public actual class Float64Buffer(public val mbuffer: MemBuffer, public val byteOffset: Int, public val size: Int) {
    public val MEM_OFFSET: Int = byteOffset / DOUBLE_SIZE
    public val MEM_SIZE: Int = size / DOUBLE_SIZE
    public inline fun getByteIndex(index: Int): Int = byteOffset + index * DOUBLE_SIZE
}
public actual inline val Float64Buffer.mem: MemBuffer get() = mbuffer
public actual inline val Float64Buffer.offset: Int get() = MEM_OFFSET
public actual inline val Float64Buffer.size: Int get() = MEM_SIZE
public actual operator fun Float64Buffer.get(index: Int): Double = mbuffer.getDouble(getByteIndex(index))
public actual operator fun Float64Buffer.set(index: Int, value: Double): Unit = mbuffer.setDouble(getByteIndex(index), value)

@PublishedApi internal const val SHORT_SIZE_BYTES: Int = 2
@PublishedApi internal const val INT_SIZE_BYTES: Int = 4
@PublishedApi internal const val FLOAT_SIZE_BYTES: Int = 4
@PublishedApi internal const val DOUBLE_SIZE_BYTES: Int = 8

public actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
    src.data.copyInto(dst.data, dstPos, srcPos, srcPos + size)
}
public actual fun arraycopy(src: ByteArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
    src.copyInto(dst.data, dstPos, srcPos, srcPos + size)
}
public actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: ByteArray, dstPos: Int, size: Int): Unit {
    src.data.copyInto(dst, dstPos, srcPos, srcPos + size)
}
public actual fun arraycopy(src: ShortArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst.data.setShortAt((dstPos + n) * SHORT_SIZE_BYTES, src[srcPos + n])
}
public actual fun arraycopy(src: IntArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst.data.setIntAt((dstPos + n) * INT_SIZE_BYTES, src[srcPos + n])
}
public actual fun arraycopy(src: FloatArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst.data.setFloatAt((dstPos + n) * FLOAT_SIZE_BYTES, src[srcPos + n])
}
public actual fun arraycopy(src: DoubleArray, srcPos: Int, dst: MemBuffer, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst.data.setDoubleAt((dstPos + n) * DOUBLE_SIZE_BYTES, src[srcPos + n])
}
public actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: ShortArray, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst[dstPos + n] = src.data.getShortAt((srcPos + n) * SHORT_SIZE_BYTES)
}
public actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: IntArray, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst[dstPos + n] = src.data.getIntAt((srcPos + n) * INT_SIZE_BYTES)
}
public actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: FloatArray, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst[dstPos + n] = src.data.getFloatAt((srcPos + n) * FLOAT_SIZE_BYTES)
}
public actual fun arraycopy(src: MemBuffer, srcPos: Int, dst: DoubleArray, dstPos: Int, size: Int): Unit {
    for (n in 0 until size) dst[dstPos + n] = src.data.getDoubleAt((srcPos + n) * DOUBLE_SIZE_BYTES)
}

public actual abstract class Fast32Buffer(public val bb: ByteArray)
//actual /*inline*/ class Fast32Buffer(val bb: ByteArray)
public class Fast32BufferF(bb: ByteArray) : Fast32Buffer(bb)

public actual fun NewFast32Buffer(mem: MemBuffer): Fast32Buffer = Fast32BufferF(mem.data)

public actual val Fast32Buffer.length: Int get() = this.bb.size * 4
public actual inline fun Fast32Buffer.getF(index: Int): Float = this.bb.getFloatAt(index * 4)
public actual inline fun Fast32Buffer.setF(index: Int, value: Float) { this.bb.setFloatAt(index * 4, value) }
public actual inline fun Fast32Buffer.getI(index: Int): Int = this.bb.getIntAt(index * 4)
public actual inline fun Fast32Buffer.setI(index: Int, value: Int) { this.bb.setIntAt(index * 4, value) }
