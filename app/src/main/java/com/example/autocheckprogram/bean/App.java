package com.example.autocheckprogram.bean;

import android.graphics.drawable.Drawable;

public class App {
    private final Drawable icon;
    private final String name;
    private final String packageName;

    public App(Drawable icon, String name, String packageName) {
        this.icon = icon;
        this.name = name;
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public String getName() {
        return name;
    }
}
