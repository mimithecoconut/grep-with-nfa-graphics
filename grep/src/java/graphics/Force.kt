package graphics

import kotlin.math.pow

class Force {
    companion object {
        // Applies force with given magnitude to v1 and v2 in opposite directions
        // If repulsion is true, then v1 and v2 repulse each other, if repulsion is false, then they attract
        fun applyForceBetweenStates(
            v1: Task2and4.Companion.NFAState,
            v2: Task2and4.Companion.NFAState,
            magnitude: Double,
            repulsion: Boolean
        ) {
            val displacement: Vector
            // direction that force is applied to v2
            if (repulsion) {
                displacement = Vector.add(v2.position, Vector.multiply(-1.0, v1.position))
            } else {
                displacement = Vector.add(v1.position, Vector.multiply(-1.0, v2.position))
            }
            displacement.normalize()
            displacement.multiply(magnitude)
            v2.addForce(displacement)
            // apply same force in opposite direction to v1
            val opposite = displacement.clone()
            opposite.multiply(-1.0)
            v1.addForce(opposite)
        }

        fun computeSpringForce(v1: Task2and4.Companion.NFAState, v2: Task2and4.Companion.NFAState) {
            // force exerted by spring is c * log(length) / 10
            val springMag = 8 * Math.log(v1.distanceTo(v2) / 10)
            applyForceBetweenStates(v1, v2, springMag, false)
        }

        fun computeVertexRepulsion(v1: Task2and4.Companion.NFAState, v2: Task2and4.Companion.NFAState) {
            // force between any two vertices is c/d^2 where d is the distance between the vertices
            val repulsionMag = 500000 / v1.distanceTo(v2).pow(2)
            applyForceBetweenStates(v1, v2, repulsionMag, true)
        }
    }
}