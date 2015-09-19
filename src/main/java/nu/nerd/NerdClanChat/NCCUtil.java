package nu.nerd.NerdClanChat;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NCCUtil {


    public static String joinArray(String separator, String[] arr) {
        StringBuilder sb = new StringBuilder();
        for (String s : arr) {
            sb.append(s);
            sb.append(separator);
        }
        return sb.toString().trim();
    }


    public static String formatList(List<String> list, ChatColor color1, ChatColor color2) {
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            if (list.indexOf(item) % 2 == 0) {
                sb.append(color1);
            }
            else {
                sb.append(color2);
            }
            sb.append(item);
            if (list.indexOf(item) != (list.size() - 1)) {
                sb.append(color1);
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    public static String formatColorList(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String item : list) {
            sb.append(ChatColor.valueOf(item));
            sb.append(item);
            if (list.indexOf(item) != (list.size() - 1)) {
                sb.append(ChatColor.WHITE);
                sb.append(", ");
            }
        }
        return sb.toString();
    }


    public static List<String> colorList() {
        List<String> colorList = new ArrayList<String>();
        Set<String> blacklist = new HashSet<String>();
        blacklist.add("MAGIC");
        blacklist.add("BOLD");
        blacklist.add("STRIKETHROUGH");
        blacklist.add("UNDERLINE");
        blacklist.add("ITALIC");
        blacklist.add("RESET");
        for (ChatColor col : ChatColor.values()) {
            colorList.add(col.name());
        }
        colorList.removeAll(blacklist);
        return colorList;
    }


}
