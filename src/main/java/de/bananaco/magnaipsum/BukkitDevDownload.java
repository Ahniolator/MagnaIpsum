package de.bananaco.magnaipsum;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitDevDownload {

    private final String version;
    private final String link;
    private final String plugin;

    public BukkitDevDownload(String plugin, String version, String link) throws Exception {
        this.version = version;
        this.plugin = plugin.toLowerCase();

        URL pluginDownload = new URL(link);
        BufferedReader in = new BufferedReader(new InputStreamReader(pluginDownload.openStream()));

        String inputLine;
        while ((inputLine = in.readLine()) != null) {
            inputLine = inputLine.trim();

            if (inputLine.contains("Download")) {
                Pattern regex = Pattern.compile("http[^>]*jar", Pattern.MULTILINE);
                Matcher regexMatcher = regex.matcher(inputLine);

                Pattern regex2 = Pattern.compile("http[^>]*zip", Pattern.MULTILINE);
                Matcher regexMatcher2 = regex2.matcher(inputLine);

                if (regexMatcher.find()) {
                    link = regexMatcher.group();
                    this.link = link;
                    return;
                } else if (regexMatcher2.find()) {
                    link = regexMatcher2.group();
                    this.link = link;
                    return;
                } else {
                    throw new NoInstallableJarException();
                }
            }
        }
        this.link = link;
    }

    public String getVersion() {
        return version;
    }

    public String getLink() {
        return link;
    }

    public String toString() {
        return version + "=" + link;
    }

    private void installZip(JavaPlugin p, CommandSender sender, long time) throws Exception {
        URL url = new URL(link);

        URLConnection uc = url.openConnection();
        File fileName = new File("plugins/zips/" + plugin + ".zip");
        File textFile = new File("plugins/plugins/" + plugin + ".txt");

        if (!fileName.exists()) {
            fileName.getParentFile().mkdirs();
            fileName.createNewFile();
        }
        if (!textFile.exists()) {
            textFile.getParentFile().mkdir();
            textFile.createNewFile();
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new DataInputStream(new FileInputStream(textFile))));
        String thisVersion = br.readLine();

        if (thisVersion == null || !thisVersion.equals(version)) {
            sender.sendMessage(ChatColor.GREEN + "--Installing version:" + this.version);
            br.close();

            OutputStreamWriter osText = new OutputStreamWriter(new FileOutputStream(textFile));
            osText.write(this.version);
            osText.flush();
            osText.close();

            BufferedInputStream bin = new BufferedInputStream(url.openStream());
            byte[] b = new byte[uc.getContentLength()];
            FileOutputStream out = new FileOutputStream(fileName);
            int bytesRead = 0;
            int offset = 0;
            while (offset < uc.getContentLength()) {
                bytesRead = bin.read(b, offset, b.length - offset);
                if (bytesRead == -1) {
                    break;
                }
                offset += bytesRead;
            }
            out.write(b);
            out.flush();
            out.close();

            ZipFile zf = new ZipFile(fileName);

            @SuppressWarnings("rawtypes")
            Enumeration e = zf.entries();
            while (e.hasMoreElements()) {
                ZipEntry ze = (ZipEntry) e.nextElement();

                File f = new File("plugins/" + ze.getName());
                System.out.println(f.getCanonicalPath());
                if (!f.exists()) {
                    if (!f.getName().contains(".")) {
                        f.mkdirs();
                    } else {
                        f.createNewFile();
                        FileOutputStream fout = new FileOutputStream(f);
                        InputStream in = zf.getInputStream(ze);
                        for (int c = in.read(); c != -1; c = in.read()) {
                            fout.write(c);
                        }
                        in.close();
                        fout.close();
                    }
                }
            }
            p.getServer().broadcastMessage(ChatColor.BLUE + "[BDD] " + ChatColor.GREEN + plugin + " was installed");
            sender.sendMessage(ChatColor.BLUE + "--Installed " + plugin + ". Took " + (System.currentTimeMillis() - time) + "ms");

            p.getServer().reload();
        } else {
            sender.sendMessage(ChatColor.GREEN + "--" + this.plugin + " up to date with version:" + this.version);
        }
    }

    public void installJar(JavaPlugin p, CommandSender sender, long time, boolean reload) throws Exception {
        if (link.endsWith(".zip")) {
            installZip(p, sender, time);
            return;
        }

        URL url = new URL(link);
        String file = new File(url.getFile()).getName();
        URLConnection uc = url.openConnection();

        File fileName = new File("plugins/" + file);
        File textFile = new File("plugins/plugins/" + plugin + ".txt");

        if (!textFile.exists()) {
            textFile.getParentFile().mkdir();
            textFile.createNewFile();
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new DataInputStream(new FileInputStream(textFile))));
        String thisVersion = br.readLine();
        String oldFile = br.readLine();

        if (oldFile != null) {
            if (!oldFile.equals(file)) {
                throw new Exception("Cannot install due to different jarfile names");
            }
        }

        if (thisVersion == null || !thisVersion.equals(version)) {

            if (fileName.exists() && textFile.exists()) {
                new BukkitDevBackup(plugin).backup();
                System.out.println("Attempted to backup " + plugin);
            }
            if (!fileName.exists()) {
                fileName.getParentFile().mkdirs();
                fileName.createNewFile();
            }


            sender.sendMessage(ChatColor.GREEN + "--Installing version:" + this.version);

            OutputStreamWriter osText = new OutputStreamWriter(new FileOutputStream(textFile));
            osText.write(this.version);
            osText.append("\n" + file);
            osText.flush();
            osText.close();

            BufferedInputStream bin = new BufferedInputStream(url.openStream());
            byte[] b = new byte[uc.getContentLength()];
            FileOutputStream out = new FileOutputStream(fileName);
            int bytesRead = 0;
            int offset = 0;
            while (offset < uc.getContentLength()) {
                bytesRead = bin.read(b, offset, b.length - offset);
                if (bytesRead == -1) {
                    break;
                }
                offset += bytesRead;
            }
            out.write(b);
            out.flush();
            out.close();
            p.getServer().broadcastMessage(ChatColor.BLUE + "[MagnaIpsum] " + ChatColor.GREEN + plugin + " was installed");
            sender.sendMessage(ChatColor.BLUE + "--Installed " + plugin + ". Took " + (System.currentTimeMillis() - time) + "ms");
            if (!reload) {
                description(p, fileName);
            } else {
                p.getServer().reload();
            }
        } else {
            sender.sendMessage(ChatColor.GREEN + "--" + this.plugin + " up to date with version:" + this.version);
        }
    }

    private void description(JavaPlugin p, File f) throws Exception {
        Plugin plugin = p.getServer().getPluginManager().loadPlugin(f);

        p.getServer().getPluginManager().enablePlugin(plugin);
    }
}