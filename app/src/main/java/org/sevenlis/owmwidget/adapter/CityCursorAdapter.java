package org.sevenlis.owmwidget.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;

import org.sevenlis.owmwidget.R;
import org.sevenlis.owmwidget.classes.City;
import org.sevenlis.owmwidget.database.DBLocal;

public class CityCursorAdapter extends CursorAdapter implements Filterable {
    private LayoutInflater layoutInflater;
    private DBLocal dbLocal;
    
    public CityCursorAdapter(Context context, Cursor cursor, boolean autoRequery) {
        super(context, cursor, autoRequery);
        layoutInflater = LayoutInflater.class.cast(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        dbLocal = new DBLocal(context);
    }
    
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        View view = layoutInflater.inflate(R.layout.city_search_item,viewGroup,false);
        return fillRowFromCursor(view,cursor);
    }
    
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        fillRowFromCursor(view,cursor);
    }
    
    @Override
    public CharSequence convertToString(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow("name"));
    }
    
    @SuppressLint("InflateParams")
    private View fillRowFromCursor(View view, Cursor cursor) {
        if (view == null)
            view = layoutInflater.inflate(R.layout.city_search_item,null,false);
        TextView textViewCity = view.findViewById(R.id.textViewCity);
        City city = dbLocal.getCity(cursor.getInt(cursor.getColumnIndexOrThrow("id")));
        textViewCity.setText(city.getNameAndCountry());
        return view;
    }
}
