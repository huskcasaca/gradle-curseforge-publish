package io.github.huskcasaca.gradlecurseforgeplugin

public data class Changelog(
    val content: String,
    val type: ChangelogType
)

public enum class ChangelogType {
    HTML,
    MARKDOWN,
    TEXT
}