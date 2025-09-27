package io.github.pylonmc.pylon.core.config.adapter

import com.destroystokyo.paper.MaterialTags
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper
import io.github.pylonmc.pylon.core.item.ItemTypeWrapper.Companion.toItemTypeTag
import io.github.pylonmc.pylon.core.registry.PylonRegistry
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import java.lang.reflect.Modifier

object ItemTagConfigAdapter : ConfigAdapter<Tag<ItemTypeWrapper>> {
    override val type = Tag::class.java

    override fun convert(value: Any): Tag<ItemTypeWrapper> {
        val string = ConfigAdapter.STRING.convert(value)
        if (!string.startsWith("#")) {
            throw IllegalArgumentException("Item tag must start with '#': $value")
        }
        val tagKey = NamespacedKey.fromString(string.drop(1)) ?: throw IllegalArgumentException("Invalid tag: $value")
        val tag = Bukkit.getTag("items", tagKey, Material::class.java)
        if (tag != null) {
            return tag.toItemTypeTag()
        }

        val paperTag = paperRegistry[tagKey]
        if (paperTag != null) {
            return paperTag
        }

        val pylonTag = PylonRegistry.ITEM_TAGS[tagKey]
        if (pylonTag != null) {
            return pylonTag
        }
        throw IllegalArgumentException("Item tag not found: $value")
    }

    private val paperRegistry = object : HashMap<NamespacedKey, Tag<ItemTypeWrapper>>() {
        init {
            for (entry in MaterialTags::class.java.declaredFields) {
                if (entry.modifiers and Modifier.STATIC == 0) continue

                val value = entry.get(null) ?: continue
                if (value !is Tag<*>) continue

                val content = value.values.first()
                if (content !is Material) continue

                @Suppress("UNCHECKED_CAST")
                val realTag = value as Tag<Material>

                this[realTag.key] = realTag.toItemTypeTag()
            }
        }

        override operator fun get(key: NamespacedKey): Tag<ItemTypeWrapper>? {
            if (key.namespace != "paper") return null
            return super[key]
        }
    }
}