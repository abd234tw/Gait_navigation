package com.example.allen.gait_navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lucasurbas.listitemview.ListItemView;

public class SettingFragment extends Fragment {

    ListItemView lstAccount,lstRefresh,lstNotification,lstVoice;
    boolean isClick = true;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        lstAccount = view.findViewById(R.id.list_item_account);
        lstRefresh = view.findViewById(R.id.list_item_refresh);
        lstNotification = view.findViewById(R.id.list_item_notification);
        lstVoice = view.findViewById(R.id.list_item_voice);

        lstAccount.setOnMenuItemClickListener(new ListItemView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.setting_logout:
                        Toast.makeText(getContext(),"Logout",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        lstRefresh.setOnMenuItemClickListener(new ListItemView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.setting_info:
                        Toast.makeText(getContext(),"setting_info",Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.setting_remove:
                        Toast.makeText(getContext(),"setting_remove",Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        lstVoice.setOnMenuItemClickListener(new ListItemView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {

            }
        });

        lstNotification.setOnMenuItemClickListener(new ListItemView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                isClick = !isClick;
                lstNotification.inflateMenu(isClick?R.menu.check_menu:R.menu.uncheck_menu);
            }
        });
        return view;
    }
}
