package org.bailey.scheme

fun main() {
    val env = ScmEnv()
    val macroMap = mutableMapOf<String, ScmExp.Procedure>()
    env.addPrimitives()
    while (true) {
        try {
            print("scm> ")
            val iter = tokenize(readLine()!!).iterator()
            val parsed = parse(iter)
            println("PARSED: $parsed")
            val expanded = parsed.map { expand(it, macroMap) }[0]
            println("AFTER EXPANSION: $expanded")
            println("EVAL RESULT: ${eval(expanded, env)}")
        } catch (e: Exception) {
            println("====================")
            println("ERROR: $e")
            println("STACKTRACE: ${e.stackTraceToString()}")
        }
    }
}