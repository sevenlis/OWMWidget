package org.sevenlis.owmwidget.adapter;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import org.sevenlis.owmwidget.activity.MainActivity;
import org.sevenlis.owmwidget.classes.Forecast;
import org.sevenlis.owmwidget.fragment.ForecastFragment;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivityPagerAdapter extends FragmentStatePagerAdapter {
    private List<Date> datesList;
    
    public MainActivityPagerAdapter(FragmentManager fragmentManager, List<Date> datesList) {
        super(fragmentManager);
        this.datesList = datesList;
    }
    
    @Override
    public Fragment getItem(int position) {
        ForecastFragment fragment = new ForecastFragment();
        Bundle args = new Bundle();
        args.putLong("DATE_START_MILLIS",datesList.get(position).getTime());
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public int getCount() {
        return this.datesList.size();
    }
    
    @Override
    public CharSequence getPageTitle(int position) {
        DateFormat dateFormat = new SimpleDateFormat("E. dd.MM.yyyy", Locale.getDefault());
        return dateFormat.format(datesList.get(position));
    }
}
