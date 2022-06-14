import cfg.*

class Task1 {
    companion object {
        fun makeCFG(): CFG {
            // Set up literals terminals
            val numLiterals = (0x7E - 0x20 + 1) - 8 + 1 //given literals, minus special chars, plus tab
            val literals = arrayOfNulls<CFG.Symbol>(numLiterals)
            literals[0] = CFG.Terminal((0x09).toChar().toString())
            val specialChars = arrayOf('|', '*', '(', ')', '.', '+', '?', '\\')
            var ind = 1
            for (c in ' '..'~') {
                if (!specialChars.contains(c)) {
                    literals[ind] = CFG.Terminal(c.toString())
                    ind++
                }
            }

            // Add productions to CFG
            val S = CFG.Nonterminal("S") // start
            val A = CFG.Nonterminal("A") // highest priority regex (union)
            val B = CFG.Nonterminal("B") // second priority regex (concat)
            val C = CFG.Nonterminal("C") // third priority regex (everything else)
            val or = CFG.Terminal("|") // "or"
            val star = CFG.Terminal("*") // "keene star", zero or more
            val lb = CFG.Terminal("(") // "left bracket"
            val rb = CFG.Terminal(")") // "right bracket"
            val plus = CFG.Terminal("+") // "plus", one or more
            val qm = CFG.Terminal("?") // "question mark", zero or one
            val dot = CFG.Terminal(".") // "dot", wildcard
            val bs = CFG.Terminal("\\") // "backslash", escape character

            val cfg = CFG(S)
            cfg.addRule(S, C)
            cfg.addRule(C, B)
            cfg.addRule(B, A)
            cfg.addRule(C, C, or, C) // E -> E|E
            cfg.addRule(B, B, B) // E -> EE
            cfg.addRule(B, A, star) // E -> E*
            cfg.addRule(B, A, plus) // E -> E+
            cfg.addRule(B, A, qm) // E -> E?
            cfg.addRule(A, lb, C, rb) // E -> (E)
            cfg.addRule(A, dot) // E -> ., terminal
            for (i in literals.indices) {
                cfg.addRule(A, literals[i]) // E -> a literal, terminal
            }
            for (i in specialChars.indices) {
                cfg.addRule(A, bs, CFG.Terminal(specialChars[i].toString())) // E -> \? escaped char, terminal
            }
            val otherRegex = arrayOf("s", "d", "w", "S", "W", "D")
            for (i in otherRegex.indices) {
                cfg.addRule(A, bs, CFG.Terminal(otherRegex[i])) // E -> \s other regex, terminal
            }

            return cfg
        }

        fun testCFG(cfg: CFG) {

            assert(EarleyParser.parse("a", cfg) != null)
            assert(EarleyParser.parse("~", cfg) != null)
            assert(EarleyParser.parse("A", cfg) != null)
            assert(EarleyParser.parse("7?5'+pvS+Y+", cfg) != null)
            assert(EarleyParser.parse("aaaa", cfg) != null)
            assert(EarleyParser.parse("a?", cfg) != null)
            assert(EarleyParser.parse("(ab)+", cfg) != null)
            assert(EarleyParser.parse("(ab*)", cfg) != null)
            assert(EarleyParser.parse("(ab+a)", cfg) != null)
            assert(EarleyParser.parse("d((ab)+)*b", cfg) != null)
            assert(EarleyParser.parse("dlka(ab)", cfg) != null)
            assert(EarleyParser.parse("aB?cd*dd", cfg) != null)
            assert(EarleyParser.parse("This (is) a \\sentence\\\\", cfg) != null) // \sentence\\, escaped s and \
            assert(EarleyParser.parse(".", cfg) != null)
            assert(EarleyParser.parse("a.b", cfg) != null)
            assert(EarleyParser.parse("a.*b", cfg) != null)
            assert(EarleyParser.parse("a|b", cfg) != null)
            assert(EarleyParser.parse("aa|bb", cfg) != null)
            assert(EarleyParser.parse("a*|b", cfg) != null)
            assert(EarleyParser.parse("a|b|c|d|e", cfg) != null)
            assert(EarleyParser.parse("d((ab)+|ee)*b", cfg) != null)

            assert(EarleyParser.parse("+", cfg) == null)
            assert(EarleyParser.parse("+*+", cfg) == null)
            assert(EarleyParser.parse("", cfg) == null)
            assert(EarleyParser.parse("|b", cfg) == null)
            assert(EarleyParser.parse("*|b", cfg) == null)
            assert(EarleyParser.parse("aa+*", cfg) == null)
            assert(EarleyParser.parse("a**", cfg) == null)

            println("CFG Tests Passed")
        }
    }
}