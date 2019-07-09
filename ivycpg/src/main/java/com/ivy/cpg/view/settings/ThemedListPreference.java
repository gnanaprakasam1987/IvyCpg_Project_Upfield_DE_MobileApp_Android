package com.ivy.cpg.view.settings;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

/**
 * Created by mayuri.v on 4/13/2017.
 */
public class ThemedListPreference extends ListPreference  implements AdapterView.OnItemClickListener {

    public static final String TAG = "ThemedListPreference";

    private int mClickedDialogEntryIndex;

    public ThemedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ThemedListPreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        // inflate custom layout with custom title & listview
        View view = View.inflate(getContext(), R.layout.dialog_settings, null);

        CharSequence mDialogTitle = getDialogTitle();
        if (mDialogTitle == null) mDialogTitle = getTitle();
        TextView mTitleView = view.findViewById(R.id.dialog_title);
        mTitleView.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        mTitleView.setText(mDialogTitle);
        Button mDoneBTN = view.findViewById(R.id.ok_btn);
        mDoneBTN.setTypeface(FontUtils.getFontBalooHai(getContext(), FontUtils.FontType.REGULAR));
        mDoneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mClickedDialogEntryIndex = findIndexOfValue(getOutletData());
                ThemedListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                getDialog().dismiss();
            }
        });
        Button mCancelBTN = view.findViewById(R.id.cancel_btn);
        mCancelBTN.setTypeface(FontUtils.getFontBalooHai(getContext(),FontUtils.FontType.REGULAR));
        mCancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mClickedDialogEntryIndex = findIndexOfValue(getOutletData());
                ThemedListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                getDialog().dismiss();
            }
        });
        GridView list = view.findViewById(android.R.id.list);
        // note the layout we're providing for the ListView entries
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                getContext(), R.layout.btn_radio,
                getEntries());
        if (getEntries().length > 1) {
            list.setNumColumns(2);
        } else {
            list.setNumColumns(1);
        }
        list.setAdapter(adapter);
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if(findIndexOfValue(getValue())>=0) {
            list.setItemChecked(findIndexOfValue(getValue()), true);
        }
         list.setOnItemClickListener(this);

        return view;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // adapted from ListPreference
        if (getEntries() == null || getEntryValues() == null) {
            // throws exception
            super.onPrepareDialogBuilder(builder);
            return;
        }


        mClickedDialogEntryIndex = findIndexOfValue(getValue());

        // .setTitle(null) to prevent default (blue)
        // title+divider from showing up
        builder.setTitle(null);


        builder.setPositiveButton(null, null);
        builder.setNegativeButton(null, null);

        //onDialogClosed(true);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        mClickedDialogEntryIndex = position;
//        ThemedListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_NEUTRAL);
        //getDialog().dismiss();
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        // adapted from ListPreference
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0
                && getEntryValues() != null) {
            System.out.println("getSummary==");
            setSummary(getSummary());
            String value = getEntryValues()[mClickedDialogEntryIndex]
                    .toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    public CharSequence getSummary() {
//        int pos = findIndexOfValue(getOutletData());
//        return getEntries()[pos];

        int pos = findIndexOfValue(getValue());
        System.out.println("pos==2=="+pos);
        try {
            if (pos >= 0)
                return getEntries()[pos];
            else
                return super.getSummary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return super.getSummary();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        //To set ripple effect for the list item
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
        view.setBackgroundResource(outValue.resourceId);

        TextView titleView = view.findViewById(android.R.id.title);
        titleView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        titleView.setTypeface(FontUtils.getFontRoboto(getContext(), FontUtils.FontType.LIGHT));
        TextView summaryView = view.findViewById(android.R.id.summary);
        summaryView.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        summaryView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }
}
