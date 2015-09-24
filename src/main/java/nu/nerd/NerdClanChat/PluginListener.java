package nu.nerd.NerdClanChat;


import nu.nerd.NerdClanChat.database.Bulletin;
import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class PluginListener implements Listener {


    private final NerdClanChat plugin;


    public PluginListener(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        this.updateStoredPlayerName(event);
        this.printBulletins(event);
    }


    public void updateStoredPlayerName(PlayerJoinEvent event) {

        boolean isNewPlayer = false;
        String UUID = event.getPlayer().getUniqueId().toString();
        String name = event.getPlayer().getName();
        PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);

        if ( meta.getName() != null && !meta.getName().equals(name) ) {
            plugin.channelMembersTable.updateChannelMemberNames(UUID, name);
        }

        if (meta.getName() == null || meta.getName().equals("")) {
            isNewPlayer = true;
        }

        meta.setName(name);
        plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

        if (isNewPlayer) {
            plugin.playerMetaTable.save(meta);
            plugin.playerMetaCache.setMetaPersisted(UUID, true);
        }

    }


    public void printBulletins(PlayerJoinEvent event) {

        String UUID = event.getPlayer().getUniqueId().toString();
        List<ChannelMember> channels = plugin.transientPlayerCache.getChannelsForPlayer(UUID);
        Integer limit = plugin.config.BULLETIN_LIMIT;

        if (channels != null && channels.size() > 0) {
            for (ChannelMember cm : channels) {
                List<Bulletin> bulletins = plugin.channelCache.getBulletins(cm.getChannel());
                if (bulletins.size() > 0 && cm.isSubscribed()) {
                    Channel channel = plugin.channelCache.getChannel(cm.getChannel());
                    String tag = String.format("%s[%s] ", ChatColor.valueOf(channel.getColor()), channel.getName());
                    if (limit > 0 && bulletins.size() > limit) {
                        bulletins = bulletins.subList(bulletins.size()-limit, bulletins.size());
                    }
                    for (Bulletin bulletin : bulletins) {
                        String msg = tag + ChatColor.valueOf(channel.getAlertColor()) + bulletin.getMessage();
                        event.getPlayer().sendMessage(msg);
                    }
                }
            }
        }

    }


}
