package impl

import Task1
import Task3
import impl.Timer


fun main(args : Array<String>) {
//    val cfg = Task1.makeCFG()
//
//    Task3.matchFile(args[0], cfg, args[1])
    System.setProperty("java.awt.headless", "false")
    Timer.resetNFA("")
}
