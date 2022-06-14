package graphics

import kotlin.math.pow

class Vector(pos: Pair<Double, Double>) {
    companion object {
        val ZERO = Vector(0.0, 0.0)
        val MAX = Vector(800.0, 600.0)
        fun add(v1: Vector, v2: Vector) : Vector {
            return Vector(v1.x + v2.x, v1.y + v2.y)
        }
        fun multiply(s: Double, v: Vector) : Vector {
            return Vector(s * v.x, s * v.y)
        }
    }
    var x = pos.first
    var y = pos.second

    constructor(x: Double, y: Double) : this(Pair(x, y))

    fun add(v: Vector) {
        x += v.x
        y += v.y
    }

    fun reverseX(v: Vector){
        x = -1 * v.x
    }

    fun reverseY(v: Vector){
        y = -1 * v.y 
    }

    fun multiply(s: Double) {
        x *= s
        y *= s
    }

    fun magnitude() : Double {
        return Math.sqrt(x.pow(2) + y.pow(2))
    }

    fun normalize() {
        val mag = magnitude()
        x /= mag
        y /= mag
    }

    fun clone() : Vector {
        return Vector(x, y)
    }
}