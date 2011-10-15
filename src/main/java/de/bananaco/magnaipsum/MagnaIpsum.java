package de.bananaco.magnaipsum;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class MagnaIpsum extends JavaPlugin {

    public MagnaIpsum plugin = this;
    public JavaPlugin p = this;
    public Configuration c;
    public boolean whitelist;
    public List<String> allowed;
    public String deny;
    //List of plugins supported by aPluginator
    public HashMap<String, aObj> aSupportedPlugins = new HashMap();
    //Used when conducting searches for aPluginator
    public ArrayList<aObj> supPlugs = new ArrayList();

    @Override
    public void onDisable() {
        System.out.println("[" + this.getDescription().getName() + "] v" + this.getDescription().getVersion() + " has been disabled!");
    }

    @Override
    public void onEnable() {

        //Get aPluginator supported plugins from site
        ThreadRunner.runThread(new aSupportThread(this));

        this.c = new Configuration(this);
        c.load();
        this.whitelist = c.getBoolean("whitelist", false);
        this.deny = c.getString("deny-message", "Cannot install this plugin");
        List<String> pl = new ArrayList<String>();
        pl.add("bpermissions");
        pl.add("bchat");
        pl = c.getStringList("whitelist-plugins", pl);
        c.setProperty("whitelist-plugins", pl);
        c.save();
        this.allowed = pl;

        System.out.println("[" + this.getDescription().getName() + "] v" + this.getDescription().getVersion() + " has been enabled!");
    }

    @Override
    public boolean onCommand(final CommandSender sender, Command command, String label, final String[] args) {
        if (sender instanceof Player) {
            if (!((Player) sender).hasPermission("magna.ipsum")) {
                sender.sendMessage(ChatColor.RED + "--You are not allowed to do that");
                return true;
            }
        }
        if (command.getName().equalsIgnoreCase("bupdate")) {
            if (args.length == 0) {
                return ThreadRunner.runThread(new Runnable() {

                    public void run() {
                        File dir = new File("plugins/plugins/");
                        sender.sendMessage(ChatColor.GREEN + "--Beginning update check");
                        String[] children = dir.list();
                        if (children == null) {
                            sender.sendMessage(ChatColor.RED + "--No files to update");
                        } else {
                            long fulltime = System.currentTimeMillis();
                            for (int i = 0; i < children.length; i++) {
                                String plugin = children[i].replace(".txt", "");

                                long time = System.currentTimeMillis();
                                try {
                                    BukkitDevDownload bdd = new Sync().get(plugin);
                                    final JavaPlugin pl = p;
                                    if (bdd != null) {
                                        bdd.installJar(pl, sender, time, false);
                                    } else {
                                        throw new NoInstallableJarException();
                                    }
                                } catch (Exception e) {
                                    sender.sendMessage(ChatColor.RED + "--" + e.getMessage());
                                }
                            }
                            sender.sendMessage(ChatColor.BLUE + "--Update check complete. Took " + (System.currentTimeMillis() - fulltime) + "ms");
                        }
                    }
                });
            } else if (args.length == 1 && this.aSupportedPlugins.get(args[0]) != null) {
                //If they check for a specific plugin name and it is supported
                long time = System.currentTimeMillis();
                String arg = args[0];
                Plugin pl = null;
                PluginManager pm = plugin.getServer().getPluginManager();

                //Replace underscores in the plugin name argument with spaces
                arg = arg.replace("_", " ");
                pl = pm.getPlugin(arg);
                
                //Check if is enabled
                if (pl == null) {
                    sender.sendMessage(ChatColor.RED + "--That plugin is not enabled on this server!");
                    return true;
                }

                //Check if supported
                if (this.aSupportedPlugins.get(pl.getDescription().getName()) == null) {
                    sender.sendMessage(ChatColor.RED + "--That plugin is not supported by the MagnaIpsum!");
                    sender.sendMessage(ChatColor.RED + "--Ask the developer to send: Ahniolator a message about it!");
                    return true;
                }

                //Update Stuff Here:
                String name = pl.getDescription().getName();
                try {
                    new BukkitDevBackup(name).backup();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                pm.disablePlugin(pl);
                if (this.aSupportedPlugins.get(name).getURL().toString().endsWith(".jar")) {
                    sender.sendMessage(ChatColor.GREEN + "--Downloading: " + name + " from a static url");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "--Downloading: " + name + " from BukkitDev");
                }
                try {
                    if (download(this.aSupportedPlugins.get(name))) {
                        sender.sendMessage(ChatColor.GREEN + "--Downloaded: " + name);
                        this.getServer().reload();
                        pl = pm.getPlugin(arg);
                        sender.sendMessage(ChatColor.GREEN + "--Updated " + ChatColor.GOLD + pl.getDescription().getName() + ChatColor.GREEN + " successfully to version: " + ChatColor.WHITE + pl.getDescription().getVersion() + "!");
                        sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - time) + "ms");
                        return true;
                    } else {
                        sender.sendMessage(ChatColor.RED + "--Could not update: " + ChatColor.GOLD + plugin.getDescription().getName());
                        pm.enablePlugin(pl);
                        try {
                            new BukkitDevBackup(name).rollback(this);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return true;
                    }
                } catch (Exception e) {
                    sender.sendMessage(ChatColor.RED + "--Could not update: " + ChatColor.GOLD + pl.getDescription().getName());
                    pm.enablePlugin(pl);
                    try {
                        new BukkitDevBackup(name).rollback(this);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    e.printStackTrace();
                    return true;
                }
            } else {
                sender.sendMessage(ChatColor.RED + "--That plugin is not supported by the MagnaIpsum!");
                sender.sendMessage(ChatColor.RED + "--Ask the developer to send: Ahniolator a message about it!");
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("bsearch")) {
            if (args.length == 1) {
                return ThreadRunner.runThread(new Runnable() {

                    public void run() {
                        String plugin = args[0];
                        List<String> result = null;
                        long start = System.currentTimeMillis();
                        try {
                            sender.sendMessage(ChatColor.GREEN + "--Searching");
                            result = new Sync().search(plugin);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (result != null) {
                            String[] results = new String[result.size()];
                            results = result.toArray(results);
                            String message = Arrays.toString(results);
                            if (message.length() > 1) {
                                message = message.substring(1, message.length() - 1);
                            }
                            if (results.length == 0) {
                                sender.sendMessage(ChatColor.RED + "--No results were returned");
                            } else {
                                sender.sendMessage(ChatColor.GREEN + "--" + results.length + " results: " + ChatColor.WHITE + message);
                            }
                            sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                        }
                    }
                });
            } else if (args.length > 1 && (args[0].equalsIgnoreCase("key") || args[0].equalsIgnoreCase("keys") || args[0].equalsIgnoreCase("keyword") || args[0].equalsIgnoreCase("keywords"))) {
                boolean getDesc = false;
                long start = System.currentTimeMillis();
                sender.sendMessage(ChatColor.GREEN + "--Searching");
                this.getSupportedPlugins();
                ArrayList<aObj> list = new ArrayList();
                for (int x = 1; x < args.length; x++) {
                    String string = args[x];
                    if (string.equalsIgnoreCase("desc") || string.equalsIgnoreCase("-desc") || string.equalsIgnoreCase("description") || string.equalsIgnoreCase("-description")) {
                        getDesc = true;
                        continue;
                    }
                    for (int z = 0; z < this.supPlugs.size(); z++) {
                        aObj obj = this.supPlugs.get(z);
                        for (String key : obj.getKeys()) {
                            if (key.toLowerCase().contains(string.toLowerCase())) {
                                if (!list.contains(obj)) {
                                    obj.addToVal();
                                    list.add(obj);
                                } else {
                                    obj.addToVal();
                                }
                            }
                        }
                    }
                }
                if (list.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "--No results were returned");
                    return true;
                }
                aObj res = null;
                sender.sendMessage(ChatColor.GREEN + "--" + list.size() + " results: ");
                Collections.sort(list, new aObjComparator());
                int x = 0;
                for (aObj obj : list) {
                    x++;
                    res = obj;
                    if (res.getVal() == 1) {
                        sender.sendMessage("--" + res.getName() + " with: " + res.getVal() + " match");
                    } else {
                        sender.sendMessage("--" + res.getName() + " with: " + res.getVal() + " matches");
                    }
                    if (getDesc) {
                        sender.sendMessage("--Description: " + res.getDesc());
                    }
                    obj.resetVal();
                    if (x >= 5) {
                        break;
                    }
                }
                sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                list.clear();
                return true;
            }
        }
        if (command.getName().equals("bctrlz")) {
            if (args.length == 1) {
                return ThreadRunner.runThread(new Runnable() {

                    public void run() {
                        String plugin = args[0];
                        try {
                            sender.sendMessage(ChatColor.GREEN + "--Restoring");
                            new BukkitDevBackup(plugin).rollback(p, sender);
                        } catch (Exception e) {
                            sender.sendMessage(ChatColor.RED + "--" + e.getMessage());
                        }
                    }
                });
            }
        }

        if (command.getName().equalsIgnoreCase("buninstall")) {
            if (args.length >= 1) {
                return ThreadRunner.runThread(new Runnable() {

                    public void run() {
                        try {
                            for (int i = 0; i < args.length; i++) {
                                long time = System.currentTimeMillis();

                                String plugin = args[i];
                                String message = "";
                                if (args.length > 1) {
                                    message = " [" + (int) (i + 1) + "/" + args.length + "]";
                                }
                                sender.sendMessage(ChatColor.GREEN + "--Uninstalling " + plugin + message);
                                BukkitDevBackup bdb = new BukkitDevBackup(plugin);
                                bdb.backup();

                                bdb.delete(p, sender, time);

                                if (i == args.length - 1) {
                                    p.getServer().reload();
                                }
                            }
                        } catch (Exception e) {
                            sender.sendMessage(ChatColor.RED + "--" + e.getMessage());
                        }
                    }
                });
            }
        }
        if (command.getName().equalsIgnoreCase("binstall")) {
            if (args.length == 1 && this.aSupportedPlugins.get(args[0]) != null) {
                return ThreadRunner.runThread(new Runnable() {

                    public void run() {
                        long start = System.currentTimeMillis();
                        String arg = args[0];
                        //Replace underscores in the plugin name argument with spaces
                        arg = arg.replace("_", " ");

                        String name = arg;
                        if (whitelist) {
                            if (!allowed.contains(name)) {
                                sender.sendMessage(ChatColor.RED + "--" + deny);
                                return;
                            }
                        }
                        PluginManager pm = plugin.getServer().getPluginManager();

                        //Check if is enabled
                        if (pm.getPlugin(name) != null) {
                            sender.sendMessage(ChatColor.RED + "--That plugin is already running on this server!");
                            return;
                        }

                        //Update Stuff Here:
                        if (plugin.aSupportedPlugins.get(name).getURL().toString().endsWith(".jar")) {
                            sender.sendMessage("--Downloading: " + name + " from a static url");
                        } else {
                            sender.sendMessage("--Downloading: " + name + " from BukkitDev");
                        }
                        try {
                            if (download(plugin.aSupportedPlugins.get(name))) {
                                sender.sendMessage("--Downloaded: " + name);
                                plugin.getServer().reload();
                                sender.sendMessage(ChatColor.GREEN + "--Installed " + ChatColor.GOLD + pm.getPlugin(name).getDescription().getName() + ChatColor.GREEN + " version: " + ChatColor.WHITE + pm.getPlugin(name).getDescription().getVersion() + " successfully!");
                                sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                                return;
                            } else {
                                sender.sendMessage(ChatColor.RED + "--Could not install: " + ChatColor.GOLD + name);
                                sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                                return;
                            }
                        } catch (Exception e) {
                            if (sender == null) {
                                System.out.println("cs is null!");
                                sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                                return;
                            }
                            if (name == null) {
                                sender.sendMessage("name is null!");
                                sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                                return;
                            }
                            sender.sendMessage(ChatColor.RED + "--Could not install: " + ChatColor.GOLD + name);
                            e.printStackTrace();
                            sender.sendMessage(ChatColor.BLUE + "--Took " + (System.currentTimeMillis() - start) + "ms to complete");
                            return;
                        }
                    }
                });
            }
            if (args.length >= 1) {
                return ThreadRunner.runThread(new Runnable() {

                    public void run() {

                        for (int i = 0; i < args.length; i++) {
                            String plugin = args[i];
                            String message = "";
                            if (args.length > 1) {
                                message = " [" + (int) (i + 1) + "/" + args.length + "]";
                            }
                            if (whitelist) {
                                if (!allowed.contains(plugin)) {
                                    sender.sendMessage(ChatColor.RED + "--" + deny);
                                    return;
                                }
                            }

                            long time = System.currentTimeMillis();
                            sender.sendMessage(ChatColor.GREEN + "--Beginning install" + message);
                            try {
                                BukkitDevDownload bdd = new Sync().get(plugin);
                                final JavaPlugin pl = p;
                                if (bdd != null) {
                                    bdd.installJar(pl, sender, time, true);
                                } else {
                                    throw new NoInstallableJarException();
                                }
                                if (plugin.equals(args[args.length - 1])) {
                                    p.getServer().reload();
                                }
                            } catch (Exception e) {
                                sender.sendMessage(ChatColor.RED + "--" + e.getMessage());
                            }
                        }
                    }
                });
            }
        }
        return false;
    }

    //Download method for aPluginator supported plugins. Optional for BukkitDev Links
    private boolean download(aObj obj) throws MalformedURLException, URISyntaxException, IOException {
        URL url = obj.getURL();
        OutputStream oos = null;
        URLConnection connection = null;
        InputStream iis = null;
        String inLine;
        int x = 0;

        //Checks for BukkitDev URL
        if (!url.toString().endsWith(".jar")) {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                    url.openStream()));
            while ((inLine = in.readLine()) != null) {
                if (inLine.contains("/files/")) {
                    x++;
                    if (x > 2) {
                        inLine = inLine.replace("\t", "");
                        inLine = inLine.replace(" ", "");
                        String[] string = inLine.split("\"", 5);
                        inLine = "http://dev.bukkit.org" + string[3];
                        System.out.println(inLine);
                        url = new URL(inLine);
                        break;
                    }
                }
            }
            in.close();
            in = new BufferedReader(
                    new InputStreamReader(
                    url.openStream()));
            while ((inLine = in.readLine()) != null) {
                if (inLine.contains(".jar")) {
                    inLine = inLine.replace("/t", "");
                    inLine = inLine.replace(" ", "");
                    String[] string = inLine.split("\"", 7);
                    inLine = string[5];
                    System.out.println(inLine);
                    url = new URL(inLine);
                    break;
                }
            }
            in.close();
        }


        try {
            oos = new BufferedOutputStream(new FileOutputStream(obj.getPath()));
            connection = url.openConnection();
            iis = connection.getInputStream();
            byte[] buffer = new byte[1024];
            int numRead;
            long numWritten = 0;
            while ((numRead = iis.read(buffer)) != -1) {
                oos.write(buffer, 0, numRead);
                numWritten += numRead;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (iis != null) {
                    iis.close();
                }
                if (oos != null) {
                    oos.close();
                }
                return true;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
        }
    }

    private void getSupportedPlugins() {
        this.supPlugs.clear();
        for (String string : this.aSupportedPlugins.keySet()) {
            if (this.aSupportedPlugins.get(string) != null) {
                this.supPlugs.add(this.aSupportedPlugins.get(string));
            }
        }
    }
}
