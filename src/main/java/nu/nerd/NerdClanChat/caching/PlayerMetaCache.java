package nu.nerd.NerdClanChat.caching;


import nu.nerd.NerdClanChat.NerdClanChat;
import nu.nerd.NerdClanChat.database.PlayerMeta;

import java.util.HashMap;

public class PlayerMetaCache {


    public NerdClanChat plugin;
    private HashMap<String, PlayerMeta> playerMeta;
    private HashMap<String, Boolean> persisted;


    public PlayerMetaCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.playerMeta = new HashMap<String, PlayerMeta>();
        this.persisted = new HashMap<String, Boolean>();
    }


    public PlayerMeta getPlayerMeta(String UUID) {
        if (this.playerMeta.containsKey(UUID)) {
            return this.playerMeta.get(UUID); //cache hit
        } else {
            PlayerMeta pm = plugin.playerMetaTable.getPlayerMeta(UUID); //load from database on cache miss
            if (pm == null) { //if there isn't an entry for a player, create it
                pm = new PlayerMeta(UUID);
                try {
                    plugin.playerMetaTable.save(pm);
                } catch (Exception ex) {
                    plugin.getLogger().warning(ex.toString());
                }
            }
            this.playerMeta.put(UUID, pm);
            this.persisted.put(UUID, true);
            return pm;
        }
    }


    public boolean isMetaPersisted(String UUID) {
        if (!(this.persisted.containsKey(UUID))) {
            return false;
        }
        else {
            return this.persisted.get(UUID);
        }
    }


    public void setMetaPersisted(String UUID, boolean isPersisted) {
        if (this.persisted.containsKey(UUID)) {
            this.persisted.remove(UUID);
        }
        this.persisted.put(UUID, isPersisted);
    }


    public void updatePlayerMeta(String UUID, PlayerMeta meta) {
        if (this.playerMeta.containsKey(UUID)) {
            this.playerMeta.remove(UUID);
        }
        this.playerMeta.put(UUID, meta);
        this.setMetaPersisted(UUID, false);
    }


    //todo: persist cache, probably on a timer and plugin unload. Write meta entries that haven't been peristed yet


}
