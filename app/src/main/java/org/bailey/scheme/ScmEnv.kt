package org.bailey.scheme

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.bailey.newsreader.channels.LispLogger

class ScmEnv {
    private val outer: ScmEnv?
    private val dict = mutableMapOf<String, ScmExp>()

    constructor(outer: ScmEnv? = null) {
        this.outer = outer
    }

    constructor(params: List<String>, args: List<ScmExp>, outer: ScmEnv? = null) {
        this.outer = outer
        compareArgCount(
            params.size,
            args.size,
            params.size,
            "apply, required ${params.size}, got ${args.size}"
        )
        for (n in params.indices)
            dict[params[n]] = args[n]
    }

    fun find(name: String): ScmEnv =
        if (dict.containsKey(name)) this
        else outer?.find(name) ?: throw NoSuchElementException(name)

    operator fun get(name: String): ScmExp = dict[name] ?: throw NoSuchElementException(name)

    operator fun set(name: String, value: ScmExp) {
        dict[name] = value
    }

    private inline fun <reified T> typeHelper(args: List<ScmExp>, name: String): ScmExp {
        compareArgCount(1, args.size, 1, name)
        val arg = args.first()
        return ScmExp.Boolean(arg is T)
    }

    fun addPrimitives() {
        fun sumHelper(
            start: Number,
            args: List<ScmExp>,
            opFloat: (Float, Float) -> Number,
            opInt: (Int, Int) -> Number
        ): ScmExp {
            var result: Number = start
            var isFloat = false
            for (n in args) {
                require(n is ScmExp.Number) {
                    msgTypeError.format(
                        "arithmetics operator",
                        "Number"
                    )
                }
                val v = n.value
                if (!isFloat && v is Float)
                    isFloat = true
                result =
                    if (isFloat)
                        opFloat(result.toFloat(), v as Float)
                    else
                        opInt(result.toInt(), v as Int)
            }
            return ScmExp.Number(result)
        }

        fun car(arg: ScmExp): ScmExp {
            require(arg is ScmExp.List) { msgTypeError.format("car", TAG_LIST) }
            require(arg.size > 0) { "List is empty" }
            return arg.first()
        }

        fun cdr(arg: ScmExp): ScmExp {
            require(arg is ScmExp.List) { msgTypeError.format("cdr", TAG_LIST) }
            require(arg.size > 0) { "List is empty" }
            return ScmExp.List(arg.drop(1))
        }

        fun combHelper(list: ScmExp, vararg procs: (ScmExp) -> ScmExp): ScmExp {
            return procs.first()(
                (if (procs.size == 1)
                    list
                else
                    combHelper(list as ScmExp.List, *procs.drop(1).toTypedArray()))
            )
        }

        dict += mapOf(
            "c-end" to ScmExp.String("\n"),
            "c-tab" to ScmExp.String("\t"),
            "c-quote" to ScmExp.String("\""),
            "c-backslash" to ScmExp.String("\\"),

            "and" to ScmExp.PrimitiveProcedure { args ->
                for (exp in args)
                    if (exp != ScmExp.Boolean(true))
                        return@PrimitiveProcedure ScmExp.Boolean(false)
                ScmExp.Boolean(true)
            },
            "or" to ScmExp.PrimitiveProcedure { args ->
                for (exp in args)
                    if (exp == ScmExp.Boolean(true))
                        return@PrimitiveProcedure ScmExp.Boolean(true)
                ScmExp.Boolean(false)
            },
            "map" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, -1, "map")
                val func = args.first()
                val lists = args.drop(1)
                require(func is ScmExp.Procedure) { msgTypeError.format("map", "procedure") }
                require(lists.all { it is ScmExp.List }) { msgTypeError.format("map", "list") }
                val minSize = lists.minOf { (it as ScmExp.List).size }
                ScmExp.List((0 until minSize).map { i ->
                    apply(func as ScmExp.Procedure, lists.map { list -> (list as ScmExp.List)[i] })
                })
            },
            "for-each" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, -1, "for-each")
                val func = args.first()
                val lists = args.drop(1)
                require(func is ScmExp.Procedure) { msgTypeError.format("for-each", "procedure") }
                require(lists.all { it is ScmExp.List }) { msgTypeError.format("for-each", "list") }
                val minSize = lists.minOf { (it as ScmExp.List).size }
                for (i in (0 until minSize)) {
                    apply(func as ScmExp.Procedure, lists.map { list -> (list as ScmExp.List)[i] })
                }
                ScmExp.Nil
            },
            "fold" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(3, args.size, -1, "fold")
                var acc = args.first()
                val func = args[1]
                val lists = args.drop(2)
                require(func is ScmExp.Procedure) { msgTypeError.format("fold", "procedure") }
                require(lists.all { it is ScmExp.List }) { msgTypeError.format("fold", "list") }
                val minSize = lists.minOf { (it as ScmExp.List).size }
                (0 until minSize).forEach { i ->
                    acc = apply(
                        func as ScmExp.Procedure,
                        listOf(acc, *lists.map { list -> (list as ScmExp.List)[i] }.toTypedArray())
                    )
                }
                acc
            },
            "=" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, -1, "=")
                val first = args.first()
                for (exp in args.drop(1))
                    if (exp != first)
                        return@PrimitiveProcedure ScmExp.Boolean(false)
                ScmExp.Boolean(true)
            },
            "atom?" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, 1, "atom?")
                val first = args.first()
                ScmExp.Boolean(first is ScmExp.Atom)
            },
            "empty?" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, 1, "empty?")
                val list = args.first()
                ScmExp.Boolean(list is ScmExp.List && list.isEmpty())
            },
            "nil?" to ScmExp.PrimitiveProcedure { args ->
                typeHelper<ScmExp.Nil>(args, "nil?")
            },
            "apply" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "apply")
                val (operator, argsList) = args
                require(operator is ScmExp.Procedure) { msgNotApplicable.format(operator) }
                require(argsList is ScmExp.List) { "Improper args list" }
                apply(operator, argsList)
            },
            "eval" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, 1, "eval")
                val first = eval(args.first(), this)
                first
            },
            "log" to ScmExp.PrimitiveProcedure { args ->
                LispLogger.log(LispLogger.LogTag.PRINT, args.joinToString(separator = " "))
                ScmExp.Nil
            },
            "+" to ScmExp.PrimitiveProcedure { args ->
                sumHelper(
                    0,
                    args,
                    { acc, x ->
                        acc + x
                    },
                    { acc, x ->
                        acc + x
                    }
                )
            },
            "*" to ScmExp.PrimitiveProcedure { args ->
                sumHelper(
                    1,
                    args,
                    { acc, x ->
                        acc * x
                    },
                    { acc, x ->
                        acc * x
                    }
                )
            },
            "-" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, -1, "-")
                val first = args.first()
                require(first is ScmExp.Number) { msgTypeError.format("-", "Number") }
                val value = first.value
                when (args.size) {
                    1 -> {
                        val isFloat = value is Float
                        ScmExp.Number(
                            if (isFloat)
                                0 - (value as Float)
                            else
                                0 - (value as Int)
                        )
                    }
                    else -> {
                        sumHelper(
                            value,
                            args.drop(1),
                            { acc, x ->
                                acc - x
                            },
                            { acc, x ->
                                acc - x
                            }
                        )
                    }
                }
            },
            "/" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, -1, "/")
                val first = args.first()
                require(first is ScmExp.Number) { msgTypeError.format("/", "Number") }
                val value = first.value.toFloat()
                when (args.size) {
                    1 -> {
                        require(value != 0f) { msgDivByZero }
                        ScmExp.Number(1 / value)
                    }
                    else -> {
                        var result: Float = value
                        for (n in args.drop(1)) {
                            require(n is ScmExp.Number) { msgTypeError.format("/", "Number") }
                            val v = n.value.toFloat()
                            require(v != 0f) { msgDivByZero }
                            result /= v
                        }
                        ScmExp.Number(result)
                    }
                }
            },

            ">" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, ">")
                val (a, b) = args
                require(a is ScmExp.Number && b is ScmExp.Number) {
                    msgTypeError.format(
                        ">",
                        "Number"
                    )
                }
                ScmExp.Boolean(a.value.toFloat() > b.value.toFloat())
            },
            "<" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "<")
                val (a, b) = args
                require(a is ScmExp.Number && b is ScmExp.Number) {
                    msgTypeError.format(
                        "<",
                        "Number"
                    )
                }
                ScmExp.Boolean(a.value.toFloat() < b.value.toFloat())
            },
            ">=" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, ">=")
                val (a, b) = args
                require(a is ScmExp.Number && b is ScmExp.Number) {
                    msgTypeError.format(
                        ">=",
                        "Number"
                    )
                }
                ScmExp.Boolean(a.value.toFloat() >= b.value.toFloat())
            },
            "<=" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "<=")
                val (a, b) = args
                require(a is ScmExp.Number && b is ScmExp.Number) {
                    msgTypeError.format(
                        "<=",
                        "Number"
                    )
                }
                ScmExp.Boolean(a.value.toFloat() <= b.value.toFloat())
            },

            "number?" to ScmExp.PrimitiveProcedure { args ->
                typeHelper<ScmExp.Number>(args, "number?")
            },
            "boolean?" to ScmExp.PrimitiveProcedure { args ->
                typeHelper<ScmExp.Boolean>(args, "boolean?")
            },
            "string?" to ScmExp.PrimitiveProcedure { args ->
                typeHelper<ScmExp.String>(args, "string?")
            },
            "symbol?" to ScmExp.PrimitiveProcedure { args ->
                typeHelper<ScmExp.Symbol>(args, "symbol?")
            },
            "list?" to ScmExp.PrimitiveProcedure { args ->
                typeHelper<ScmExp.List>(args, "list?")
            },

            "to-string" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, 1, "to-string")
                val exp = args.first()
                if (exp !is ScmExp.String)
                    ScmExp.String(exp.toString())
                else
                    exp
            },

            "append" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, -1, "append")
                require(args.all { it is ScmExp.List })
                ScmExp.List(*args.flatMap { it as ScmExp.List }.toTypedArray())
            },
            "zip" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, -1, "append")
                require(args.all { it is ScmExp.List })
                val size = (args.first() as ScmExp.List).size
                require(args.drop(1).all { (it as ScmExp.List).size == size })
                val r = mutableListOf<ScmExp>()
                for (i in (0 until size)) {
                    r.add(ScmExp.List(args.map { (it as ScmExp.List)[i] }))
                }
                ScmExp.List(r)
            },
            "pop" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "pop")
                val (list, index) = args
                require(list is ScmExp.List)
                require(index is ScmExp.Number && index.value is Int)
                list.removeAt(index.value)
            },
            "string-append" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, -1, "string-append")
                require(args.all { it is ScmExp.String }) {
                    msgTypeError.format(
                        "string-append",
                        "String"
                    )
                }
                ScmExp.String(args.fold("") { acc, it -> acc + (it as ScmExp.String).value })
            },
            TAG_LIST to ScmExp.PrimitiveProcedure { args ->
                ScmExp.List(*args.toTypedArray())
            },
            "length" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, 1, "length")
                val first = args.first()
                require(first is ScmExp.List)
                ScmExp.Number(first.size)
            },
            "reverse" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(1, args.size, 1, "length")
                val first = args.first()
                require(first is ScmExp.List)
                ScmExp.List(first.reversed())
            },

            "request" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 3, "request")
                val type = args[0]
                val url = args[1]
                require(
                    type is ScmExp.String && type.value in listOf(
                        "GET",
                        "POST",
                    )
                ) { "Unknown action $type" }
                require(url is ScmExp.String)
                val client = OkHttpClient()
                val request = Request.Builder()
                    .apply {
                        this.url(url.value)
                        when (type.value) {
                            "GET" -> {

                            }
                            "POST" -> {
                                compareArgCount(3, args.size, 3, "request")
                                val body = args[2]
                                require(body is ScmExp.List && body.size == 2)
                                val (dataType, data) = body
                                require(dataType is ScmExp.String)
                                this.post(
                                    data.toString()
                                        .toRequestBody(dataType.value.toMediaTypeOrNull())
                                )
                            }
                        }
                    }
                    .build()
                val answer = client.newCall(request).execute().body
                if (answer == null)
                    ScmExp.Nil
                else
                    ScmExp.String(answer.string())
            },

            "rm-prefix" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "rm-prefix")
                val (str, prefix) = args
                require(str is ScmExp.String)
                require(prefix is ScmExp.String)
                ScmExp.String(str.value.removePrefix(prefix.value))
            },
            "rm-suffix" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "rm-suffix")
                val (str, prefix) = args
                require(str is ScmExp.String)
                require(prefix is ScmExp.String)
                ScmExp.String(str.value.removeSuffix(prefix.value))
            },

            "regex-find-values" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "regex-find")
                val (string, regex) = args
                require(string is ScmExp.String)
                require(regex is ScmExp.String)

                val values = regex.value.toRegex()
                    .findAll(string.value)
                    .map {
                        ScmExp.String(it.value)
                    }
                    .asIterable()

                ScmExp.List(values)
            },
            "regex-find-groups" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "regex-find")
                val (string, regex) = args
                require(string is ScmExp.String)
                require(regex is ScmExp.String)

                val values = regex.value.toRegex()
                    .findAll(string.value)
                    .map { match ->
                        ScmExp.List(
                            match.groups.map { group ->
                                ScmExp.String(group!!.value)
                            }
                        )
                    }
                    .asIterable()

                ScmExp.List(values)
            },

            "nth" to ScmExp.PrimitiveProcedure { args ->
                compareArgCount(2, args.size, 2, "nth")
                val list = args[0]
                require(list is ScmExp.List)
                val n = args[1]
                require(n is ScmExp.Number)
                require(n.value is Int)
                list[n.value]
            },

            "car" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car)
            },
            "cdr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr)
            },

            "caar" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car, ::car)
            },
            "cadr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car, ::cdr)
            },
            "cdar" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr, ::car)
            },
            "cddr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr, ::cdr)
            },

            "caaar" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car, ::car, ::car)
            },
            "caadr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car, ::car, ::cdr)
            },
            "cadar" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car, ::cdr, ::car)
            },
            "caddr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::car, ::cdr, ::cdr)
            },
            "cdaar" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr, ::car, ::car)
            },
            "cdadr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr, ::car, ::cdr)
            },
            "cddar" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr, ::cdr, ::car)
            },
            "cdddr" to ScmExp.PrimitiveProcedure { args ->
                combHelper(args.first(), ::cdr, ::cdr, ::cdr)
            }
        )
    }
}
