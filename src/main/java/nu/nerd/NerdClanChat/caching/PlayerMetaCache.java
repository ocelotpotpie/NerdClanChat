/**
 * This is a memory-first cache that player commands can write to, and then
 * is persisted to the database on a schedule.
 */

package nu.nerd.NerdClanChat.caching;


import nu.nerd.NerdClanChat.NerdClanChat;
import nu.nerd.NerdClanChat.database.PlayerMeta;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerMetaCache {


    public NerdClanChat plugin;
    private ConcurrentHashMap<String, PlayerMeta> playerMeta;
    private ConcurrentHashMap<String, Boolean> persisted;


    public PlayerMetaCache(NerdClanChat plugin) {
        this.plugin = plugin;
        this.playerMeta = new ConcurrentHashMap<String, PlayerMeta>();
        this.persisted = new ConcurrentHashMap<String, Boolean>();
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


    public PlayerMeta getPlayerMetaByName(String name) {
        PlayerMeta retVal = null;
        for (PlayerMeta entry : this.playerMeta.values()) {
            if (entry.getName().equalsIgnoreCase(name)) {
                retVal = entry;
            }
        }
        if (retVal == null) {
            retVal = plugin.playerMetaTable.getPlayerMetaByName(name.toLowerCase());
        }
        return retVal;
    }


    public void persistCache() {
        plugin.getDatabase().beginTransaction();
        try {
            Iterator<Map.Entry<String, Boolean>> iterator = this.persisted.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Boolean> p = iterator.next();
                if (!p.getValue()) {
                    PlayerMeta meta = this.getPlayerMeta(p.getKey());
                    plugin.getDatabase().save(meta);
                    p.setValue(true);
                }
            }
            plugin.getDatabase().commitTransaction();
        } catch (Exception ex) {
            plugin.getLogger().warning(ex.toString());
        } finally {
            plugin.getDatabase().endTransaction();
        }
    }


}
