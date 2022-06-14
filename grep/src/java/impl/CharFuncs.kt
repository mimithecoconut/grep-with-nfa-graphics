package impl

import cfg.CFG

class CharFuncs {
    companion object {
        private val escaped = arrayOf("\\|", "\\*", "\\(", "\\)", "\\.", "\\+", "\\?", "\\\\")
        val wildcards = arrayOf(".", "\\s", "\\d", "\\w", "\\S", "\\W", "\\D")
        private val literals = getLiterals()
        private val wildMap = getCharClasses()

        // returns whether a (possibly wildcard) character matches a target character
        fun wildcardMatch(symbol: String, target: Char) : Char? {
            val translation = toLiteral(symbol)
            if (translation!!.contains(target)) return target
            return null
        }

        // converts an nfa transition label to its corresponding literal(s)
        private fun toLiteral(symbol: String) : List<Char>? {
            if (wildcards.contains(symbol)) {
                return wildMap[symbol]?.toList()
            }
            else if (escaped.contains(symbol)) {
                return listOf(symbol[1])
            }
            else if (symbol.length == 1 && literals.contains(symbol[0])) {
                return listOf(symbol[0])
            }
            else {
                throw IllegalArgumentException("CharFuncs: Unknown Symbol " + symbol)
            }
        }

        // get all allowed plaintext characters, not special ones like \? or \s
        private fun getLiterals(): List<Char> {
            val literals = mutableListOf<Char>()
            val specialChars = arrayOf('|', '*', '(', ')', '.', '+', '?', '\\')
            literals.add((0x09).toChar())
            for (c in ' '..'~') {
                if (!specialChars.contains(c)) {
                    literals.add(c)
                }
            }
            return literals
        }

        // get a map of wildcard strings to their char classes
        private fun getCharClasses(): MutableMap<String, MutableList<Char>> {
            val charClassMap = HashMap<String, MutableList<Char>>()

            for (wildcard in wildcards) {
                charClassMap[wildcard] = mutableListOf()
            }

            for (c in literals) {
                charClassMap["."]?.add(c)
                if (c in 'A'..'Z' || c in 'a'..'z') {
                    charClassMap["\\s"]?.add(c)
                } else {
                    charClassMap["\\S"]?.add(c)
                }
                if (c in '0'..'9') {
                    charClassMap["\\d"]?.add(c)
                } else {
                    charClassMap["\\D"]?.add(c)
                }
                if (c == (0x09).toChar() || c == ' ') {
                    charClassMap["\\w"]?.add(c)
                } else {
                    charClassMap["\\W"]?.add(c)
                }
            }
            for (c in escaped) {
                charClassMap["."]?.add(c[1])
                charClassMap["\\S"]?.add(c[1])
                charClassMap["\\W"]?.add(c[1])
                charClassMap["\\D"]?.add(c[1])
            }

            return charClassMap
        }
    }
}