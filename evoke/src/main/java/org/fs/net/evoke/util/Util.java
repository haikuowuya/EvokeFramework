package org.fs.net.evoke.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadFactory;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Fatih on 28/01/15.
 * as org.fs.net.evoke.util.Util
 */
public final class Util {

    private Util() { }

    /**
     * don't find Math.pow() function useful.
     * @param x element to get power of n
     * @param n number of the power
     * @return n times x
     */
    public static int pow(int x, int n) {
        int m;
        if(n == 0) {
            return  1;
        } else if(n % 2 == 0) {
            m = pow(x, n / 2);
            return m * m;
        } else  {
            return x * pow(x, n - 1);
        }
    }

    /**
     * Copy file a to b and remove a. 
     * @param source source file
     * @param destination destination file
     * @return true if success, false if fail.
     */
    public static boolean move(File source, File destination) {
        try {
            FileInputStream in = new FileInputStream(source);
            FileOutputStream out = new FileOutputStream(destination);
            byte[] buffer = new byte[1024 * 1024];
            int size;
            while ((size = in.read(buffer)) >= 0) {
                out.write(buffer, 0, size);
            }
            in.close();
            out.flush();
            out.close();            
            source.delete();
            return true;
        } catch (Exception exp) {
            exp.printStackTrace();   
        }        
        return false;
    }

    /**
     * Zip files will be extracted this way.
     * @param source
     * @param destination
     */
    public static void extract(File source, File destination) {
        FileInputStream     in  = null;
        ZipInputStream      zin = null;
        FileOutputStream    out = null;
        try {
            in = new FileInputStream(source);
            zin = new ZipInputStream(in);
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                File file = new File(destination, entry.getName());
                if(entry.isDirectory()) {
                    file.mkdirs();
                } 
                else {
                    File parent = file.getParentFile();
                    if(!parent.exists()) {
                        parent.mkdirs();
                    }
                    out = new FileOutputStream(file);
                    byte[] buffer = new byte[8192];
                    int seed;
                    while ((seed = zin.read(buffer, 0, buffer.length)) != -1) {
                        out.write(buffer, 0, seed);
                    }
                    out.close();
                    out = null;
                }
            }
        }
        catch (IOException ioe) {
            if(LogUtil.isLogEnabled()) {
                ioe.printStackTrace();
            }
        }
        finally {
            try {
                zin.close();
                in.close();
                out.close();
            } catch (Exception e) {
                if(LogUtil.isLogEnabled()) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    /**
     * New thread creation factory
     * @param name name of thread
     * @param daemon if its daemon or not.
     * @return ThreadFactory instance initialized for given values.
     */
    public static ThreadFactory threadFactory(final String name, final boolean daemon) {
        return new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, name);
                thread.setDaemon(daemon);
                return thread;
            }
        };
    }
}
