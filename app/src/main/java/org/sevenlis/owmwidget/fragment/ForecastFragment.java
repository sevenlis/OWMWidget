package org.sevenlis.owmwidget.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.sevenlis.owmwidget.R;
import org.sevenlis.owmwidget.activity.MainActivity;
import org.sevenlis.owmwidget.classes.Forecast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ForecastFragment extends Fragment {
    
    public ForecastFragment() {}
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);
        ListView forecastListView = view.findViewById(R.id.forecastListView);
    
        List<Forecast> forecastList = new ArrayList<>();
        
        Bundle args = getArguments();
        if (args != null) {
            Date dateStart = new Date(args.getLong("DATE_START_MILLIS"));
            for (Forecast forecast : MainActivity.forecastList) {
                if (forecast.getDateStart().equals(dateStart)) {
                    forecastList.add(forecast);
                }
            }
        }
        
        ForecastListViewAdapter adapter = new ForecastListViewAdapter(forecastList);
        forecastListView.setAdapter(adapter);
        
        return view;
    }
    
    public void setFieldTexts(View view, Forecast forecast) {
        ImageView imageIcon = view.findViewById(R.id.image_icon);
        TextView textDateTime = view.findViewById(R.id.text_date_time);
        TextView textWeatherDescr = view.findViewById(R.id.text_weather_descr);
        TextView textCurTemp = view.findViewById(R.id.text_cur_temp);
        TextView textTempMinMax = view.findViewById(R.id.text_temp_min_max);
        TextView textPrecip = view.findViewById(R.id.text_precipitation);
        TextView textClouds = view.findViewById(R.id.text_clouds);
        TextView textPressure = view.findViewById(R.id.text_pressure);
        TextView textHumidity = view.findViewById(R.id.text_humidity);
        TextView textWind = view.findViewById(R.id.text_wind);
        
        Picasso.with(getContext()).load(forecast.getImageUrl()).into(imageIcon);
        textDateTime.setText(forecast.getDateTimeForView());
        textWeatherDescr.setText(forecast.getWeatherDescription());
        textCurTemp.setText(forecast.getTemperatureForView(getContext()));
        textTempMinMax.setText(forecast.getTemperatureMinMaxForView(getContext()));
        textPrecip.setText(forecast.getPrecipitationForView());
        textClouds.setText(forecast.getCloudsForView());
        textPressure.setText(forecast.getPressureForView());
        textHumidity.setText(forecast.getHumidityForView());
        textWind.setText(forecast.getWindForView());
    }
    
    class ForecastListViewAdapter extends BaseAdapter {
        List<Forecast> forecastList;
    
        ForecastListViewAdapter(List<Forecast> forecastList) {
            this.forecastList = forecastList;
        }
    
        @Override
        public int getCount() {
            return forecastList.size();
        }
    
        @Override
        public Object getItem(int i) {
            return forecastList.get(i);
        }
    
        @Override
        public long getItemId(int i) {
            return i;
        }
    
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = getLayoutInflater().inflate(R.layout.list_item_forecast,viewGroup,false);
            }
            setFieldTexts(view,forecastList.get(i));
            return view;
        }
    }
}
