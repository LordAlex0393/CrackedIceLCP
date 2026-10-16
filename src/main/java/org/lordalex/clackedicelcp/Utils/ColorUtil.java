package org.lordalex.clackedicelcp.Utils;

import net.md_5.bungee.api.ChatColor;

public class ColorUtil {
    public static String get(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}
