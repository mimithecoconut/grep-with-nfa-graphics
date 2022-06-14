import java.io.File
import java.io.InputStream
import Task2and4.Companion.NFA
import cfg.CFG
import impl.Timer

class BM(val prefix: String) {
    private val preBad = preprocessBad()
    private val preGood = preprocessGood()
    val galilPeriod = precomputeGalil()

    // B-M algorithm to find next occurrence of the prefix in the text on or after startInd
    fun getNext(text: String, prefix: String, startInd: Int) : Int {
        val testing = false
        if (startInd >= text.length) {
            return -1
        }
        if (prefix == "") {
            return startInd
        }
        val m = prefix.length - 1 // zero-indexed "length" of the pattern
        var offset = startInd; // offset of pattern from the start of the text
        var ind = prefix.length - 1; // index, from back to front, into pattern
        while (ind >= 0 && ind + offset < text.length) {
            val textInd = ind + offset
            if (testing) println("prefix[" + ind + "]: " + prefix[ind])
            if (testing) println("text[" + textInd + "]: " + text[textInd])
            if (prefix[ind] != text[textInd]) {
                // Mismatch occurs:
                var shift = 0;
                /*
                good:
                if full suffix exists, shift to align full suffix
                else if partial suffix is prefix, shift prefix to align with partial suffix
                else shift P past matched substring entirely
                bad:
                if mismatched char exists, align last instance of correct char
                else shift P past mismatched char
                take max of good and bad
                 */
                if (ind < m) { // If no substring matched, skip dealing with this case
                    if (preGood.first[ind + 1] != null && preGood.first[ind + 1] != -1) {
                        // Good case 1: full suffix exists, shift to align full suffix
                        shift = Math.max(shift, ind - preGood.first[ind + 1]!! + 1)
                        if (testing) println("good case 1, shift is " + (ind - preGood.first[ind + 1]!! + 1) + ", " + preGood.first[ind + 1])
                    }
                    else if (preGood.second[ind + 1] != null) {
                        // Good case 2: partial suffix is prefix, shift prefix to align with partial suffix if it exists
                        shift = Math.max(shift, ind - preGood.second[ind + 1]!! + 1)
                        if (testing) println("good case 2, shift is " + (ind - preGood.second[ind + 1]!! + 1))
                    }
                    else {
                        // Good case 3: shift P past matched substring entirely
                        shift = Math.max(shift, m + 1)
                        if (testing) println("good case 3, shift is " + (m + 1))
                    }
                }
                if (preBad[text[textInd]] != null) {
                    // Bad case 1: mismatched char exists, align last instance of correct char
                    shift = Math.max(shift, Math.max(1, ind - preBad[text[textInd]]!!))
                    if (testing) println("bad case 1, shift is " + Math.max(1, (ind - preBad[text[textInd]]!!)))
                }
                else {
                    // Bad case 2: shift P past mismatched char
                    shift = Math.max(shift, ind + 1)
                    if (testing) println("bad case 2, shift is " + (ind + 1))
                }
                // Otherwise, shift the pattern past the current substring (from preGood.second)
                offset += shift
                ind = prefix.length - 1 // reset index into pattern
            }
            else if (ind == 0) {
                // text matched completely
                return offset
            }
            else {
                ind--
            }
            if (testing) println()
        }
        return -1
    }

    // Implementation of the Boyer-Moore algorithm to find all occurrences of a prefix in the text
    fun getStarts(text: String, testing: Boolean) : List<Int> {
        if (prefix == "") return text.indices.asIterable().toList()
        val starts = mutableListOf<Int>()
        val m = prefix.length - 1 // zero-indexed "length" of the pattern
        var offset = 0; // offset of pattern from the start of the text
        var ind = prefix.length - 1; // index, from back to front, into pattern
        while (ind >= 0 && ind + offset < text.length) {
            val textInd = ind + offset
            if (testing) println("prefix[" + ind + "]: " + prefix[ind])
            if (testing) println("text[" + textInd + "]: " + text[textInd])
            if (prefix[ind] != text[textInd]) {
                // Mismatch occurs:
                var shift = 0;
                /*
                good:
                if full suffix exists, shift to align full suffix
                else if partial suffix is prefix, shift prefix to align with partial suffix
                else shift P past matched substring entirely
                bad:
                if mismatched char exists, align last instance of correct char
                else shift P past mismatched char
                take max of good and bad
                 */
                if (ind < m) { // If no substring matched, skip dealing with this case
                    if (preGood.first[ind + 1] != null && preGood.first[ind + 1] != -1) {
                        // Good case 1: full suffix exists, shift to align full suffix
                        shift = Math.max(shift, ind - preGood.first[ind + 1]!! + 1)
                        if (testing) println("good case 1, shift is " + (ind - preGood.first[ind + 1]!! + 1) + ", " + preGood.first[ind + 1])
                    }
                    else if (preGood.second[ind] != null) {
                        // Good case 2: partial suffix is prefix, shift prefix to align with partial suffix if it exists
                        shift = Math.max(shift, ind - preGood.second[ind + 1]!! + 1)
                        if (testing) println("good case 2, shift is " + (ind - preGood.second[ind + 1]!! + 1))
                    }
                    else {
                        // God case 3: shift P past matched substring entirely
                        shift = Math.max(shift, m + 1)
                        if (testing) println("good case 3, shift is " + (m + 1))
                    }
                }
                if (preBad[text[textInd]] != null) {
                    // Bad case 1: mismatched char exists, align last instance of correct char
                    shift = Math.max(shift, Math.max(1, ind - preBad[text[textInd]]!!))
                    if (testing) println("bad case 1, shift is " + Math.max(1, (ind - preBad[text[textInd]]!!)))
                }
                else {
                    // Bad case 2: shift P past mismatched char
                    shift = Math.max(shift, ind + 1)
                    if (testing) println("bad case 2, shift is " + (ind + 1))
                }
                // Otherwise, shift the pattern past the current substring (from preGood.second)
                offset += shift
                ind = prefix.length - 1 // reset index into pattern
            }
            else if (ind == 0) {
                // text matched completely
                starts.add(offset)
                // restart algorithm using galil
                if (testing) println("galil with period " + galilPeriod)
                offset += galilPeriod
                ind = prefix.length - 1
            }
            else {
                ind--
            }
            if (testing) println()
        }
        if (testing) {
            println(starts)
            println("\n")
        }
        return starts
    }

