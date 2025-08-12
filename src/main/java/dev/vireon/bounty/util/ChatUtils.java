package dev.vireon.bounty.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

public class ChatUtils {

    public static final NumberFormat FORMATTER = NumberFormat.getCompactNumberInstance();
    public static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder().character('&').hexColors().build();
    public static final LegacyComponentSerializer LEGACY_AMPERSAND = LegacyComponentSerializer.builder().hexColors().build();
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private final static MiniMessage MINI_MESSAGE = MiniMessage.builder()
            .tags(TagResolver.standard())
            .postProcessor(component -> component.decoration(TextDecoration.ITALIC, false))
            .build();

    static {
        FORMATTER.setMinimumIntegerDigits(1);
        FORMATTER.setMaximumIntegerDigits(20);
        FORMATTER.setMaximumFractionDigits(2);
    }

    public static String fromLegacy(Component component) {
        return LEGACY_AMPERSAND.serialize(component);
    }

    public static Component colorLegacyString(String string) {
        return LEGACY.deserialize(string);
    }

    public static String toLegacy(String string, TagResolver... placeholders) {
        Component component = ChatUtils.format(string, placeholders);
        return LEGACY.serialize(component);
    }

    public static List<String> toLegacy(List<String> list, TagResolver... placeholders) {
        return list.stream().map(s -> toLegacy(s, placeholders)).collect(Collectors.toList());
    }

    public static Component format(String string, TagResolver... placeholders) {
        return MINI_MESSAGE.deserialize(string, placeholders);
    }

    public static List<Component> format(List<String> list, TagResolver... placeholders) {
        return list.stream().map(s -> MINI_MESSAGE.deserialize(s, placeholders)).collect(Collectors.toList());
    }

    public static void sendMessage(CommandSender player, Component component) {
        player.sendMessage(component);
    }

    public static void sendMessage(CommandSender player, List<Component> components) {
        components.forEach(s -> ChatUtils.sendMessage(player, s));
    }

}