package it.ygo.lifepoints

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Panel = Color(0xFF111113)
private val KeyBg = Color(0xFF232327)
private val KeyPressed = Color(0xFF45454D)
private val Dmg = Color(0xFFFF6B6B)
private val Heal = Color(0xFF5BD98C)
private val Dim = Color(0xFF8A8A93)
private val P1 = Color(0xFF5CC8FF)
private val P2 = Color(0xFFFFB347)

private enum class Overlay { None, History, NewDuel }

@Composable
private fun KeyBtn(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    size: TextUnit = 18.sp,
    onClick: () -> Unit,
) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    Box(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (pressed) KeyPressed else KeyBg)
            .clickable(interactionSource = src, indication = null, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text, style = TextStyle(color = color, fontSize = size, fontWeight = FontWeight.Bold), maxLines = 1)
    }
}

@Composable
private fun DigitRow(modifier: Modifier, labels: List<String>, onKey: (String) -> Unit) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        labels.forEach { l ->
            KeyBtn(l, Color.White, Modifier.weight(1f).fillMaxHeight(), 24.sp) { onKey(l) }
        }
    }
}

@Composable
fun App(st: DuelState) {
    var overlay by remember { mutableStateOf(Overlay.None) }
    var selected by remember { mutableStateOf(-1) }
    BackHandler(overlay != Overlay.None || selected >= 0) {
        if (overlay != Overlay.None) overlay = Overlay.None else selected = -1
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .windowInsetsPadding(WindowInsets.displayCutout)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Giocatori affiancati, tutto leggibile dallo stesso verso
            Row(Modifier.weight(1f).fillMaxWidth()) {
                PlayerHalf(0, st, selected == 0, Modifier.weight(1f).fillMaxHeight()) { selected = 0 }
                PlayerHalf(1, st, selected == 1, Modifier.weight(1f).fillMaxHeight()) { selected = 1 }
            }
            CenterBar(st, onHistory = { overlay = Overlay.History }, onNew = { overlay = Overlay.NewDuel })
        }
        if (selected >= 0) {
            // Single shared full-screen keypad: acts on the selected player
            Keypad(
                lp = st.lp[selected],
                otherLp = st.lp[1 - selected],
                player = selected,
                onApply = { st.add(selected, it) },
                onClose = { selected = -1 },
            )
        }
        when (overlay) {
            Overlay.History -> HistoryPanel(st) { overlay = Overlay.None }
            Overlay.NewDuel -> NewDuelPanel(st) { overlay = Overlay.None }
            Overlay.None -> Unit
        }
    }
}

@Composable
private fun CenterBar(st: DuelState, onHistory: () -> Unit, onNew: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(44.dp).padding(horizontal = 6.dp).padding(bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        KeyBtn(
            "↶  UNDO",
            if (st.history.isEmpty()) Dim else Color.White,
            Modifier.weight(2f).fillMaxHeight(), 17.sp,
        ) { st.undo() }
        KeyBtn("HISTORY", Dim, Modifier.weight(1.5f).fillMaxHeight(), 14.sp, onHistory)
        KeyBtn("NEW", Dim, Modifier.weight(1f).fillMaxHeight(), 14.sp, onNew)
    }
}

@Composable
private fun PlayerHalf(p: Int, st: DuelState, isSelected: Boolean, modifier: Modifier, onSelect: () -> Unit) {
    val accent = if (p == 0) P1 else P2
    val lp = st.lp[p]
    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier
            .padding(6.dp)
            .clip(shape)
            .background(Panel)
            .then(if (isSelected) Modifier.border(2.dp, accent, shape) else Modifier)
    ) {
        Column(
            Modifier.fillMaxSize().padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            BoxWithConstraints(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null, onClick = onSelect),
                contentAlignment = Alignment.Center,
            ) {
                val fs = minOf(maxHeight.value * 0.75f, maxWidth.value * 0.28f).coerceAtLeast(24f).sp
                BasicText(
                    lp.toString(),
                    style = TextStyle(
                        color = if (lp == 0) Dmg else Color.White,
                        fontSize = fs,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 1,
                )
                BasicText(
                    "PLAYER ${p + 1}",
                    Modifier.align(Alignment.TopStart),
                    style = TextStyle(color = accent, fontSize = 12.sp, fontWeight = FontWeight.Bold),
                )
                BasicText(
                    "tap for keypad",
                    Modifier.align(Alignment.TopEnd),
                    style = TextStyle(color = Dim, fontSize = 11.sp),
                )
            }
            Row(Modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (v in listOf(1000, 500, 100)) {
                    KeyBtn("−$v", Dmg, Modifier.weight(1f).fillMaxHeight(), 17.sp) { st.add(p, -v) }
                }
                KeyBtn("÷2", Color.White, Modifier.weight(1f).fillMaxHeight(), 18.sp) { st.half(p) }
            }
            Row(Modifier.fillMaxWidth().height(46.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (v in listOf(1000, 500, 100)) {
                    KeyBtn("+$v", Heal, Modifier.weight(1f).fillMaxHeight(), 17.sp) { st.add(p, v) }
                }
                KeyBtn("123", Dim, Modifier.weight(1f).fillMaxHeight(), 16.sp, onSelect)
            }
        }
    }
}

