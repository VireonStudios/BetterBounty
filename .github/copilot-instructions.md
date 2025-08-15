# BetterBounty Minecraft Plugin

BetterBounty is a Java 21 Minecraft server plugin for Paper/Folia servers that provides a GUI-based bounty system with economy integration. Players can place and claim bounties through an intuitive interface with persistent SQLite storage.

Always reference these instructions first and fallback to search or bash commands only when you encounter unexpected information that does not match the info here.

## Working Effectively

### Environment Setup
- Install Java 21 (required): `sudo apt update && sudo apt install openjdk-21-jdk`
- Verify Java version: `java -version` (must show Java 21)
- Set JAVA_HOME if needed: `export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`

### Build and Test the Repository
- Build the plugin: `./gradlew clean build` -- takes 3-5 minutes. NEVER CANCEL. Set timeout to 10+ minutes.
- **NETWORK REQUIREMENT**: Build requires internet access to Maven repositories:
  - repo.papermc.io (Paper API)
  - repo.triumphteam.dev (GUI and Command libraries)
  - jitpack.io (SignGUI)
  - repo.tcoded.com (FoliaLib)
- **If build fails due to network restrictions**: Document as "Build fails due to firewall/network limitations preventing access to Maven repositories"
- Output jar: `build/libs/BetterBounty v<version>.jar`
- Clean build artifacts: `./gradlew clean`

### Testing and Validation
- **NO UNIT TESTS**: This project has no automated test suite
- **MANUAL VALIDATION REQUIRED**: You must test functionality using a Minecraft server
- **CRITICAL**: After making changes, always manually test:
  1. Plugin loads without errors on server startup
  2. `/bounty` command opens the GUI
  3. `/bounty add <player> <amount>` works with confirmation flow
  4. `/bounty remove <player>` works for admins
  5. `/bounty reload` reloads configuration
- **SERVER REQUIREMENT**: Testing requires Paper/Folia server 1.19.2+ with Vault and economy plugin

### Development Environment
- IDE: IntelliJ IDEA recommended (has Lombok support built-in)
- Enable annotation processing for Lombok
- Import as Gradle project
- Set Project SDK to Java 21

## Runtime Requirements

### Server Dependencies
- **Java**: 21 (OpenJDK or Oracle)
- **Server**: Paper or Folia (versions 1.19.2 - 1.21.8)
- **Required Plugins**: Vault + economy plugin (EssentialsX, CMI, etc.)
- **Database**: SQLite (auto-created at `plugins/BetterBounty/database.db`)

### Installation Process
1. Ensure Vault and economy plugin are installed
2. Drop BetterBounty jar into `plugins/` folder
3. Start server (generates config at `plugins/BetterBounty/config.yml`)
4. Edit config as needed, then `/bounty reload`

## Code Structure and Navigation

### Key Source Files
- **Main Plugin**: `src/main/java/dev/vireon/bounty/BountyPlugin.java`
- **Commands**: `src/main/java/dev/vireon/bounty/command/MainCommand.java`
- **GUI System**: `src/main/java/dev/vireon/bounty/gui/` (MainGui.java, ConfirmGui.java)
- **Database**: `src/main/java/dev/vireon/bounty/database/impl/SQLiteDatabase.java`
- **Economy**: `src/main/java/dev/vireon/bounty/economy/impl/VaultEconomyManager.java`
- **Configuration**: `src/main/java/dev/vireon/bounty/config/YamlConfig.java`

### Important Configuration Files
- **Plugin Manifest**: `src/main/resources/plugin.yml`
- **Default Config**: `src/main/resources/config.yml`
- **Build Config**: `build.gradle`

### External Dependencies
- **TriumphGUI**: GUI framework (`dev.triumphteam.gui`)
- **TriumphCMDs**: Command system (`dev.triumphteam.cmd`)
- **SignGUI**: Sign input dialogs (`de.rapha149.signgui`)
- **FoliaLib**: Scheduler compatibility (`com.tcoded.folialib`)
- **Vault API**: Economy integration (`net.milkbowl.vault`)
- **bStats**: Usage metrics (`org.bstats`)

