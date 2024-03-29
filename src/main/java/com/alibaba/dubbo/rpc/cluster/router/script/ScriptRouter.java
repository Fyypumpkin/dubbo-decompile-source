/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.rpc.cluster.router.script;

import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.dubbo.rpc.cluster.Router;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class ScriptRouter
implements Router {
    private static final Logger logger = LoggerFactory.getLogger(ScriptRouter.class);
    private static final Map<String, ScriptEngine> engines = new ConcurrentHashMap<String, ScriptEngine>();
    private final ScriptEngine engine;
    private final int priority;
    private final String rule;
    private final URL url;

    @Override
    public URL getUrl() {
        return this.url;
    }

    public ScriptRouter(URL url) {
        this.url = url;
        String type = url.getParameter("type");
        this.priority = url.getParameter("priority", 0);
        String rule = url.getParameterAndDecoded("rule");
        if (type == null || type.length() == 0) {
            type = "javascript";
        }
        if (rule == null || rule.length() == 0) {
            throw new IllegalStateException(new IllegalStateException("route rule can not be empty. rule:" + rule));
        }
        ScriptEngine engine = engines.get(type);
        if (engine == null) {
            engine = new ScriptEngineManager().getEngineByName(type);
            if (engine == null) {
                throw new IllegalStateException(new IllegalStateException("Unsupported route rule type: " + type + ", rule: " + rule));
            }
            engines.put(type, engine);
        }
        this.engine = engine;
        this.rule = rule;
    }

    @Override
    public <T> List<Invoker<T>> route(List<Invoker<T>> invokers, URL url, Invocation invocation) throws RpcException {
        try {
            List<Invoker<Object>> invokersCopy = new ArrayList<Invoker<T>>(invokers);
            Compilable compilable = (Compilable)((Object)this.engine);
            Bindings bindings = this.engine.createBindings();
            bindings.put("invokers", invokersCopy);
            bindings.put("invocation", (Object)invocation);
            bindings.put("context", (Object)RpcContext.getContext());
            CompiledScript function = compilable.compile(this.rule);
            Object obj = function.eval(bindings);
            if (obj instanceof Invoker[]) {
                invokersCopy = Arrays.asList((Invoker[])obj);
            } else if (obj instanceof Object[]) {
                invokersCopy = new ArrayList();
                for (Object inv : (Object[])obj) {
                    invokersCopy.add((Invoker)inv);
                }
            } else {
                invokersCopy = (List)obj;
            }
            return invokersCopy;
        }
        catch (ScriptException e) {
            logger.error("route error , rule has been ignored. rule: " + this.rule + ", method:" + invocation.getMethodName() + ", url: " + RpcContext.getContext().getUrl(), e);
            return invokers;
        }
    }

    @Override
    public int compareTo(Router o) {
        if (o == null || o.getClass() != ScriptRouter.class) {
            return 1;
        }
        ScriptRouter c = (ScriptRouter)o;
        return this.priority == c.priority ? this.rule.compareTo(c.rule) : (this.priority > c.priority ? 1 : -1);
    }
}

