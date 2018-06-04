package org.sevenlis.owmwidget.service;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PersistableBundle;
import android.support.annotation.RequiresApi;

import org.sevenlis.owmwidget.util.Const;
import org.sevenlis.owmwidget.widget.AppWidgetConfigureActivity;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class WidgetUpdateJobService extends JobService {
    
    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        PersistableBundle jobExtras = jobParameters.getExtras();
        int mAppWidgetId = jobExtras.getInt(Const.WIDGET_ID_KEY, AppWidgetManager.INVALID_APPWIDGET_ID);
        
        Intent serviceIntent = new Intent(getApplicationContext(), WidgetUpdateService.class);
        serviceIntent.setAction(AppWidgetConfigureActivity.ACTION_APPWIDGET_UPDATE + mAppWidgetId);
        serviceIntent.putExtra(Const.CITY_ID_KEY, AppWidgetConfigureActivity.readCityIDPref(getApplicationContext(),mAppWidgetId));
        serviceIntent.putExtra(Const.WIDGET_ID_KEY, mAppWidgetId);
    
        getApplicationContext().startService(serviceIntent);
        return true;
    }
    
    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        jobFinished(jobParameters,true);
        return true;
    }
    
    public static void scheduleWidgetUpdateJob(Context context, int mAppWidgetId) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        JobInfo.Builder builder = new JobInfo.Builder(mAppWidgetId, new ComponentName(context, WidgetUpdateJobService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setRequiresCharging(false);
    
        PersistableBundle extras = new PersistableBundle();
        extras.putInt(Const.CITY_ID_KEY, AppWidgetConfigureActivity.readCityIDPref(context,mAppWidgetId));
        extras.putInt(Const.WIDGET_ID_KEY, mAppWidgetId);
        builder.setExtras(extras);
        
        builder.setPersisted(true);
    
        assert jobScheduler != null;
        jobScheduler.schedule(builder.build());
    }
    
    public static void cancelWidgetUpdateJob(Context context, int mAppWidgetId) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        assert jobScheduler != null;
        jobScheduler.cancel(mAppWidgetId);
    }
}
