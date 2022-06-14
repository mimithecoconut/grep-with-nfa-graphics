//! Earley Parsing for context-free-grammars.
#![cfg_attr(doctest, doc = "````no_test")] // highlight, but don't run the test (rust/issues/63193)
//! ```
//! let mut g = CFG::new("EXP");
//!
//! g.add_rule("EXP", vec![nt("EXP"), tr('-'), nt("EXP")]);
//! g.add_rule("EXP", vec![nt("TERM")]);
//!
//! g.add_rule("TERM", vec![nt("TERM"), tr('/'), nt("TERM")]);
//! g.add_rule("TERM", vec![nt("FACTOR")]);
//!
//! g.add_rule("FACTOR", vec![tr('('), nt("EXP"), tr(')')]);
//! for a in '0'..='9' {
//!     g.add_rule("FACTOR", vec![tr(a)]);
//! }
//!
//! assert!(parse("5--5", &g).is_none());
//! assert!(parse("5-5", &g).is_some());
//!
//! let result = parse("(5-5)/(2-3/4)", &g);
//! assert!(result.is_some());
//! println!("{:#?}", PrettyPrint(&result.unwrap().collapse()));
//! // TERM(FACTOR('(', EXP('5', '-', '5'), ')'), '/', FACTOR('(', EXP('2', '-', TERM('3', '/', '4')), ')'))
//! ````

use std::cmp;
use std::collections::{BTreeSet, HashMap, VecDeque};
use std::rc::Rc;

pub type Terminal = char;
pub type NonTerminal = String;

/// A sequence of `Symbol`s forms the right-hand-side of a CFG production.
#[derive(Debug, Clone, Hash, PartialEq, Eq, PartialOrd, Ord)]
pub enum Symbol {
    Terminal(Terminal),
    NonTerminal(NonTerminal),
}

impl Symbol {
    fn strval(&self) -> String {
        match self {
            Symbol::Terminal(ref c) => c.to_string(),
            Symbol::NonTerminal(ref s) => s.clone(),
        }
    }
}

/// Convenience function for creating a nonterminal `Symbol`
pub fn nt(x: impl Into<NonTerminal>) -> Symbol {
    Symbol::NonTerminal(x.into())
}

/// Convenience function for creating a terminal `Symbol`
pub fn tr(x: Terminal) -> Symbol {
    Symbol::Terminal(x)
}

/// A struct holding production rules for a CFG.
pub struct CFG {
    start: NonTerminal,
    rule_map: HashMap<NonTerminal, Vec<Vec<Symbol>>>,
    dummy: Vec<Vec<Symbol>>,
}

impl CFG {
    /// Initialize the CFG with the starting symbol.
    pub fn new(start: impl Into<NonTerminal>) -> Self {
        Self {
            start: start.into(),
            rule_map: HashMap::new(),
            dummy: Vec::new(),
        }
    }

    pub fn add_rule(&mut self, lhs: impl Into<NonTerminal>, rhs: Vec<Symbol>) {
        let lhs: NonTerminal = lhs.into();
        self.rule_map
            .entry(lhs)
            .or_insert_with(|| Vec::new())
            .push(rhs)
    }

    pub fn rules(&self, lhs: &NonTerminal) -> &[Vec<Symbol>] {
        self.rule_map.get(lhs).unwrap_or(&self.dummy).as_slice()
    }
}

#[derive(Debug, Clone)]
pub struct ASTNode {
    sym: Symbol,
    children: Vec<ASTNode>,
}

impl ASTNode {
    pub fn string_value(&self) -> String {
        match self.sym {
            Symbol::Terminal(ref c) => c.to_string(),
            Symbol::NonTerminal(ref s) => s.clone(),
        }
    }

    pub fn symbol(&self) -> &Symbol {
        &self.sym
    }

    pub fn is_terminal(&self) -> bool {
        match self.sym {
            Symbol::Terminal(_) => true,
            Symbol::NonTerminal(_) => false,
        }
    }

