package org.sevenlis.owmwidget.widget;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.FilterQueryProvider;
import android.widget.Spinner;

import org.sevenlis.owmwidget.R;
import org.sevenlis.owmwidget.adapter.CityCursorAdapter;
import org.sevenlis.owmwidget.classes.City;
import org.sevenlis.owmwidget.database.DBLocal;
import org.sevenlis.owmwidget.service.WidgetUpdateJobService;
import org.sevenlis.owmwidget.service.WidgetUpdateService;
import org.sevenlis.owmwidget.util.Const;

import java.util.ArrayList;
import java.util.Arrays;

public class AppWidgetConfigureActivity extends Activity {
    
    public static final String PREFS_NAME = AppWidget.class.getSimpleName();
    public static final String PREF_CITY_ID_KEY = "appWidget_city_id_";
    public static final String PREF_UPDATE_INTERVAL_KEY = "appWidget_update_interval_";
    public static final String PREF_DATETIME_UPDATED_KEY = "appWidget_datetime_updated_";
    public static final String ACTION_APPWIDGET_UPDATE = "ACTION_APPWIDGET_UPDATE_";
    
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    
    private String[] intervalNames;
    private String[] intervalValues;
    
    private DBLocal dbLocal;
    private City city;
    private AutoCompleteTextView mCityAutoCompleteText;
    private int widgetUpdateInterval;
    private CityCursorAdapter cityCursorAdapter;
    
    View.OnClickListener mOnClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AppWidgetConfigureActivity.this;
    
            savePrefs(context, mAppWidgetId, city, widgetUpdateInterval);
    
            // It is the responsibility of the configuration activity to update the app widget
            //AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            //AppWidget.updateAppWidget(context, appWidgetManager, mAppWidgetId);
    
            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, resultValue);
            
            setUpdateRepeatingAlarm(AppWidgetConfigureActivity.this, mAppWidgetId);
    
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                WidgetUpdateJobService.scheduleWidgetUpdateJob(context, mAppWidgetId);
            }
            
            finish();
        }
    };
    
    public AppWidgetConfigureActivity() {
        super();
    }
    
    public static int readCityIDPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(PREF_CITY_ID_KEY + appWidgetId, -1);
    }
    
    public static int readUpdateIntervalPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(PREF_UPDATE_INTERVAL_KEY + appWidgetId, 60);
    }
    
    public static void savePrefs(Context context, int appWidgetId, City city, int updateInterval) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefs.putInt(PREF_CITY_ID_KEY + appWidgetId, city.getId());
        prefs.putInt(PREF_UPDATE_INTERVAL_KEY + appWidgetId, updateInterval);
        prefs.apply();
    }
    
    public static void deletePrefs(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefs.remove(PREF_CITY_ID_KEY + appWidgetId);
        prefs.remove(PREF_UPDATE_INTERVAL_KEY + appWidgetId);
        prefs.apply();
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.app_widget_configure);
    
        dbLocal = new DBLocal(AppWidgetConfigureActivity.this);
        
        setResult(RESULT_CANCELED);
    
        mCityAutoCompleteText = findViewById(R.id.city_name_text);
        cityCursorAdapter = new CityCursorAdapter(this, dbLocal.getCitiesCursorFilter(null),false);
        mCityAutoCompleteText.setAdapter(cityCursorAdapter);
        
        cityCursorAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence charSequence) {
                return dbLocal.getCitiesCursorFilter(charSequence == null ? null : charSequence.toString());
            }
        });
        
        mCityAutoCompleteText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
    
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                cityCursorAdapter.getFilter().filter(charSequence);
            }
    
            @Override
            public void afterTextChanged(Editable editable) {}
        });
        
        mCityAutoCompleteText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long tag) {
                Cursor selected = (Cursor) adapterView.getItemAtPosition(position);
                city = new City(
                        selected.getInt(selected.getColumnIndexOrThrow("id")),
                        selected.getString(selected.getColumnIndexOrThrow("name")),
                        selected.getString(selected.getColumnIndexOrThrow("country")),
                        selected.getFloat(selected.getColumnIndexOrThrow("lat")),
                        selected.getFloat(selected.getColumnIndexOrThrow("lon"))
                );
                mCityAutoCompleteText.setText(city.getNameAndCountry());
            }
        });
        
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        city = dbLocal.getCity(readCityIDPref(AppWidgetConfigureActivity.this, mAppWidgetId));
        mCityAutoCompleteText.setText(city.getNameAndCountry());
    
        findViewById(R.id.add_button).setOnClickListener(mOnClickListener);
        
        intervalNames = getResources().getStringArray(R.array.time_interval_names);
        intervalValues = getResources().getStringArray(R.array.time_interval_values);
        widgetUpdateInterval = readUpdateIntervalPref(AppWidgetConfigureActivity.this,mAppWidgetId);
        
        Spinner spTimeIntervals = findViewById(R.id.sp_time_intervals);
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(AppWidgetConfigureActivity.this, android.R.layout.simple_spinner_item, intervalNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spTimeIntervals.setAdapter(adapter);
        spTimeIntervals.setSelection(new ArrayList<>(Arrays.asList(intervalValues)).indexOf(String.valueOf(widgetUpdateInterval)));
        spTimeIntervals.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long tag) {
                String selected = String.class.cast(adapterView.getItemAtPosition(position));
                widgetUpdateInterval = Integer.valueOf(intervalValues[new ArrayList<>(Arrays.asList(intervalNames)).indexOf(selected)]);
            }
    
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });
        
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbLocal.close();
    }
    
    public static void setUpdateRepeatingAlarm(Context context, int mAppWidgetId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        
        Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
        serviceIntent.setAction(AppWidgetConfigureActivity.ACTION_APPWIDGET_UPDATE + mAppWidgetId);
        serviceIntent.putExtra(Const.CITY_ID_KEY, readCityIDPref(context,mAppWidgetId));
        serviceIntent.putExtra(Const.WIDGET_ID_KEY, mAppWidgetId);
    
        context.getApplicationContext().startService(serviceIntent);
        
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(),mAppWidgetId,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        assert alarmManager != null;
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),readUpdateIntervalPref(context,mAppWidgetId) * 60 * 1000, pendingIntent);
    }
    
    public static void stopUpdateRepeatingAlarm(Context context, int appWidgetId) {
        AlarmManager alarmManager = (AlarmManager) context.getApplicationContext().getSystemService(ALARM_SERVICE);
    
        Intent serviceIntent = new Intent(context.getApplicationContext(), WidgetUpdateService.class);
        serviceIntent.setAction(AppWidgetConfigureActivity.ACTION_APPWIDGET_UPDATE + appWidgetId);
        serviceIntent.putExtra(Const.CITY_ID_KEY, readCityIDPref(context,appWidgetId));
        serviceIntent.putExtra(Const.WIDGET_ID_KEY, appWidgetId);
    
        PendingIntent pendingIntent = PendingIntent.getService(context.getApplicationContext(),appWidgetId,serviceIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        assert alarmManager != null;
        alarmManager.cancel(pendingIntent);
    }
    
}

