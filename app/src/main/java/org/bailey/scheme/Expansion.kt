package org.bailey.scheme

typealias MacroMap = MutableMap<String, ScmExp.Procedure>

// for java
fun expandAll(exp: MutableList<ScmExp>): List<ScmExp> =
    expandAll(exp, mutableMapOf())

fun expandAll(exp: MutableList<ScmExp>, macroMap: MacroMap): List<ScmExp> =
    exp.map { expand(it, macroMap) }

fun expand(
    exp: ScmExp,
    macroMap: MacroMap,
    isTop: Boolean = true,
): ScmExp {
    require(!(exp is ScmExp.List && exp.isEmpty()))
    return when (exp) {
        is ScmExp.List -> when {
            // remember that exp is the whole expression containing both operator and operands
            // (+ 1 2 3 4) ;; argCount = 5
            isTaggedList(exp, TAG_SET) -> {
                val argCount = exp.size
                compareArgCount(3, argCount, 3, TAG_SET)
                require(exp[1] is ScmExp.Symbol) { msgAux.format(TAG_SET) }
                ScmExp.List(exp.slice(0..1) + listOf(expand(exp[2], macroMap, false)))
            }
            isTaggedList(exp, TAG_IF) -> {
                val argCount = exp.size
                compareArgCount(3, argCount, 4, TAG_IF)
                ScmExp.List(
                    exp.first(),
                    expand(exp[1], macroMap, false),
                    expand(exp[2], macroMap, false),
                    if (argCount == 3)
                        ScmExp.Nil
                    else
                        expand(exp[3], macroMap, false)
                )
            }
            isTaggedList(exp, TAG_LAMBDA) -> {
                compareArgCount(3, exp.size, -1, TAG_LAMBDA)
                val body = exp.slice(2 until exp.size).map { expand(it, macroMap, false) }
                ScmExp.List(
                    *exp.slice(0..1).toTypedArray(),
                    when (body.size) {
                        1 -> body.first()
                        else -> ScmExp.List(
                            ScmExp.Symbol(TAG_BEGIN),
                            *body.toTypedArray()
                        )
                    }
                )
            }
            isTaggedList(exp, TAG_DEFINE) -> {
                compareArgCount(3, exp.size, -1, TAG_DEFINE)
                val name = exp[1]
                when {
                    name is ScmExp.Symbol -> ScmExp.List(
                        exp.slice(0..1) + listOf(
                            expand(
                                exp[2],
                                macroMap,
                                false
                            )
                        )
                    )
                    name is ScmExp.List && name.size >= 1 -> ScmExp.List(
                        exp[0],
                        name[0],
                        expand(
                            ScmExp.List(
                                ScmExp.Symbol(TAG_LAMBDA),
                                ScmExp.List(name.drop(1)), // args
                                *exp.drop(2).map { expand(it, macroMap, false) }
                                    .toTypedArray() // body
                            ),
                            macroMap,
                            false
                        )
                    )
                    else -> throw Exception(msgAux.format(TAG_DEFINE))
                }
            }
            isTaggedList(exp, TAG_LET) -> {
                compareArgCount(3, exp.size, -1, TAG_LET)
                val names = exp[1]
                require(names is ScmExp.List && names.all { it is ScmExp.List }) {
                    msgAux.format(
                        TAG_LET
                    )
                }
                val body = exp.drop(2)
                ScmExp.List(
                    ScmExp.Symbol(TAG_BEGIN),
                    *names.map {
                        val (name, value) = it as ScmExp.List
                        require(name is ScmExp.Symbol) { "Names in '$TAG_LET' can only be symbols" }
                        ScmExp.List(
                            ScmExp.Symbol(TAG_DEFINE),
                            name,
                            expand(value, macroMap, false)
                        )
                    }.toTypedArray(),
                    *body.map { expand(it, macroMap, false) }.toTypedArray()
                )
            }
            isTaggedList(exp, TAG_DEFMACRO) -> {
                compareArgCount(3, exp.size, -1, TAG_DEFMACRO)
                val nameAndArgs = exp[1]
                require(nameAndArgs is ScmExp.List && nameAndArgs.size > 0 && nameAndArgs.all { it is ScmExp.Symbol }) {
                    msgAux.format(TAG_DEFMACRO)
                }
                require(isTop) { "Macro definition is only allowed at the top level" }
                val macroValue = expand(
                    ScmExp.List(
                        ScmExp.Symbol(TAG_LAMBDA),
                        ScmExp.List(*nameAndArgs.drop(1).toTypedArray()), // macro arguments
                        *exp.drop(2).toTypedArray() // macro body
                    ),
                    macroMap, false
                )
                val procedure = eval(macroValue, ScmEnv().apply { addPrimitives() })
                require(procedure is ScmExp.Procedure) { msgAux.format(TAG_DEFMACRO) }
                macroMap[(nameAndArgs.first() as ScmExp.Symbol).value] = procedure
                ScmExp.Nil
            }
            isTaggedList(exp, TAG_QUOTE) -> exp
            isTaggedList(exp, TAG_QUASIQUOTE) -> {
                require(exp.size == 2) { msgAux.format(TAG_QUASIQUOTE) }
                expandQuasiquote(exp[1], macroMap)
            }
            isTaggedList(exp, TAG_BEGIN) -> {
                ScmExp.List(
                    exp.first(),
                    *exp.drop(1).map { expand(it, macroMap, isTop) }.toTypedArray()
                )
            }
            exp.isNotEmpty() && exp.first() is ScmExp.Symbol && macroMap.containsKey((exp.first() as ScmExp.Symbol).value) -> {
                expand(
                    apply(macroMap[(exp.first() as ScmExp.Symbol).value]!!, exp.drop(1)),
                    macroMap,
                    false
                )
            }
            else -> ScmExp.List(exp.map { expand(it, macroMap, false) })
        }
        else -> exp
    }
}

fun expandQuasiquote(exp: ScmExp, macroMap: MutableMap<String, ScmExp.Procedure>): ScmExp {
    if (exp is ScmExp.List) {
        if (exp.size == 2) {
            val (a, b) = exp
            if (a is ScmExp.Symbol && a.value == TAG_UNQUOTE)
                return expand(b, macroMap, false)
        }
        return ScmExp.List(
            ScmExp.Symbol(TAG_LIST),
            *exp.map { expandQuasiquote(it, macroMap) }.toTypedArray()
        )
    } else {
        return makeTaggedList(TAG_QUOTE, exp)
    }
}
