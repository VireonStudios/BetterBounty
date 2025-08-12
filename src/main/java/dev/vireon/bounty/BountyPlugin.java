package dev.vireon.bounty;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import dev.triumphteam.cmd.bukkit.BukkitCommandManager;
import dev.triumphteam.cmd.bukkit.message.BukkitMessageKey;
import dev.triumphteam.cmd.core.exceptions.CommandRegistrationException;
import dev.triumphteam.cmd.core.message.MessageKey;
import dev.triumphteam.cmd.core.suggestion.SuggestionKey;
import dev.vireon.bounty.bounty.BountyManager;
import dev.vireon.bounty.command.MainCommand;
import dev.vireon.bounty.config.YamlConfig;
import dev.vireon.bounty.database.Database;
import dev.vireon.bounty.database.impl.SQLiteDatabase;
import dev.vireon.bounty.economy.EconomyManager;
import dev.vireon.bounty.economy.impl.VaultEconomyManager;
import dev.vireon.bounty.listener.ConnectionListener;
import dev.vireon.bounty.listener.DeathListener;
import dev.vireon.bounty.util.ChatUtils;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Getter
public final class BountyPlugin extends JavaPlugin {

    private final YamlConfig config = new YamlConfig(this, "config.yml", true);

    private final EconomyManager economyManager = new VaultEconomyManager();
    private final BountyManager bountyManager = new BountyManager(this);
    private final Database database = new SQLiteDatabase(this);

    private FoliaLib foliaLib;
    private PlatformScheduler scheduler;

    private BukkitCommandManager<CommandSender> commandManager;

    @Override
    public void onEnable() {
        foliaLib = new FoliaLib(this);
        scheduler = foliaLib.getScheduler();

        config.create();

        economyManager.init();

        bountyManager.init();

        database.onEnable();

        registerCommands();

        this.getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ConnectionListener(this), this);

        scheduler.runTimerAsync(database::saveAllBounties, 15, 15, TimeUnit.MINUTES); // Save bounties every 15 minutes

        new Metrics(this, 26889);
    }

    @Override
    public void onDisable() {
        this.getConfig().getStringList("settings.commands").forEach(this::unregisterCommand);
        database.onDisable();
        foliaLib.getScheduler().cancelAllTasks();
    }

    public void unregisterCommand(String name) {
        getBukkitCommands(getCommandMap()).remove(name);
    }

    @NotNull
    private CommandMap getCommandMap() {
        try {
            final Server server = Bukkit.getServer();
            final Method getCommandMap = server.getClass().getDeclaredMethod("getCommandMap");
            getCommandMap.setAccessible(true);

            return (CommandMap) getCommandMap.invoke(server);
        } catch (final Exception ignored) {
            throw new CommandRegistrationException("Unable get Command Map. Commands will not be registered!");
        }
    }

    // copied from triumph-cmd, credit goes to triumph-team
    @NotNull
    private Map<String, Command> getBukkitCommands(@NotNull final CommandMap commandMap) {
        try {
            final Field bukkitCommands = SimpleCommandMap.class.getDeclaredField("knownCommands");
            bukkitCommands.setAccessible(true);
            //noinspection unchecked
            return (Map<String, org.bukkit.command.Command>) bukkitCommands.get(commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new CommandRegistrationException("Unable get Bukkit commands. Commands might not be registered correctly!");
        }
    }

    private void registerCommands() {
        commandManager = BukkitCommandManager.create(this);

        commandManager.registerSuggestion(SuggestionKey.of("online-players"), (sender, context) ->
                Bukkit.getOnlinePlayers().stream().map(player -> {
                    player.getName();
                    return player.getName();
                }).toList());

        commandManager.registerCommand(new MainCommand(this));

        commandManager.registerMessage(MessageKey.INVALID_ARGUMENT, (sender, invalidArgumentContext) ->
                ChatUtils.sendMessage(sender, ChatUtils.format(config.getString("messages.invalid-command"))));

        commandManager.registerMessage(MessageKey.UNKNOWN_COMMAND, (sender, invalidArgumentContext) ->
                ChatUtils.sendMessage(sender, ChatUtils.format(config.getString("messages.invalid-command"))));

        commandManager.registerMessage(MessageKey.NOT_ENOUGH_ARGUMENTS, (sender, invalidArgumentContext) ->
                ChatUtils.sendMessage(sender, ChatUtils.format(config.getString("messages.invalid-command"))));

        commandManager.registerMessage(MessageKey.TOO_MANY_ARGUMENTS, (sender, invalidArgumentContext) ->
                ChatUtils.sendMessage(sender, ChatUtils.format(config.getString("messages.invalid-command"))));

        commandManager.registerMessage(BukkitMessageKey.NO_PERMISSION, (sender, invalidArgumentContext) ->
                ChatUtils.sendMessage(sender, ChatUtils.format(config.getString("messages.no-perm"))));
    }

}
