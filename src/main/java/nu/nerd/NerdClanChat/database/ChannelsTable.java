package nu.nerd.NerdClanChat.database;


import com.avaje.ebean.Query;
import nu.nerd.NerdClanChat.NerdClanChat;

public class ChannelsTable {


    NerdClanChat plugin;


    public ChannelsTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public Channel getChannel(String name) {
        Channel ch = null;
        Query<Channel> query = plugin.getDatabase().find(Channel.class).where().ieq("name", name).query();
        if (query != null) {
            ch = query.findUnique();
        }
        return ch;
    }


    public boolean channelExists(String name) {
        Query<Channel> query = plugin.getDatabase().find(Channel.class).where().ieq("name", name).query();
        return query != null;
    }


    public void save(Channel channel) {
        plugin.getDatabase().save(channel);
    }


}
