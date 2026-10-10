package it.ygo.lifepoints

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue

class Entry(val player: Int, val before: Int, val after: Int, val label: String)

/** Stato del duello: due contatori LP + cronologia. Salvato in SharedPreferences quando l'app va in background. */
class DuelState {
    var startLp by mutableIntStateOf(8000)
        private set
    val lp = mutableStateListOf(8000, 8000)
    val history = mutableStateListOf<Entry>()

    private fun commit(p: Int, newValue: Int, label: String) {
        val before = lp[p]
        val after = newValue.coerceAtLeast(0)
        if (after == before) return
        lp[p] = after
        history.add(Entry(p, before, after, label))
    }

    fun add(p: Int, delta: Int) = commit(p, lp[p] + delta, if (delta > 0) "+$delta" else "−${-delta}")

    /** Dimezza arrotondando per eccesso (regola ufficiale). */
    fun half(p: Int) = commit(p, (lp[p] + 1) / 2, "÷2")

    fun undo() {
        val e = history.removeLastOrNull() ?: return
        lp[e.player] = e.before
    }

    fun reset(start: Int) {
        startLp = start
        lp[0] = start
        lp[1] = start
        history.clear()
    }

    fun save(sp: SharedPreferences) {
        sp.edit()
            .putInt("start", startLp)
            .putInt("l0", lp[0])
            .putInt("l1", lp[1])
            .putString("h", history.takeLast(300).joinToString(";") { "${it.player},${it.before},${it.after},${it.label}" })
            .apply()
    }

    fun restore(sp: SharedPreferences) {
        startLp = sp.getInt("start", 8000)
        lp[0] = sp.getInt("l0", startLp)
        lp[1] = sp.getInt("l1", startLp)
        history.clear()
        sp.getString("h", "")?.split(';')?.forEach { s ->
            val f = s.split(',')
            if (f.size == 4) {
                val p = f[0].toIntOrNull()
                val b = f[1].toIntOrNull()
                val a = f[2].toIntOrNull()
                if (p != null && b != null && a != null) history.add(Entry(p, b, a, f[3]))
            }
        }
    }
}
