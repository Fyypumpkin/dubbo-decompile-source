/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.status.support;

import com.alibaba.dubbo.common.status.Status;
import java.util.Map;
import java.util.Set;

public class StatusUtils {
    public static Status getSummaryStatus(Map<String, Status> statuses) {
        Status.Level level = Status.Level.OK;
        StringBuilder msg = new StringBuilder();
        for (Map.Entry<String, Status> entry : statuses.entrySet()) {
            String key = entry.getKey();
            Status status = entry.getValue();
            Status.Level l = status.getLevel();
            if (Status.Level.ERROR.equals((Object)l)) {
                level = Status.Level.ERROR;
                if (msg.length() > 0) {
                    msg.append(",");
                }
                msg.append(key);
                continue;
            }
            if (!Status.Level.WARN.equals((Object)l)) continue;
            if (!Status.Level.ERROR.equals((Object)level)) {
                level = Status.Level.WARN;
            }
            if (msg.length() > 0) {
                msg.append(",");
            }
            msg.append(key);
        }
        return new Status(level, msg.toString());
    }
}

