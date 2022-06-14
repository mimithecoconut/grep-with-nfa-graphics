package graphics

import Task2and4
import cfg.ASTNode
import cfg.CFG
import cfg.EarleyParser
import java.awt.*
import javax.swing.*
import impl.Timer
import kotlin.math.ln
import kotlin.system.exitProcess


class KFrame(title: String) : JFrame() {
    init {
        setTitle(title)
        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        setSize(Vector.MAX.x.toInt(), Vector.MAX.y.toInt())
        setLocationRelativeTo(null)
        screenInputs()
        isVisible = true
        setFont(Font("default", Font.BOLD, 16));
    }

    fun drawNode(g: Graphics, center: Pair<Int, Int>, diameter: Int, accept: Boolean){
        g.drawOval(center.first - diameter / 2, center.second - diameter / 2, diameter, diameter)
        if (accept) {
            g.drawOval(center.first - diameter / 3, center.second - diameter / 3, diameter * 2 / 3, diameter * 2 / 3)
        }
    }

    fun drawLine(g: Graphics, start: Pair<Int, Int>, end: Pair<Int, Int>, label: String, change : Boolean){
        drawArrowLine(g, start.first, start.second, end.first, end.second)
        val midpointX= (start.first + end.first) / 2
        val midpointY = (start.second + end.second) / 2
        g.drawString(label, midpointX, midpointY)
        if (change){
            g.setColor(Color.GREEN)
        }
        g.drawString(label, midpointX , midpointY)
    }


    fun drawSelfLoop(g: Graphics, pos: Pair<Int, Int>, label: String) {
        val x = pos.first
        val y = pos.second
        g.drawOval(x - 20, y - 40, 40, 40)
        val xPoints = intArrayOf(x, x - 5, x - 5)
        val yPoints = intArrayOf(y, y - 5, y + 5)
        g.drawString(label, x, y - 40)
        g.fillPolygon(xPoints, yPoints, 3)
    }

    fun drawArrowLine(g: Graphics, x1: Int, y1: Int, x2: Int, y2: Int) {
        val d = 5
        val h = 5
        val dx = x2 - x1
        val dy = y2 - y1
        val D = Math.sqrt((dx * dx + dy * dy).toDouble())
        var xm = D - d
        var xn = xm
        var ym = h.toDouble()
        var yn = -h.toDouble()
        var x: Double
        val sin = dy / D
        val cos = dx / D
        x = xm * cos - ym * sin + x1
        ym = xm * sin + ym * cos + y1
        xm = x
        x = xn * cos - yn * sin + x1
        yn = xn * sin + yn * cos + y1
        xn = x
        val xPoints = intArrayOf(x2, xm.toInt(), xn.toInt())
        val yPoints = intArrayOf(y2, ym.toInt(), yn.toInt())
        g.drawLine(x1, y1, x2, y2)
        g.fillPolygon(xPoints, yPoints, 3)
    }

    fun highlightLetter(str : String, i: Int, j: Int): JLabel{
        return JLabel("<html><font size='5' color=black> ${str.subSequence(0,i)}</font> " +
                "<font size='6'color=green> ${str.subSequence(i,j)}</font>" +
                "<font size='5' color=black> ${str.subSequence(j, str.length)}</font></html>")
    }

    fun highlightNode(node: Task2and4.Companion.NFAState) {
        val g = graphics
        val pos = Pair(node.position.x.toInt(), node.position.y.toInt())
        val rad = (ln(1 + node.mass) * 50).toInt()
        if (node.accept) g.color = Color.GREEN
        else g.color = Color.YELLOW
        g.fillOval(pos.first - rad / 2, pos.second - rad / 2, rad, rad)
    }

    fun clearGraphics(){
        paintComponents(graphics)
    }

    fun screenInputs() {
        val panel = JPanel()
        val layout: LayoutManager = FlowLayout()
        panel.layout = layout
        val button3 = JButton("Click to construct the NFA")
        val label3 = JLabel()

        val button2 = JButton("Click to input a sentence to traverse")
        button2.setBounds(2000,2000,100,60)
        val label2 = JLabel()
        button2.addActionListener {
            val result = JOptionPane.showInputDialog(
                this,
                "Please input a sentence to traverse",
                "Sentence input",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "aaaa"
            ) as String
            if (result != null && result.length > 0) {
                label2.text = result
                label2.isVisible = true
                Timer.startTraverse(result)
            } else {
                label2.text = ""
            }
        }
        button2.isVisible
        label2.isVisible

        val button = JButton("Click to input a regular expression")
        button.setBounds(2000,2000,100,60)
        val label = JLabel()
        button.addActionListener {
            val result = JOptionPane.showInputDialog(
                this,
                "Please input a regular expression",
                "Regular expression input",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "a?a?(a*)?aaa+a"
            ) as String
            if (result != null && result.length > 0) {
                label.text = result
                Timer.resetNFA(result)
                if (Timer.nfa.getNumStates() == 0) {
                    JOptionPane.showMessageDialog(null, "Invalid regex expression")
                }
                else {
                    Timer.tick(false)
                    label3.text = "0"
                    button3.text = "Click to construct the NFA"
                }
            } else {
                label.text = ""
            }
        }

        label3.text = "0"
        button3.setBounds(2000,2000,100,60)
        button3.addActionListener {
            if (button3.text.equals("Click to construct the NFA")) {
                if (Timer.nfa.isConstructed()) {
                    val idx = label3.text.toInt()
                    Timer.nfa.drawConstructNFA(idx)
                    label3.text = (idx + 1).toString()
                    if (idx == Timer.nfa.getStates().size - 1) {
                        button3.text = "Click to play animation"
                    }
                }
            }
            else {
                Timer.nfa.playAnimation()
            }
        }
        label3.setVisible(false)

        val button4 = JButton("Reset positions")
        button4.setBounds(2000,2000,100,60)
        button4.addActionListener {
            Timer.nfa.reorganizeStates()
            Timer.tick(false)
            label3.text = "0"
            button3.text = "Click to construct the NFA"
        }

        panel.add(button)
        panel.add(label)
        panel.add(button2)
        panel.add(label2)
        panel.add(button3)
        panel.add(label3)
        panel.add(button4)
        this.contentPane.add(panel, BorderLayout.CENTER)
    }
}