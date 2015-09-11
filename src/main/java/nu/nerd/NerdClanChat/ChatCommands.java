package nu.nerd.NerdClanChat;

import nu.nerd.NerdClanChat.database.Channel;
import nu.nerd.NerdClanChat.database.ChannelMember;
import nu.nerd.NerdClanChat.database.PlayerMeta;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;


public class ChatCommands implements CommandExecutor {


    private final NerdClanChat plugin;
    private enum MessageType {
        NORMAL, ME, ALERT, SARCASM
    }


    public ChatCommands(NerdClanChat plugin) {
        this.plugin = plugin;
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("c")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /c [#<channel>] <message>");
            }
            else if (args[0].charAt(0) == '#' && args.length == 1) {
                this.setDefaultChannel(sender, args[0].substring(1));
            } else {
                this.chat(sender, args, MessageType.NORMAL);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cq")) {
            if (args.length < 2 || args[0].charAt(0) != '#') {
                sender.sendMessage(ChatColor.RED + "Usage: /cq #<channel> <message>");
            } else {
                this.cq(sender, args);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("ca")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /ca [#<channel>] <message>");
            } else {
                this.chat(sender, args, MessageType.ALERT);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cme")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /cme [#<channel>] <message>");
            }
            else if (args[0].charAt(0) == '#' && args.length == 1) {
                this.setDefaultChannel(sender, args[0].substring(1));
            } else {
                this.chat(sender, args, MessageType.ME);
            }
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("cs")) {
            if (args.length < 1) {
                sender.sendMessage(ChatColor.RED + "Usage: /cs [#<channel>] <message>");
            }
            else if (args[0].charAt(0) == '#' && args.length == 1) {
                this.setDefaultChannel(sender, args[0].substring(1));
            } else {
                this.chat(sender, args, MessageType.SARCASM);
            }
            return true;
        }

        return false;

    }


    private void chat(CommandSender sender, String[] args, MessageType type) {
        String channel;
        String message;
        if (args[0].charAt(0) == '#') {
            channel = args[0].substring(1);
            message = this.joinArray(" ", Arrays.copyOfRange(args, 1, args.length));
        } else {
            channel = getDefaultChannel(sender);
            message = this.joinArray(" ", args);
            if (channel == null) {
                sender.sendMessage(String.format("%sYou don't have a default channel set (or aren't in any channels). Run %s/clanchat%s for help", ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.RED));
                return;
            }
        }
        this.sendMessage(sender, channel, message, type, true);
    }


    private void cq(CommandSender sender, String[] args) {
        String channel = args[0].substring(1);
        String message = this.joinArray(" ", Arrays.copyOfRange(args, 1, args.length));
        this.sendMessage(sender, channel, message, MessageType.NORMAL, false);
    }


    private void sendMessage(CommandSender sender, String channelName, String message, MessageType type, boolean changeDefault) {

        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        String tag;
        String msg;
        String name;

        // Check permission
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if ( !(members.containsKey(player.getUniqueId().toString())) ) {
                sender.sendMessage(ChatColor.RED + "You can't send a message to a channel you aren't a member of");
                return;
            }
        }
        if (type == MessageType.ALERT && !this.assertManager(sender, channelName)) return;

        // Get sender name, using ~console for console
        if (sender instanceof Player) {
            Player player = (Player) sender;
            name = ChatColor.WHITE + player.getName();
        } else {
            name = ChatColor.RED + "~console";
        }

        // Format message
        if (type == MessageType.ME) {
            tag = String.format("%s[%s]", this.color(channel.getColor()), channel.getName());
            msg = String.format("%s %s* %s %s", tag, this.color(channel.getTextColor()), ChatColor.stripColor(name), message);
        }
        else if (type == MessageType.ALERT) {
            tag = String.format("%s[%s] %s<%s> ", this.color(channel.getColor()), channel.getName(), this.color(channel.getAlertColor()), name);
            msg = tag + this.color(channel.getAlertColor()) + "" + ChatColor.UNDERLINE + message;
        }
        else if (type == MessageType.SARCASM) {
            tag = String.format("%s[%s] %s<%s%s> ", this.color(channel.getColor()), channel.getName(), ChatColor.GRAY, name, ChatColor.GRAY);
            msg = tag + this.color(channel.getTextColor()) + ChatColor.ITALIC + message;
        }
        else {
            tag = String.format("%s[%s] %s<%s%s> ", this.color(channel.getColor()), channel.getName(), ChatColor.GRAY, name, ChatColor.GRAY);
            msg = tag + this.color(channel.getTextColor()) + message;
        }

        // Send message
        plugin.getLogger().info(ChatColor.stripColor(msg));
        this.sendRawMessage(channelName, msg);

        // Change default, if applicable, and update last recieved channel
        if (changeDefault) {
            this.setDefaultChannel(sender, channelName);
        }
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                this.updateLastChannelReceived(player, channelName);
            }
        }

    }


    private void sendRawMessage(String channelName, String message) {
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (members.containsKey(player.getUniqueId().toString())) {
                player.sendMessage(message);
            }
        }
    }


    private void setDefaultChannel(CommandSender sender, String channel) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String UUID = player.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
            meta.setDefaultChannel(channel);
            plugin.playerMetaCache.updatePlayerMeta(UUID, meta);
        }
    }


    private String getDefaultChannel(CommandSender sender) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            String UUID = player.getUniqueId().toString();
            PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
            if (meta != null && meta.getDefaultChannel() != null) {
                return meta.getDefaultChannel();
            }
        }
        return null;
    }


    private void updateLastChannelReceived(Player player, String channel) {
        String UUID = player.getUniqueId().toString();
        PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
        meta.setLastReceived(channel);
        plugin.playerMetaCache.updatePlayerMeta(UUID, meta);
    }


    private String getLastChannelReceived(Player player) {
        String UUID = player.getUniqueId().toString();
        PlayerMeta meta = plugin.playerMetaCache.getPlayerMeta(UUID);
        return meta.getLastReceived();
    }


    private String joinArray(String separator, String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
            sb.append(separator);
        }
        return sb.toString().trim();
    }


    private ChatColor color(String color) {
        return ChatColor.valueOf(color);
    }


    private boolean assertManager(CommandSender sender, String channelName) {
        Channel channel = plugin.channelCache.getChannel(channelName.toLowerCase());
        HashMap<String, ChannelMember> members = plugin.channelCache.getChannelMembers(channelName.toLowerCase());
        String errorMsg = ChatColor.RED + "Sorry, you have to be a manager to do that!";
        if (!(sender instanceof Player)) {
            sender.sendMessage(errorMsg);
            return false;
        }
        Player player = (Player) sender;
        String UUID = player.getUniqueId().toString();
        if (!(members.containsKey(UUID))) {
            sender.sendMessage(errorMsg);
            return false;
        }
        if (!members.get(UUID).isManager() || !channel.getOwner().equals(UUID)) {
            sender.sendMessage(errorMsg);
            return false;
        }
        return true;
    }


}
