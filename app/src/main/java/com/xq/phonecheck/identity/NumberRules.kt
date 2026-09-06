package com.xq.phonecheck.identity

data class NumberRule(
    val prefix: String,
    val replacement: String
)

object NumberRules {

    fun parse(text: String): List<NumberRule> {
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") }
            .mapNotNull { line ->
                val separatorIndex = line.indexOf("=>")
                val rawPrefix = if (separatorIndex >= 0) line.take(separatorIndex) else line
                val rawReplacement = if (separatorIndex >= 0) line.substring(separatorIndex + 2) else ""
                val prefix = PhoneNumbers.normalize(rawPrefix)
                if (prefix.isEmpty()) null else NumberRule(prefix, rawReplacement.trim())
            }
            .distinctBy { it.prefix }
            .sortedByDescending { it.prefix.length }
            .toList()
    }

    fun apply(rawNumber: String, rulesText: String): String {
        val number = PhoneNumbers.normalize(rawNumber)
        if (number.isEmpty()) return number

        val rule = parse(rulesText).firstOrNull { number.startsWith(it.prefix) }
            ?: return number

        val remainder = number.removePrefix(rule.prefix)
        if (rule.replacement.isEmpty() && remainder.isEmpty()) return number
        return rule.replacement + remainder
    }
}
