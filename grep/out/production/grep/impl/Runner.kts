package impl

import cfg.ASTNode
import cfg.CFG
import cfg.EarleyParser
import graphics.Draw.*
import impl.Timer.Companion.resetNFA
import impl.Timer.Companion.tick
import javax.swing.JLabel

fun getUserInput() : Task2and4.Companion.NFA {
    // Takes input of regex expression and converts it to a NFA
    print("Enter a regex expression: ")
    val regex = readLine()
    println("The expression you entered is: $regex")
    val cfg: CFG = Task1.makeCFG()
    val tree: ASTNode = EarleyParser.parse(regex, cfg)
    val nfa : Task2and4.Companion.NFA = Task2and4.Companion.NFA()
    nfa.constructNFA(tree)
    nfa.setMasses()
    nfa.pruneNodes()
    return nfa
}

fun main() {
    System.setProperty("java.awt.headless", "false")
    resetNFA("")
//    Task3.testMatchFile(Task1.makeCFG())
}


main()