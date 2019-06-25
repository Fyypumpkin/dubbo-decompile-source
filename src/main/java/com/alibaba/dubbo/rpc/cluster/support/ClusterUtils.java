/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.support;

import com.alibaba.dubbo.common.URL;
import java.util.HashMap;
import java.util.Map;

public class ClusterUtils {
    public static URL mergeUrl(URL remoteUrl, Map<String, String> localMap) {
        HashMap<String, String> map = new HashMap<String, String>();
        Map<String, String> remoteMap = remoteUrl.getParameters();
        if (remoteMap != null && remoteMap.size() > 0) {
            map.putAll(remoteMap);
            map.remove("threadname");
            map.remove("default.threadname");
            map.remove("threadpool");
            map.remove("default.threadpool");
            map.remove("corethreads");
            map.remove("default.corethreads");
            map.remove("threads");
            map.remove("default.threads");
            map.remove("queues");
            map.remove("default.queues");
            map.remove("alive");
            map.remove("default.alive");
        }
        if (localMap != null && localMap.size() > 0) {
            map.putAll(localMap);
        }
        if (remoteMap != null && remoteMap.size() > 0) {
            String dataCenter;
            String methods;
            String group;
            String version;
            String dubbo = remoteMap.get("dubbo");
            if (dubbo != null && dubbo.length() > 0) {
                map.put("dubbo", dubbo);
            }
            if ((version = remoteMap.get("version")) != null && version.length() > 0) {
                map.put("version", version);
            }
            if ((group = remoteMap.get("group")) == null) {
                group = remoteMap.get("default.group");
            }
            if (group != null && group.length() > 0) {
                map.put("group", group);
            }
            if ((methods = remoteMap.get("methods")) != null && methods.length() > 0) {
                map.put("methods", methods);
            }
            if ((dataCenter = remoteMap.get("dc")) == null) {
                dataCenter = remoteMap.get("default.dc");
            }
            if (dataCenter != null && dataCenter.length() > 0) {
                map.put("dc", dataCenter);
                map.remove("default.dc");
            } else if (localMap != null && localMap.containsKey("dc") || localMap.containsKey("default.dc")) {
                map.remove("dc");
                map.remove("default.dc");
            }
            String remoteFilter = remoteMap.get("reference.filter");
            String localFilter = localMap.get("reference.filter");
            if (remoteFilter != null && remoteFilter.length() > 0 && localFilter != null && localFilter.length() > 0) {
                localMap.put("reference.filter", remoteFilter + "," + localFilter);
            }
            String remoteListener = remoteMap.get("invoker.listener");
            String localListener = localMap.get("invoker.listener");
            if (remoteListener != null && remoteListener.length() > 0 && localListener != null && localListener.length() > 0) {
                localMap.put("invoker.listener", remoteListener + "," + localListener);
            }
        }
        return remoteUrl.clearParameters().addParameters(map);
    }

    private ClusterUtils() {
    }
}

