# HotCoffee-NX

A Nintendo Switch port of CinnamonCoffee for GTA V. Rewritten in SanScript.
Ships with both Chinese and English text.

中文说明：**[README.md](README.md)**

---

## Download

From the [Releases](https://github.com/Je11yb0ne/HotCoffee-NX/releases) page — pick one of four packages:

| Package | Language | Contents |
|---|---|---|
| `HotCoffee-NX_cinnamon_zh.zip` | Chinese | This mod only |
| `HotCoffee-NX_cinnamon_en.zip` | English | This mod only |
| `HotCoffee-NX_combo_zh.zip` | Chinese | This mod + the zombie mod "脏比洛圣都" |
| `HotCoffee-NX_combo_en.zip` | English | This mod + the zombie mod "脏比洛圣都" |

Each package contains **two files** (names are final, no renaming needed):

```
simple_zombies.nsc
error_listener.nsc
```

## Installation

1. **Back up** the original `error_listener.nsc` inside the RPF.
2. Open `<SD>:/atmosphere/contents/<title id>/romfs/update/update2.rpf` with an RPF tool
   (CodeWalker, RPF Explorer, …) and go to the inner archive
   `switch/levels/gta5/script/script_rel.rpf`.
3. **Replace** the original `error_listener.nsc` with the one from the package.
4. **Add** `simple_zombies.nsc` from the package as a new entry.
5. Both entries must be plain compressed file entries (not RPF resource entries, no RSC7 wrapper).

Do not rename the files: the script name is baked into the `.nsc` at compile time and the entry
script looks the mod up by that name. If they don't match, nothing happens in game.

## Usage

| Button | Action |
|---|---|
| **`B` + D-pad Right** | Open / close the main menu |
| D-pad ↑↓ | Move |
| `A` or D-pad ←→ | Confirm |
| `B` | Back |

**Interaction**

1. Main menu → "Interact".
2. In **Approach** mode (`Settings → Game Mode: Approach`) you approach first: pick a style, then
   pick a line. She only follows you after she accepts. **Sandbox** mode skips this step.
3. Interact menu: chat (how she's doing / her mood / her goals…), ask her preferences,
   test the waters, invite her into a vehicle, give money, say goodbye, end the relationship.
4. Reputation and relationship level decide what she accepts. Asking the same thing repeatedly
   costs reputation — lose enough and she walks off.
5. Main menu → "Settings": master switch, game mode, AI mode, voice and effect toggles.

**During a scene**

| Button | Action |
|---|---|
| D-pad ←→ (hold) | Change speed |
| `X` | Reset speed |
| **`B` + D-pad Right** | Abort |

While the menu is open the mod temporarily blocks its own buttons so other mods don't fight it for
input; camera controls are never touched.

## Notes

- The **combo** build requires "脏比洛圣都" first (not included here); the **cinnamon** build does not.
- Both builds use the same `simple_zombies.nsc` slot — install only one.
- Don't touch any entries other than the two files above.
- The artifact structure, script name and native table all pass the artifact checks; in-game
  behaviour has not been verified on hardware.

## License

Everything written for this project (port code, docs) is **MIT** — see [LICENSE](LICENSE).

## Credits

- **CinnamonCoffee** — the original GTA V PC (ScriptHookVDotNet) mod. This project's gameplay
  logic and much of its dialogue come from it (most English lines are its original text; the
  Chinese lines were translated/rewritten here). See its release page for the original author and
  license terms — check them before redistributing.
- **"脏比洛圣都" (Dirty Los Santos)** — the zombie mod this integrates with. Not included here.
