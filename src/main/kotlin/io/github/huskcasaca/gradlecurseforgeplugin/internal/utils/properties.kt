package io.github.huskcasaca.gradlecurseforgeplugin.internal.utils

import org.gradle.api.provider.*

internal fun <T> Property<T>.finalizeAndGet(): T {
    finalizeValue()
    return get()
}

internal fun <T> Property<T>.finalizeAndGetOrNull(): T? {
    finalizeValue()
    return orNull
}