package org.sevenlis.owmwidget.parser;

import android.content.ContentValues;
import android.util.JsonReader;

import org.sevenlis.owmwidget.classes.City;
import org.sevenlis.owmwidget.classes.Forecast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ForecastJSONParser {
    
    public List<Forecast> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
    
        List<Forecast> forecasts = new ArrayList<>();
        City city = new City();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            if (sName.equalsIgnoreCase("list")) {
                forecasts = readForecastsArray(reader);
            } else if (sName.equalsIgnoreCase("city")) {
                city = readCity(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        reader.close();
        
        for (Forecast forecast : forecasts) {
            forecast.setCity(city);
        }
        
        return forecasts;
    }
    
    private List<Forecast> readForecastsArray(JsonReader reader) throws IOException {
        List<Forecast> forecasts = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            forecasts.add(readForecast(reader));
        }
        reader.endArray();
        return forecasts;
    }
    
    private Forecast readForecast(JsonReader reader) throws IOException {
        long dt = 0L;
        double temp = 0D;
        double temp_min = 0D;
        double temp_max = 0D;
        double pressure = 0D;
        double sea_level = 0D;
        double grnd_level = 0D;
        double humidity = 0D;
        double temp_kf = 0D;
        int weather_id = 0;
        String weather_main = "";
        String weather_description = "";
        String weather_icon = "";
        double clouds_all = 0D;
        double wind_speed = 0D;
        double wind_deg = 0D;
        double rain_3h = 0D;
        double snow_3h = 0D;
        String sys_pod = "";
        String dt_txt = "";
        
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            if (sName.equalsIgnoreCase("dt")) {
                dt = reader.nextLong();
            } else if (sName.equalsIgnoreCase("dt_txt")) {
                dt_txt = reader.nextString();
            } else if (sName.equalsIgnoreCase("main")) {
                ContentValues main = readMain(reader);
                if (main.containsKey("temp"))
                    temp = main.getAsDouble("temp");
                
                if (main.containsKey("temp_min"))
                    temp_min = main.getAsDouble("temp_min");
                
                if (main.containsKey("temp_max"))
                    temp_max = main.getAsDouble("temp_max");
                
                if (main.containsKey("pressure"))
                    pressure = main.getAsDouble("pressure");
                
                if (main.containsKey("sea_level"))
                    sea_level = main.getAsDouble("sea_level");
                
                if (main.containsKey("grnd_level"))
                    grnd_level = main.getAsDouble("grnd_level");
                
                if (main.containsKey("humidity"))
                    humidity = main.getAsDouble("humidity");
                
                if (main.containsKey("temp_kf"))
                    temp_kf = main.getAsDouble("temp_kf");
                
            } else if (sName.equalsIgnoreCase("weather")) {
                ContentValues weather = readWeather(reader);
                if (weather.containsKey("id"))
                    weather_id = weather.getAsInteger("id");
                
                if (weather.containsKey("main"))
                    weather_main = weather.getAsString("main");
                
                if (weather.containsKey("description"))
                    weather_description = weather.getAsString("description");
                
                if (weather.containsKey("icon"))
                    weather_icon = weather.getAsString("icon");
                
            } else if (sName.equalsIgnoreCase("clouds")) {
                ContentValues clouds = readDoubles(reader);
                if (clouds.containsKey("all"))
                    clouds_all = clouds.getAsDouble("all");
                
            } else if (sName.equalsIgnoreCase("wind")) {
                ContentValues wind = readDoubles(reader);
                if (wind.containsKey("speed"))
                    wind_speed = wind.getAsDouble("speed");
                if (wind.containsKey("deg"))
                    wind_deg = wind.getAsDouble("deg");
                
            } else if (sName.equalsIgnoreCase("rain")) {
                ContentValues rain = readDoubles(reader);
                if (rain.containsKey("3h"))
                    rain_3h = rain.getAsDouble("3h");
                
            } else if (sName.equalsIgnoreCase("snow")) {
                ContentValues snow = readDoubles(reader);
                if (snow.containsKey("3h"))
                    snow_3h = snow.getAsDouble("3h");
                
            } else if (sName.equalsIgnoreCase("sys")) {
                ContentValues sys = readStrings(reader);
                if (sys.containsKey("pod"))
                    sys_pod = sys.getAsString("pod");
                
            } else if (sName.equalsIgnoreCase("dt_txt")) {
                dt_txt = reader.nextString();
                
            } else {
                reader.skipValue();
                
            }
        }
        reader.endObject();
        
        return new Forecast(null,dt,temp,temp_min,temp_max,pressure,sea_level,grnd_level,humidity,temp_kf,weather_id,weather_main,weather_description,weather_icon,clouds_all,wind_speed,wind_deg,rain_3h,snow_3h,sys_pod,dt_txt);
    }
    
    private ContentValues readDoubles(JsonReader reader) throws IOException {
        ContentValues doubles = new ContentValues();
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            doubles.put(sName,reader.nextDouble());
        }
        reader.endObject();
        return doubles;
    }
    
    private ContentValues readStrings(JsonReader reader) throws IOException {
        ContentValues strings = new ContentValues();
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            strings.put(sName,reader.nextString());
        }
        reader.endObject();
        return strings;
    }
    
    private ContentValues readWeather(JsonReader reader) throws IOException {
        ContentValues weather = new ContentValues();
        reader.beginArray();
        if (reader.hasNext()) {
            reader.beginObject();
            while (reader.hasNext()) {
                String sName = reader.nextName();
                if (sName.equalsIgnoreCase("id")) {
                    weather.put(sName,reader.nextInt());
                } else if (sName.equalsIgnoreCase("main") ||
                        sName.equalsIgnoreCase("description") ||
                        sName.equalsIgnoreCase("icon")) {
                    weather.put(sName,reader.nextString());
                } else {
                    reader.skipValue();
                }
            }
            reader.endObject();
        }
        reader.endArray();
        
        return weather;
    }
    
    private ContentValues readMain(JsonReader reader) throws IOException {
        ContentValues main = new ContentValues();
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            main.put(sName,reader.nextDouble());
        }
        reader.endObject();
        return main;
    }
    
    private City readCity(JsonReader reader) throws IOException {
        int id = -1;
        String name = "";
        String country = "";
        Map<String,Float> coord = new HashMap<>();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String mName = reader.nextName();
            if (mName.equalsIgnoreCase("id")) {
                id = reader.nextInt();
            } else if (mName.equalsIgnoreCase("name")) {
                name = reader.nextString();
            } else if (mName.equalsIgnoreCase("country")) {
                country = reader.nextString();
            } else if (mName.equalsIgnoreCase("coord")) {
                coord = readCoord(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        
        return new City(id,name,country,coord.get("lat"),coord.get("lon"));
    }
    
    private Map<String,Float> readCoord(JsonReader reader) throws IOException {
        float lat = 0f, lon = 0f;
        Map<String, Float> coord = new HashMap<>();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String mName = reader.nextName();
            if (mName.equalsIgnoreCase("lat")) {
                lat = Float.parseFloat(reader.nextString());
            } else if (mName.equalsIgnoreCase("lon")) {
                lon = Float.parseFloat(reader.nextString());
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
    
        coord.put("lat", lat);
        coord.put("lon", lon);
        return coord;
    }
}