## Common Development Tasks

### Adding New Commands
1. Add subcommand method in `MainCommand.java`
2. Use `@SubCommand("name")` annotation
3. Add permissions with `@Permission("bounty.permission")`
4. Register message keys in `BountyPlugin.registerCommands()`

### Modifying GUI Elements
1. Update config template in `src/main/resources/config.yml`
2. Modify GUI logic in `src/main/java/dev/vireon/bounty/gui/`
3. Test GUI changes require server restart and plugin reload

### Database Changes
1. Update queries in `src/main/java/dev/vireon/bounty/database/Queries.java`
2. Modify database implementation in `SQLiteDatabase.java`
3. **CRITICAL**: Always handle database migrations for existing data

### Configuration Updates
1. Update default config in `src/main/resources/config.yml`
2. Update config loading in `YamlConfig.java` if structure changes
3. Test with `/bounty reload` command

## Build System Details

### Gradle Tasks
- `./gradlew build` -- Full build with shadowJar (5 minutes)
- `./gradlew shadowJar` -- Create plugin jar only (3 minutes)
- `./gradlew clean` -- Clean build artifacts
- `./gradlew dependencies` -- Show dependency tree

### Shadow Plugin Configuration
- **Relocations**: All dependencies relocated to `dev.vireon.bounty.libraries.*`
- **Output**: `build/libs/BetterBounty v<version>.jar`
- **Includes**: All runtime dependencies bundled

### Dependency Management
- **Compile-only**: Paper API, Vault API, Lombok
- **Implementation**: TriumphGUI, TriumphCMDs, SignGUI, FoliaLib, bStats
- **Repositories**: Multiple Maven repos required (see network requirements above)

## Debugging and Troubleshooting

### Common Build Issues
- **Java version mismatch**: Ensure Java 21 is active
- **Network failures**: Repository access blocked by firewall
- **Dependency conflicts**: Check for version mismatches in build.gradle

### Runtime Issues
- **Plugin won't load**: Check server.log for dependency issues
- **Commands not working**: Verify Vault is installed and economy plugin active
- **Database errors**: Check file permissions on plugins/BetterBounty/database.db
- **GUI not opening**: Ensure player has required permissions

### Performance Considerations
- **Database**: Auto-saves every 15 minutes, saves on shutdown
- **GUI**: Player heads loaded asynchronously to prevent lag
- **Folia**: Uses FoliaLib schedulers for thread safety

## Version Control and CI

### GitHub Actions
- **Workflow**: `.github/workflows/gradle.yml`
- **Triggers**: Push to master/development, PRs to master
- **Java Version**: 21 (Temurin distribution)
- **Build Command**: `./gradlew build`
- **Artifacts**: Uploads built jar as GitHub artifact

### Important Notes
- **Folia Support**: Plugin supports both Paper and Folia
- **Economy Integration**: Requires Vault-compatible economy plugin
- **MiniMessage**: All text supports MiniMessage formatting
- **Auto-updates**: Plugin checks for updates on startup
- **Metrics**: Anonymous usage statistics via bStats

## Validation Scenarios

After making changes, ALWAYS test these complete scenarios:

### Basic Functionality Test
1. Start server with BetterBounty installed
2. Join as player and run `/bounty` - GUI should open
3. Run `/bounty add TestPlayer 1000` - confirmation GUI should appear
4. Click confirm - bounty should be added, money deducted
5. Check `/bounty` GUI shows the new bounty

### Admin Functions Test
1. As admin, run `/bounty remove TestPlayer` - should remove bounty
2. Run `/bounty reload` - should reload config without errors
3. Verify changes persist after server restart

### Economy Integration Test
1. Verify Vault is connected (check startup logs)
2. Test insufficient funds scenario
3. Test bounty claiming on player kill
4. Verify money transfers correctly through economy

**NEVER SKIP VALIDATION**: These manual tests are the only way to ensure the plugin works correctly since there are no automated tests.