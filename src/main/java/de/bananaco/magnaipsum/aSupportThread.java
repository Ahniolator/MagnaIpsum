package de.bananaco.magnaipsum;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class aSupportThread implements Runnable {

    private HashMap<String, aObj> supporedPlugins = new HashMap();
    private MagnaIpsum plugin = null;
    private String pluginsPath = "plugins" + File.separator;
    public static boolean isFinished = false;

    public aSupportThread(MagnaIpsum plugin) {
        this.plugin = plugin;
    }

    public void run() {
        this.supporedPlugins = this.addSupportedPlugins();
        if (!(this.supporedPlugins == null) || !this.supporedPlugins.isEmpty()) {
            plugin.aSupportedPlugins = this.supporedPlugins;
            return;
        }
    }

    private HashMap<String, aObj> addSupportedPlugins() {
        HashMap<String, aObj> plugins = new HashMap();

        plugins.clear();
        try {
            plugins = this.downloadSupportedPlugins();
            if (plugins != null || !plugins.isEmpty()) {
                System.out.println("[MagnaIpsum] Downloaded plugin list successfully!");
                aSupportThread.isFinished = true;
                return plugins;
            }
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        System.out.println("[MagnaIpsum] Could not download plugin list! Setting default values");
        plugins.clear();
        if (plugins == null || plugins.isEmpty()) {
            plugins.put("Burning Creative Suite", new aObj("Burning Creative Suite",
                    "http://dev.bukkit.org/server-mods/burning-creative-suite/files/",
                    this.pluginsPath + "BurningCreativeSuite.jar",
                    "Fixes, General, Mechanics, Burning Creative Suite, creative, mode, survival, blocks, separate, inventories, inventory, players",
                    0,
                    "Keeping Creative players, Creative"));

            plugins.put("SunBurn", new aObj("SunBurn",
                    "http://dev.bukkit.org/server-mods/sunburn/files/",
                    this.pluginsPath + "SunBurn.jar",
                    "Fun, Mechanics, SunBurn, burn, players, mobs, fire, solar, sun, apocalypse, danger, light",
                    0,
                    "Because the sun just wasn't hot enough"));

            plugins.put("CashFlow", new aObj("CashFlow",
                    "http://dev.bukkit.org/server-mods/cashflow/files/",
                    this.pluginsPath + "CashFlow.jar",
                    "Economy, Admin tools, General, Role Playing, CashFlow, tax, salary, interval, money, iConomy, BOSEconomy, EssentialsEconomy",
                    0,
                    "Lets you create taxes and salaries at intervals"));

            plugins.put("bPermissions", new aObj("bPermissions",
                    "http://dev.bukkit.org/server-mods/bpermissions/files/",
                    this.pluginsPath + "bpermissions.jar",
                    "Anti Griefing, Admin tools, Mechanics, bPermissions, permissions, super perms, manager",
                    0,
                    "An easy-to-use SuperPerms manager"));

            plugins.put("DynaMark", new aObj("DynaMark",
                    "http://dev.bukkit.org/server-mods/dynamicmarket/files/",
                    this.pluginsPath + "DynaMark.jar",
                    "Economy, DynaMark, buy, sell, players, dynamic, prices, shop, commands, market",
                    0,
                    "Lets players buy and sell items with prices based on demand"));

            plugins.put("PlayerStatus", new aObj("PlayerStatus",
                    "http://dev.bukkit.org/server-mods/playerstatus/files/",
                    this.pluginsPath + "PlayerStatus.jar",
                    "Chat Related, PlayerStatus, do not disturb, dnd, me, emotes, private messages, pm, afk, away from keyboard",
                    0,
                    "Allows players to set their status to AFK and more"));

            plugins.put("SpoutEssentials", new aObj("SpoutEssentials",
                    "http://dev.bukkit.org/server-mods/spoutessentials/files/",
                    this.pluginsPath + "spoutEssentials.jar",
                    "fun, admin tools, fixes, mechanics, SpoutEssentials, aesthetic, modification, notifications, welcome, join, custom, poke, texture packs, capes, titles, command gui",
                    0,
                    "Allows for easy aesthetic modification, for you and your users on your Minecraft Server"));

            plugins.put("SideKick", new aObj("SideKick",
                    "http://dev.bukkit.org/server-mods/sidekick/files/",
                    this.pluginsPath + "SideKick.jar",
                    "SideKick, admin tools, economy, general, website administration, world editing management, antigriefing, chat commands, world generators, item spawning, shortcuts",
                    0,
                    "Allows you to control the Ultimate Server!"));

            plugins.put("MobDisguise", new aObj("MobDisguise",
                    "http://dev.bukkit.org/server-mods/mobdisguise/files/",
                    this.pluginsPath + "MobDisguise.jar",
                    "MobDisguise, fun, mob, disguise, transform, change, pig, chicken, creeper, cow, skeleton, zombie, ghast, pigman, giant, spider, sheep, camouflage",
                    0,
                    "Allowing you to transform into any mob you want, with no client mods!"));
            aSupportThread.isFinished = true;
            return plugins;
        }
        return null;
    }

    private HashMap<String, aObj> downloadSupportedPlugins() throws MalformedURLException, IOException {
        HashMap<String, aObj> map = new HashMap();
        URL url = new URL("http://ahniolator.aisites.com/aPluginatorPlugins.txt");
        final BufferedReader in;
        in = new BufferedReader(
                new InputStreamReader(
                url.openStream()));
        Timer timer = new Timer(true);
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if (aSupportThread.isFinished) {
                    //System.out.println("[MagnaIpsum] Success! :D");
                    return;
                }
                try {
                    System.out.println("[MagnaIpsum] Error connecting to supported plugin host! Closing connection.");
                    in.close();
                } catch (IOException ex) {
                    System.out.println("Something just went wrong... REALLY wrong. And it's probably my fault. This error should never show if the URL is correct");
                    ex.printStackTrace();
                }
            }
        }, 20 * 1000); //delay in seconds
        for (String string = null; ((string = in.readLine()) != null); string = in.readLine()) {
            String[] array = string.split(";");
            if (array[0].startsWith("#")) {
                continue;
            }
            map.put(array[0], new aObj(array[0], array[1], this.pluginsPath + array[2], array[3], Integer.parseInt(array[4]), array[5]));
        }
        in.close();
        if (map.isEmpty()) {
            return null;
        }
        return map;
    }
}
