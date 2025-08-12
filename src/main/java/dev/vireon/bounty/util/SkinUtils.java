package dev.vireon.bounty.util;

import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.OfflinePlayer;

public class SkinUtils {

    public static String getSkin(OfflinePlayer player) {
        return player.getPlayerProfile().getProperties()
                .stream()
                .filter(property -> property.getName().equals("textures"))
                .map(ProfileProperty::getValue)
                .findFirst()
                .orElse(null);
    }

}
