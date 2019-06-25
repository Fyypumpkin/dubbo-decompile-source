/*
 * Decompiled with CFR 0.139.
 */
package com.alibaba.dubbo.container.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Page {
    private final String navigation;
    private final String title;
    private final List<String> columns;
    private final List<List<String>> rows;

    public Page(String navigation) {
        this(navigation, (String)null, (String[])null, (List<List<String>>)null);
    }

    public Page(String navigation, String title, String column, String row) {
        this(navigation, title, column == null ? null : Arrays.asList(column), row == null ? null : Page.stringToList(row));
    }

    private static List<List<String>> stringToList(String str) {
        ArrayList<List<String>> rows = new ArrayList<List<String>>();
        ArrayList<String> row = new ArrayList<String>();
        row.add(str);
        rows.add(row);
        return rows;
    }

    public Page(String navigation, String title, String[] columns, List<List<String>> rows) {
        this(navigation, title, columns == null ? null : Arrays.asList(columns), rows);
    }

    public Page(String navigation, String title, List<String> columns, List<List<String>> rows) {
        this.navigation = navigation;
        this.title = title;
        this.columns = columns;
        this.rows = rows;
    }

    public String getNavigation() {
        return this.navigation;
    }

    public String getTitle() {
        return this.title;
    }

    public List<String> getColumns() {
        return this.columns;
    }

    public List<List<String>> getRows() {
        return this.rows;
    }
}

