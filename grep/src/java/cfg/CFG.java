package cfg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class CFG {

    public static class Symbol {
        protected String value;

        public String getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object obj) {
            // Subclasses are considered not equal even if the value is the same
            if (obj == null || this.getClass() != obj.getClass()) {
                return false;
            }
            return value.equals(((Symbol) obj).value);
        }

        @Override
        public int hashCode() {
            // Subclasses should hash differently
            return Objects.hash(value, getClass());
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    public static class Terminal extends Symbol {
        public Terminal(String v) {
            // Use multiple terminals for longer strings
            assert v.length() == 1;
            this.value = v;
        }
    }

    public static class Nonterminal extends Symbol {
        public Nonterminal(String v) {
            this.value = v;
        }
    }

    public final Nonterminal start;
    private final Map<Nonterminal, List<Symbol[]>> ruleMap;

    public CFG(Nonterminal nt) {
        this.start = nt;
        this.ruleMap = new HashMap<>();
    }

    public CFG(String string) {
        this(nt(string));
    }

    public List<Symbol[]> getRules(Nonterminal symbol) {
        return Collections.unmodifiableList(ruleMap.getOrDefault(symbol, List.of()));
    }

    /**
     * Add a production to the grammar of the form lhs -> rhs
     *
     * @param lhs nonterminal on the LHS of the production
     * @param rhs Symbols on the RHS of the production, may be either terminal or nonterminal
     */
    public void addRule(Nonterminal lhs, Symbol... rhs) {
        this.ruleMap.putIfAbsent(lhs, new ArrayList<>());
        this.ruleMap.get(lhs).add(rhs);
    }

    // Various ergonomic ways of adding multiple rules for a single LHS

    /**
     * Add multiple productions for a single LHS to the grammar
     *
     * @param lhs  nonterminal on the LHS of the production
     * @param rhss Iterable of RHS's
     */
    public void addRules(Nonterminal lhs, Iterable<Symbol[]> rhss) {
        rhss.forEach(rhs -> addRule(lhs, rhs));
    }

    /**
     * Add multiple productions for a single LHS to the grammar
     *
     * @param lhs  nonterminal on the LHS of the production
     * @param rhss array of RHS's
     */
    public void addRules(Nonterminal lhs, Symbol[]... rhss) {
        Arrays.stream(rhss).forEach(rhs -> addRule(lhs, rhs));
    }

    // Shorthand constructors

    public static Terminal t(String v) {
        return new Terminal(v);
    }

    public static Nonterminal nt(String v) {
        return new Nonterminal(v);
    }
}
