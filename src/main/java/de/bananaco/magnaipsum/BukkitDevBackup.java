package de.bananaco.magnaipsum;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitDevBackup {

    private final String plugin;

    public BukkitDevBackup(String plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("unused")
    public void backup() throws Exception {
        File fileToGet = new File("plugins/" + plugin + ".jar");
        File fileToGetVersion = new File("plugins/plugins/" + plugin + ".txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(
                new DataInputStream(new FileInputStream(fileToGetVersion))));
        String thisVersion = br.readLine();
        String oldFile = br.readLine();
        br.close();
        if (oldFile != null) {
            fileToGet = new File("plugins/" + oldFile);
        }


        File fileToWrite = new File("plugins/backups/" + fileToGet.getName());
        File fileToWriteVersion = new File("plugins/backups/plugins/" + plugin + ".txt");
        if (!fileToGet.exists() || !fileToGetVersion.exists()) {
            throw new Exception("Cannot backup: " + fileToGet.getName() + " does not exist!");
        }
        if (!fileToWrite.exists()) {
            fileToWrite.getParentFile().mkdirs();
            fileToWrite.createNewFile();
        }
        if (!fileToWriteVersion.exists()) {
            fileToWriteVersion.getParentFile().mkdirs();
            fileToWriteVersion.createNewFile();
        }

        FileInputStream fGet = new FileInputStream(fileToGet);
        FileInputStream fGetVersion = new FileInputStream(fileToGetVersion);


        FileOutputStream fWrite = new FileOutputStream(fileToWrite);
        FileOutputStream fWriteVersion = new FileOutputStream(fileToWriteVersion);
        int byt;
        while ((byt = fGet.read()) >= 0) {
            fWrite.write(byt);
        }
        while ((byt = fGetVersion.read()) >= 0) {
            fWriteVersion.write(byt);
        }
        fGet.close();
        fGetVersion.close();
        fWrite.close();
        fWriteVersion.close();
    }

    @SuppressWarnings("unused")
    public void delete(JavaPlugin p, CommandSender sender, long time) throws Exception {
        File fileToWrite = new File("plugins/" + plugin + ".jar");
        File fileToWriteVersion = new File("plugins/plugins/" + plugin + ".txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new DataInputStream(new FileInputStream(fileToWriteVersion))));

        String thisVersion = br.readLine();
        String oldFile = br.readLine();
        br.close();
        if (oldFile != null) {
            fileToWrite = new File("plugins/" + oldFile);
        }

        if (fileToWrite.delete()) {
            fileToWriteVersion.delete();
            sender.sendMessage(ChatColor.BLUE + "--Uninstalled " + plugin + ". Took " + (int) (System.currentTimeMillis() - time) + "ms");
        } else {
            sender.sendMessage(ChatColor.RED + "--Can't uninstall, you're probably on windows");
        }
    }

    @SuppressWarnings("unused")
    public void rollback(JavaPlugin p, CommandSender sender) throws Exception {
        File fileToWrite = new File("plugins/" + plugin + ".jar");
        File fileToWriteVersion = new File("plugins/plugins/" + plugin + ".txt");
        File fileToGet = new File("plugins/backups/" + plugin + ".jar");
        File fileToGetVersion = new File("plugins/backups/plugins/" + plugin + ".txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new DataInputStream(new FileInputStream(fileToGetVersion))));

        String thisVersion = br.readLine();
        String oldFile = br.readLine();
        br.close();
        if (oldFile != null) {
            fileToGet = new File("plugins/backups/" + oldFile);
            fileToWrite = new File("plugins/" + oldFile);
        }

        if (!fileToGet.exists() || !fileToGetVersion.exists()) {
            throw new Exception("Cannot restore: " + fileToGet.getPath() + " does not exist!");
        }

        FileInputStream fGet = new FileInputStream(fileToGet);
        FileInputStream fGetVersion = new FileInputStream(fileToGetVersion);
        FileOutputStream fWrite = new FileOutputStream(fileToWrite);
        FileOutputStream fWriteVersion = new FileOutputStream(fileToWriteVersion);
        int byt;
        while ((byt = fGet.read()) >= 0) {
            fWrite.write(byt);
        }
        while ((byt = fGetVersion.read()) >= 0) {
            fWriteVersion.write(byt);
        }
        fGet.close();
        fGetVersion.close();
        fWrite.close();
        fWriteVersion.close();

        fileToGet.delete();
        fileToGetVersion.delete();

        sender.sendMessage(ChatColor.BLUE + "--Restored " + plugin + " to previous version");
        p.getServer().reload();
    }
    
    @SuppressWarnings("unused")
    public void rollback(JavaPlugin p) throws Exception {
        File fileToWrite = new File("plugins/" + plugin + ".jar");
        File fileToWriteVersion = new File("plugins/plugins/" + plugin + ".txt");
        File fileToGet = new File("plugins/backups/" + plugin + ".jar");
        File fileToGetVersion = new File("plugins/backups/plugins/" + plugin + ".txt");

        BufferedReader br = new BufferedReader(new InputStreamReader(
                new DataInputStream(new FileInputStream(fileToGetVersion))));

        String thisVersion = br.readLine();
        String oldFile = br.readLine();
        br.close();
        if (oldFile != null) {
            fileToGet = new File("plugins/backups/" + oldFile);
            fileToWrite = new File("plugins/" + oldFile);
        }

        if (!fileToGet.exists() || !fileToGetVersion.exists()) {
            throw new Exception("Cannot restore: " + fileToGet.getPath() + " does not exist!");
        }

        FileInputStream fGet = new FileInputStream(fileToGet);
        FileInputStream fGetVersion = new FileInputStream(fileToGetVersion);
        FileOutputStream fWrite = new FileOutputStream(fileToWrite);
        FileOutputStream fWriteVersion = new FileOutputStream(fileToWriteVersion);
        int byt;
        while ((byt = fGet.read()) >= 0) {
            fWrite.write(byt);
        }
        while ((byt = fGetVersion.read()) >= 0) {
            fWriteVersion.write(byt);
        }
        fGet.close();
        fGetVersion.close();
        fWrite.close();
        fWriteVersion.close();

        fileToGet.delete();
        fileToGetVersion.delete();

        p.getServer().reload();
    }
}
