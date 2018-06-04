package org.sevenlis.owmwidget.classes;

public class City {
    private int id;
    private String name;
    private String country;
    private float lat;
    private float lon;
    
    public City(int id, String name, String country, float lat, float lon) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.lat = lat;
        this.lon = lon;
    }
    
    public City() {
        this(-1,"","",0f,0f);
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCountry() {
        return country;
    }
    
    public float getLat() {
        return lat;
    }
    
    public float getLon() {
        return lon;
    }
    
    public String getNameAndCountry() {
        StringBuilder sb = new StringBuilder();
        if (!getName().isEmpty()) {
            sb.append(getName());
            if (!getCountry().isEmpty()) {
                sb.append(", ").append(getCountry());
            }
        } else if (!getCountry().isEmpty()) {
            sb.append(getCountry());
        }
        return sb.toString();
    }
}
