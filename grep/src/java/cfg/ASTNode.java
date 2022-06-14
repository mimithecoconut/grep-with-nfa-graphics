package cfg;

import cfg.CFG.Symbol;

import java.util.Objects;

public class ASTNode {
    private final boolean isTerminal;
    private final ASTNode[] children;
    private final Symbol symbol;

    public ASTNode(Symbol symbol, ASTNode[] children, boolean isTerminal) {
        this.symbol = symbol;
        this.children = children;
        this.isTerminal = isTerminal;
    }

    public ASTNode(Symbol symbol, ASTNode[] children) {
        this(symbol, children, false);
    }

    public ASTNode(Symbol symbol) {
        this(symbol, null, true);
    }

    public String toString() {
        if (this.isTerminal) {
            return this.symbol.toString();
        } else {
            StringBuilder s = new StringBuilder();
            for (ASTNode child : this.getChildren()) {
                s.append(child.toString());
            }
            return s.toString();
        }
    }

    public boolean isTerminal() {
        return this.isTerminal;
    }

    public ASTNode[] getChildren() {
        return this.children;
    }

    public String getValue() {
        return this.symbol.toString();
    }

    public int numChildren() {
        return this.children == null ? 0 : this.children.length;
    }

    public ASTNode getLeftChild() {
        return this.children[0];
    }

    public ASTNode getRightChild() {
        return this.children[this.children.length - 1];
    }

    /**
     * Collapse the parse tree.
     *
     * @return a tree where no nonterminal node has a single child
     */
    public ASTNode collapse() {
        if (this.children == null || this.isTerminal)
            return this;

        // Collapse nodes with a single child
        if (children.length == 1)
            return children[0].collapse();

        for (int i = 0; i < children.length; i++) {
            children[i] = children[i].collapse();
        }
        return this;
    }
}
