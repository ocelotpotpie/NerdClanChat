package nu.nerd.NerdClanChat;

import org.bukkit.scheduler.BukkitRunnable;


public class PlayerMetaPersistTask extends BukkitRunnable {

    private final NerdClanChat plugin;

    public PlayerMetaPersistTask(NerdClanChat plugin) {
        this.plugin = plugin;
    }

    public void run() {
        plugin.getLogger().info("Writing player meta to persistence database...");
        plugin.playerMetaCache.persistCache();
        plugin.getLogger().info("Done.");
    }

}