    pub fn children(&self) -> &[ASTNode] {
        self.children.as_slice()
    }

    /// Collapse the parse tree.
    /// Returns a tree where no nonterminal node has a single child.
    pub fn collapse(mut self) -> ASTNode {
        if self.children.is_empty() || self.is_terminal() {
            self
        } else if self.children.len() == 1 {
            return self.children.pop().unwrap().collapse();
        } else {
            self.children = self.children.into_iter().map(|c| c.collapse()).collect();
            self
        }
    }
}

#[derive(Debug, Clone)]
struct EarleyState {
    lhs: NonTerminal,
    rhs: Vec<Symbol>,
    rhs_idx: usize,
    start_idx: usize,

    left_parent: Option<Rc<EarleyState>>, // These are intern IDs
    right_parent: Option<Rc<EarleyState>>,
}

impl EarleyState {
    fn done(&self) -> bool {
        self.rhs_idx == self.rhs.len()
    }

    fn new(lhs: NonTerminal, rhs: Vec<Symbol>, start_idx: usize) -> Self {
        Self {
            lhs,
            rhs,
            start_idx,
            rhs_idx: 0,
            left_parent: None,
            right_parent: None,
        }
    }

    fn advance(&self) -> Self {
        Self {
            lhs: self.lhs.clone(),
            rhs: self.rhs.clone(),
            rhs_idx: self.rhs_idx + 1,
            start_idx: self.start_idx,
            left_parent: None,
            right_parent: None,
        }
    }

    fn next_sym(&self) -> Symbol {
        self.rhs[self.rhs_idx].clone()
    }
}

impl std::fmt::Display for EarleyState {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        write!(f, "{} ->", self.lhs)?;
        for i in 0..self.rhs.len() {
            if i == self.rhs_idx {
                write!(f, " .")?;
            }
            write!(f, " {}", self.rhs[i].strval())?;
        }
        write!(f, " {}", self.start_idx)
    }
}

impl cmp::Ord for EarleyState {
    fn cmp(&self, other: &EarleyState) -> cmp::Ordering {
        // Actual compare
        (self.start_idx + self.rhs_idx)
            .cmp(&(other.start_idx + other.rhs_idx))
            // by done
            .then_with(|| self.done().cmp(&other.done()))
            // doesn't matter, but needs to be consistent
            .then_with(|| self.lhs.cmp(&other.lhs))
            .then_with(|| self.rhs.cmp(&other.rhs))
            .then_with(|| self.rhs_idx.cmp(&other.rhs_idx))
            .then_with(|| self.start_idx.cmp(&other.start_idx))
    }
}

impl cmp::PartialOrd for EarleyState {
    fn partial_cmp(&self, other: &EarleyState) -> Option<cmp::Ordering> {
        Some(cmp::Ord::cmp(self, other))
    }
}

impl cmp::PartialEq for EarleyState {
    fn eq(&self, other: &EarleyState) -> bool {
        self.cmp(other) == cmp::Ordering::Equal
    }
}

impl cmp::Eq for EarleyState {}

