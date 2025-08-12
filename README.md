## BetterBounty ⚔️💰

⚡ Simple, fast, and Folia-supported bounty plugin for Minecraft servers. 🎮 Players can place bounties via a clean GUI,
search names, sort by amount, bounty or name, and claim rewards automatically on kill.

🛠️ Fully configurable with MiniMessage formatting, Vault economy support, and SQLite database.

### Key Features ✨

- 🖥️ **GUI-first**: Paginated GUI with player heads, refresh, search, and sort controls. **Exact replica of DonutSMP!**
  🍩
- 🔎 **Search**: In-game sign input to filter by player name
- 🔀 **Sorting**: Toggle between **Amount → Recently Set → Name**
- ✅ **Confirm flow**: Add bounty through a confirmation GUI
- 🏦 **Vault economy**: Money withdrawn on set; deposited to killer on kill
- 💾 **Auto-save**: Persists bounties to SQLite and saves every 15 minutes + on shutdown for more reliability
- 🍃 **Folia supported**: Uses FoliaLib schedulers; `folia-supported: true`
- ⚙️ **Config-driven**: All messages and GUI texts can be customized easily
- 🛡️ **Admin commands**: Remove bounties, without hassle of editing databases manually

### Requirements 📦

- ☕ **Java**: 21
- 🖥️ **Server**: Paper/Folia (`1.19.2 - 1.21.8`)
- 🔌 **Dependencies**: `Vault` and a Vault-compatible economy provider

### Installation 🚀

1. 🔌 Install `Vault` and your economy plugin
2. ⬇️ Download the latest `BetterBounty` jar
   from [GitHub Releases](https://github.com/VireonStudios/BetterBounty/releases/latest)
3. 📁 Drop the downloaded jar into `plugins/`
4. ▶️ Start the server and generate the config
5. ⚙️ Edit `plugins/BetterBounty/config.yml` as desired, then run `/bounty reload`

### Commands ⌨️

- 🪙 `/bounty` or `/bounties` — Open the bounties GUI (players only)
- ➕ `/bounty add <player> <amount>` — Start the confirm flow to add to a bounty (players only)
- ➖ `/bounty remove <player>` — Remove a player's bounty
- 🔄 `/bounty reload` — Reload the plugin config

### Permissions 🔐

- 🔑 `bounty.remove` — Allow `/bounty remove`
- 🔑 `bounty.reload` — Allow `/bounty reload`
- 🔑 `betterbounty.updatecheck` — Receive update notifications on join

### Notes 📝

- 👤 Adding bounties is player-only (not console)
- 🧭 Targets must have joined the server at least once to be referenced
- 🚫 Self-kills do not award bounty
- 🎨 Player skins and names are updated on join for more accurate visuals and sorting

### Configuration (MiniMessage) 🎨

All messages and GUI text use MiniMessage. Hex colors like `<#03fcb1>` are supported.
📖 Check [MiniMessage docs](https://docs.advntr.dev/minimessage/format.html) for more details.

### Data storage 🗄️

- 🗃️ SQLite database at: `plugins/BetterBounty/database.db`
- 💾 Auto-saves every 15 minutes and on plugin disable for reliability

### How it works (briefly) 🧠

- 💸 Placing a bounty deducts funds immediately using Vault
- 📋 Bounties are displayed in a paginated GUI with skull textures
- ⚔️💀 On death by another player, the killer is paid and the bounty is removed
- 🔄 Player name and skin texture are refreshed on join for accurate sorting and visuals

### Building from source 🛠️

This project uses Gradle with the Shadow plugin (relocations included).

```bash
./gradlew build
```

📦 Output: `build/libs/BetterBounty v<version>.jar`

### Metrics 📈

📊 This plugin uses bStats for anonymous usage statistics.\
You can disable it in `plugins/bStats/config.yml`.

### Libraries Used 📚

- [TriumphGUI](https://github.com/triumphteam/triumph-gui/): For everything GUI-related
- [TriumphCMDs](https://github.com/triumphteam/triumph-cmds): For command handling
- [SignGUI](https://github.com/Rapha149/SignGUI): For the sign input GUI
- [FoliaLib](https://github.com/TechnicallyCoded/FoliaLib): For Folia support and schedulers
- [Vault API](https://github.com/MilkBowl/VaultAPI): For economy support
- [bStats](https://bstats.org/): For anonymous usage statistics
- [Lombok](https://projectlombok.org/): For reducing boilerplate code

### License 📄

MIT — see `LICENSE`.
