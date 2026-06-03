# BetterDo

[![Android CI](https://github.com/wangzisfa/better-do/actions/workflows/android.yml/badge.svg)](https://github.com/wangzisfa/better-do/actions/workflows/android.yml)

A native Android **model-in-the-loop** to‑do app. BetterDo isn't a plain list — an
AI agent (the mascot **"Dodo"**) reads your tasks, **derives** sensible follow‑up
to‑dos, **splits** tasks into steps, **comments** on each item, and **nudges** you —
all in a persona you choose: 温柔 *gentle* · 教练 *coach* · 毒舌 *savage*.

Built from a detailed design mockup with a warm, editorial "paper" aesthetic, full
light/dark themes, and an ember accent. The app icon is the blue folded‑check mark.

> **v1 scope:** the AI runs on a **mock, LLM‑ready** agent — the full designed
> experience works fully offline using curated sample responses, behind an
> `AgentService` interface a real model can drop into later.

## Features

- **Onboarding / import** — paste a messy list, import from calendar, or connect
  Todoist/Notion (stubbed), then watch the Agent "tidy" it and pick a persona.
- **Today** — date + toned greeting, the Agent's **morning brief**, focus / today /
  done sections, rich todo rows (tag · time · 🔥streak · "Agent 加的" badge · comment
  preview), and toned completion reactions.
- **Todo detail** — AI‑generated **subtasks** and a **discussion thread** between you
  and Dodo (likes + reply), with a "让 Agent 拆成几步" action.
- **Derive flow** *(signature)* — the Agent reads your day and suggests next‑step
  todos with per‑tone reasoning; accept / ignore / "让 Agent 再想想".
- **Review (复盘)** — completion ring, the Agent's toned recap, stats, carried‑over items.
- **Quick add** — jot something; the Agent acknowledges and offers a reminder.
- **Toned reminders** via WorkManager notifications; **light/dark**, **accent** swatches,
  persona switching in **Settings**.

## Tech stack

Kotlin · Jetpack Compose (Material 3) · Navigation‑Compose · Room · DataStore ·
WorkManager · Coroutines · kotlinx‑serialization. Manual DI (no Hilt). minSdk 26,
compile/target SDK 35.

## Project layout

```
app/src/main/java/com/betterdo/app/
  domain/model/       Todo, Subtask, Comment, Toned, enums (AgentTone, Tag, …)
  data/local/         Room (TodoEntity, TodoDao, BetterDoDatabase)
  data/prefs/         SettingsRepository (DataStore)
  data/repo/          TodoRepository
  data/seed/          SeedData — sample day + all toned copy (from the design)
  agent/              AgentService (seam) · MockAgentService (v1) · LlmAgentService (stub)
  notifications/      ReminderScheduler · ReminderWorker · NotificationHelper
  ui/theme/           warm‑paper tokens, typography, BetterDoTheme
  ui/components/       BdIcon · DodoAvatar · TodoRow · Cards · atoms · BottomBar
  ui/screens/         onboarding · today · detail · derive · review · quickadd · settings
  ui/nav/AppNav.kt    NavHost + bottom bar + quick‑add sheet
```

## Build & run

Requires the **Android SDK** (via Android Studio) and JDK 17+.

```bash
# In Android Studio: open the project → let Gradle sync → Run on a device/emulator.
# Or from the command line:
./gradlew assembleDebug      # build the debug APK
./gradlew installDebug       # install on a connected device/emulator
./gradlew test               # run the JVM unit tests (agent + seed logic)
```

The first build downloads dependencies from Google's Maven (`dl.google.com`) and
Maven Central, so it needs network access to those hosts.

### Fonts

The design's faces are **bundled** in `app/src/main/res/font` (both OFL-licensed):
**Instrument Serif** (italic display, Latin) and **Noto Sans SC** (body — the variable
font, so each weight maps to its `wght` axis). CJK glyphs absent from Instrument Serif
fall back to the system CJK face, matching the design's font stack.

## Continuous integration

`.github/workflows/android.yml` builds on every push / PR (and via **Run workflow**):
it runs the unit tests, builds the debug APK with `./gradlew assembleDebug`, and
uploads it as the **`betterdo-debug-apk`** artifact (download it from the run's summary
page on the Actions tab).

**Releases:** pushing a `v*` tag (e.g. `git tag v0.1.0 && git push origin v0.1.0`)
runs the same pipeline and then publishes a **GitHub Release** with the APK attached
as `betterdo-<tag>.apk` — a public, login-free download link. (The APK is debug-signed;
wire a release keystore via repo secrets for a Play-ready build.)

## Going live with a real model

Implement `LlmAgentService` (the contract + per‑tone prompt sketch are in the file)
and bind it instead of `MockAgentService` in `di/AppContainer.kt` — one line. Every
generative call returns *all three* tones so persona switching stays instant.

## Tests

`./gradlew test` runs `MockAgentServiceTest` and `SeedDataTest`, covering the derive
batches, tone resolution, and seed integrity (7 todos, AI source notes, every AI
comment authored in all three personas).
