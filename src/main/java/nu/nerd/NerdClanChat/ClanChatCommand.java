package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.Invite;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class ClanChatCommand implements CommandExecutor {


    private final NerdClanChat plugin;
    private HashMap<String, String> confirmChannelDeletion;
    private enum ChannelColor { COLOR, TEXTCOLOR, ALERTCOLOR }


    public ClanChatCommand(NerdClanChat plugin) {
        this.plugin = plugin;
        this.confirmChannelDeletion = new HashMap<String, String>();
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (args.length < 1) {
            this.printHelpText(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("more")) {
            this.printMoreHelpText(sender);
            return true;
        }

        else if (args[0].equalsIgnoreCase("create")) {
            if (args.length > 1) {
                this.createChannel(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat create <channel>");
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("delete")) {
            if (args.length > 1) {
                this.deleteChannel(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat delete <channel>");
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("confirm") && args.length == 2) {
            if (args[1].equalsIgnoreCase("delete")) {
                this.actuallyDeleteChannel(sender);
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("color")) {
            if (args.length == 3) {
                this.setChannelColor(sender, args[1], args[2], ChannelColor.COLOR);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat color <channel> <color>");
                sender.sendMessage("Available colors: " + NCCUtil.formatColorList(NCCUtil.colorList()));
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("textcolor")) {
            if (args.length == 3) {
                this.setChannelColor(sender, args[1], args[2], ChannelColor.TEXTCOLOR);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat textcolor <channel> <color>");
                sender.sendMessage("Available colors: " + NCCUtil.formatColorList(NCCUtil.colorList()));
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("alertcolor")) {
            if (args.length == 3) {
                this.setChannelColor(sender, args[1], args[2], ChannelColor.ALERTCOLOR);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat alertcolor <channel> <color>");
                sender.sendMessage("Available colors: " + NCCUtil.formatColorList(NCCUtil.colorList()));
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("members")) {
            if (args.length == 2) {
                this.listChannelMembers(sender, args[1]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat members <channel>");
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("invite")) {
            if (args.length == 3) {
                this.inviteMember(sender, args[1], args[2]);
            } else {
                sender.sendMessage(ChatColor.RED + "Usage: /clanchat invite <channel> <player>");
            }
            return true;
        }

        else if (args[0].equalsIgnoreCase("test")) {
            try {
                Channel ch = new Channel();
                ch.setName(args[1]);
                ch.setOwner("fake-uuid-placeholder");
                ch.setColor("BLUE");
                ch.setTextColor("GRAY");
                ch.setAlertColor("GRAY");
                ch.setSecret(false);
                ch.setPub(false);
                plugin.channelsTable.save(ch);
            } catch (Exception ex) {
                sender.sendMessage(ex.toString());
            }
            return true;
        }

        else {
            this.printHelpText(sender);
            return true;
        }

    }


    private void createChannel(CommandSender sender, String name) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be run by a player.");
            return;
        }
        name = name.toLowerCase();
        Player owner = (Player) sender;
        if (plugin.channelCache.getChannel(name) != null) {
            sender.sendMessage(ChatColor.RED + "That channel already exists. If you would like to join the channel, speak with the owner.");
        } else {

            if (!name.matches("^(?i)[a-z0-9_]+$")) {
                sender.sendMessage(ChatColor.RED + "Oops! That channel name isn't valid. You can only have letters and numbers in your channel name.");
                return;
            }
            if (name.length() > 16) {
                sender.sendMessage(ChatColor.RED + "Oops! Please limit your channel name length to 16 characters!");
                return;
            }

            //Create the channel
            Channel ch = new Channel(name, owner.getUniqueId().toString());
            ChannelMember mem = new ChannelMember(name, owner.getUniqueId().toString(), owner.getName(), true);
            try {
                plugin.channelsTable.save(ch);
                plugin.channelMembersTable.save(mem);
            } catch (Exception ex) {
                sender.sendMessage(ChatColor.RED + "There was an error creating the channel.");
                plugin.getLogger().warning(ex.toString());
                return;
            }

            plugin.channelCache.updateChannel(name, ch); //Cache the newly created channel

            //Update owner's player meta to make this their default channel
            String UUID = owner.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
            meta.setDefaultChannel(name);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);

            sender.sendMessage(ChatColor.BLUE + "You will receive bulletins from this channel on login. To unsubscribe run /clanchat unsubscribe " + name);
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "Channel created!");

        }
    }


    private void deleteChannel(CommandSender sender, String name) {

        if (!this.senderIsOwner(sender, name, false)) {
            sender.sendMessage(ChatColor.RED + "Only the owner can delete a channel");
            return;
        }

        Player owner = (Player) sender;
        this.confirmChannelDeletion.put(owner.getUniqueId().toString(), name.toLowerCase());
        sender.sendMessage(ChatColor.RED + "This command is irreversible. Your channel and all associated data WILL Be lost!");
        sender.sendMessage(ChatColor.BLUE + "Type \"/clanchat confirm delete\" to confirm you actually want to delete #" + name);

    }


    private void actuallyDeleteChannel(CommandSender sender) {
        if (sender instanceof Player) {

            Player owner = (Player) sender;
            String ownerUUID = owner.getUniqueId().toString();
            String channelName;

            if (!this.confirmChannelDeletion.containsKey(ownerUUID)) {
                return;
            } else {
                channelName = this.confirmChannelDeletion.get(ownerUUID);
                this.confirmChannelDeletion.remove(ownerUUID);
            }

            try {
                Channel ch = plugin.channelCache.getChannel(channelName);
                plugin.channelsTable.delete(ch);
                plugin.channelMembersTable.deleteChannelMembers(channelName);
                plugin.bulletinsTable.deleteChannelBulletins(channelName);
                plugin.invitesTable.deleteChannelInvites(channelName);
                plugin.channelCache.remove(channelName);
            } catch (Exception ex) {
                plugin.getLogger().warning(ex.toString());
                sender.sendMessage(ChatColor.RED + "There was an error deleting your channel.");
                return;
            }

            sender.sendMessage(ChatColor.RED + "Your channel was deleted!");

        }
    }


    private void setChannelColor(CommandSender sender, String channelName, String color, ChannelColor key) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        channelName = channelName.toLowerCase();
        color = color.toUpperCase();
        Channel channel = plugin.channelCache.getChannel(channelName);

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + String.format("The channel \"%s\" doesn't exist", channelName));
            return;
        }

        if (!NCCUtil.colorList().contains(color)) {
            sender.sendMessage(ChatColor.RED + "Only one of the following colors can be used:");
            sender.sendMessage(NCCUtil.formatColorList(NCCUtil.colorList()));
            return;
        }

        if (key == ChannelColor.ALERTCOLOR) {
            channel.setAlertColor(color);
        } else if (key == ChannelColor.TEXTCOLOR) {
            channel.setTextColor(color);
        } else {
            channel.setColor(color);
        }
        plugin.channelsTable.save(channel);
        plugin.channelCache.updateChannel(channelName, channel);
        String msg = String.format("Channel %s changed to %s%s!", key.name().toLowerCase(), ChatColor.valueOf(color), color);
        sender.sendMessage(ChatColor.BLUE + msg);

    }


    private void listChannelMembers(CommandSender sender, String channelName) {

        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());

        if (channel == null) {
            sender.sendMessage(ChatColor.RED + "That channel does not exist");
            return;
        }

        if (!this.senderIsMember(sender, channelName) && channel.isSecret()) {
            sender.sendMessage(ChatColor.RED + "This channel is secret. You must be a member of the channel to see who is in the channel");
            return;
        }

        // Collect players into online and offline lists
        List<String> online = new ArrayList<String>();
        List<String> offline = new ArrayList<String>();
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                online.add(player.getName());
            }
        }
        for (ChannelMember member : members.values()) {
            if (!online.contains(member.getName())) {
                offline.add(member.getName());
            }
        }

        // Output
        sender.sendMessage(ChatColor.GOLD + "Online: " + NCCUtil.formatList(online, ChatColor.WHITE, ChatColor.GRAY));
        sender.sendMessage(ChatColor.GOLD + "Offline: " + NCCUtil.formatList(offline, ChatColor.WHITE, ChatColor.GRAY));

    }


    private void inviteMember(CommandSender sender, String channelName, String playerName) {

        if (!this.senderIsManager(sender, channelName, false)) {
            sender.sendMessage(ChatColor.RED + "Sorry, you have to be a manager to do that!");
            return;
        }

        channelName = channelName.toLowerCase();
        Channel channel = plugin.channelCache.getChannel(channelName);
        PlayerMeta playerMeta = plugin.playerMetaTable.getPlayerMetaByName(playerName.toLowerCase());

        if (playerMeta == null) {
            sender.sendMessage(ChatColor.RED + "Sorry, but that player hasn't logged on recently. Try again later.");
            return;
        }

        if (plugin.invitesTable.alreadyInvited(playerMeta.getUUID(), channelName)) {
            sender.sendMessage(ChatColor.BLUE + "That player was already invited, but they haven't accepted yet");
            return;
        }

        try {
            Invite inv = new Invite(channelName, playerMeta.getUUID());
            plugin.invitesTable.save(inv);
        } catch (Exception ex) {
            sender.sendMessage(ChatColor.RED + "There was an error processing the invite.");
            plugin.getLogger().warning(ex.toString());
            return;
        }

        Player player = plugin.getServer().getPlayer(UUID.fromString(playerMeta.getUUID()));
        if (player != null) {
            player.sendMessage(ChatColor.BLUE + String.format("You have been invited to %s by %s.", channelName, sender.getName()));
            player.sendMessage(ChatColor.BLUE + String.format("Type %s/clanchat join %s%s to join", ChatColor.GRAY, channelName, ChatColor.BLUE));
        }

        sender.sendMessage(ChatColor.BLUE + String.format("%s has been invited to %s", playerName, channelName));

    }


    private boolean senderIsMember(CommandSender sender, String channelName) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        if (!(sender instanceof Player)) return true;
        if (channel == null) return false;
        Player player = (Player) sender;
        return members.containsKey(player.getUniqueId().toString());
    }


    private boolean senderIsManager(CommandSender sender, String channelName, boolean allowConsole) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        if (allowConsole && !(sender instanceof Player)) return true;
        if (!(sender instanceof Player)) return false;
        if (channel == null) return false;
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        if (!(members.containsKey(UUID))) return false;
        return !(!members.get(UUID).isManager() || !channel.getOwner().equals(UUID));
    }


    private boolean senderIsOwner(CommandSender sender, String channelName, boolean allowConsole) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        if (allowConsole && !(sender instanceof Player)) return true;
        if (!(sender instanceof Player)) return false;
        if (channel == null) return false;
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        return channel.getOwner().equals(UUID);
    }


    private void printHelpText(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "ClanChat usage:");
        sender.sendMessage(ChatColor.BLUE + "/clanchat create <channel>" + ChatColor.WHITE + " - Creates a new channel, with you as the owner.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat join <channel>" + ChatColor.WHITE + " - Joins a channel that you've already been invited to.");
        sender.sendMessage(ChatColor.BLUE + "/c [#<channel>] <message>" + ChatColor.WHITE + " - Sends a message to the channel. You must be a member of the channel. If you do not include a channel (prefixed by '#'), it will default to the last channel used.");
        sender.sendMessage(ChatColor.BLUE + "/c #<channel>" + ChatColor.WHITE + "Set your default channel without sending a message.");
        sender.sendMessage(ChatColor.BLUE + "/cq #<channel> <message>" + ChatColor.WHITE + " - Sends a quick message to the specified channel. This does not change your default channel.");
        sender.sendMessage(ChatColor.BLUE + "/ca [#<channel>] <message>" + ChatColor.WHITE + " - Sends an alert message to the channel. You must be an owner/manager of the channel. If a channel name is not included it will alert your current channel.");
        sender.sendMessage(ChatColor.BLUE + "/cme [#<channel>] <message>" + ChatColor.WHITE + "Sends a \"/me\" type message to your current, or specified, channel.");
        sender.sendMessage(ChatColor.BLUE + "/cr <message>" + ChatColor.WHITE + " - Sends a message to the last channel that you received a message from, regardless of what your default channel is.");
        sender.sendMessage(ChatColor.BLUE + "/cb [#<channel>]" + ChatColor.WHITE + " - List bulletins for all channels you are a member of, or specify a channel to receive its bulletins.");
        sender.sendMessage(ChatColor.BLUE + "/cm [#<channel>]" + ChatColor.WHITE + " - Lists all members in your default channel. If a channel is given, lists members in that channel.");
        sender.sendMessage(ChatColor.GRAY + "Type " + ChatColor.BLUE + "/clanchat more" + ChatColor.GRAY + " for additional commands.");
    }


    private void printMoreHelpText(CommandSender sender) {
        sender.sendMessage(ChatColor.BLUE + "/cs [#<channel>] <message>" + ChatColor.WHITE + "Sends a \"/s\" type sarcasm message to your current, or specified, channel.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat delete <channel>" + ChatColor.WHITE + " - Deletes a channel. Owner only.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat color <channel> <color>" + ChatColor.WHITE + " - Sets the channel color. Set by channel owner/managers.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat textcolor <channel> <color>" + ChatColor.WHITE + " - Sets the channel text color. Set by channel owner/managers.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat alertcolor <channel> <color>" + ChatColor.WHITE + " - Sets the channel alert color. Set by channel owner/managers.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat members <channel>" + ChatColor.WHITE + " - Lists all the members in a channel.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat invite <channel> <player>" + ChatColor.WHITE + " - Invites a player to the channel. You must be a manager to invite.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat uninvite <channel> <player>" + ChatColor.WHITE + " - Uninvites a previously invited player. You must be a manager.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat changeowner <channel> <player>" + ChatColor.WHITE + " - Changes the owner on the given channel. Only the owner can set this. " + ChatColor.RED + "Careful! This can't be undone.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat addmanager <channel> <player>" + ChatColor.WHITE + " - Adds a manager to the channel. You must be the owner to run this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat removemanager <channel> <player>" + ChatColor.WHITE + " - Removes a manager from the channel. You must be the owner to run this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat listmanagers <channel>" + ChatColor.WHITE + " - List all managers in the channel. You must be owner to run this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat remove <channel> <player>" + ChatColor.WHITE + " - Removes a player from the channel. You must be a manager to do this.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat leave <channel>" + ChatColor.WHITE + " - Leaves a channel that you're in.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat list" + ChatColor.WHITE + " - Lists all the channels you are in.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat public" + ChatColor.WHITE + " - Lists all public channels.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat flags" + ChatColor.WHITE + " - Sets channel flags. Type /clanchat flags for more information.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat addbulletin <channel> <bulletin>" + ChatColor.WHITE + " - Add bulletin to the channel.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat removebulletin <channel> <number>" + ChatColor.WHITE + " - Remove the bulletin from the channel, <number> starts at 1 with the top bulletin.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat subscribe <channel>" + ChatColor.WHITE + " - Subscribe to a channel's bulletins.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat unsubscribe <channel>" + ChatColor.WHITE + " - Unsubscribe from a channel's bulletins.");
        sender.sendMessage(ChatColor.BLUE + "/clanchat subscriptions" + ChatColor.WHITE + " - List your current bulletin subscriptions.");
        if (sender.hasPermission("nerdclanchat.admin")) {
            sender.sendMessage(ChatColor.BLUE + "/clanchat channels" + ChatColor.WHITE + " - Lists all the channels and their owners.");
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.BLUE + "/clanchat chat <channel> <message>" + ChatColor.WHITE + " - Chats to an arbitrary channel. Only available to console.");
        }
    }


}
