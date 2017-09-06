package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.sd.png.bo.LevelBO;

import java.util.Vector;

/**
 * Created by dharmapriya.k on 10/17/2016,9:55 AM.
 */
public class Spinadapter extends ArrayAdapter<LevelBO> {

    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private Vector<LevelBO> values;

    public Spinadapter(Context context, int textViewResourceId,
                       Vector<LevelBO> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount() {
        return values.size();
    }

    public LevelBO getItem(int position) {
        return values.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    // And the "magic" goes here
    // This is for the "passive" state of the spinner
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // I created a dynamic TextView here, but you can reference your own  custom layout for each spinner item
        TextView label = new TextView(context);
        label.setTextSize(12);
        label.setPadding(2, 2, 2, 2);
        label.setTextColor(Color.WHITE);
        // Then you can get the current item using the values array (Users array) and the current position
        // You can NOW reference each method you has created in your bean object (User class)
        label.setText(values.get(position).getLevelName());

        // And finally return your dynamic (or custom) view for each spinner item
        return label;
    }

    // And here is when the "chooser" is popped up
    // Normally is the same view, but you can customize it if you want
    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setTextColor(Color.BLACK);
        label.setTextSize(14);
        label.setPadding(8, 8, 8, 8);
        label.setText(values.get(position).getLevelName());

        return label;
    }
}
