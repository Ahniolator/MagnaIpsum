package de.bananaco.magnaipsum;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class aObj {

    private URL url = null;
    private File path = null;
    private String[] keywords = null;
    private Integer initVal = 0, val = initVal;
    private String name = null, desc = null;

    public aObj(String name, String url, String path, String keywords, int initVal, String desc) {
        try {
            this.name = name;
            this.url = new URL(url);
            this.path = new File(path);
            this.keywords = keywords.replace(" ", "").toLowerCase().split(",");
            this.val = initVal;
            this.initVal = initVal;
            this.desc = desc;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public URL getURL() {
        return this.url;
    }

    public File getPath() {
        return this.path;
    }

    public String[] getKeys() {
        return this.keywords;
    }

    public Integer getVal() {
        return this.val;
    }

    public void resetVal() {
        this.val = this.initVal;
    }

    public void addToVal() {
        this.val = this.val + 1;
    }

    public String getName() {
        return this.name;
    }

    public String getDesc() {
        return this.desc;
    }
}
