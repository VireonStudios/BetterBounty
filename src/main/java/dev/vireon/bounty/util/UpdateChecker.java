package dev.vireon.bounty.util;

import dev.vireon.bounty.BountyPlugin;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.logging.Level;

@RequiredArgsConstructor
@Getter
public class UpdateChecker {

    private static final String URL = "https://raw.githubusercontent.com/VireonStudios/Versions/main/BetterBounty";

    private final BountyPlugin plugin;

    private String updateMessage;
    private boolean upToDate = true;

    public void checkUpdates() {
        String version = plugin.getDescription().getVersion();
        if (version.endsWith("DEV")) {
            plugin.getLogger().info("You are running a developer version of BetterBounty, skipping update check.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                HttpsURLConnection con = (HttpsURLConnection) URI.create(URL).toURL().openConnection();
                con.setUseCaches(false);
                InputStreamReader reader = new InputStreamReader(con.getInputStream());
                String[] split = (new BufferedReader(reader)).readLine().split(";");
                String latestVersion = split[0];
                updateMessage = split[1];
                this.upToDate = latestVersion.equals(version);

                if (!this.upToDate) {
                    this.plugin.getLogger().info(String.format("An update was found for %s!", plugin.getDescription().getName()));
                    this.plugin.getLogger().info("Update message:");
                    this.plugin.getLogger().info(updateMessage);
                } else
                    this.plugin.getLogger().info("Plugin is up to date, no update found.");
            } catch (IOException exception) {
                this.plugin.getLogger().log(Level.WARNING, "Could not check for updates!", exception);
            }
        });
    }

}