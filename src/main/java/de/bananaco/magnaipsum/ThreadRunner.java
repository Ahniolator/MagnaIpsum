package de.bananaco.magnaipsum;

public class ThreadRunner extends Thread {

    private final Runnable r;

    ThreadRunner(Runnable r) {
        this.r = r;
    }

    @Override
    public void run() {
        r.run();
        interrupt();
    }

    public static boolean runThread(Runnable r) {
        ThreadRunner tr = new ThreadRunner(r);
        tr.start();
        return true;
    }
}
