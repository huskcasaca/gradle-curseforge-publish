package io.github.huskcasaca.gradlecurseforgeplugin


// semvar with snapshot support without buildMetadata
public enum class MinecraftVersion(
    public val major: Int,
    public val minor: Int,
    public val patch: Int,
    public val snapshot: Boolean = false
) {

    VERSION_1_12_SNAPSHOT(1, 12, 0, true),
    VERSION_1_12         (1, 12, 0),
    VERSION_1_12_1       (1, 12, 1),
    VERSION_1_12_2       (1, 12, 2),

    VERSION_1_13_SNAPSHOT(1, 13, 0, true),
    VERSION_1_13         (1, 13, 0),
    VERSION_1_13_1       (1, 13, 1),
    VERSION_1_13_2       (1, 13, 2),

    VERSION_1_14_SNAPSHOT(1, 14, 0, true),
    VERSION_1_14         (1, 14, 0),
    VERSION_1_14_1       (1, 14, 1),
    VERSION_1_14_2       (1, 14, 2),
    VERSION_1_14_3       (1, 14, 3),
    VERSION_1_14_4       (1, 14, 4),

    VERSION_1_15_SNAPSHOT(1, 15, 0, true),
    VERSION_1_15         (1, 15, 0),
    VERSION_1_15_1       (1, 15, 1),
    VERSION_1_15_2       (1, 15, 2),

    VERSION_1_16_SNAPSHOT(1, 16, 0, true),
    VERSION_1_16         (1, 16, 0),
    VERSION_1_16_1       (1, 16, 1),
    VERSION_1_16_2       (1, 16, 2),
    VERSION_1_16_3       (1, 16, 3),
    VERSION_1_16_4       (1, 16, 4),
    VERSION_1_16_5       (1, 16, 5),

    VERSION_1_17_SNAPSHOT(1, 17, 0, true),
    VERSION_1_17         (1, 17, 0),
    VERSION_1_17_1       (1, 17, 1),

    VERSION_1_18_SNAPSHOT(1, 18, 0, true),
    VERSION_1_18         (1, 18, 0),
    VERSION_1_18_1       (1, 18, 1),
    VERSION_1_18_2       (1, 18, 2),

    VERSION_1_19_SNAPSHOT(1, 19, 0, true),
    VERSION_1_19         (1, 19, 0),
    VERSION_1_19_1       (1, 19, 1),
    VERSION_1_19_2       (1, 19, 2);

    override fun toString(): String = buildString {
        append("$major.$minor")
        if (patch != 0) {
            append(".$patch")
        }
        if (snapshot) {
            append("-snapshot")
        }
    }

}