    private fun preprocessBad() : MutableMap<Char, Int> {
        val preprocess = HashMap<Char, Int>()
        for (i in prefix.indices) {
            preprocess[prefix[i]] = i
        }
        return preprocess
    }

    private fun preprocessGood() : Pair<MutableMap<Int, Int?>, MutableMap<Int, Int?>> {
        val preL = HashMap<Int, Int?>()
        var i = prefix.length - 1 // 0-indexed
        var j = prefix.length - 2
        while (j >= 0) {
            if (prefix[j] == prefix[i]) { //prefix starting at j matches suffix starting at i
                preL[i] = j
                i--
                j = i - 1
            }
            else {
                j--
            }
        }

        // preH differs from wikipedia
        // preH[j] = i if P[m-i..m) = P[0..i), aka the next suffix of P that is a also prefix of P
        val preH = HashMap<Int, Int?>()
        for (i in 2..prefix.length) {
            val j = prefix.length - i
            if (prefix.substring(0, i) == prefix.substring(j, prefix.length)) {
                preH[j] = i
            }
            else {
                preH[j] = preH[j + 1]
            }
        }
        return Pair(preL, preH)
    }

    private fun precomputeGalil() : Int {
        if (preGood.second[1] == null) {
            return 1
        }
        // period of repetition by https://stackoverflow.com/questions/38206841/boyer-moore-galil-rule
        return prefix.length - preGood.second[1]!!
    }
}
class Task5 {
    companion object {
        fun getStarts(prefix: String, text: String, testing: Boolean): List<Int> {
            val bm = BM(prefix)
            return bm.getStarts(text, testing)
        }

        // Linear time string search for efficiency comparison
        fun getStartsLinear(prefix: String, text: String, testing: Boolean): List<Int> {
            val starts = mutableListOf<Int>()
            for (i in 0..text.length - prefix.length) {
                if (text.substring(i, i + prefix.length) == prefix) {
                    starts.add(i)
                }
            }
            return starts
        }

        fun testBM(testing: Boolean) {
            assert(getStarts("", "hello", testing) == listOf(0, 1, 2, 3, 4))
            assert(getStarts("fox", "there is a foxy fox jumping over the lazy dog", testing) == listOf(11, 16)) // test general
            assert(getStarts("ababa", "bababababab", testing) == listOf(1, 3, 5)) // test galil
            assert(getStarts("ANAMPNAM", "MANPANAMANAMPNAM", testing) == listOf(8)) // test good rule 1
            assert(getStarts("NAMPNAM", "MANPANAMANAMPNAM", testing) == listOf(9)) // test good rule 1
            assert(getStarts("AMKKKKAM", "LLAMKKAMKKKAMKKKKAMLLL", testing) == listOf(11)) // test good rule 1

            println("Boyer-Moore Tests Passed")
        }

        fun testEffCFG(testing: Boolean) {
            val timer = Timer()
            timer.start()
            assert(getStartsLinear("fox", "there is a foxy fox jumping over the lazy dog", testing) == listOf(11, 16)) // test general
            println("linear: " + timer.end())
            timer.start()
            assert(getStarts("fox", "there is a foxy fox jumping over the lazy dog", testing) == listOf(11, 16)) // test general
            println("BM: " + timer.end())
            timer.start()
            assert(getStartsLinear("fox", "there is a foxy fox jumping over the lazy dog", testing) == listOf(11, 16)) // test general
            println("linear: " + timer.end())
            timer.start()
            assert(getStarts("fox", "there is a foxy fox jumping over the lazy dog", testing) == listOf(11, 16)) // test general
            println("BM: " + timer.end())
            timer.start()
            assert(getStartsLinear("ANAMPNAM", "MANPANAMANAMPNAM", testing) == listOf(8)) // test good rule 1 v1
            println("linear: " + timer.end())
            timer.start()
            assert(getStarts("ANAMPNAM", "MANPANAMANAMPNAM", testing) == listOf(8)) // test good rule 1 v1
            println("BM: " + timer.end())
            timer.start()
            assert(getStartsLinear("ANAMPNAM", "MANPANAMANAMPNAM", testing) == listOf(8)) // test good rule 1 v1
            println("linear: " + timer.end())
            timer.start()
            assert(getStarts("ANAMPNAM", "MANPANAMANAMPNAM", testing) == listOf(8)) // test good rule 1 v1
            println("BM: " + timer.end())
        }
    }
}