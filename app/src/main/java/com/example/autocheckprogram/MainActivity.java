package com.example.autocheckprogram;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autocheckprogram.adapter.AppListAdapter;
import com.example.autocheckprogram.bean.App;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        PackageManager packageManager = getPackageManager();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        applications.removeIf(applicationInfo -> (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        Iterator<ApplicationInfo> appIterator = applications.iterator();

        LinkedList<App> apps = new LinkedList<>();

        while (appIterator.hasNext()) {

            ApplicationInfo appInfo = appIterator.next();

            Drawable appIcon = packageManager.getApplicationIcon(appInfo);
            String appName = packageManager.getApplicationLabel(appInfo).toString();
            String appPackage = appInfo.packageName;

            App app = new App(appIcon, appName, appPackage);
            apps.add(app);
        }

        RecyclerView appList = findViewById(R.id.app_list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        appList.setHasFixedSize(true);
        appList.setLayoutManager(layoutManager);
        appList.setAdapter(new AppListAdapter(this, apps));
    }
}