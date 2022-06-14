import cfg.CFG.Terminal
import cfg.CFG.Nonterminal
import cfg.CFG.Symbol
import cfg.ASTNode
import cfg.CFG
import cfg.EarleyParser
import graphics.Vector
import java.util.*
import kotlin.collections.LinkedHashMap
import impl.CharFuncs
import kotlin.collections.HashMap
import kotlin.math.pow
import impl.Timer
import graphics.Draw.KFrame
import impl.Timer.Companion.tick
import javax.swing.JFrame

class Task2and4 {
    companion object {
        class NFA() {
            private var numStates: Int = 0
            private var states: MutableList<NFAState>
            private var transitions: LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>>
            private var startState: NFAState
            private var leafs: MutableMap<NFAState, MutableList<NFAState>> = HashMap()
            private var isConstructed: Boolean = false

            val operations: MutableList<String> = mutableListOf("?", "*", "+", "|", ")", "(")

            init {
                // Initializes the instance variables of the NFA instance
                numStates = 0
                states = mutableListOf()
                transitions = linkedMapOf()
                startState = NFAState(-1, Epsilon(), false)
            }

            fun isConstructed(): Boolean {
                return isConstructed
            }

            fun getNumStates(): Int {
                return this.numStates
            }

            fun getStates(): MutableList<NFAState> {
                return this.states
            }

            fun setStates(s: MutableList<NFAState>) {
                this.states = s
            }

            fun getTransitions(): LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>> {
                return this.transitions
            }

            fun transitionExists(A: NFAState, B: NFAState) : Boolean {
                if (A != null && B != null) {
                    val transListA = this.transitionsAt(A)
                    if (transListA != null) {
                        for (pair in transListA) {
                            if (pair.first === B) return true
                        }
                    }
                    val transListB = this.transitionsAt(B)
                    if (transListB != null) {
                        for (pair in transListB) {
                            if (pair.first === A) return true
                        }
                    }
                }
                return false
            }

            private fun transitionsAt(state: NFAState) : MutableList<Pair<NFAState, Symbol>>? {
                for (transition in this.transitions) {
                    if (transition.key == state) {
                        return transition.value
                    }
                }
                return null
            }

            private fun linkedHashMapAt(map: LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>>, state: NFAState) : MutableList<Pair<NFAState, Symbol>>? {
                for (transition in map) {
                    if (transition.key == state) {
                        return transition.value
                    }
                }
                return null
            }

            private fun transitionsContains(A: NFAState, pairB: Pair<NFAState, Symbol>) : Boolean {
                val transList = transitionsAt(A)!!
                for (pair in transList) {
                    if (pair.first === pairB.first && pair.second == pairB.second) return true
                }
                return false
            }

            private fun linkedHashMapContains(map: LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>>, A: NFAState, pairB: Pair<NFAState, Symbol>) : Boolean {
                val transList = linkedHashMapAt(map, A)!!
                for (pair in transList) {
                    if (pair.first === pairB.first && pair.second == pairB.second) return true
                }
                return false
            }

            private fun mapGet(map: MutableMap<NFAState, Int>, state:NFAState) : Int? {
                for (pair in map) {
                    if (pair.key == state) {
                        return pair.value
                    }
                }
                return null
            }

            private fun mapSet(map: MutableMap<NFAState, Int>, state: NFAState, v: Int) {
                for (pair in map) {
                    if (pair.key == state) {
                        map[pair.key] = v
                        return
                    }
                }
                map[state] = v
            }

            private fun mapGet(map: MutableMap<NFAState, MutableList<NFAState>>, state: NFAState) : MutableList<NFAState>? {
                for (pair in map) {
                    if (pair.key == state) {
                        return pair.value
                    }
                }
                return null
            }

            private fun mapSet(map: MutableMap<NFAState, MutableList<NFAState>>, state:NFAState, v: MutableList<NFAState>) {
                for (pair in map) {
                    if (pair.key == state) {
                        map[pair.key] = v
                        return
                    }
                }
                map[state] = v
            }

            private fun mapAdd(map: MutableMap<NFAState, MutableList<NFAState>>, state: NFAState, v: NFAState) {
                for (pair in map) {
                    if (pair.key == state) {
                        map[pair.key]?.add(v)
                        return
                    }
                }
                map[state] = mutableListOf(v)
            }

            private fun mapAddAll(map: MutableMap<NFAState, MutableList<NFAState>>, state: NFAState, v: MutableList<NFAState>) {
                for (pair in map) {
                    if (pair.key == state) {
                        map[pair.key]?.addAll(v)
                        return
                    }
                }
                map[state] = v
            }

            fun getTransitions(state: NFAState) : MutableList<Pair<String, NFAState>> {
                val transitions = this.transitionsAt(state)
                val strTransitions = mutableListOf<Pair<String, NFAState>>()
                if (transitions == null) return strTransitions
                for (transition in transitions) {
                    strTransitions.add(Pair(transition.second.value, transition.first))
                }
                return strTransitions
            }

            fun setTransitions(t: LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>>) {
                this.transitions = t
            }

            fun getStartState(): NFAState {
                return this.startState
            }

            fun setStartState(s: NFAState) {
                this.startState = s
            }

            // uses DFA to traverse the parse tree and construct the NFA accordingly
            fun constructNFA(root: ASTNode) {
                clearNFA()
                val st = Stack<NFAState>()
                constructNFAHelper(root, st)
                // If the NFA consists only of a terminal, create a start state
                if (!(this.startState.sym is Epsilon)) {
                    val newState = NFAState(this.numStates, Epsilon(), false)
                    this.numStates++
                    states.add(newState)
                    mapSet(leafs, newState, mutableListOf(this.startState))
                    val pair = Pair(this.startState, this.startState.sym)
                    this.startState = newState
                    transitions[newState] = mutableListOf(pair)
                }
                findAcceptStates()
                computeEpsilonClosure()
                pruneNodes()
                this.numStates = this.states.size
                reorganizeStates()
                setMasses()
                isConstructed = true
            }

            fun playAnimation() {
                while (Timer.dt > 0) {
                    tick(true)
                }
                Timer.resumeAnimation()
            }

            // recursive function to construct NFA
            fun constructNFAHelper(v: ASTNode, work: Stack<NFAState>) {
                if (v.isTerminal()) {
                    if (!work.isEmpty() && work.peek().sym.value == "\\") {
                        work.push(this.createTerminal(v.getValue()))
                    }
                    else if (!operations.contains(v.getValue())) {
                        work.push(this.createTerminal(v.getValue()))
                    }
                } else {
                    val children = v.getChildren()
                    for (child in children) {
                        constructNFAHelper(child, work)
                    }
                    if (children.size == 3) {
                        // parentheses case
                        if (children[0].getValue() == "(" && children[2].getValue() == ")") {
                            // Do nothing if we have parentheses because children
                            // have already been evaluated
                        }
                        // union case
                        if (children[1].getValue() == "|") {
                            val first = work.pop()
                            val second = work.pop()
                            work.push(union(first, second))
                        }
                    }
                    if (children.size == 2) {
                        // Kleene star case
                        if (children[0].getValue() != "\\" && children[1].getValue() == "*") {
                            val state = work.pop()
                            work.push(kleeneStar(state))
                        }
                        // Question mark case
                        else if (children[0].getValue() != "\\" && children[1].getValue() == "?") {
                            val state = work.pop()
                            work.push(questionMark(state))
                        }
                        // Addition sign case
                        else if (children[0].getValue() != "\\" && children[1].getValue() == "+") {
                            val state = work.pop()
                            work.push(additionSign(state, true, false))
                        }
                        // Concatenation case
                        else {
                            val second = work.pop()
                            val first = work.pop()
                            work.push(concatenation(first, second))
                        }
                    }
                }
                if (!work.isEmpty() && (work.peek().index > startState.index)) {
                    startState = work.peek()
                }
            }

             public fun findLeafs(state: NFAState) : MutableList<NFAState> {
                /*
                Finds all leafs (no outgoing nodes that aren't ancestors) of a state
                Algo:
                Goes to every transition of current node, gets their leafs, stops when leafs list is empty; that's a leaf
                 */
                val acc = mutableListOf<NFAState>()
                if (mapGet(leafs, state)!!.isEmpty()) {
                    // is a leaf
                    acc.add(state)
                    return acc
                }
                for (next in mapGet(leafs, state)!!) {
                    val nextAcc = findLeafs(next)
                    for (leaf in nextAcc) {
                        if (!acc.contains(leaf)) {
                            acc.add(leaf)
                        }
                    }
                }
                return acc
            }

            // adds terminal A into the NFA
            fun createTerminal(t: String): NFAState {
                val term = NFAState(this.numStates, Terminal(t), false)
                states.add(term)
                mapSet(leafs, term, mutableListOf())
                this.numStates++
                return term
            }

            // adds A|B into the NFA
            fun union(A: NFAState, B: NFAState): NFAState {
                val startState = NFAState(this.numStates, Epsilon(), false)
                this.numStates++
                states.add(startState)
                mapSet(leafs, startState, mutableListOf())
                val pairA = Pair(A, A.sym)
                val pairB = Pair(B, B.sym)
                val second: MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                second.add(pairA)
                second.add(pairB)
                transitions[startState] = second
                mapAddAll(leafs, startState, findLeafs(A))
                mapAddAll(leafs, startState, findLeafs(B))
                return startState
            }

            // adds AB into the NFA
            fun concatenation(A: NFAState, B: NFAState): NFAState {
                concatenationHelper(A, B, mutableMapOf())
                val startState = NFAState(this.numStates, Epsilon(), false)
                states.add(startState)
                mapSet(leafs, startState, mutableListOf())
                this.numStates++
                val pairA = Pair(A, A.sym)
                val startList: MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                startList.add(pairA)
                transitions[startState] = startList
                mapAddAll(leafs, startState, findLeafs(A))
                return startState
            }

            fun concatenationHelper(A: NFAState, B: NFAState, parents: MutableMap<NFAState, MutableList<NFAState>>) {
                //concat B with leafs of A, effectively replacing all leafs of A with all leafs of B
                val leafs = findLeafs(A)
                for (leaf in leafs) {
                    val pairB = Pair(B, B.sym)
                    val list: MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                    list.add(pairB)
                    mapAdd(this.leafs, leaf, B)
                    if (transitionsAt(leaf) == null) {
                        transitions[leaf] = list
                    }
                    else if (!transitionsContains(leaf, pairB)) {
                        transitionsAt(leaf)?.add(pairB)
                    }
                }
            }

            // adds A+ into the NFA
            fun additionSign(A: NFAState, selfLoop: Boolean, acceptEmpty: Boolean): NFAState {
                if (selfLoop) {
                    operationHelper(A, A, mutableListOf())
                }
                val startState = NFAState(this.numStates, Epsilon(), false)
                states.add(startState)
                this.numStates++
                mapSet(leafs, startState, mutableListOf())

                val list: MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                val pairA = Pair(A, A.sym)
                list.add(pairA)
                mapAddAll(leafs, startState, findLeafs(A))
                if (acceptEmpty) {
                    val emptyState = NFAState(this.numStates, Epsilon(), false)
                    this.numStates++
                    states.add(emptyState)
                    mapSet(leafs, emptyState, mutableListOf())
                    mapAdd(leafs, startState, emptyState)
                    val pairEmpty = Pair(emptyState, Epsilon())
                    list.add(pairEmpty)
                }
                transitions[startState] = list
                return startState
            }

            // adds A* into the NFA
            fun kleeneStar(A: NFAState): NFAState {
                return additionSign(A, true, true)
            }

            private fun operationHelper(A: NFAState, endState: NFAState, visited: MutableList<NFAState>) {
                visited.add(endState)
                var end = true
                val t = transitionsAt(endState)?.toMutableList()
                if (t != null) {
                    for (tr in t) {
                        if (!visited.contains(tr.first)) {
                            operationHelper(A, tr.first, visited)
                            end = false
                        }
                    }
                }
                if (end) {
                    val pair = Pair(A, A.sym)
                    val list: MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                    list.add(pair)
                    if (endState != A) {
                        mapAdd(leafs, A, endState)
                    }
                    if (transitionsAt(endState) == null) {
                        transitions[endState] = list
                    }
                    else if (!transitionsContains(endState, pair)) {
                        transitionsAt(endState)?.add(pair)
                    }
                }
            }

            // adds A? into the NFA
            fun questionMark(A: NFAState): NFAState {
                return additionSign(A, false, true)
            }

            // uses DFS to find states in NFA in which no transitions to other
            // states can be found and denotes these as accept states
            fun findAcceptStates() {
                val accepts = findLeafs(startState)
                for (leaf in accepts) {
                    leaf.accept = true
                }
            }

            fun computeEpsilonClosure() {
                var old: LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>> = linkedMapOf()
                var curr: LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>> = linkedMapOf()
                // Add all epsilon transitions to curr
                for (transition in transitions) {
                    var changed = false
                    val startList: MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                    for (next in transition.value) {
                        if (next.second is Epsilon) {
                            startList.add(next)
                            changed = true
                        }
                    }
                    if (changed) {
                        curr[transition.key] = startList
                    }
                }

                while (!old.equals(curr)) {
                    old = curr
                    curr = linkedMapOf()
                    old.forEach { (oldStart, oldEnd) ->
                        curr[oldStart] = oldEnd.toMutableList()
                    }
                    old.forEach { (oldStart, oldEnd) ->
                        for (endState in oldEnd) {
                            if (old.containsKey(endState.first)){
                                for (next in old[endState.first]!!) {
                                    if (!curr[oldStart]!!.contains(next)) {
                                        curr[oldStart]!!.add(next)
                                    }
                                }
                            }
                        }
                    }
                }


                val ans : LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>> = linkedMapOf()
                old.forEach{ (start, end) ->
                    val endList : MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                    for (c in end){
                        if (c.first.accept){
                            start.accept = true
                        }
                        val nextStates : MutableList<Pair<NFAState, Symbol>>? = transitionsAt(c.first)
                        if (nextStates != null) {
                            // look at all next states to merge
                            for (nextState in nextStates){
                                if (nextState.second !is Epsilon) {
                                    endList.add(nextState)
                                }
                            }
                        }
                    }
                    ans[start] = endList
                }

                val finalAns : LinkedHashMap<NFAState, MutableList<Pair<NFAState, Symbol>>> = linkedMapOf()
                transitions.forEach{ (start, end) ->
                    // if already there add the ones that are not epsilon s
                    if (ans.containsKey(start)){
                        val endList : MutableList<Pair<NFAState, Symbol>> = mutableListOf()
                        for (c in end){
                            if (c.second !is Epsilon){
                                endList.add(c)
                            }

                        }
                        ans[start]?.let { endList.addAll(it) }
                        finalAns[start] = endList
                    }
                    else {
                        finalAns[start] = end
                    }
                }
                transitions = finalAns
            }

            fun drawConstructNFA(index: Int) {
                val tempList : MutableList<NFAState> = mutableListOf()
                for (i in this.states.size - 1 - index..this.states.size - 1) {
                    tempList.add(this.states.get(i))
                }
                tick(false, tempList)
            }

            fun clearNFA() {
                states.clear()
                transitions.clear()
                leafs.clear()
                numStates = 0
                startState = NFAState(-1, Epsilon(), false)
            }

            // Computes the longest prefix that all paths in the NFA must take
            // Returns a Pair with the first element being the prefix and the
            // second element being a list of states that the prefix ends in
            fun computePrefix(): Pair<String, MutableList<NFAState>> {
                // Current depth of NFA traversal
                var depth = 0
                // Current symbol to be compared in prefix
                var currentSym: String? = null
                // Working stack
                val queue: Queue<Pair<Int, Pair<NFAState, Symbol>>> = LinkedList()
                // States at current depth of traversal
                var currDepthStates: MutableList<NFAState> = mutableListOf()
                // States at previous level of traversal
                var prevDepthStates: MutableList<NFAState> = mutableListOf(startState)
                // True if the the current level is the end of the prefix computation
                var lastLevel = false
                // Computed prefix
                var prefix = ""
                // States that can be reached via a path containing the prefix
                val doneStates: MutableList<NFAState> = mutableListOf()

                val visited: MutableList<NFAState> = mutableListOf()
                // If the start state is an accept state, the prefix is empty
                if (startState.accept) {
                    if (!doneStates.contains(startState)) {
                        doneStates.add(startState)
                    }
                    return Pair(prefix, doneStates)
                }
                for (startTrn in transitionsAt(startState)!!) {
                    queue.add(Pair(depth, startTrn))
                }
                while (queue.size != 0) {
                    val next = queue.remove()
                    visited.add(next.second.first)
                    // Now traversing the next level of the NFA
                    if (next.first != depth) {
                        depth = next.first
                        if (currentSym == "\\\\" || currentSym == "\\.") {
                            prefix += currentSym.substring(1)
                        }
                        else if (currentSym?.substring(0, 1) != "\\" && !CharFuncs.wildcards.contains(currentSym)) {
                            prefix += currentSym
                        }
                        if (lastLevel) {
                            if (currentSym != ".") {
                                // We are done computing the prefix
                                for (state in currDepthStates) {
                                    if (!doneStates.contains(state)) {
                                        doneStates.add(state)
                                    }
                                }
                            }
                            else {
                                // Case where current symbol is . wildcard
                                for (state in prevDepthStates) {
                                    if (!doneStates.contains(state)) {
                                        doneStates.add(state)
                                    }
                                }
                            }
                            return Pair(prefix, doneStates)
                        }
                        prevDepthStates = currDepthStates
                        currDepthStates = mutableListOf()
                        // Use null to represent unknown next symbol
                        currentSym = null
                    }
                    if (next.second.first.accept) {
                        lastLevel = true
                    }
                    // If symbol on current transition does not match, we are done
                    if (next.second.second.value != currentSym) {
                        if (currentSym == null) {
                            currentSym = next.second.second.value
                            if (currentSym == "\\" && (prefix.length == 0 || prefix.substring(prefix.length - 1) != "\\")) {
                                currentSym += transitionsAt(next.second.first)?.get(0)?.second?.value
                            }
                            if ((prefix.length == 0 || prefix.substring(prefix.length - 1) != ".") && CharFuncs.wildcards.contains(currentSym)) {
                                lastLevel = true
                            }
                        }
                        else {
                            for (state in prevDepthStates) {
                                if (!doneStates.contains(state)) {
                                    doneStates.add(state)
                                }
                            }
                            return Pair(prefix, doneStates)
                        }
                    }
                    if (!currDepthStates.contains(next.second.first)) {
                        currDepthStates.add(next.second.first)
                    }
                    // Traverse all outgoing transitions
                    if (transitionsAt(next.second.first) != null) {
                        for (t in transitionsAt(next.second.first)!!) {
                            // If any state has a loop, only traverse it if other
                            // states still have children
                            queue.add(Pair(depth + 1, t))
                            if (!visited.contains(t.first)) {
                                queue.add(Pair(depth + 1, t))
                            }
                        }
                    }
                    else {
                        lastLevel = true
                    }
                    if (lastLevel && currentSym == ".") {
                        // Case where first symbol is . wildcard
                        for (state in prevDepthStates) {
                            if (!doneStates.contains(state)) {
                                doneStates.add(state)
                            }
                        }
                        return Pair(prefix, doneStates)
                    }
                }
                // Case where we finish traversing but all transition symbols match
                // Return states in last traversed level
                prefix += currentSym
                for (state in currDepthStates) {
                    if (!doneStates.contains(state)) {
                        doneStates.add(state)
                    }
                }
                return Pair(prefix, doneStates)
            }

            fun reorganizeStates() {
                // Current depth of NFA traversal
                var depth = 0
                // Working queue
                val queue: Queue<Pair<Int, Pair<NFAState, Symbol>>> = LinkedList()
                // States that have already been visited
                val visitedStates: MutableList<NFAState> = mutableListOf(startState)
                val levels: LinkedHashMap<Int, MutableList<NFAState>> = LinkedHashMap()
                levels.put(depth, mutableListOf(startState))
                if (startState in transitions.keys) {
                    for (startTrn in transitionsAt(startState)!!) {
                        queue.add(Pair(depth + 1, startTrn))
                    }
                }
                while (queue.size != 0) {
                    val next = queue.remove()
                    if (next.first !in levels.keys) {
                        levels.put(next.first, mutableListOf())
                    }
                    levels.get(next.first)?.add(next.second.first)
                    visitedStates.add(next.second.first)
                    // Now traversing the next level of the NFA
                    if (next.first != depth) {
                        depth = next.first
                    }
                    // Traverse all outgoing transitions
                    if (transitionsAt(next.second.first) != null) {
                        for (t in transitionsAt(next.second.first)!!) {
                            if (t.first !in visitedStates) {
                                queue.add(Pair(depth + 1, t))
                            }
                        }
                    }
                }
                val xSpacing = Vector.MAX.x / (depth + 2)
                var curr = xSpacing
                for (i in 0..depth) {
                    val ySpacing = Vector.MAX.y / (levels.get(i)?.size?.plus(1)!!)
                    for (j in 0..levels.get(i)!!.size - 1) {
                        levels.get(i)!![j].position = Vector(curr, ySpacing * (j + 1))
                    }
                    curr += xSpacing
                }
            }

            fun setMasses() {
                for (state in states) {
                    if (mapGet(leafs, state) == null) state.setMass(1.0)
                    else state.setMass(findLeafs(state).size.toDouble())
                }
            }

            fun pruneNodes() {
                val reachable = mutableListOf<NFAState>()
                val queue = mutableListOf<NFAState>()
                queue.add(startState)
                var ind = 0
                while (ind < queue.size) {
                    val cur = queue[ind]
                    ind++
                    reachable.add(cur)
                    if (transitionsAt(cur) != null) {
                        for ((next, _) in transitionsAt(cur)!!) {
                            if (!reachable.contains(next)) {
                                queue.add(next)
                            }
                        }
                    }
                }
                states = reachable
                val newTransitions = linkedMapOf<NFAState, MutableList<Pair<NFAState, Symbol>>>()
                for ((node, list) in transitions) {
                    if (reachable.contains(node)) {
                        for (pair in transitionsAt(node)!!) {
                            //if pair.first is reachable and node is reachable, add to new transitions
                            if (reachable.contains(pair.first)) {
                                if (linkedHashMapAt(newTransitions, node) == null) {
                                    newTransitions[node] = mutableListOf(pair)
                                } else if (!linkedHashMapContains(newTransitions, node, pair)) {
                                    linkedHashMapAt(newTransitions, node)?.add(pair)
                                }
                            }
                        }
                    }
                }
                transitions = newTransitions
                setMasses()
                this.states = LinkedHashSet(this.states).toMutableList()
            }
        }

        fun testNFAConstruction() {
            val cfg: CFG = Task1.makeCFG()
            val testTree1: ASTNode = EarleyParser.parse("ab*|c(de)?", cfg)
            val testTree2: ASTNode = EarleyParser.parse("f+o", cfg)
            val testTree3: ASTNode = EarleyParser.parse("foxe?", cfg)
            val testTree4: ASTNode = EarleyParser.parse("a*|aa", cfg)
            val testTree5: ASTNode = EarleyParser.parse("(de)?f", cfg)
            val testTree6: ASTNode = EarleyParser.parse("(de)*", cfg)
            val nfa: NFA = NFA()
            nfa.clearNFA()
            nfa.constructNFA(testTree4)
            println(nfa.getStates().toString())
            println(nfa.getTransitions().toString())
            nfa.clearNFA()
            println("-------")
            nfa.constructNFA(testTree3)
            println(nfa.getStates().toString())
            println(nfa.getTransitions().toString())
            nfa.clearNFA()

        }

        fun testComputePrefix() {
            val cfg: CFG = Task1.makeCFG()
            val nfa: NFA = NFA()
            nfa.constructNFA(EarleyParser.parse("aa|a|a", cfg))
            val test2 = nfa.computePrefix()
            assert(test2.first == "a")
            assert(test2.second.size == 3)
            nfa.constructNFA(EarleyParser.parse("b|a|a", cfg))
            val test3 = nfa.computePrefix()
            assert(test3.first == "")
            assert(test3.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("fo*x", cfg))
            val test4 = nfa.computePrefix()
            assert(test4.first == "")
            assert(test4.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("foo(d|l)", cfg))
            val test5 = nfa.computePrefix()
            assert(test5.first == "foo")
            assert(test5.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("Caltech|California", cfg))
            val test6 = nfa.computePrefix()
            assert(test6.first == "Cal")
            assert(test6.second.size == 2)
            nfa.constructNFA(EarleyParser.parse("a+|aaa", cfg))
            val test7 = nfa.computePrefix()
            assert(test7.first == "a")
            assert(test7.second.size == 2)
            nfa.constructNFA(EarleyParser.parse("f+o", cfg))
            val test8 = nfa.computePrefix()
            assert(test8.first == "f")
            assert(test8.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("foxe?", cfg))
            val test9 = nfa.computePrefix()
            assert(test9.first == "fox")
            assert(test9.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("foxe.hss", cfg))
            val test10 = nfa.computePrefix()
            assert(test10.first == "foxe")
            assert(test10.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("fox\\\\yes", cfg))
            val test11 = nfa.computePrefix()
            assert(test11.first == "fox\\yes")
            assert(test11.second.size == 1)
            nfa.constructNFA(EarleyParser.parse("f\\?\\*\\+", cfg))
            val test12 = nfa.computePrefix()
            assert(test12.first == "f?*+")
            assert(test12.second.size == 1)
            println("Compute prefix tests passed")
        }

        class NFAState(val index: Int, var sym: Symbol, var accept: Boolean) {
            var position : Vector = calculateRandomPosition()
            var force : Vector = Vector.ZERO.clone() //cumulative force vector
            var velocity : Vector = Vector.ZERO.clone()
            var mass : Double = 0.0

            fun calculateRandomPosition() : Vector {
                val x = Math.random() * Vector.MAX.x
                val y = Math.random() * Vector.MAX.y
                return Vector(x, y)
            }

            @JvmName("setMass1")
            fun setMass(m: Double) {
                mass = m
            }

            fun addForce(f: Vector) {
                force.add(f)
            }

            //Updates velocity/position and resets force
            fun applyForce(dt: Double) {
                val old_v = velocity.clone()
                velocity.add(Vector.multiply(dt / mass, force))
                position.add(Vector.multiply(dt / 2, Vector.add(old_v, velocity)))
                force = Vector.ZERO.clone()
            }

            fun reverseForce(dir: String) {
                val old_v = velocity.clone()
                if (dir.equals("lr")) {
                    velocity.reverseX(old_v)
                }
                else if (dir.equals("ud")){
                    velocity.reverseY(old_v)
                    }
            }


            fun distanceTo(s: NFAState) : Double {
                return Math.sqrt((this.position.x - s.position.x).pow(2) + (this.position.y - s.position.y).pow(2))
            }
        }

        class Epsilon : Symbol() {
            init {
                this.value = "epsilon"
            }
        }
    }
}
