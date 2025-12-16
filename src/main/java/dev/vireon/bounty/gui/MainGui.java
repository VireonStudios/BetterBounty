package dev.vireon.bounty.gui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import de.rapha149.signgui.SignGUI;
import de.rapha149.signgui.SignGUIAction;
import de.rapha149.signgui.exception.SignGUIVersionException;
import dev.triumphteam.gui.builder.item.PaperItemBuilder;
import dev.triumphteam.gui.builder.item.SkullBuilder;
import dev.triumphteam.gui.components.GuiAction;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.PaginatedGui;
import dev.vireon.bounty.BountyPlugin;
import dev.vireon.bounty.bounty.Bounty;
import dev.vireon.bounty.bounty.BountyIndex;
import dev.vireon.bounty.util.ChatUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MainGui {

    private static final Cache<UUID, Byte> CLICK_CACHE = CacheBuilder.newBuilder().expireAfterWrite(1, TimeUnit.SECONDS).build();

    public static void open(Player player, BountyIndex.SortField sort, @Nullable String filter, BountyPlugin plugin) {
        PaginatedGui gui = Gui.paginated()
                .title(ChatUtils.format(plugin.getConfig().getString("gui.title")))
                .rows(plugin.getConfig().getInt("gui.rows"))
                .pageSize(plugin.getConfig().getInt("gui.page-size"))
                .disableAllInteractions().create();

        Collection<Bounty> bounties = switch (sort) {
            case AMOUNT -> plugin.getBountyManager().getBountyMap().listAllByAmount();
            case NAME -> plugin.getBountyManager().getBountyMap().listByName();
            case LAST_UPDATED -> plugin.getBountyManager().getBountyMap().listAllByLastUpdated();
        };

        if (filter != null && !filter.isEmpty()) {
            bounties.removeIf(bounty -> !bounty.getPlayerName().toLowerCase().contains(filter.toLowerCase()));
        }

        for (Bounty bounty : bounties) {
            SkullBuilder builder;
            if (bounty.getSkinTexture() != null) builder = PaperItemBuilder.skull().texture(bounty.getSkinTexture());
            else builder = PaperItemBuilder.skull();

            gui.addItem(builder
                    .name(ChatUtils.format(plugin.getConfig().getString("gui.items.bounty.name"),
                            Placeholder.unparsed("player", bounty.getPlayerName())))
                    .lore(ChatUtils.format(plugin.getConfig().getStringList("gui.items.bounty.lore"),
                            Placeholder.unparsed("bounty", ChatUtils.FORMATTER.format(bounty.getAmount()))))
                    .asGuiItem());
        }

        setItem(plugin, "gui.items.info", sort, gui, event -> {
            if (CLICK_CACHE.getIfPresent(player.getUniqueId()) != null) {
                plugin.getScheduler().runAtEntity(player, task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f)));
                ChatUtils.sendMessage(player, ChatUtils.format(plugin.getConfig().getString("messages.click-cooldown")));
                return;
            }

            CLICK_CACHE.put(player.getUniqueId(), (byte) 0);

            plugin.getScheduler().runAtEntity(player, task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.success")), Sound.Source.MASTER, 1.0f, 1.0f)));
            open(player, sort, filter, plugin);
        });
        setItem(plugin, "gui.items.previous-page", sort, gui, event -> gui.previous());
        setItem(plugin, "gui.items.next-page", sort, gui, event -> gui.next());
        setItem(plugin, "gui.items.sort", sort, gui, event -> {
            if (CLICK_CACHE.getIfPresent(player.getUniqueId()) != null) {
                plugin.getScheduler().runAtEntity(player, task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f)));
                ChatUtils.sendMessage(player, ChatUtils.format(plugin.getConfig().getString("messages.click-cooldown")));
                return;
            }

            CLICK_CACHE.put(player.getUniqueId(), (byte) 0);

            plugin.getScheduler().runAtEntity(player, task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.success")), Sound.Source.MASTER, 1.0f, 1.0f)));
            open(player, sort.next(), filter, plugin);
        });
        setItem(plugin, "gui.items.search", sort, gui, event -> {
            if (plugin.getFoliaLib().isFolia()) {
                plugin.getScheduler().runAtEntity(player, _task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f)));
                return;
            }

            try {
                SignGUI signGUI = SignGUI.builder()
                        .setAdventureLines(ChatUtils.format(plugin.getConfig().getStringList("messages.sign.lines")).toArray())
                        .setType(Material.valueOf(plugin.getConfig().getString("messages.sign.material", "OAK_SIGN").toUpperCase()))
                        .setColor(DyeColor.valueOf(plugin.getConfig().getString("messages.sign.color", "WHITE").toUpperCase()))
                        .setGlow(plugin.getConfig().getBoolean("messages.sign.glow"))
                        .setHandler((p, result) -> {
                            String input = result.getLine(0);
                            if (input == null || input.isEmpty()) {
                                return List.of(
                                        SignGUIAction.run(() -> plugin.getScheduler().runAtEntity(player, task -> open(player, sort, null, plugin))),
                                        SignGUIAction.run(() -> plugin.getScheduler().runAtEntity(player, task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.fail")), Sound.Source.MASTER, 1.0f, 1.0f))))
                                );
                            }

                            return List.of(
                                    SignGUIAction.run(() -> plugin.getScheduler().runAtEntity(player, task -> open(player, sort, input, plugin))),
                                    SignGUIAction.run(() -> plugin.getScheduler().runAtEntity(player, task -> player.playSound(Sound.sound(Key.key(plugin.getConfig().getString("settings.sounds.success")), Sound.Source.MASTER, 1.0f, 1.0f))))
                            );
                        })
                        .build();

                signGUI.open(player);
            } catch (SignGUIVersionException e) {
                throw new RuntimeException(e);
            }
        });

        gui.open(player);
    }

    private static void setItem(BountyPlugin plugin, String path, BountyIndex.SortField sort, PaginatedGui gui, GuiAction<InventoryClickEvent> action) {
        if (plugin.getConfig().getInt(path + ".slot") < 0) return;

        gui.setItem(plugin.getConfig().getInt(path + ".slot"),
                PaperItemBuilder.from(Material.valueOf(plugin.getConfig().getString(path + ".material")))
                        .name(ChatUtils.format(plugin.getConfig().getString(path + ".name")))
                        .lore(ChatUtils.format(plugin.getConfig().getStringList(path + ".lore"),
                                Placeholder.parsed("sort", plugin.getConfig().getString("messages.sort-types." + sort.name(), sort.name()))))
                        .asGuiItem(action));
    }

}