@Composable
private fun PadRow(
    modifier: Modifier,
    digits: List<String>,
    opLabel: String,
    opColor: Color,
    onDigit: (String) -> Unit,
    onOp: () -> Unit,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        digits.forEach { d ->
            KeyBtn(d, Color.White, Modifier.weight(1f).fillMaxHeight(), 34.sp) { onDigit(d) }
        }
        KeyBtn(opLabel, opColor, Modifier.weight(1f).fillMaxHeight(), 38.sp, onOp)
    }
}

@Composable
private fun Keypad(lp: Int, otherLp: Int, player: Int, onApply: (Int) -> Unit, onClose: () -> Unit) {
    val accent = if (player == 0) P1 else P2
    var input by remember { mutableStateOf("") }
    val v = input.toIntOrNull() ?: 0

    fun apply(sign: Int) {
        val amount = input.toIntOrNull() ?: return
        if (amount > 0) onApply(sign * amount)
        onClose()
    }

    OverlayBox {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Left: player info, typed amount and result preview
            Column(
                Modifier
                    .weight(0.9f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Panel)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                BasicText("PLAYER ${player + 1}", style = TextStyle(color = accent, fontSize = 18.sp, fontWeight = FontWeight.Bold))
                BasicText("LP $lp", style = TextStyle(color = Dim, fontSize = 30.sp, fontWeight = FontWeight.Bold))
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    BasicText(
                        if (input.isEmpty()) "0" else input,
                        style = TextStyle(color = Color.White, fontSize = 56.sp, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                    )
                }
                Column(Modifier.fillMaxWidth().height(60.dp)) {
                    if (v > 0) {
                        BasicText("−  ${(lp - v).coerceAtLeast(0)}", style = TextStyle(color = Dmg, fontSize = 24.sp, fontWeight = FontWeight.Bold))
                        BasicText("+  ${lp + v}", style = TextStyle(color = Heal, fontSize = 24.sp, fontWeight = FontWeight.Bold))
                    }
                }
                BasicText("PLAYER ${(1 - player) + 1}: $otherLp", style = TextStyle(color = Dim, fontSize = 14.sp))
                KeyBtn("✕  CLOSE", Dim, Modifier.fillMaxWidth().height(48.dp), 16.sp, onClose)
            }
            // Right: calculator-style keypad
            Column(Modifier.weight(1.6f).fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val digit: (String) -> Unit = { input = appendDigits(input, it) }
                val rowMod = Modifier.weight(1f).fillMaxWidth()
                PadRow(rowMod, listOf("7", "8", "9"), "⌫", Color.White, digit) { input = input.dropLast(1) }
                PadRow(rowMod, listOf("4", "5", "6"), "C", Color.White, digit) { input = "" }
                PadRow(rowMod, listOf("1", "2", "3"), "−", Dmg, digit) { apply(-1) }
                PadRow(rowMod, listOf("0", "00", "000"), "+", Heal, digit) { apply(1) }
            }
        }
    }
}

private fun appendDigits(cur: String, s: String): String {
    val n = (cur + s).trimStart('0')
    return if (n.length > 6) cur else n
}

@Composable
private fun OverlayBox(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xF5000000))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .padding(12.dp)
    ) { content() }
}

@Composable
private fun HistoryPanel(st: DuelState, onClose: () -> Unit) {
    val items = st.history.toList().asReversed()
    OverlayBox {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    "HISTORY",
                    Modifier.weight(1f),
                    style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                )
                KeyBtn("CLOSE", Color.White, Modifier.width(120.dp).fillMaxHeight(), 15.sp, onClose)
            }
            if (items.isEmpty()) {
                BasicText("No changes yet", style = TextStyle(color = Dim, fontSize = 18.sp))
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    itemsIndexed(items) { i, e ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            BasicText("#${items.size - i}", Modifier.width(64.dp), style = TextStyle(color = Dim, fontSize = 16.sp))
                            BasicText(
                                "P${e.player + 1}",
                                Modifier.width(48.dp),
                                style = TextStyle(color = if (e.player == 0) P1 else P2, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            )
                            BasicText(
                                "${e.before}  →  ${e.after}",
                                Modifier.weight(1f),
                                style = TextStyle(color = Color.White, fontSize = 20.sp),
                            )
                            BasicText(
                                e.label,
                                style = TextStyle(
                                    color = if (e.label.startsWith("+")) Heal else Dmg,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NewDuelPanel(st: DuelState, onClose: () -> Unit) {
    var custom by remember { mutableStateOf("") }
    OverlayBox {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicText(
                    "NEW DUEL · Starting LP",
                    Modifier.weight(1f),
                    style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                )
                KeyBtn("CANCEL", Color.White, Modifier.width(120.dp).fillMaxHeight(), 15.sp, onClose)
            }
            Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(4000, 8000, 16000).forEach { v ->
                    KeyBtn("$v", if (v == st.startLp) P1 else Color.White, Modifier.weight(1f).fillMaxHeight(), 32.sp) {
                        st.reset(v); onClose()
                    }
                }
            }
            Row(
                Modifier.weight(1f).fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                BasicText(
                    custom.ifEmpty { "Custom" },
                    Modifier.weight(2f),
                    style = TextStyle(color = if (custom.isEmpty()) Dim else Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold),
                )
                KeyBtn("START", Heal, Modifier.weight(1f).fillMaxHeight(), 22.sp) {
                    val v = custom.toIntOrNull()
                    if (v != null && v > 0) { st.reset(v); onClose() }
                }
            }
            DigitRow(
                Modifier.weight(1f).fillMaxWidth(),
                listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "⌫"),
            ) { custom = if (it == "⌫") custom.dropLast(1) else appendDigits(custom, it) }
        }
    }
}
