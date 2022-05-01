package com.soywiz.kds

interface IMultimap<K, V> {

    fun asMap(): Map<K, Collection<V>>

    fun clear()

    fun containsEntry(key: K, value: V): Boolean

    fun containsKey(key: K)

    fun containsValue(value: V)

    // Returns a view collection of all key-value pairs contained
    // in this multimap.
    //
    // For example, if we have a multimap of:
    // 1 -> [11, 12, 13]
    // 2 -> [21, 22]
    // This will return a collection of:
    // [<1, 11>, <1, 12>, <1, 13>, <2, 21>, <2, 22>]
    fun entries(): Collection<Pair<K, V>>

    fun forEach(fn: (key: K, value: V) -> Unit) {
        entries().forEach {
            fn(it.first, it.second)
        }
    }

    fun get(key: K): Collection<V>

    fun isEmpty(): Boolean

    fun keySet(): Set<K>

    fun put(key: K, value: V): Boolean

    fun putAll(key: K, values: Iterable<V>)
}
