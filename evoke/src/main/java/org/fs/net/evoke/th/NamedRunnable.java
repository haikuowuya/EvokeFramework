package org.fs.net.evoke.th;

/**
 * Created by Fatih on 28/01/15.
 * as org.fs.net.evoke.th.NamedRunnable
 */
public abstract class NamedRunnable implements Runnable {
    
    private final String name;
    
    public NamedRunnable(String format, Object... args) {
        this.name = String.format(format, args);
    }

    /**
     * final part of run method, this has name swapping in runtime.
     */
    @Override
    public final void run() {
        String oldName = Thread.currentThread().getName();
        Thread.currentThread().setName(name);
        try {
            execute();
        } finally {
            Thread.currentThread().setName(oldName);
        }
    }

    /**
     * Execute part for thread to run this. 
     */
    protected abstract void execute();

}
