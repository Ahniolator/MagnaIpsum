package de.bananaco.magnaipsum;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.bukkit.ChatColor;

public class Sync {

    public List<String> search(String plugin) throws Exception {
        plugin = plugin.toLowerCase();
        List<String> results = new ArrayList<String>();
        URL url = new URL("http://dev.bukkit.org/server-mods/?search=" + plugin);

        String inputLine;
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        while ((inputLine = in.readLine()) != null) {

            inputLine = inputLine.trim().toLowerCase();

            if (inputLine.startsWith("<td") && inputLine.contains("<a href=\"/server-mods/")) {
                //    		System.out.println(inputLine);

                String result = "";
                try {
                    Pattern regex = Pattern.compile("(?<=/)[^/]*(?=/\">)", Pattern.MULTILINE);
                    Matcher regexMatcher = regex.matcher(inputLine);
                    if (regexMatcher.find()) {
                        result = regexMatcher.group();

                        if (!results.contains(result)) {
                            results.add(isAwesome(result));
                        }
                        
                        //On the next line I'm getting a incompatable types warning. You might want to check that out.
                        if (results.equals(plugin)) {
                            return results;
                        }
                    } else {
                        throw new Exception("Cannot find BukkitDev site");
                    }
                } catch (PatternSyntaxException ex) {
                    ex.printStackTrace();
                }
            }
        }
        String pl = plugin;
        while (results.isEmpty() && plugin.length() > 2) {
            pl = pl.substring(0, plugin.length() - 1);
            results = search(pl);
        }



        return results;
    }

    public BukkitDevDownload get(String plugin) throws Exception {
        plugin = plugin.toLowerCase();
        URL url = new URL("http://dev.bukkit.org/server-mods/" + plugin + "/files/");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {

            inputLine = inputLine.trim();
            if (inputLine.startsWith("<td") && inputLine.contains("<a href=\"/server-mods/" + plugin + "/files")) {

                String result = "";
                try {
                    Pattern regex = Pattern.compile("/[^\"]*" + plugin + "[^\"]*", Pattern.MULTILINE);
                    Matcher regexMatcher = regex.matcher(inputLine);
                    if (regexMatcher.find()) {
                        result = "http://dev.bukkit.org" + regexMatcher.group();
                    }
                } catch (PatternSyntaxException ex) {
                    // Syntax error in the regular expression
                }
                String version = "";
                try {
                    Pattern regex = Pattern.compile("(?<=\">)[^<]*(?=</a>)", Pattern.MULTILINE);
                    Matcher regexMatcher = regex.matcher(inputLine);
                    if (regexMatcher.find()) {
                        version = regexMatcher.group();
                    }
                } catch (PatternSyntaxException ex) {
                    ex.printStackTrace();
                }
                in.close();
                return new BukkitDevDownload(plugin, version, result);
            }
        }
        in.close();
        return null;
    }

    private String isAwesome(String check) {
        if (check.equals("bukkitdevdownloader") || check.equals("bpermissions") || check.equals("bchat") || check.equals("pinapp") || check.equals("aznbans") || check.contains("banana")) {
            return ChatColor.GOLD + check + ChatColor.WHITE;
        }
        return check;
    }
}