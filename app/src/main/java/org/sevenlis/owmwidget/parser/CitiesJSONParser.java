package org.sevenlis.owmwidget.parser;

import android.content.ContentValues;
import android.util.JsonReader;

import org.sevenlis.owmwidget.classes.City;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CitiesJSONParser {
    
    public List<City> readJsonStream(InputStream in) throws IOException {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try {
            return readCitiesArray(reader);
        } finally {
            reader.close();
        }
    }
    
    private List<City> readCitiesArray(JsonReader reader) throws IOException {
        List<City> cities = new ArrayList<>();
        reader.beginArray();
        while (reader.hasNext()) {
            cities.add(readCity(reader));
        }
        reader.endArray();
        return cities;
    }
    
    private City readCity(JsonReader reader) throws IOException {
        int id = -1;
        String name = "";
        String country = "";
        ContentValues coord = new ContentValues();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            if (sName.equalsIgnoreCase("id")) {
                id = reader.nextInt();
            } else if (sName.equalsIgnoreCase("name")) {
                name = reader.nextString();
            } else if (sName.equalsIgnoreCase("country")) {
                country = reader.nextString();
            } else if (sName.equalsIgnoreCase("coord")) {
                coord = readCoord(reader);
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        
        return new City(id,name,country,coord.getAsFloat("lat"),coord.getAsFloat("lon"));
    }
    
    private ContentValues readCoord(JsonReader reader) throws IOException {
        float lat = 0f, lon = 0f;
        ContentValues coord = new ContentValues();
        
        reader.beginObject();
        while (reader.hasNext()) {
            String sName = reader.nextName();
            if (sName.equalsIgnoreCase("lat")) {
                lat = Float.parseFloat(reader.nextString());
            } else if (sName.equalsIgnoreCase("lon")) {
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
