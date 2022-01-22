package org.bailey.scheme

const val msgAux = "Invalid use of auxiliary expression %s"
const val msgNotApplicable = "Invalid operator in application %s"
const val msgArgNE = "Not enough args for %s"
const val msgArgTM = "Too many args args for %s"
const val msgTypeError = "In %s. Invalid type, expected %s"
const val msgDivByZero = "Divide by zero"


fun compareArgCount(min: Int, count: Int, max: Int, name: String) {
    val msg = when {
        count < min -> msgArgNE
        max != -1 && count > max -> msgArgTM
        else -> null
    }
    require(msg == null) { msg!!.format(name) }
}