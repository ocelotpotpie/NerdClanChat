package nu.nerd.NerdClanChat.database;


import nu.nerd.NerdClanChat.NerdClanChat;

public class BulletinsTable {


    NerdClanChat plugin;


    public BulletinsTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public void save(Channel channel) {
        plugin.getDatabase().save(channel);
    }


}
