package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

/**
 * Created by mayuri.v on 4/13/2017.
 */
public class ThemedListPreference extends ListPreference  implements AdapterView.OnItemClickListener {

    public static final String TAG = "ThemedListPreference";

    private int mClickedDialogEntryIndex;

    private CharSequence mDialogTitle;
    private BusinessModel bmodel;

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

        mDialogTitle = getDialogTitle();
        if (mDialogTitle == null) mDialogTitle = getTitle();
        TextView mTitleView = (TextView) view.findViewById(R.id.dialog_title);
        mTitleView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mTitleView.setText(mDialogTitle);
        Button mDoneBTN = (Button) view.findViewById(R.id.ok_btn);
        mDoneBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mDoneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mClickedDialogEntryIndex = findIndexOfValue(getValue());
                ThemedListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_POSITIVE);
                getDialog().dismiss();
            }
        });
        Button mCancelBTN = (Button) view.findViewById(R.id.cancel_btn);
        mCancelBTN.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mCancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mClickedDialogEntryIndex = findIndexOfValue(getValue());
                ThemedListPreference.this.onClick(getDialog(), DialogInterface.BUTTON_NEGATIVE);
                getDialog().dismiss();
            }
        });
        GridView list = (GridView) view.findViewById(android.R.id.list);
        // note the layout we're providing for the ListView entries
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(
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
//        int pos = findIndexOfValue(getValue());
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
        bmodel = (BusinessModel) getContext().getApplicationContext();
        TextView titleView = (TextView) view.findViewById(android.R.id.title);
        titleView.setTextColor(ContextCompat.getColor(getContext(),R.color.white));
        titleView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        TextView summaryView = (TextView) view.findViewById(android.R.id.summary);
        summaryView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        summaryView.setTextColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
    }
}
