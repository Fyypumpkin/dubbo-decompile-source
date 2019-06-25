/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.router.condition;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.common.utils.NetUtils;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.alibaba.dubbo.common.utils.UrlUtils;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionRouter
implements Router,
Comparable<Router> {
    private static final Logger logger = LoggerFactory.getLogger(ConditionRouter.class);
    private static Pattern ROUTE_PATTERN = Pattern.compile("([&!=,]*)\\s*([^&!=,\\s]+)");
    protected final URL url;
    protected final int priority;
    protected final boolean force;
    protected final Map<String, MatchPair> whenCondition;
    protected final Map<String, MatchPair> thenCondition;

    public ConditionRouter(URL url) {
        this.url = url;
        this.priority = url.getParameter("priority", 0);
        this.force = url.getParameter("force", false);
        try {
            String rule = url.getParameterAndDecoded("rule");
            if (rule == null || rule.trim().length() == 0) {
                throw new IllegalArgumentException("Illegal route rule!");
            }
            int i = (rule = rule.replace("consumer.", "").replace("provider.", "")).indexOf("=>");
            String whenRule = i < 0 ? null : rule.substring(0, i).trim();
            String thenRule = i < 0 ? rule.trim() : rule.substring(i + 2).trim();
            HashMap<String, MatchPair> when = StringUtils.isBlank(whenRule) || "true".equals(whenRule) ? new HashMap<String, MatchPair>() : ConditionRouter.parseRule(whenRule);
            Map<String, MatchPair> then = StringUtils.isBlank(thenRule) || "false".equals(thenRule) ? null : ConditionRouter.parseRule(thenRule);
            this.whenCondition = when;
            this.thenCondition = then;
        }
        catch (ParseException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Map<String, MatchPair> parseRule(String rule) throws ParseException {
        HashMap<String, MatchPair> condition = new HashMap<String, MatchPair>();
        if (StringUtils.isBlank(rule)) {
            return condition;
        }
        MatchPair pair = null;
        Set<String> values = null;
        Matcher matcher = ROUTE_PATTERN.matcher(rule);
        while (matcher.find()) {
            String separator = matcher.group(1);
            String content = matcher.group(2);
            if (separator == null || separator.length() == 0) {
                pair = new MatchPair();
                condition.put(content, pair);
                continue;
            }
            if ("&".equals(separator)) {
                if (condition.get(content) == null) {
                    pair = new MatchPair();
                    condition.put(content, pair);
                    continue;
                }
                pair = (MatchPair)condition.get(content);
                continue;
            }
            if ("=".equals(separator)) {
                if (pair == null) {
                    throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator + "' at index " + matcher.start() + " before \"" + content + "\".", matcher.start());
                }
                values = pair.matches;
                values.add(content);
                continue;
            }
            if ("!=".equals(separator)) {
                if (pair == null) {
                    throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator + "' at index " + matcher.start() + " before \"" + content + "\".", matcher.start());
                }
                values = pair.mismatches;
                values.add(content);
                continue;
            }
            if (",".equals(separator)) {
                if (values == null || values.isEmpty()) {
                    throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator + "' at index " + matcher.start() + " before \"" + content + "\".", matcher.start());
                }
                values.add(content);
                continue;
            }
            throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator + "' at index " + matcher.start() + " before \"" + content + "\".", matcher.start());
        }
        return condition;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        if (invokers == null || invokers.isEmpty()) {
            return invokers;
        }
        try {
            if (!this.matchWhen(url, invocation)) {
                return invokers;
            }
            ArrayList<Invoker<T>> result = new ArrayList<Invoker<T>>();
            if (this.thenCondition == null) {
                logger.warn("The current consumer in the service blacklist. consumer: " + NetUtils.getLocalHost() + ", service: " + url.getServiceKey());
                return result;
            }
            for (Invoker<T> invoker : invokers) {
                if (!this.matchThen(invoker.getUrl(), url)) continue;
                result.add(invoker);
            }
            if (!result.isEmpty()) {
                return result;
            }
            if (this.force) {
                logger.warn("The route result is empty and force execute. consumer: " + NetUtils.getLocalHost() + ", service: " + url.getServiceKey() + ", router: " + url.getParameterAndDecoded("rule"));
                return result;
            }
        }
        catch (Throwable t) {
            logger.error("Failed to execute condition router rule: " + this.getUrl() + ", invokers: " + invokers + ", cause: " + t.getMessage(), t);
        }
        return invokers;
    }

    @Override
    public URL getUrl() {
        return this.url;
    }

    @Override
    public int compareTo(Router o) {
        if (o == null || o.getClass() != ConditionRouter.class) {
            return 1;
        }
        ConditionRouter c = (ConditionRouter)o;
        return this.priority == c.priority ? this.url.toFullString().compareTo(c.url.toFullString()) : (this.priority > c.priority ? 1 : -1);
    }

    public boolean matchWhen(URL url, Invocation invocation) {
        return this.whenCondition == null || this.whenCondition.isEmpty() || this.matchCondition(this.whenCondition, url, null, invocation);
    }

    public boolean matchThen(URL url, URL param) {
        return this.thenCondition != null && !this.thenCondition.isEmpty() && this.matchCondition(this.thenCondition, url, param, null);
    }

    public boolean matchCondition(Map<String, MatchPair> condition, URL url, URL param, Invocation invocation) {
        Map<String, String> sample = url.toMap();
        boolean result = false;
        for (Map.Entry<String, MatchPair> matchPair : condition.entrySet()) {
            String sampleValue;
            String key = matchPair.getKey();
            if (invocation != null && ("method".equals(key) || "methods".equals(key))) {
                sampleValue = invocation.getMethodName();
            } else {
                sampleValue = sample.get(key);
                if (sampleValue == null) {
                    sampleValue = sample.get("default." + key);
                }
            }
            if (sampleValue != null) {
                if (!matchPair.getValue().isMatch(sampleValue, param)) {
                    return false;
                }
                result = true;
                continue;
            }
            if (!matchPair.getValue().matches.isEmpty()) {
                return false;
            }
            result = true;
        }
        return result;
    }

    public static final class MatchPair {
        final Set<String> matches = new HashSet<String>();
        final Set<String> mismatches = new HashSet<String>();

        public boolean isMatch(String value, URL param) {
            if (!this.matches.isEmpty() && this.mismatches.isEmpty()) {
                for (String match : this.matches) {
                    if (!UrlUtils.isMatchGlobPattern(match, value, param)) continue;
                    return true;
                }
                return false;
            }
            if (!this.mismatches.isEmpty() && this.matches.isEmpty()) {
                for (String mismatch : this.mismatches) {
                    if (!UrlUtils.isMatchGlobPattern(mismatch, value, param)) continue;
                    return false;
                }
                return true;
            }
            if (!this.matches.isEmpty() && !this.mismatches.isEmpty()) {
                for (String mismatch : this.mismatches) {
                    if (!UrlUtils.isMatchGlobPattern(mismatch, value, param)) continue;
                    return false;
                }
                for (String match : this.matches) {
                    if (!UrlUtils.isMatchGlobPattern(match, value, param)) continue;
                    return true;
                }
                return false;
            }
            return false;
        }
    }

}

