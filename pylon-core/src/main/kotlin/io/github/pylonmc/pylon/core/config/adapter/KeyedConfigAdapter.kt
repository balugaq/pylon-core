package io.github.pylonmc.pylon.core.config.adapter

import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Keyed
import org.bukkit.NamespacedKey
import org.bukkit.Registry

abstract class KeyedConfigAdapter<T : Keyed> : ConfigAdapter<T> {

    abstract fun fromKey(key: NamespacedKey): T

    final override fun convert(value: Any): T {
        return fromKey(ConfigAdapter.NAMESPACED_KEY.convert(value))
    }

    companion object {

        @JvmStatic
        fun <T : Keyed> fromGetter(clazz: Class<T>, fromKey: (NamespacedKey) -> T): ConfigAdapter<T> =
            object : KeyedConfigAdapter<T>() {
                override val type = clazz
                override fun fromKey(key: NamespacedKey): T = fromKey(key)
            }

        @JvmSynthetic
        inline fun <reified T : Keyed> fromGetter(crossinline fromKey: (NamespacedKey) -> T): ConfigAdapter<T> =
            object : KeyedConfigAdapter<T>() {
                override val type = T::class.java
                override fun fromKey(key: NamespacedKey): T = fromKey(key)
            }

        @JvmStatic
        fun <T : Keyed> fromRegistry(clazz: Class<T>, registry: PylonRegistry<T>): ConfigAdapter<T> =
            fromGetter(clazz, registry::getOrThrow)

        @JvmSynthetic
        inline fun <reified T : Keyed> fromRegistry(registry: PylonRegistry<T>): ConfigAdapter<T> =
            fromGetter(registry::getOrThrow)

        @JvmStatic
        fun <T : Keyed> fromRegistry(clazz: Class<T>, registry: Registry<T>): ConfigAdapter<T> =
            fromGetter(clazz, registry::getOrThrow)

        @JvmSynthetic
        inline fun <reified T : Keyed> fromRegistry(registry: Registry<T>): ConfigAdapter<T> =
            fromGetter(registry::getOrThrow)
    }
}