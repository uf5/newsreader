package org.bailey.scheme

private val tokRegex = Regex("""\s*([()',`~^#]|"(?:\\.|[^\\"])*"?|;.*|[^\s()',`~^#]*)""")


fun tokenize(script: String): Sequence<String> {
    return tokRegex.findAll(script)
        .map { it.groupValues[1] }
        .filter { !(it == "" || it.startsWith(';')) }
}
