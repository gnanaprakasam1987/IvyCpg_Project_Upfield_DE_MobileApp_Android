package com.ivy.sd.png.view.van;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ivy.sd.png.survey.AnswerBO;

import java.util.ArrayList;


/**
 * Created by vinodh.r on 3/15/2017.
 */

public class SurveySpinner extends ArrayAdapter<AnswerBO> {
    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private ArrayList<AnswerBO> values;
    private int textViewResourceId;

    public SurveySpinner(Context context, int textViewResourceId,
                         ArrayList<AnswerBO> values) {
        super(context, textViewResourceId, values);
        this.context = context;
        this.values = values;
    }

    public int getCount() {
        return values.size();
    }

    public AnswerBO getItem(int position) {
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
        label.setText(values.get(position).getAnswer());

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
        label.setText(values.get(position).getAnswer());

        return label;
    }

}
