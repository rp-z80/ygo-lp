# YGO LP — Yu-Gi-Oh! Life Points Counter

A tiny, fast and distraction-free **Life Points counter for Yu-Gi-Oh! duels**, built natively for Android with Kotlin and Jetpack Compose.

Put your phone on the table, tap, duel. That's it.

> **Disclaimer:** this is an unofficial, fan-made tool. It is not affiliated with, endorsed by or sponsored by Konami. *Yu-Gi-Oh!* is a trademark of its respective owners.

---

## Why this app exists

I built it because the official app is too heavy for what is, in the end, a simple job, and the other counters I tried weren't very functional at the table. I wanted something that opens instantly, stays out of the way, and just does the math.

## Features

- **Side-by-side layout** – both players' Life Points are shown next to each other, all oriented the same way, in large and easy-to-read numbers.
- **Landscape only** – designed to rest on the table between the two players (works in both landscape orientations).
- **Quick buttons, always visible** – `−1000`, `−500`, `−100`, `+100`, `+500`, `+1000` and `÷2` for each player (halving rounds up, as per the rules).
- **Full-screen calculator keypad** – tap a player's LP (or the `123` button) to open a big, calculator-style keypad, including `0`, `00` and `000`. It shows a live preview of the result for both subtraction and addition before you confirm. The keypad only appears when you ask for it, so the duel screen stays clean.
- **Undo** – one tap to revert the last change.
- **Scrollable history** – a detailed log of every LP change for both players (who, before → after, amount), handy to settle disputes.
- **Custom starting LP** – 4000, 8000, 16000 or any custom value.
- **Screen always on** – the display never goes to sleep while the app is in the foreground (no extra permissions required).
- **Dark, OLED-friendly design** – pure black background to reduce eye strain and save battery.
- **State is preserved** – LP and history are saved automatically, so a duel survives the app being closed or killed by the system.
- **No frills** – no ads, no analytics, no network access, no permissions, no heavy animations.

## Lightweight by design

- Only `compose.ui` and `compose.foundation` are used: no Material library, no extended icon packs.
- No ViewModel, navigation or dependency-injection frameworks; the whole state lives in a single small class.
- Release builds use R8 code shrinking and resource shrinking, plus baseline profiles installation for faster startup.

## Requirements

- Android 7.0 (API 24) or newer.

## Build from source

1. Clone the repository:
   ```bash
   git clone https://github.com/<your-username>/<your-repo>.git
   ```
2. Open the project folder in **Android Studio** (Koala or newer) and let Gradle sync.
3. Run the `app` configuration on a device or emulator.

To build an optimized APK, select the **release** build variant and use  
`Build > Generate App Bundles or APKs > Generate APKs`.  
By default the release variant is signed with the debug key so it can be installed right away; replace it with your own signing config if you plan to distribute it.

## Project structure

```
app/src/main/java/it/ygo/lifepoints/
├── MainActivity.kt   # Window setup (landscape, keep-screen-on, immersive mode), state persistence
├── DuelState.kt      # LP values, history, undo, save/restore
└── Ui.kt             # All Compose UI: player panels, keypad, history, new duel
```

## How it was made

This project is entirely **vibe-coded** with **Claude** and **Gemini**: I wrote the functional and technical specifications, and the AI models wrote the code. I reviewed and tested the result, but I'm not a professional Android developer, so expect rough edges and feel free to point them out.

## Contributing

Contributions are very welcome! In particular:

- **A graphics / UI rework would be greatly appreciated.** I'm not a designer, so the current look is purely functional. If you have ideas (or the skills) to make it nicer while keeping it light and fast, please open an issue or a pull request.
- Bug reports and suggestions via issues.

When contributing, please keep the project's spirit: minimal, fast, readable from a distance, and easy on battery.

## License

This project is released under the **GNU General Public License v3.0**. You are free to use, study, modify and share it under the terms of that license.  
See the [LICENSE](LICENSE) file for the full text, or <https://www.gnu.org/licenses/gpl-3.0.html>.
