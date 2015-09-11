package nu.nerd.NerdClanChat.database;


import nu.nerd.NerdClanChat.NerdClanChat;

public class InvitesTable {


    NerdClanChat plugin;


    public InvitesTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public void save(Channel channel) {
        plugin.getDatabase().save(channel);
    }


}
