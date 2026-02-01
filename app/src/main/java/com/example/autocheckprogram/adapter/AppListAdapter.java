package com.example.autocheckprogram.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.autocheckprogram.R;
import com.example.autocheckprogram.bean.App;


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

        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
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

                    intent = context.getPackageManager().getLaunchIntentForPackage(app.getPackageName());

                    context.startActivity(intent);
                }
            });
        }
    }
}