// "Create enum constant 'A'" "true"
// ERROR: No value passed for parameter n
package p

fun foo() = X.A

enum class X(n: Int) {

    A : X()

}