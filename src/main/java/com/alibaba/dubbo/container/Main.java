/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container;

import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.extension.ExtensionLoader;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.container.Container;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

public class Main {
    public static final String CONTAINER_KEY = "dubbo.container";
    public static final String SHUTDOWN_HOOK_KEY = "dubbo.shutdown.hook";
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final ExtensionLoader<Container> loader = ExtensionLoader.getExtensionLoader(Container.class);
    private static volatile boolean running = true;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String config = ConfigUtils.getProperty(CONTAINER_KEY, loader.getDefaultExtensionName());
                args = Constants.COMMA_SPLIT_PATTERN.split(config);
            }
            final ArrayList<Container> containers = new ArrayList<Container>();
            for (int i = 0; i < args.length; ++i) {
                containers.add(loader.getExtension(args[i]));
            }
            logger.info("Use container type(" + Arrays.toString(args) + ") to run dubbo serivce.");
            if ("true".equals(System.getProperty(SHUTDOWN_HOOK_KEY))) {
                Runtime.getRuntime().addShutdownHook(new Thread(){

                    /*
                     * WARNING - Removed try catching itself - possible behaviour change.
                     * Enabled aggressive block sorting
                     * Enabled unnecessary exception pruning
                     * Enabled aggressive exception aggregation
                     * Converted monitor instructions to comments
                     * Lifted jumps to return sites
                     */
                    @Override
                    public void run() {
                        Iterator iterator = containers.iterator();
                        while (iterator.hasNext()) {
                            Container container = (Container)iterator.next();
                            try {
                                container.stop();
                                logger.info("Dubbo " + container.getClass().getSimpleName() + " stopped!");
                            }
                            catch (Throwable t) {
                                logger.error(t.getMessage(), t);
                            }
                            Class<Main> t = Main.class;
                            // MONITORENTER : com.alibaba.dubbo.container.Main.class
                            running = false;
                            Main.class.notify();
                            // MONITOREXIT : t
                        }
                    }
                });
            }
            for (Container container : containers) {
                container.start();
                logger.info("Dubbo " + container.getClass().getSimpleName() + " started!");
            }
            System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss]").format(new Date()) + " Dubbo service server started!");
        }
        catch (RuntimeException e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
            System.exit(1);
        }
        Class<Main> e = Main.class;
        synchronized (Main.class) {
            while (running) {
                try {
                    Main.class.wait();
                }
                catch (Throwable i) {}
            }
            // ** MonitorExit[e] (shouldn't be in output)
            return;
        }
    }

}

