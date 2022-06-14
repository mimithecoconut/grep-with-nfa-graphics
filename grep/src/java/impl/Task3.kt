import java.io.File
import java.io.InputStream
import cfg.CFG
import cfg.EarleyParser.*
import impl.CharFuncs
import impl.Timer

class Task3 {
    companion object {
        // returns a list of all start indices where "grep" would find a match for an nfa regex in a text
        private fun accepts(nfa: Task2and4.Companion.NFA, startStates: List<Task2and4.Companion.NFAState>, bm: BM, text: String) : MutableList<String> {
            val matches = mutableListOf<String>()
            val prefix = bm.prefix
            var next = bm.getNext(text, prefix, 0)

            //precompute prefix's visited for display purposes
            val visited = mutableListOf<Task2and4.Companion.NFAState>()
            if (prefix == "") {
                visited.add(nfa.getStartState())
            }
            else {
                val stack = mutableListOf<Pair<Task2and4.Companion.NFAState, Int>>()
                stack.add(Pair(nfa.getStartState(), -1))
                var ind = 0
                while (!stack.isEmpty()) {
                    val (curState, curInd) = stack[ind]
                    stack.removeAt(ind)
                    ind--
                    if (startStates.contains(curState)) {
                        break
                    }
                    visited.add(curState)
                    if (curInd + 1 < text.length || curState.sym.value == "\\") {
                        val transitions = nfa.getTransitions(curState)
                        for ((nextLabel, nextState) in transitions) {
                            if (curState.sym.value == "\\") {
                                val wildcardMatch = CharFuncs.wildcardMatch("\\" + nextLabel, prefix[curInd + 1])
                                if (wildcardMatch != null) {
                                    stack.add(Pair(nextState, curInd + 1))
                                    ind++
                                }
                            }
                            else if (nextLabel == "\\") {
                                stack.add(Pair(nextState, curInd))
                                ind++
                            }
                            else {
                                val wildcardMatch = CharFuncs.wildcardMatch(nextLabel, prefix[curInd + 1])
                                if (wildcardMatch != null) {
                                    stack.add(Pair(nextState, curInd + 1))
                                    ind++
                                }
                            }
                        }
                    }
                }
            }

            while (next != -1) {
                Timer.tick(false)
                var longestMatch : String? = null
                var longestInd : Int? = null
                var longestVisited : MutableList<Task2and4.Companion.NFAState>? = null
                val worklist = mutableListOf<Pair<Task2and4.Companion.NFAState, Int>>()
                val visWorklist = mutableListOf<MutableList<Task2and4.Companion.NFAState>>()
                for (state in startStates) {
                    worklist.add(Pair(state, next + prefix.length - 1))
                    val vis = visited.toMutableList()
                    vis.add(state)
                    visWorklist.add(vis)
                }

                for (ind in 1..visited.size) {
                    Timer.traverseTick(text, next, ind, visited.subList(0, ind))
                }

                while (!worklist.isEmpty()) {
                    val (curState, curInd) = worklist[0]
                    val vis = visWorklist[0].toMutableList()
                    worklist.removeAt(0)
                    visWorklist.removeAt(0)
                    vis.add(curState)
                    Timer.traverseTick(text, next, curInd, vis)
                    if (curState.accept) {
                        longestMatch = text.substring(next, curInd + 1)
                        longestInd = curInd
                        longestVisited = vis
                    }
                    if (curInd + 1 < text.length || curState.sym.value == "\\") {
                        val transitions = nfa.getTransitions(curState)
                        for ((nextLabel, nextState) in transitions) {
                            if (curState.sym.value == "\\") {
                                val wildcardMatch = CharFuncs.wildcardMatch("\\" + nextLabel, text[curInd + 1])
                                if (wildcardMatch != null) {
                                    worklist.add(Pair(nextState, curInd + 1))
                                    visWorklist.add(vis)
                                }
                            }
                            else if (nextLabel == "\\") {
                                worklist.add(Pair(nextState, curInd))
                                visWorklist.add(vis)
                            }
                            else {
                                val wildcardMatch = CharFuncs.wildcardMatch(nextLabel, text[curInd + 1])
                                if (wildcardMatch != null) {
                                    worklist.add(Pair(nextState, curInd + 1))
                                    visWorklist.add(vis)
                                }
                            }
                        }
                    }
                }
                if (longestMatch != null && longestMatch != "") {
                    // text match; move past greedy longest match (as normal grep does; see "ababa" "aba")
                    matches.add(longestMatch)
                    next = bm.getNext(text, prefix, next + longestMatch.length)
                    Timer.traverseTick(text, next, longestInd!!, longestVisited!!)
                }
                else {
                    // no text match; search at next possible galil index
                    next = bm.getNext(text, prefix, next + bm.galilPeriod)
                }
            }
            return matches
        }

        fun matchFile(regex: String, cfg: CFG, pathname: String) : MutableList<Pair<Int, String>> {
            val nfa = Task2and4.Companion.NFA()
            nfa.constructNFA(parse(regex, cfg))
            val (prefix, startStates) = nfa.computePrefix()
            val bm = BM(prefix)

            val inputStream: InputStream = File(pathname).inputStream()

            var lineNum = 1
            val matches = mutableListOf<Pair<Int, String>>()
            inputStream.bufferedReader().forEachLine {
                val accepted = accepts(nfa, startStates, bm, it)

                for (str in accepted) {
                    matches.add(Pair(lineNum, str))
                    println(lineNum.toString() + ":" + str)
                }
                lineNum++
            }
            return matches
        }

        fun matchString(nfa: Task2and4.Companion.NFA, string: String) {
            val (prefix, startStates) = nfa.computePrefix()
            val bm = BM(prefix)

            val accepted = accepts(nfa, startStates, bm, string)

            for (str in accepted) {
                println("1: " + str)
            }
        }

        fun testMatchFile(cfg: CFG) {
            val test_file = "test.txt"
            //matchFile("nope", test_file)
            //matchFile("fox", cfg, test_file) //works
            //matchFile("f.x", cfg, test_file) //works
            //matchFile("fo*x", cfg, test_file) //works
            //matchFile("foxe?", cfg, test_file) //works
            //matchFile("fox.|f.x.", cfg, test_file) //works
            //matchFile("\\sox", cfg, test_file) //works
            //matchFile("f\\sx", cfg, test_file) //works
            //matchFile("f\\Sx", cfg, test_file) //works
            //matchFile("f?", cfg, test_file) //works
            //matchFile("f+o", cfg, test_file) //works
            //matchFile("(Jeffo)*", cfg, test_file) //works
            //matchFile("foxy?", cfg, test_file) //works
            //matchFile(".*fox", cfg, test_file) //works
            //matchFile("Jeffo the.*", cfg, test_file) //works
            //matchFile("fox|foxe|foxes", cfg, test_file) //works
            //matchFile("(Jeffo|fox) the.*", cfg, test_file) //works
            //matchFile("(Jeffo|fox)* the.*", cfg, test_file) //works
            //matchFile("\\s+(Jeffo|fox)* the.*", cfg, test_file) //works
            matchFile("\\s+(Jeffo|fox)+ the.*", cfg, test_file) //fails
            //matchFile("(Jeffo)", cfg, test_file) //works
            //matchFile("(Jeffo)|(Jeffo)", cfg, test_file) //works
            //matchFile("(Jeffo|Jeffo)", cfg, test_file) //works
            //matchFile("(Jeffo|fox)", cfg, test_file) //works
            //matchFile("(f|b)ox", cfg, test_file) //works
            //matchFile("(Jeffo|fox) ", cfg, test_file) //works
            //matchFile("(Jeffo|fox).", cfg, test_file) //works
            //matchFile("(Jeffo|fox)Z.", cfg, test_file) //WORKS
            //matchFile("(fox|Jeffo|foxes)ABCD.", cfg, test_file) //WORKS
            //match("foxe?|t?fox") //WORKS
            //match("foxe?|Jeffo") //works
            //match("f?ox") //WORKS
            //match("(Jeffo|fox)*") //works
            //match("(Jeffo)+") //works
            //match("(Jeffo)*.*") //works
            //match("Jef?o") //works
            //match("(\\s)*") //works
            //match("\\s\\w(Jeffo|fox).*") //WORKS
            //match("\\?") //works
            //match("\\s+fox|\\S+fox") //WORKS
            //match("\\s+fox") //WORKS

        }

        private fun match(pattern: String) {
            val test_file = "test.txt"
            val cfg = Task1.makeCFG()
            matchFile(pattern, cfg, test_file)
        }
    }
}