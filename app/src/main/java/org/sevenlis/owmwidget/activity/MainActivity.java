package org.sevenlis.owmwidget.activity;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import org.sevenlis.owmwidget.R;
import org.sevenlis.owmwidget.adapter.MainActivityPagerAdapter;
import org.sevenlis.owmwidget.classes.City;
import org.sevenlis.owmwidget.classes.Forecast;
import org.sevenlis.owmwidget.database.DBLocal;
import org.sevenlis.owmwidget.parser.ForecastJSONParser;
import org.sevenlis.owmwidget.util.Const;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final List<Forecast> forecastList = new ArrayList<>();
    final List<Date> datesList = new ArrayList<>();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    
        DBLocal dbLocal = new DBLocal(this);
        final Calendar cal = Calendar.getInstance();
    
        TextView textViewCity = findViewById(R.id.textView_city);
        ViewPager viewPager = findViewById(R.id.viewPager);
        final PagerTabStrip pagerTabStrip = findViewById(R.id.pagerTabStrip);
        
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            City city = dbLocal.getCity(extras.getInt(Const.CITY_ID_KEY));
    
            ForecastJSONParser parser = new ForecastJSONParser();
            try {
                InputStream inputStream = new FileInputStream(Const.getForecastFile(this, city));
                forecastList.addAll(parser.readJsonStream(inputStream));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
    
            textViewCity.setText(city.getNameAndCountry());
        } else {
            textViewCity.setVisibility(View.GONE);
            viewPager.setVisibility(View.GONE);
            pagerTabStrip.setVisibility(View.GONE);
        }
    
        for (Forecast forecast : forecastList) {
            Date dateStart = forecast.getDateStart();
            if (!datesList.contains(dateStart)) {
                datesList.add(dateStart);
            }
        }
        
        MainActivityPagerAdapter mainActivityPagerAdapter = new MainActivityPagerAdapter(getSupportFragmentManager(), datesList);
        viewPager.setAdapter(mainActivityPagerAdapter);
    
        pagerTabStrip.setDrawFullUnderline(true);
        pagerTabStrip.setTextColor(getDateColor(cal));
        pagerTabStrip.setTabIndicatorColor(getDateColor(cal));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
    
            @Override
            public void onPageSelected(int position) {
                cal.setTime(datesList.get(position));
                pagerTabStrip.setTextColor(getDateColor(cal));
                pagerTabStrip.setTabIndicatorColor(getDateColor(cal));
            }
    
            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        
    }
    
    private int getDateColor(Calendar dateCalendar) {
        int dayOfWeek = dateCalendar.get(Calendar.DAY_OF_WEEK);
        if (dayOfWeek == 1 || dayOfWeek == 7) {
            return ContextCompat.getColor(this, R.color.colorRed);
        } else {
            return ContextCompat.getColor(this, R.color.colorDarkGrey);
        }
    }
    
    public Forecast getForecast(int i) {
        return forecastList.get(i);
    }
    
}
