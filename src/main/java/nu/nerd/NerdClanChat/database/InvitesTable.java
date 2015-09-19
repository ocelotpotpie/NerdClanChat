package nu.nerd.NerdClanChat.database;


import com.avaje.ebean.Query;
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


    public boolean alreadyInvited(String UUID, String channel) {
        Query<Invite> query = plugin.getDatabase().find(Invite.class).where()
                .ieq("uuid", UUID)
                .ieq("channel", channel)
                .query();
        if (query != null) {
            if (query.findRowCount() > 0) return true;
        }
        return false;
    }


    public void save(Invite invite) {
        plugin.getDatabase().save(invite);
    }


}
