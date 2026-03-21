package com.autocheckprogram.adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.autocheckprogram.AutoCheckService;
import com.autocheckprogram.MainActivity;
import com.autocheckprogram.R;
import com.autocheckprogram.bean.App;
import com.autocheckprogram.enums.PageView;


import java.util.List;

public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.ListViewHolder> {

    private final Context context;
    private final List<App> apps;

    public AppListAdapter(Context context, List<App> appInfos) {
        this.context = context;
        this.apps = appInfos;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false);
        return (new ListViewHolder(view));
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {

        App app = apps.get(position);
        holder.icon.setImageDrawable(app.getIcon());
        holder.name.setText(app.getName());
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public class ListViewHolder extends RecyclerView.ViewHolder {
        final ImageView icon;
        final TextView name;
        final Button startApp;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.app_icon);
            name = itemView.findViewById(R.id.app_name);
            startApp = itemView.findViewById(R.id.start_app);

            startApp.setOnClickListener(new View.OnClickListener() {
                App app;
                Intent intent;

                @Override
                public void onClick(View v) {

                    app = apps.get(getAdapterPosition());

                    if ("QQ".equals(app.getName())) {
                        Toast.makeText(context, "QQ签到功能尚在开发中 (>▂<) ！", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (!isEnabledAccessibilityService()) {
                        intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                        context.startActivity(intent);
                        Toast.makeText(context, "请先开启无障碍功能", Toast.LENGTH_LONG).show();
                        return;
                    }

                    intent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());

//                    Log.d("IntentContentTest", String.valueOf(intent));

                    MainActivity.isChecking = true;
                    AutoCheckService.nowPage = PageView.HOME_PAGE;
                    context.startActivity(intent);
                }

                private boolean isEnabledAccessibilityService() {
                    String allEnabledAccessibilityService = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
                    if (allEnabledAccessibilityService == null) return false;

                    return allEnabledAccessibilityService.contains(context.getPackageName());
                }
            });
        }
    }
}