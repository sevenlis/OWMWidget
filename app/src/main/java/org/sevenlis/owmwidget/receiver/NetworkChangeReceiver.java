package org.sevenlis.owmwidget.receiver;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.sevenlis.owmwidget.service.WidgetUpdateService;
import org.sevenlis.owmwidget.util.Const;
import org.sevenlis.owmwidget.widget.AppWidget;
import org.sevenlis.owmwidget.widget.AppWidgetConfigureActivity;

public class NetworkChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        assert action != null;
        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE") || action.equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            
            SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(AppWidgetConfigureActivity.PREFS_NAME, Context.MODE_PRIVATE);
            int[] widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context.getApplicationContext(), AppWidget.class));
            for (int widgetId : widgetIds) {
                Intent serviceIntent = new Intent(context, WidgetUpdateService.class);
                serviceIntent.setAction(AppWidgetConfigureActivity.ACTION_APPWIDGET_UPDATE + widgetId);
                serviceIntent.putExtra(Const.CITY_ID_KEY, prefs.getInt(AppWidgetConfigureActivity.PREF_CITY_ID_KEY + widgetId,-1));
                serviceIntent.putExtra(Const.WIDGET_ID_KEY, widgetId);
                context.startService(serviceIntent);
            }
                
        }
    }
}
