package cfg;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Stream;

import cfg.CFG.Nonterminal;
import cfg.CFG.Symbol;
import cfg.CFG.Terminal;

public class EarleyParser {

    /**
     * Parse the given input string according to the given grammar
     *
     * @param input   string to parse
     * @param grammar CFG to parse with
     * @return an ASTNode representing the parse tree, or null if the input cannot be produced by the grammar
     */
    public static ASTNode parse(String input, CFG grammar) {
        @SuppressWarnings("unchecked")
        SortedSet<EarleyState>[] mem = Stream.generate(TreeSet::new).limit(input.length() + 1)
                .toArray(TreeSet[]::new);

        for (Symbol[] rhs : grammar.getRules(grammar.start)) {
            mem[0].add(new EarleyState(grammar.start, rhs, 0));
        }

        for (int i = 0; i <= input.length(); i++) {
            ArrayDeque<EarleyState> q = new ArrayDeque<>(mem[i]);
            while (!q.isEmpty()) {
                EarleyState currState = q.poll();

                if (!currState.isDone()) {
                    Symbol nextRHSSymbol = currState.nextSymbol();

                    if (nextRHSSymbol instanceof Nonterminal) {
                        // Predict
                        Nonterminal nt = (Nonterminal) nextRHSSymbol;
                        for (Symbol[] rhs : grammar.getRules(nt)) {
                            EarleyState newState = new EarleyState(nt, rhs, i);
                            if (mem[i].add(newState)) {
                                newState.leftParent = currState;
                                q.add(newState);
                            }
                        }
                    } else {
                        // Scan
                        Terminal t = (Terminal) nextRHSSymbol;
                        if (i < input.length() && t.value.equals(String.valueOf(input.charAt(i)))) {
                            EarleyState newState = currState.advance();
                            if (mem[i + 1].add(newState)) {
                                newState.leftParent = currState;
                            }
                        }
                    }
                } else {
                    // Complete
                    for (EarleyState state : mem[currState.startIdx]) {
                        if (!state.isDone() && state.nextSymbol().equals(currState.lhs)) {
                            EarleyState newState = state.advance();
                            if (mem[i].add(newState)) {
                                newState.leftParent = state;
                                newState.rightParent = currState;
                                q.add(newState);
                            }
                        }
                    }
                }
            }
        }

        Optional<EarleyState> end = mem[input.length()].stream()
                .filter((s) -> s.lhs.equals(grammar.start) && s.startIdx == 0 && s.isDone()).findFirst();

        return end.map(EarleyParser::generateParseTree).orElse(null);
    }

    public static ASTNode generateParseTree(EarleyState state) {
        EarleyState iter = state;
        ASTNode[] children = new ASTNode[state.rhs.length];

        for (int i = state.rhs.length - 1; i >= 0; i--) {
            Symbol s = state.rhs[i];
            if (s instanceof Nonterminal) {
                children[i] = generateParseTree(iter.rightParent);
            } else {
                children[i] = new ASTNode(s);
            }
            iter = iter.leftParent;
        }

        return new ASTNode(state.lhs, children);
    }
}
