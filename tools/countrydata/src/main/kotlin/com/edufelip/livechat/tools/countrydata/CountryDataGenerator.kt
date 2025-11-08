package com.edufelip.livechat.tools.countrydata

import com.google.i18n.phonenumbers.PhoneNumberUtil
import java.io.File
import java.text.Collator
import java.util.Locale

private data class CountryRecord(
    val isoCode: String,
    val name: String,
    val dialCode: String,
    val flag: String,
)

fun main() {
    val util = PhoneNumberUtil.getInstance()
    val locales = Locale.getISOCountries().toList()

    val records =
        locales.mapNotNull { isoCode ->
            val uppercaseIso = isoCode.uppercase(Locale.ROOT)
            val dial = util.getCountryCodeForRegion(uppercaseIso)
            if (dial == 0) return@mapNotNull null

            val locale = Locale("", uppercaseIso)
            val displayName = locale.getDisplayCountry(Locale.ENGLISH).ifBlank { uppercaseIso }
            CountryRecord(
                isoCode = uppercaseIso,
                name = displayName,
                dialCode = "+$dial",
                flag = uppercaseIso.toFlagEmoji(),
            )
        }.distinctBy { it.isoCode }
            .sortedWith(compareBy(Collator.getInstance(Locale.ENGLISH), CountryRecord::name))

    val outputDir =
        File(
            projectRoot(),
            "livechatApp/src/commonMain/kotlin/com/edufelip/livechat/ui/features/onboarding/generated",
        )
    outputDir.mkdirs()
    val outputFile = outputDir.resolve("CountryDefaults.generated.kt")
    outputFile.writeText(records.toKotlinSource())

    println("Generated ${records.size} country entries at ${outputFile.canonicalPath}")
}

private fun projectRoot(): String {
    var dir = File(System.getProperty("user.dir"))
    while (dir.parentFile != null && dir.listFiles()?.any { it.name == "settings.gradle.kts" } == false) {
        dir = dir.parentFile
    }
    return dir.canonicalPath
}

private fun String.toFlagEmoji(): String {
    if (length != 2) return this
    val first = Character.codePointAt(this, 0) - 0x41 + 0x1F1E6
    val second = Character.codePointAt(this, 1) - 0x41 + 0x1F1E6
    return String(intArrayOf(first, second), 0, 2)
}

private fun List<CountryRecord>.toKotlinSource(): String {
    val builder = StringBuilder()
    builder.appendLine("package com.edufelip.livechat.ui.features.onboarding.generated")
    builder.appendLine("import com.edufelip.livechat.ui.features.onboarding.CountryOption")
    builder.appendLine()
    builder.appendLine("internal val GeneratedCountryOptions = listOf(")
    forEachIndexed { index, record ->
        builder.append("    CountryOption(")
        builder.append("isoCode = \"${record.isoCode}\", ")
        builder.append("name = \"${record.name.escapeForKotlin()}\", ")
        builder.append("dialCode = \"${record.dialCode}\", ")
        builder.append("flag = \"${record.flag.escapeForKotlin()}\"")
        builder.append(")")
        if (index != lastIndex) builder.append(",")
        builder.appendLine()
    }
    builder.appendLine(")")
    return builder.toString()
}

private fun String.escapeForKotlin(): String =
    buildString(length) {
        for (ch in this@escapeForKotlin) {
            when (ch) {
                '\\' -> append("\\\\")
                '\"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '$' -> append("\\$")
                else -> append(ch)
            }
        }
    }
