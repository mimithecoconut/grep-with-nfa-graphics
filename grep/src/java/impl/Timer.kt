package impl

import kotlin.math.pow
import graphics.Vector
import java.lang.Math.log
import kotlin.math.pow
import Task2and4.Companion.NFAState
import cfg.ASTNode
import cfg.CFG
import cfg.CFG.Symbol
import cfg.EarleyParser
import graphics.Draw.*
import graphics.Force.Companion.computeSpringForce
import graphics.Force.Companion.computeVertexRepulsion
import java.awt.Color
import java.awt.Graphics
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.*
import javax.swing.JFrame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.Timer
import kotlin.math.ln

class Timer {
    companion object {
        val SECOND : Long = 1000000000
        val timer = Timer()
        val frame : JFrame = KFrame("grep")
        var logX = 0.0
        var dt : Double = 0.1
        val CONST = 50
        var first = true // flag for clearing screen
        val nfa = Task2and4.Companion.NFA()
        var regex : String = ""
        fun tick(computeForces: Boolean, stateSubset: MutableList<NFAState>? = null) {
            if (!computeForces) {
               try {
                    Thread.sleep(500)
                } catch (ie: InterruptedException) {
                    Thread.currentThread().interrupt()
                }
                timer.start()
                timer.tickHelper(computeForces, frame, stateSubset)
            }
            else if (timer.end() > SECOND/10.0) {
                timer.start()
                timer.tickHelper(computeForces, frame, stateSubset)
            }
        }

        fun resumeAnimation() {
            logX = 0.0
            dt = 0.1
        }

        fun resetNFA(regex: String) {
            this.regex = regex
            val cfg = Task1.makeCFG()
            val tree = EarleyParser.parse(regex, cfg)
            if (tree != null) {
                nfa.clearNFA()
                nfa.constructNFA(tree)
                nfa.setMasses()
                nfa.pruneNodes()
                logX = 0.0
                first = true
            }
        }

        fun startTraverse(string: String) {
            tick(false)
            Task3.matchString(nfa, string)
        }

        fun traverseTick(text: String, start: Int, end: Int, vis: MutableList<NFAState>) {
            /*
            from start to end inclusive, highlight those letters
            highlight all the nodes in vis as well
             */
            //println("tick " + text + ", " + start.toString() + ", " + end.toString())
            try {
                Thread.sleep(500)
            } catch (ie: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            //frame.contentPane.remove(frame.contentPane.componentCount - 1)
            //frame.contentPane.add((frame as KFrame).highlightLetter(text, start, Math.min(text.length, end + 1)))
            for (n in vis) {
                (frame as KFrame).highlightNode(n)
            }
        }
    }
    private var time: Long = 0
    fun start() {
        time = System.nanoTime()
    }

    fun end(): Long {
        return System.nanoTime() - time
    }

    private fun calculateDt() {
        val x = -2*CONST * (logX-CONST*4) //each tick is 10th of a second, run for 30 seconds
        if (x <= 0.2) {
            dt = 0.0
        }
        else {
            dt = log(x) / (CONST * 2)
        }
        logX++
    }

    private fun tickHelper(computeForces: Boolean, frame: JFrame, stateSubset: MutableList<NFAState>? = null) {
        lateinit var states: MutableList<NFAState>
        if (stateSubset == null) {
            states = nfa.getStates()
        }
        else {
            states = stateSubset
        }

        if (computeForces) {
            for (i in states.indices) {
                for (j in i + 1..states.size - 1) {
                    // only nonadjacent vertices repel each other
                    if (!nfa.transitionExists(states[i], states[j]) && states[i] != states[j]) {
                        computeVertexRepulsion(states[i], states[j])
                    }
                }
            }
            val transitions = nfa.getTransitions()
            for (transition in transitions) {
                for (next in transition.value) {
                    if (transition.key != next.first) {
                        computeSpringForce(transition.key, next.first)
                    }
                }
            }
        }

        calculateDt()
        if (dt > 0 || first) {
            frame.update(frame.graphics)
            for (state in states) {
                if (computeForces) {
                    state.applyForce(dt)
                    // prevent from going out of frame
                    val width : Int = frame.getContentPane().getWidth()
                    val height: Int = frame.getContentPane().getHeight()
                    if (state.position.x < 50 || state.position.x >= width - 50){
                        state.reverseForce("lr")
                    }
                    if (state.position.y < 50 || state.position.y >= height - 50){
                        state.reverseForce("ud")
                    }
                }
                val pos = Pair(state.position.x.toInt(), state.position.y.toInt())
                //println(pos.first.toString() + ", " + pos.second.toString())
                val rad = (ln(1 + state.mass) * 50).toInt()
                (frame as KFrame).drawNode(frame.graphics, pos, rad, state.accept)
                val transitions = nfa.getTransitions(state)
                for ((sym, next) in transitions) {
                    if (states.contains(next)) {
                        val nextPos = Pair(next.position.x.toInt(), next.position.y.toInt())
                        if (state == next) {
                            (frame as KFrame).drawSelfLoop(frame.graphics, pos, sym)
                        } else {
                            (frame as KFrame).drawLine(frame.graphics, pos, nextPos, sym, false)
                        }
                    }
                }
            }

            // Redraw states to prevent stuttering:
            for (state in states) {
                val pos = Pair(state.position.x.toInt(), state.position.y.toInt())
                val rad = (ln(1 + state.mass) * 50).toInt()
                (frame as KFrame).drawNode(frame.graphics, pos, rad, state.accept)
            }
        }
    }
}
