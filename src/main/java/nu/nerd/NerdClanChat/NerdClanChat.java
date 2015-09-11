/**
 * NerdClanChat
 * Java port of CHClanchat (https://github.com/NerdNu/CHClanChat)
 * by redwall_hp (http://github.com/redwallhp)
 */

package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.caching.*;
import nu.nerd.NerdClanChat.database.*;
import org.bukkit.plugin.java.JavaPlugin;

import javax.persistence.PersistenceException;
import java.util.ArrayList;
import java.util.Map;


public final class NerdClanChat extends JavaPlugin {


    public ChannelsTable channelsTable;
    public ChannelMembersTable channelMembersTable;
    public PlayerMetaTable playerMetaTable;
    public BulletinsTable bulletinsTable;
    public InvitesTable invitesTable;

    public ChannelCache channelCache;
    public PlayerMetaCache playerMetaCache;


    @Override
    public void onEnable() {

        // Database
        this.setUpDatabase();
        this.channelsTable = new ChannelsTable(this);
        this.channelMembersTable = new ChannelMembersTable(this);
        this.playerMetaTable = new PlayerMetaTable(this);
        this.bulletinsTable = new BulletinsTable(this);
        this.invitesTable = new InvitesTable(this);

        // Cache
        this.channelCache = new ChannelCache(this);
        this.playerMetaCache = new PlayerMetaCache(this);

        // Commands
        ChatCommands chatCommands = new ChatCommands(this);
        this.getCommand("clanchat").setExecutor(new ClanChatCommand(this));
        this.getCommand("c").setExecutor(chatCommands);
        this.getCommand("cq").setExecutor(chatCommands);
        this.getCommand("ca").setExecutor(chatCommands);
        this.getCommand("cme").setExecutor(chatCommands);
        this.getCommand("cs").setExecutor(chatCommands);

    }


    public boolean setUpDatabase() {
        try {
            getDatabase().find(Channel.class).findRowCount();
        } catch (PersistenceException ex) {
            getLogger().info("Initializing database.");
            installDDL();
            return true;
        }
        return false;
    }


    @Override
    public ArrayList<Class<?>> getDatabaseClasses() {
        ArrayList<Class<?>> list = new ArrayList<Class<?>>();
        list.add(Channel.class);
        list.add(ChannelMember.class);
        list.add(PlayerMeta.class);
        list.add(Bulletin.class);
        list.add(Invite.class);
        return list;
    }


}
