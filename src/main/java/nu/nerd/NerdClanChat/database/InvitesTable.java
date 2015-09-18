package nu.nerd.NerdClanChat.database;


import com.avaje.ebean.SqlUpdate;
import nu.nerd.NerdClanChat.NerdClanChat;

public class InvitesTable {


    NerdClanChat plugin;


    public InvitesTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public void deleteChannelInvites(String channel) {
        String query = "delete from clanchat_invites where channel=:channel";
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(query).setParameter("channel", channel);
        update.execute();
    }


    public void save(Channel channel) {
        plugin.getDatabase().save(channel);
    }


}
