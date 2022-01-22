package org.bailey.scheme

private typealias KtNumber = Number
private typealias KtString = String
private typealias KtBoolean = Boolean
private typealias KtMutList = MutableList<ScmExp>
private typealias KtList = List<ScmExp>

sealed interface ScmExp {
    sealed interface Atom

    data class Number(val value: KtNumber) : ScmExp, Atom {
        override fun toString(): KtString = value.toString()
    }

    data class String(val value: KtString) : ScmExp, Atom {
        override fun toString(): KtString = value
    }

    data class Boolean(val value: KtBoolean) : ScmExp, Atom {
        override fun toString(): KtString = if (value) "true" else "false"
    }

    object Nil : Atom, ScmExp {
        override fun toString(): KtString = "nil"
    }

    data class Symbol(val value: KtString) : ScmExp {
        override fun toString(): KtString = value
    }

    class List(values: Iterable<ScmExp>) : KtMutList by mutableListOf(), ScmExp {
        init {
            this.addAll(values)
        }

        constructor (vararg values: ScmExp) : this(values.toList())

        override fun toString(): KtString = "(${this.joinToString(separator = " ")})"

        override fun equals(other: Any?): KtBoolean {
            return when (other) {
                is List -> {
                    if (this.size != other.size)
                        return false
                    for (i in this.indices) {
                        if (this[i] != other[i])
                            return false
                    }
                    true
                }
                else -> false
            }
        }

        override fun hashCode(): Int = javaClass.hashCode()
    }

    sealed interface Procedure
    data class PrimitiveProcedure(val value: (ops: KtList) -> ScmExp) : ScmExp, Procedure {
        override fun toString(): KtString = "#PRIMITIVE-PROCEDURE"
    }

    data class CompoundProcedure(
        val exp: ScmExp,
        val params: kotlin.collections.List<kotlin.String>,
        val env: ScmEnv
    ) : ScmExp, Procedure {
        override fun toString(): KtString = "#COMPOUND-PROCEDURE"
    }
}

fun isTaggedList(list: ScmExp.List, tag: String): Boolean {
    if (list.isEmpty())
        return false
    val first = list.first()
    return first is ScmExp.Symbol && first.value == tag
}

fun makeTaggedList(tag: String, exp: ScmExp): ScmExp =
    ScmExp.List(ScmExp.Symbol(tag), exp)

const val TAG_QUOTE = "quote"
const val TAG_QUASIQUOTE = "quasiquote"
const val TAG_UNQUOTE = "unquote"
const val TAG_LIST = "list"
const val TAG_DEFINE = "define"
const val TAG_SET = "set!"
const val TAG_WHILE = "while"
const val TAG_LET = "let"
const val TAG_LAMBDA = "lambda"
const val TAG_BEGIN = "begin"
const val TAG_IF = "if"
const val TAG_DEFMACRO = "defmacro"