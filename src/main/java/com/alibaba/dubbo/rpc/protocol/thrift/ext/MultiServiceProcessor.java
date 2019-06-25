/*
 * Decompiled with CFR 0.139.
 * 
 * Could not load the following classes:
 *  org.apache.thrift.TException
 *  org.apache.thrift.TProcessor
 *  org.apache.thrift.protocol.TBinaryProtocol
 *  org.apache.thrift.protocol.TBinaryProtocol$Factory
 *  org.apache.thrift.protocol.TProtocol
 *  org.apache.thrift.protocol.TProtocolFactory
 *  org.apache.thrift.transport.TIOStreamTransport
 *  org.apache.thrift.transport.TTransport
 */
package com.alibaba.dubbo.rpc.protocol.thrift.ext;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.thrift.TException;
import org.apache.thrift.TProcessor;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolFactory;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

public class MultiServiceProcessor
implements TProcessor {
    private static final Logger logger = LoggerFactory.getLogger(MultiServiceProcessor.class);
    private ConcurrentMap<String, TProcessor> processorMap = new ConcurrentHashMap<String, TProcessor>();
    private TProtocolFactory protocolFactory = new TBinaryProtocol.Factory();

    public boolean process(TProtocol in, TProtocol out) throws TException {
        short magic = in.readI16();
        if (magic != -9540) {
            logger.error(new StringBuilder(24).append("Unsupported magic ").append(magic).toString());
            return false;
        }
        in.readI32();
        in.readI16();
        byte version = in.readByte();
        String serviceName = in.readString();
        long id = in.readI64();
        ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
        TIOStreamTransport transport = new TIOStreamTransport((OutputStream)bos);
        TProtocol protocol = this.protocolFactory.getProtocol((TTransport)transport);
        TProcessor processor = (TProcessor)this.processorMap.get(serviceName);
        if (processor == null) {
            logger.error(new StringBuilder(32).append("Could not find processor for service ").append(serviceName).toString());
            return false;
        }
        boolean result = processor.process(in, protocol);
        ByteArrayOutputStream header = new ByteArrayOutputStream(512);
        TIOStreamTransport headerTransport = new TIOStreamTransport((OutputStream)header);
        TProtocol headerProtocol = this.protocolFactory.getProtocol((TTransport)headerTransport);
        headerProtocol.writeI16(magic);
        headerProtocol.writeI32(Integer.MAX_VALUE);
        headerProtocol.writeI16((short)32767);
        headerProtocol.writeByte(version);
        headerProtocol.writeString(serviceName);
        headerProtocol.writeI64(id);
        headerProtocol.getTransport().flush();
        out.writeI16(magic);
        out.writeI32(bos.size() + header.size());
        out.writeI16((short)(65535 & header.size()));
        out.writeByte(version);
        out.writeString(serviceName);
        out.writeI64(id);
        out.getTransport().write(bos.toByteArray());
        out.getTransport().flush();
        return result;
    }

    public TProcessor addProcessor(Class service, TProcessor processor) {
        if (service != null && processor != null) {
            return this.processorMap.putIfAbsent(service.getName(), processor);
        }
        return processor;
    }

    public void setProtocolFactory(TProtocolFactory factory) {
        if (factory != null) {
            this.protocolFactory = factory;
        }
    }
}

