package nu.nerd.NerdClanChat.database;


import com.avaje.ebean.Query;
import nu.nerd.NerdClanChat.NerdClanChat;

public class PlayerMetaTable {


    NerdClanChat plugin;


    public PlayerMetaTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public PlayerMeta getPlayerMeta(String UUID) {
        PlayerMeta pm = null;
        Query<PlayerMeta> query = plugin.getDatabase().find(PlayerMeta.class).where().ieq("UUID", UUID).query();
        if (query != null) {
            pm = query.findUnique();
        }
        return pm;
    }


    public void save(PlayerMeta playerMeta) {
        plugin.getDatabase().save(playerMeta);
    }


}
