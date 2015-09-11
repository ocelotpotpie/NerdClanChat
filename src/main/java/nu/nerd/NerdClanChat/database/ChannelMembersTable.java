package nu.nerd.NerdClanChat.database;


import com.avaje.ebean.Query;
import nu.nerd.NerdClanChat.NerdClanChat;

import java.util.HashMap;

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


    public void save(ChannelMember channelMember) {
        plugin.getDatabase().save(channelMember);
    }


}
