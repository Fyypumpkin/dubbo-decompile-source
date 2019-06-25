/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.filter;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.ConcurrentHashSet;
import com.alibaba.dubbo.common.utils.ConfigUtils;
import com.alibaba.dubbo.common.utils.NamedThreadFactory;
import com.alibaba.dubbo.rpc.Filter;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Activate(group={"provider"}, value={"accesslog"})
public class AccessLogFilter
implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(AccessLogFilter.class);
    private static final String ACCESS_LOG_KEY = "dubbo.accesslog";
    private static final String FILE_DATE_FORMAT = "yyyyMMdd";
    private static final String MESSAGE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final int LOG_MAX_BUFFER = 5000;
    private static final long LOG_OUTPUT_INTERVAL = 5000L;
    private final ConcurrentMap<String, Set<String>> logQueue = new ConcurrentHashMap<String, Set<String>>();
    private final ScheduledExecutorService logScheduled = Executors.newScheduledThreadPool(2, new NamedThreadFactory("Dubbo-Access-Log", true));
    private volatile ScheduledFuture<?> logFuture = null;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void init() {
        if (this.logFuture == null) {
            ScheduledExecutorService scheduledExecutorService = this.logScheduled;
            synchronized (scheduledExecutorService) {
                if (this.logFuture == null) {
                    this.logFuture = this.logScheduled.scheduleWithFixedDelay(new LogTask(), 5000L, 5000L, TimeUnit.MILLISECONDS);
                }
            }
        }
    }

    private void log(String accesslog, String logmessage) {
        this.init();
        Set logSet = (Set)this.logQueue.get(accesslog);
        if (logSet == null) {
            this.logQueue.putIfAbsent(accesslog, new ConcurrentHashSet());
            logSet = (Set)this.logQueue.get(accesslog);
        }
        if (logSet.size() < 5000) {
            logSet.add(logmessage);
        }
    }

    @Override
    public Result invoke(Invoker<?> invoker, Invocation inv) throws RpcException {
        try {
            String accesslog = invoker.getUrl().getParameter("accesslog");
            if (ConfigUtils.isNotEmpty(accesslog)) {
                RpcContext context = RpcContext.getContext();
                String serviceName = invoker.getInterface().getName();
                String version = invoker.getUrl().getParameter("version");
                String group = invoker.getUrl().getParameter("group");
                StringBuilder sn = new StringBuilder();
                sn.append("[").append(new SimpleDateFormat(MESSAGE_DATE_FORMAT).format(new Date())).append("] ").append(context.getRemoteHost()).append(":").append(context.getRemotePort()).append(" -> ").append(context.getLocalHost()).append(":").append(context.getLocalPort()).append(" - ");
                if (null != group && group.length() > 0) {
                    sn.append(group).append("/");
                }
                sn.append(serviceName);
                if (null != version && version.length() > 0) {
                    sn.append(":").append(version);
                }
                sn.append(" ");
                sn.append(inv.getMethodName());
                sn.append("(");
                Class<?>[] types = inv.getParameterTypes();
                if (types != null && types.length > 0) {
                    boolean first = true;
                    for (Class<?> type : types) {
                        if (first) {
                            first = false;
                        } else {
                            sn.append(",");
                        }
                        sn.append(type.getName());
                    }
                }
                sn.append(") ");
                Object[] args = inv.getArguments();
                if (args != null && args.length > 0) {
                    sn.append(JSON.json(args));
                }
                String msg = sn.toString();
                if (ConfigUtils.isDefault(accesslog)) {
                    LoggerFactory.getLogger("dubbo.accesslog." + invoker.getInterface().getName()).info(msg);
                } else {
                    this.log(accesslog, msg);
                }
            }
        }
        catch (Throwable t) {
            logger.warn("Exception in AcessLogFilter of service(" + invoker + " -> " + inv + ")", t);
        }
        return invoker.invoke(inv);
    }

    private class LogTask
    implements Runnable {
        private LogTask() {
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        @Override
        public void run() {
            try {
                if (AccessLogFilter.this.logQueue != null && AccessLogFilter.this.logQueue.size() > 0) {
                    for (Map.Entry entry : AccessLogFilter.this.logQueue.entrySet()) {
                        try {
                            String now;
                            String last;
                            String accesslog = (String)entry.getKey();
                            Set logSet = (Set)entry.getValue();
                            File file = new File(accesslog);
                            File dir = file.getParentFile();
                            if (null != dir && !dir.exists()) {
                                dir.mkdirs();
                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("Append log to " + accesslog);
                            }
                            if (file.exists() && !(now = new SimpleDateFormat(AccessLogFilter.FILE_DATE_FORMAT).format(new Date())).equals(last = new SimpleDateFormat(AccessLogFilter.FILE_DATE_FORMAT).format(new Date(file.lastModified())))) {
                                File archive = new File(file.getAbsolutePath() + "." + last);
                                file.renameTo(archive);
                            }
                            FileWriter writer = new FileWriter(file, true);
                            try {
                                Iterator iterator = logSet.iterator();
                                while (iterator.hasNext()) {
                                    writer.write((String)iterator.next());
                                    writer.write("\r\n");
                                    iterator.remove();
                                }
                                writer.flush();
                            }
                            finally {
                                writer.close();
                            }
                        }
                        catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

}

