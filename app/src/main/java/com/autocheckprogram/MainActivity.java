package com.autocheckprogram;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.autocheckprogram.adapter.AppListAdapter;
import com.autocheckprogram.bean.App;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static String MI_GAMING_COMMUNITY_PACKAGE = "com.mihoyo.hyperion";
    public static String QQ_PACKAGE = "com.tencent.mobileqq";

    public static boolean isChecking = false;

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

//        Log.d("生命周期测试", "onCreate()");


//        Button testButton = findViewById(R.id.test_button);
//
//        testButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d("AppNameTest", getString(R.string.app_name));
//            }
//        });

        PackageManager packageManager = getPackageManager();

        List<ApplicationInfo> applications = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        applications.removeIf(applicationInfo -> (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0);
        Iterator<ApplicationInfo> appIterator = applications.iterator();

        LinkedList<App> apps = new LinkedList<>();

        while (appIterator.hasNext()) {

            ApplicationInfo appInfo = appIterator.next();

            String appName = packageManager.getApplicationLabel(appInfo).toString();
            if (appName.equals(getString(R.string.app_name))) continue;

            Drawable appIcon = packageManager.getApplicationIcon(appInfo);
            String appPackage = appInfo.packageName;

//            Log.d("AppPackageTest", appPackage);

            App app = new App(appIcon, appName, appPackage);
            apps.add(app);
        }

//        Log.d("AppStartTest", apps.toString());

        RecyclerView appList = findViewById(R.id.app_list);

//        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
//        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);

        appList.setHasFixedSize(true);
        appList.setLayoutManager(gridLayoutManager);
        appList.setAdapter(new AppListAdapter(this, apps));
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("生命周期测试", "onStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("生命周期测试", "onResume()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("生命周期测试", "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("生命周期测试", "onStop()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("生命周期测试", "onRestart()");
    }

    @Override
    protected void onDestroy() {
        Log.d("生命周期测试", "onDestroy()");
        isChecking = false;
        super.onDestroy();
    }
}