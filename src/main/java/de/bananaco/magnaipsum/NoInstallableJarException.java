package de.bananaco.magnaipsum;

public class NoInstallableJarException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public NoInstallableJarException() {
        super("Failed: No installable jar found");
    }
}