/// Perform Earley parsing on the input using the given CFG.
pub fn parse(input: &str, grammar: &CFG) -> Option<ASTNode> {
    let mut mem = vec![BTreeSet::new(); input.len() + 1];
    for rhs in grammar.rules(&grammar.start) {
        mem[0].insert(Rc::new(EarleyState::new(
            grammar.start.clone(),
            rhs.clone(),
            0,
        )));
    }

    for i in 0..=input.len() {
        let mut q = mem[i].iter().map(|s| s.clone()).collect::<VecDeque<_>>();
        while let Some(curr_state) = q.pop_front() {
            if !curr_state.done() {
                match curr_state.next_sym() {
                    Symbol::NonTerminal(ref nt) => {
                        // predict
                        for rhs in grammar.rules(nt) {
                            let mut new_state =
                                Rc::new(EarleyState::new(nt.clone(), rhs.clone(), i));
                            if !mem[i].contains(&new_state) {
                                Rc::get_mut(&mut new_state).unwrap().left_parent =
                                    Some(Rc::clone(&curr_state));
                                mem[i].insert(Rc::clone(&new_state));
                                q.push_back(new_state);
                            }
                        }
                    }
                    Symbol::Terminal(t) => {
                        // Scan
                        if i < input.len() && t == input.as_bytes()[i] as char {
                            let mut new_state = Rc::new(curr_state.advance());
                            if !mem[i + 1].contains(&new_state) {
                                Rc::get_mut(&mut new_state).unwrap().left_parent =
                                    Some(Rc::clone(&curr_state));
                                mem[i + 1].insert(new_state);
                            }
                        }
                    }
                }
            } else {
                // Complete
                let iterlist = mem[curr_state.start_idx]
                    .iter()
                    .map(|s| Rc::clone(s))
                    .collect::<Vec<_>>();
                for state in iterlist.into_iter() {
                    if !state.done()
                        && state.next_sym() == Symbol::NonTerminal(curr_state.lhs.clone())
                    {
                        let mut new_state = Rc::new(state.advance());
                        if !mem[i].contains(&new_state) {
                            Rc::get_mut(&mut new_state).unwrap().left_parent =
                                Some(Rc::clone(&state));
                            Rc::get_mut(&mut new_state).unwrap().right_parent =
                                Some(Rc::clone(&curr_state));
                            mem[i].insert(Rc::clone(&new_state));
                            q.push_back(new_state);
                        }
                    }
                }
            }
        }
    }

    fn generate_parse_tree(state: Rc<EarleyState>) -> ASTNode {
        let mut iter = Rc::clone(&state);
        let mut children = Vec::new();
        for i in (0..state.rhs.len()).rev() {
            match state.rhs[i] {
                Symbol::NonTerminal(_) => children.insert(
                    0,
                    generate_parse_tree(Rc::clone(iter.right_parent.as_ref().unwrap())),
                ),
                Symbol::Terminal(tt) => children.insert(
                    0,
                    ASTNode {
                        sym: tr(tt),
                        children: Vec::new(),
                    },
                ),
            }
            iter = Rc::clone(iter.left_parent.as_ref().unwrap());
        }
        return ASTNode {
            sym: nt(state.lhs.clone()),
            children,
        };
    }

    mem[input.len()]
        .iter()
        .filter(|&s| s.lhs == grammar.start && s.start_idx == 0 && s.done())
        .nth(0)
        .map(|state| generate_parse_tree(Rc::clone(state)))
}

/// A struct with a pretty `Debug` impl for `ASTNode`s.
pub struct PrettyPrint<'a>(pub &'a ASTNode);

impl<'a> std::fmt::Debug for PrettyPrint<'a> {
    fn fmt(&self, f: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        match self.0.sym {
            Symbol::Terminal(c) => write!(f, "'{}'", c),
            Symbol::NonTerminal(ref s) => {
                let mut tup = f.debug_tuple(s);
                for child in self.0.children() {
                    tup.field(&PrettyPrint(child));
                }
                tup.finish()
            }
        }
    }
}

#[cfg(test)]
mod test {
    use super::*;

    #[test]
    fn test_arith() {
        let mut g = CFG::new("EXP");

        g.add_rule("EXP", vec![nt("EXP"), tr('-'), nt("EXP")]);
        g.add_rule("EXP", vec![nt("TERM")]);

        g.add_rule("TERM", vec![nt("TERM"), tr('/'), nt("TERM")]);
        g.add_rule("TERM", vec![nt("FACTOR")]);

        g.add_rule("FACTOR", vec![tr('('), nt("EXP"), tr(')')]);
        for a in '0'..='9' {
            g.add_rule("FACTOR", vec![tr(a)]);
        }

        assert!(parse("5--5", &g).is_none());
        assert!(parse("5-5", &g).is_some());

        let result = parse("(5-5)/(2-3/4)", &g);
        assert!(result.is_some());
        println!("{:?}", PrettyPrint(&result.unwrap().collapse()));
    }
}
