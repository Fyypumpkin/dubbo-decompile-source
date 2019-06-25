/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;

public class IOUtils {
    private static final int BUFFER_SIZE = 8192;

    private IOUtils() {
    }

    public static long write(InputStream is, OutputStream os) throws IOException {
        return IOUtils.write(is, os, 8192);
    }

    public static long write(InputStream is, OutputStream os, int bufferSize) throws IOException {
        long total = 0L;
        byte[] buff = new byte[bufferSize];
        while (is.available() > 0) {
            int read = is.read(buff, 0, buff.length);
            if (read <= 0) continue;
            os.write(buff, 0, read);
            total += (long)read;
        }
        return total;
    }

    public static String read(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        try {
            IOUtils.write(reader, writer);
            String string = writer.getBuffer().toString();
            return string;
        }
        finally {
            writer.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static long write(Writer writer, String string) throws IOException {
        StringReader reader = new StringReader(string);
        try {
            long l = IOUtils.write(reader, writer);
            return l;
        }
        finally {
            ((Reader)reader).close();
        }
    }

    public static long write(Reader reader, Writer writer) throws IOException {
        return IOUtils.write(reader, writer, 8192);
    }

    public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {
        int read;
        long total = 0L;
        char[] buf = new char[8192];
        while ((read = reader.read(buf)) != -1) {
            writer.write(buf, 0, read);
            total += (long)read;
        }
        return total;
    }

    public static String[] readLines(File file) throws IOException {
        if (file == null || !file.exists() || !file.canRead()) {
            return new String[0];
        }
        return IOUtils.readLines(new FileInputStream(file));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String[] readLines(InputStream is) throws IOException {
        ArrayList<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            String[] arrstring = lines.toArray(new String[0]);
            return arrstring;
        }
        finally {
            reader.close();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void writeLines(OutputStream os, String[] lines) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));
        try {
            for (String line : lines) {
                writer.println(line);
            }
            writer.flush();
        }
        finally {
            writer.close();
        }
    }

    public static void writeLines(File file, String[] lines) throws IOException {
        if (file == null) {
            throw new IOException("File is null.");
        }
        IOUtils.writeLines(new FileOutputStream(file), lines);
    }

    public static void appendLines(File file, String[] lines) throws IOException {
        if (file == null) {
            throw new IOException("File is null.");
        }
        IOUtils.writeLines(new FileOutputStream(file, true), lines);
    }
}

