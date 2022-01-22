package org.bailey.scheme

private fun parseAtom(token: String): ScmExp {
    fun toBoolOrNull(): ScmExp.Boolean? {
        return when (token) {
            "true" -> ScmExp.Boolean(true)
            "false" -> ScmExp.Boolean(false)
            else -> null
        }
    }

    // :)
    fun toNilOrNull(): ScmExp.Nil? {
        return if (token == "nil")
            ScmExp.Nil
        else
            null
    }

    fun toNumberOrNull(): ScmExp.Number? {
        val asNumber = token.toIntOrNull() ?: token.toFloatOrNull()
        return if (asNumber != null)
            ScmExp.Number(asNumber)
        else
            null
    }

    fun toStringOrNull(): ScmExp.String? {
        return if (token.startsWith('"')) {
            val strTokIter = token.removeSurrounding("\"").iterator()
            var r = ""
            while (strTokIter.hasNext()) {
                r += when (val c = strTokIter.nextChar()) {
                    '\\' -> {
                        when (val esc = strTokIter.nextChar()) {
                            'n' -> '\n'
                            '"' -> '\"'
                            '\\' -> '\\'
                            else -> throw Exception("Unknown escape sequence $esc")
                        }
                    }
                    else -> c
                }
            }
            ScmExp.String(r)
        } else
            null
    }

    return toNilOrNull() ?: toBoolOrNull() ?: toStringOrNull() ?: toNumberOrNull() ?: ScmExp.Symbol(
        token
    )
}

private fun parseList(iter: Iterator<String>): ScmExp.List {
    val acc = mutableListOf<ScmExp>()
    var balanced = false
    while (iter.hasNext()) {
        val token = iter.next()
        if (token == ")") {
            balanced = true
            break
        }
        acc.add(parseToken(token, iter))
    }
    if (!balanced)
        throw Exception("Unbalanced parenthesis")
    return ScmExp.List(acc)
}

private fun parseToken(iter: Iterator<String>): ScmExp =
    parseToken(iter.next(), iter)

private fun parseToken(token: String, iter: Iterator<String>): ScmExp =
    when (token) {
        "(" -> parseList(iter)
        ")" -> throw Exception("Unexpected ')'")
        "'" -> makeTaggedList(TAG_QUOTE, parseToken(iter))
        "`" -> makeTaggedList(TAG_QUASIQUOTE, parseToken(iter))
        "," -> makeTaggedList(TAG_UNQUOTE, parseToken(iter))
        else -> parseAtom(token)
    }

fun parse(iter: Iterator<String>): List<ScmExp> {
    val exprs = mutableListOf<ScmExp>()
    while (iter.hasNext())
        exprs.add(parseToken(iter))
    return exprs
}
