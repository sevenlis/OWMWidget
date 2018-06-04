package org.sevenlis.owmwidget.classes;

import android.content.Context;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.sevenlis.owmwidget.R;

public class Forecast {
    private City city;
    private long dt;
    private double temp;
    private double temp_min;
    private double temp_max;
    private double pressure;
    private double sea_level;
    private double grnd_level;
    private double humidity;
    private double temp_kf;
    private int weather_id;
    private String weather_main;
    private String weather_description;
    private String weather_icon;
    private double clouds_all;
    private double wind_speed;
    private double wind_deg;
    private double rain_3h;
    private double snow_3h;
    private String sys_pod;
    private String dt_txt;
    
    public Forecast(City city, long dt, double temp, double temp_min, double temp_max,
                    double pressure, double sea_level, double grnd_level, double humidity,
                    double temp_kf, int weather_id, String weather_main, String weather_description,
                    String weather_icon, double clouds_all, double wind_speed, double wind_deg,
                    double rain_3h, double snow_3h, String sys_pod, String dt_txt) {
        this.city = city;
        this.dt = dt;
        this.temp = temp;
        this.temp_min = temp_min;
        this.temp_max = temp_max;
        this.pressure = pressure;
        this.sea_level = sea_level;
        this.grnd_level = grnd_level;
        this.humidity = humidity;
        this.temp_kf = temp_kf;
        this.weather_id = weather_id;
        this.weather_main = weather_main;
        this.weather_description = weather_description;
        this.weather_icon = weather_icon;
        this.clouds_all = clouds_all;
        this.wind_speed = wind_speed;
        this.wind_deg = wind_deg;
        this.rain_3h = rain_3h;
        this.snow_3h = snow_3h;
        this.sys_pod = sys_pod;
        this.dt_txt = dt_txt;
    }
    
    public Forecast() {
        this(new City(),0L,0d,0d,0d,0d,0d,0d,0d,0d,0,"","","",0d,0d,0d,0d,0d,"","");
    }
    
    public void setCity(City city) {
        this.city = city;
    }
    
    public Date getDate() {
        return new Date(this.dt * 1000);
    }
    
    public Date getDateStart() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDate());
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);
        return cal.getTime();
    }
    
    public String getDateForView() {
        DateFormat dateFormat = new SimpleDateFormat("E. dd.MM.yyyy", Locale.getDefault());
        return dateFormat.format(getDate());
    }
    
    public String getTimeForView() {
        DateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(getDate());
    }
    
    public String getDateForWidget() {
        DateFormat dateFormat = new SimpleDateFormat("E. dd.MM.yy", Locale.getDefault());
        return dateFormat.format(getDate());
    }
    
    public String getDateTimeForView() {
        DateFormat dateFormat = new SimpleDateFormat("E. dd.MM.yyyy  HH:mm", Locale.getDefault());
        return dateFormat.format(getDate());
    }
    
    public String getCloudsForView() {
        return MessageFormat.format("Облачность: {0}{1}", this.clouds_all, "%");
    }
    
    public String getTemperatureMinMaxForView(Context context) {
        String signMin = this.temp_min > 0 ? "+" : "";
        String signMax = this.temp_max > 0 ? "+" : "";
        return MessageFormat.format("Температура: {0}{1}{2} ... {3}{4}{5}", signMin, this.temp_min, context.getString(R.string.celsius), signMax, this.temp_max, context.getString(R.string.celsius));
    }
    
    public String getTemperatureMinMaxForWidget(Context context) {
        String signMin = this.temp_min > 0 ? "+" : "";
        String signMax = this.temp_max > 0 ? "+" : "";
        return MessageFormat.format("{0}{1} ... {2}{3}{4}", signMin, this.temp_min, signMax, this.temp_max, context.getString(R.string.celsius));
    }
    
    public String getTemperatureForView(Context context) {
        String sign = this.temp > 0 ? "+" : "";
        return MessageFormat.format("{0}{1}{2}", sign, this.temp, context.getString(R.string.celsius));
    }
    
    public String getPrecipitationForView() {
        if (this.rain_3h == 0 && this.snow_3h == 0) {
            return MessageFormat.format("Осадки: {0}", "без осадков");
        }
        
        if (this.rain_3h != 0 && this.snow_3h != 0) {
            return MessageFormat.format("Осадки: дождь {0} мм., снег {1} мм.", this.rain_3h, this.snow_3h);
        } else if (this.rain_3h != 0) {
            return MessageFormat.format("Осадки: дождь {0} мм.", this.rain_3h);
        } else {
            return MessageFormat.format("Осадки: снег {0} мм.", this.snow_3h);
        }
    }
    
    public String getWeatherDescription() {
        return this.weather_description;
    }
    
    public String getHumidityForView() {
        return MessageFormat.format("Отн. влажность: {0}{1}", this.humidity, "%");
    }
    
    public String getPressureForView() {
        return MessageFormat.format("Атм. давление: {0} {1}", hPaToMmHg(this.pressure), "мм.рт.ст.");
    }
    
    public String getWindForView() {
        return MessageFormat.format("Ветер: {0}, {1} м/с", degToCompass(this.wind_deg), this.wind_speed);
    }
    
    public String getImageUrl() {
        return MessageFormat.format("http://openweathermap.org/img/w/{0}.png", this.weather_icon);
    }
    
    private String degToCompass(double deg) {
        int val = (int) ((deg / 22.5) + .5);
        //String[] arr = {"N","NNE","NE","ENE","E","ESE", "SE", "SSE","S","SSW","SW","WSW","W","WNW","NW","NNW"};
        String[] arr = {"С","ССВ","СВ","ВСВ","В","ВЮВ", "ЮВ", "ЮЮВ","Ю","ЮЮЗ","ЮЗ","ЗЮЗ","З","ЗСЗ","СЗ","ССЗ"};
        return arr[(val % 16)];
    }
    
    private double hPaToMmHg(double hPa) {
        return hPa * 0.75006375541921;
    }
}