package org.sevenlis.owmwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.view.View;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;

import org.sevenlis.owmwidget.R;
import org.sevenlis.owmwidget.activity.MainActivity;
import org.sevenlis.owmwidget.classes.City;
import org.sevenlis.owmwidget.classes.Forecast;
import org.sevenlis.owmwidget.database.DBLocal;
import org.sevenlis.owmwidget.parser.ForecastJSONParser;
import org.sevenlis.owmwidget.service.WidgetUpdateJobService;
import org.sevenlis.owmwidget.service.WidgetUpdateService;
import org.sevenlis.owmwidget.util.Const;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AppWidgetConfigureActivity AppWidgetConfigureActivity}
 */
public class AppWidget extends AppWidgetProvider {
    
    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        DBLocal dbLocal = new DBLocal(context);
        City city = dbLocal.getCity(AppWidgetConfigureActivity.readCityIDPref(context, appWidgetId));
    
        Calendar rightNow = Calendar.getInstance();
        Forecast forecast = null;
        try {
            File forecastFile = Const.getForecastFile(context,city);
            if (forecastFile.exists()) {
                List<Forecast> forecastList = new ForecastJSONParser().readJsonStream(new FileInputStream(forecastFile));
                for (Forecast lForecast : forecastList) {
                    if (lForecast.getDate().after(rightNow.getTime())) {
                        forecast = lForecast;
                        break;
                    }
                }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        SharedPreferences prefs = context.getSharedPreferences(AppWidgetConfigureActivity.PREFS_NAME, Context.MODE_PRIVATE);
        Date updateDate = new Date(prefs.getLong(AppWidgetConfigureActivity.PREF_DATETIME_UPDATED_KEY + appWidgetId, Calendar.getInstance().getTimeInMillis()));
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM HH:mm", Locale.getDefault());
    
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_widget);
    
        views.setTextViewText(R.id.city_name_text, city.getNameAndCountry());
        views.setTextViewText(R.id.time_updated, sdf.format(updateDate));
        views.setImageViewResource(R.id.image_update, R.drawable.ic_sync);
        if (forecast == null) {
            views.setTextViewText(R.id.cur_temp_text, "---");
            
            views.setViewVisibility(R.id.date_text, View.INVISIBLE);
            views.setViewVisibility(R.id.time_text, View.INVISIBLE);
            views.setViewVisibility(R.id.min_max_temp_text, View.INVISIBLE);
            views.setViewVisibility(R.id.image_icon, View.INVISIBLE);
            
        } else {
            views.setViewVisibility(R.id.date_text, View.VISIBLE);
            views.setViewVisibility(R.id.time_text, View.VISIBLE);
            views.setViewVisibility(R.id.min_max_temp_text, View.VISIBLE);
            views.setViewVisibility(R.id.image_icon, View.VISIBLE);
            
            views.setTextViewText(R.id.cur_temp_text, forecast.getTemperatureForView(context));
            views.setTextViewText(R.id.date_text, forecast.getDateForWidget());
            views.setTextViewText(R.id.time_text, forecast.getTimeForView());
            views.setTextViewText(R.id.min_max_temp_text, forecast.getTemperatureMinMaxForWidget(context));
    
            loadImage(context, forecast, views, appWidgetManager, appWidgetId);
        }
    
        Intent configIntent = new Intent(context, AppWidgetConfigureActivity.class);
        configIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
        configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pIntentConfig = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.city_name_text, pIntentConfig);
        
        Intent mainIntent = new Intent(context, MainActivity.class);
        mainIntent.putExtra(Const.CITY_ID_KEY,city.getId());
        PendingIntent pIntentMain = PendingIntent.getActivity(context, appWidgetId, mainIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.image_icon,pIntentMain);
    
        Intent updateIntent = new Intent(context, WidgetUpdateService.class);
        updateIntent.setAction(AppWidgetConfigureActivity.ACTION_APPWIDGET_UPDATE + appWidgetId);
        updateIntent.putExtra(Const.CITY_ID_KEY, AppWidgetConfigureActivity.readCityIDPref(context,appWidgetId));
        updateIntent.putExtra(Const.WIDGET_ID_KEY, appWidgetId);
        PendingIntent pIntentUpdate = PendingIntent.getService(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.image_update,pIntentUpdate);
        views.setOnClickPendingIntent(R.id.time_updated,pIntentUpdate);
        
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
    
    private static void loadImage(final Context context, final Forecast forecast, final RemoteViews views, final AppWidgetManager appWidgetManager, final int appWidgetId) {
        final Handler handler = new Handler();
        
        Runnable load = new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bitmap = Picasso.with(context).load(forecast.getImageUrl()).get();
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            views.setImageViewBitmap(R.id.image_icon, bitmap);
                            appWidgetManager.updateAppWidget(appWidgetId, views);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        
        final Thread loadingThread = new Thread(load);
        loadingThread.start();
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetId : appWidgetIds) {
            AppWidgetConfigureActivity.deletePrefs(context, appWidgetId);
            AppWidgetConfigureActivity.stopUpdateRepeatingAlarm(context, appWidgetId);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WidgetUpdateJobService.cancelWidgetUpdateJob(context,appWidgetId);
            }
        }
    }
    
    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }
    
    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

