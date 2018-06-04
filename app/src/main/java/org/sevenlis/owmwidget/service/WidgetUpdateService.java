package org.sevenlis.owmwidget.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

import org.sevenlis.owmwidget.classes.City;
import org.sevenlis.owmwidget.database.DBLocal;
import org.sevenlis.owmwidget.util.Const;
import org.sevenlis.owmwidget.util.NetworkUtil;
import org.sevenlis.owmwidget.widget.AppWidget;
import org.sevenlis.owmwidget.widget.AppWidgetConfigureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

public class WidgetUpdateService extends Service {
    
    public WidgetUpdateService() {}
    
    void sendUpdateWidget(int mAppWidgetId) {
        Intent intent = new Intent(getApplicationContext(), AppWidget.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{mAppWidgetId});
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(),mAppWidgetId,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int mAppWidgetId = extras.getInt(Const.WIDGET_ID_KEY, AppWidgetManager.INVALID_APPWIDGET_ID);
            City city = new DBLocal(getApplicationContext()).getCity(extras.getInt(Const.CITY_ID_KEY, -1));
            new DownloadForecastTask(city, mAppWidgetId).execute();
        }
        return START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
    
    @SuppressLint("StaticFieldLeak")
    private class DownloadForecastTask extends AsyncTask<Void,Integer,Void> {
        private City city;
        private int appWidgetId;
    
        DownloadForecastTask(City city, int appWidgetId) {
            this.city = city;
            this.appWidgetId = appWidgetId;
        }
    
        @Override
        protected Void doInBackground(Void... voids) {
            downloadForecast(city);
            return null;
        }
    
        @Override
        protected void onPostExecute(Void aVoid) {
            sendUpdateWidget(appWidgetId);
        }
        
        private void downloadForecast(City city) {
            if (NetworkUtil.getConnectivityStatus(getApplicationContext()) == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                return;
            }
            
            final int batchSize = 4096;
            final File folder = Const.getForecastsFolder(getApplicationContext());
            final File file = new File(String.format("%s%s%s", folder.getAbsolutePath(), File.separator, String.valueOf(city.getId()) + ".json"));
            final String forecastLink = Const.getForecastLink(city);
    
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
    
            try {
                connection = (HttpURLConnection) new URL(forecastLink).openConnection();
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    throw new IOException();
                } else if (!folder.exists()) {
                    if (!folder.mkdir()) {
                        throw new IOException();
                    }
                }
                boolean deleted = true;
                boolean created = true;
                if (file.exists())
                    deleted = file.delete();
                if (deleted)
                    created = file.createNewFile();
                if (!created)
                    throw new IOException();
                
                input = connection.getInputStream();
                output = new FileOutputStream(file);
        
                int count;
                byte[] data = new byte[batchSize];
                while ((count = input.read(data)) != -1) {
                    output.write(data,0,count);
                }
        
            } catch (MalformedURLException e) {
                e.printStackTrace();
        
            } catch (IOException e) {
                e.printStackTrace();
        
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                    if (connection != null)
                        connection.disconnect();
                } catch (IOException ignored) {
                    //do nothing
                }
                SharedPreferences prefs = getApplicationContext().getSharedPreferences(AppWidgetConfigureActivity.PREFS_NAME, MODE_PRIVATE);
                prefs.edit().putLong(AppWidgetConfigureActivity.PREF_DATETIME_UPDATED_KEY + appWidgetId, Calendar.getInstance().getTimeInMillis()).apply();
            }
        }
    }
}
