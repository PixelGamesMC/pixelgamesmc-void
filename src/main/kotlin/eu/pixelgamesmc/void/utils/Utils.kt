package eu.pixelgamesmc.void.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

val GSON: Gson = GsonBuilder().serializeNulls().setPrettyPrinting().create()

val PREFIX = Component.text("ITEM", NamedTextColor.AQUA, TextDecoration.BOLD)
    .append(Component.text("FIESTA", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD))
    .append(Component.text(" » ", NamedTextColor.DARK_GRAY))