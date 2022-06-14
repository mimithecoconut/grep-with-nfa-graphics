package cfg;

import java.util.Arrays;
import java.util.Objects;

import cfg.CFG.Nonterminal;
import cfg.CFG.Symbol;

public class EarleyState implements Comparable<EarleyState> {
    public Nonterminal lhs;
    public Symbol[] rhs;
    public int rhsIdx;
    public int startIdx;

    public EarleyState leftParent;
    public EarleyState rightParent;

    public EarleyState(Nonterminal lhs, Symbol[] rhs, int rhsIdx, int startIdx) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.rhsIdx = rhsIdx;
        this.startIdx = startIdx;
    }

    public EarleyState(Nonterminal lhs, Symbol[] rhs, int startIdx) {
        this(lhs, rhs, 0, startIdx);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, Arrays.hashCode(rhs), rhsIdx, startIdx);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EarleyState))
            return false;

        return compareTo((EarleyState) obj) == 0;
    }

    public int compareTo(EarleyState x) {
        // Order rules by state first
        if (startIdx + rhsIdx != x.startIdx + x.rhsIdx) {
            return (startIdx + rhsIdx) - (x.startIdx + x.rhsIdx);
        }

        // Completed rules come first
        if (isDone() != x.isDone()) {
            return (rhs.length - rhsIdx) - (x.rhs.length - x.rhsIdx);
        }

        // \shrug, give up and arbitrary-compare at this point
        return toString().compareTo(x.toString());
    }

    public EarleyState advance() {
        return new EarleyState(this.lhs, this.rhs, this.rhsIdx + 1, this.startIdx);
    }

    public boolean isDone() {
        return this.rhsIdx == this.rhs.length;
    }

    public Symbol nextSymbol() {
        return this.rhs[this.rhsIdx];
    }

    @Override
    public String toString() {
        StringBuilder res = new StringBuilder(lhs.toString());
        res.append(" ->");
        for (int i = 0; i < rhs.length; i++) {
            if (i == rhsIdx) {
                res.append(" .");
            }
            res.append(" ").append(rhs[i].toString());
        }
        res.append(" ").append(startIdx);
        return res.toString();
    }
}
