package nu.nerd.NerdClanChat.database;


import com.avaje.ebean.Query;
import com.avaje.ebean.SqlUpdate;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChannelMembersTable {


    NerdClanChat plugin;


    public ChannelMembersTable(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public HashMap<String, ChannelMember> getChannelMembers(String channel) {
        HashMap<String, ChannelMember> members = new HashMap<String, ChannelMember>();
        Query<ChannelMember> query = plugin.getDatabase().find(ChannelMember.class).where().ieq("channel", channel).query();
        if (query != null) {
            for (ChannelMember member : query.findList()) {
                members.put(member.getUUID(), member);
            }
        }
        return members;
    }


    public List<ChannelMember> getChannelsForPlayer(String UUID) {
        List<ChannelMember> channels = new ArrayList<ChannelMember>();
        Query<ChannelMember> query = plugin.getDatabase().find(ChannelMember.class).where().ieq("uuid", UUID).query();
        if (query != null) {
            for (ChannelMember channel : query.findList()) {
                channels.add(channel);
            }
        }
        return channels;
    }


    public void updateChannelMemberNames(String UUID, String newName) {
        String query = "update clanchat_members set name=:name where uuid=:uuid";
        SqlUpdate update = plugin.getDatabase().createSqlUpdate(query)
                .setParameter("name", newName)
                .setParameter("uuid", UUID);
        update.execute();
    }


    public void save(ChannelMember channelMember) {
        plugin.getDatabase().save(channelMember);
    }


}
