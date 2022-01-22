package org.bailey.scheme

fun evalAll(exps: List<ScmExp>, env: ScmEnv): ScmExp =
    exps.fold(ScmExp.Nil as ScmExp) { _, exp -> eval(exp, env) }

fun eval(exp: ScmExp, env: ScmEnv): ScmExp =
    when (exp) {
        is ScmExp.Atom, is ScmExp.Procedure -> exp
        is ScmExp.Symbol -> env.find(exp.value)[exp.value]
        is ScmExp.List -> when {
            isTaggedList(exp, TAG_QUOTE) -> {
                require(exp.size == 2) { msgAux.format(TAG_QUOTE) }
                exp[1]
            }
            isTaggedList(exp, TAG_SET) -> {
                require(exp.size == 3) { msgAux.format(TAG_DEFINE) }
                val (_, name, setValue) = exp
                require(name is ScmExp.Symbol) { "Can only set! a symbol" }
                env.find(name.value)[name.value] = eval(setValue, env)
                ScmExp.Nil
            }
            isTaggedList(exp, TAG_DEFINE) -> {
                require(exp.size == 3) { msgAux.format(TAG_DEFINE) }
                val (_, name, setValue) = exp
                require(name is ScmExp.Symbol) { "Can only define a symbol" }
                env[name.value] = eval(setValue, env)
                ScmExp.Nil
            }
            isTaggedList(exp, TAG_IF) -> {
                require(exp.size == 4) { msgAux.format(TAG_IF) }
                val (_, test, onTrue, onFalse) = exp
                val cond = eval(test, env)
                require(cond is ScmExp.Boolean) { "Condition in '$TAG_IF' should be a boolean value" }
                eval(
                    if (cond.value)
                        onTrue
                    else
                        onFalse,
                    env
                )
            }
            isTaggedList(exp, TAG_WHILE) -> {
                require(exp.size == 3) { msgAux.format(TAG_IF) }
                val (_, test, body) = exp
                val cond = {
                    val cond = eval(
                        test,
                        env
                    )
                    require(cond is ScmExp.Boolean) { "Condition in '$TAG_WHILE' should be a boolean value" }
                    cond.value
                }

                while (cond()) {
                    eval(body, env)
                }
                ScmExp.Nil
            }
            isTaggedList(exp, TAG_LAMBDA) -> {
                require(exp.size == 3) { msgAux.format(TAG_LAMBDA) }
                val (_, params, lambda_exp) = exp
                require(params is ScmExp.List) { "Lambda params should be in a list" }
                for (param in params) {
                    require(param is ScmExp.Symbol) { "Lambda params could only be symbols" }
                }
                ScmExp.CompoundProcedure(
                    lambda_exp,
                    params.map { (it as ScmExp.Symbol).value },
                    env
                )
            }
            isTaggedList(exp, TAG_BEGIN) -> {
                exp.drop(1).fold(ScmExp.Nil as ScmExp) { _, exp1 -> eval(exp1, env) }
            }
            else -> {
                val exprs = exp.map { eval(it, env) }
                val operator = exprs.first()
                require(operator is ScmExp.Procedure) { "Operator is not applicable (not a procedure)" }
                apply(operator, exprs.drop(1))
            }
        }
    }

fun apply(proc: ScmExp.Procedure, args: List<ScmExp>): ScmExp =
    when (proc) {
        is ScmExp.PrimitiveProcedure -> proc.value(args)
        is ScmExp.CompoundProcedure -> eval(proc.exp, ScmEnv(proc.params, args, proc.env))
    }