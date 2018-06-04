package org.sevenlis.owmwidget.util;

import android.content.Context;

import org.sevenlis.owmwidget.classes.City;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

public final class Const {
    public static final String CITY_ID_KEY = "CITY_ID_KEY";
    public static final String WIDGET_ID_KEY = "WIDGET_ID_KEY";
    
    
    private static final String OWM_APPID_KEY = "d2d4914ba498207cdf634a249f1862d7";

    public static String getForecastLink(City city) {
        return MessageFormat.format("http://api.openweathermap.org/data/2.5/forecast?id={0}&mode=json&units=metric&lang=ru&appid={1}", String.valueOf(city.getId()), OWM_APPID_KEY);
    }
    
    public static String getCitySearchLink(String name) {
        return MessageFormat.format("http://api.openweathermap.org/data/2.5/find?q={0}&type=like&units=metric&lang=ru&appid={1}", name, OWM_APPID_KEY);
    }
    
    public static File getForecastsFolder(Context context) {
        return new File(String.format("%s%s%s", context.getFilesDir().getAbsolutePath(), File.separator, "forecasts"));
    }
    
    public static File getForecastFile(Context context, City city) {
        return new File(String.format("%s%s%s", getForecastsFolder(context).getAbsolutePath(), File.separator, String.valueOf(city.getId()) + ".json"));
    }
}
