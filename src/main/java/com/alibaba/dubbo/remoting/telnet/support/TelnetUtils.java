/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.remoting.telnet.support;

import java.util.Arrays;
import java.util.List;

public class TelnetUtils {
    public static String toList(List<List<String>> table) {
        int[] widths = new int[table.get(0).size()];
        for (int j = 0; j < widths.length; ++j) {
            for (List<String> row : table) {
                widths[j] = Math.max(widths[j], row.get(j).length());
            }
        }
        StringBuilder buf = new StringBuilder();
        for (List<String> row : table) {
            if (buf.length() > 0) {
                buf.append("\r\n");
            }
            for (int j = 0; j < widths.length; ++j) {
                int pad;
                if (j > 0) {
                    buf.append(" - ");
                }
                String value = row.get(j);
                buf.append(value);
                if (j >= widths.length - 1 || (pad = widths[j] - value.length()) <= 0) continue;
                for (int k = 0; k < pad; ++k) {
                    buf.append(" ");
                }
            }
        }
        return buf.toString();
    }

    public static String toTable(String[] header, List<List<String>> table) {
        return TelnetUtils.toTable(Arrays.asList(header), table);
    }

    public static String toTable(List<String> header, List<List<String>> table) {
        int k;
        int j;
        int totalWidth = 0;
        int[] widths = new int[header.size()];
        int maxwidth = 70;
        int maxcountbefore = 0;
        for (int j2 = 0; j2 < widths.length; ++j2) {
            widths[j2] = Math.max(widths[j2], header.get(j2).length());
        }
        for (List<String> row : table) {
            int countbefore = 0;
            for (int j3 = 0; j3 < widths.length; ++j3) {
                widths[j3] = Math.max(widths[j3], row.get(j3).length());
                int n = totalWidth = totalWidth + widths[j3] > maxwidth ? maxwidth : totalWidth + widths[j3];
                if (j3 >= widths.length - 1) continue;
                countbefore += widths[j3];
            }
            maxcountbefore = Math.max(countbefore, maxcountbefore);
        }
        widths[widths.length - 1] = Math.min(widths[widths.length - 1], maxwidth - maxcountbefore);
        StringBuilder buf = new StringBuilder();
        buf.append("+");
        for (j = 0; j < widths.length; ++j) {
            for (int k2 = 0; k2 < widths[j] + 2; ++k2) {
                buf.append("-");
            }
            buf.append("+");
        }
        buf.append("\r\n");
        buf.append("|");
        for (j = 0; j < widths.length; ++j) {
            String cell = header.get(j);
            buf.append(" ");
            buf.append(cell);
            int pad = widths[j] - cell.length();
            if (pad > 0) {
                for (int k3 = 0; k3 < pad; ++k3) {
                    buf.append(" ");
                }
            }
            buf.append(" |");
        }
        buf.append("\r\n");
        buf.append("+");
        for (j = 0; j < widths.length; ++j) {
            for (k = 0; k < widths[j] + 2; ++k) {
                buf.append("-");
            }
            buf.append("+");
        }
        buf.append("\r\n");
        for (List<String> row : table) {
            StringBuffer rowbuf = new StringBuffer();
            rowbuf.append("|");
            for (int j4 = 0; j4 < widths.length; ++j4) {
                String cell = row.get(j4);
                rowbuf.append(" ");
                for (int remaing = cell.length(); remaing > 0; --remaing) {
                    if (rowbuf.length() >= totalWidth) {
                        buf.append(rowbuf.toString());
                        rowbuf = new StringBuffer();
                    }
                    rowbuf.append(cell.substring(cell.length() - remaing, cell.length() - remaing + 1));
                }
                int pad = widths[j4] - cell.length();
                if (pad > 0) {
                    for (int k4 = 0; k4 < pad; ++k4) {
                        rowbuf.append(" ");
                    }
                }
                rowbuf.append(" |");
            }
            buf.append(rowbuf).append("\r\n");
        }
        buf.append("+");
        for (int j5 = 0; j5 < widths.length; ++j5) {
            for (k = 0; k < widths[j5] + 2; ++k) {
                buf.append("-");
            }
            buf.append("+");
        }
        buf.append("\r\n");
        return buf.toString();
    }
}

