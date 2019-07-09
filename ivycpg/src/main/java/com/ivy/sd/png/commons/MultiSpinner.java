package com.ivy.sd.png.commons;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import androidx.appcompat.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanifa.m on 9/21/2016.
 */
public class MultiSpinner extends AppCompatSpinner implements DialogInterface.OnCancelListener {
    public static final int DEFAULT_ARROW_WIDTH_DP = 12;


    private List<KeyPairBoolData> items;
    private String defaultText = "";
    private String spinnerTitle = "";
    private SpinnerListener listener;
    MyAdapter adapter;
    public static AlertDialog.Builder builder;
    public static AlertDialog ad;
    private boolean mOpened;

    //AttributeSet
    private int btnColor;

    /*
    * **********************************************************************************
    * CONSTRUCTORS
    * **********************************************************************************
    */

    public MultiSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public MultiSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public MultiSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    /*
    * **********************************************************************************
    * INITIALISATION METHODS
    * **********************************************************************************
    */

    private void init(Context context, AttributeSet attrs) {

        initAttributes(context, attrs);

    }

    private void initAttributes(Context context, AttributeSet attrs) {

        TypedArray defaultArray = context.obtainStyledAttributes(new int[]{R.attr.colorControlNormal, R.attr.colorAccent});
        int defaultBaseColor = defaultArray.getColor(0, 0);
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner);
        btnColor = array.getColor(R.styleable.MyTextView_textColorPrimary, defaultBaseColor);


    }


    @Override
    public void setSelection(final int position) {
        this.post(new Runnable() {
            @Override
            public void run() {
                MultiSpinner.super.setSelection(position);
            }
        });
    }

    @Override
    public int getSelectedItemPosition() {
        return super.getSelectedItemPosition();
    }


    /*
     * **********************************************************************************
     * LISTENER METHODS
     * **********************************************************************************
    */

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    break;
            }
            invalidate();
        }
        return super.onTouchEvent(event);
    }


    /*********************************************************
     * MULTI CHOICE SPINNER DIALOG USER INTERFACE
     *********************************************************/


    public List<KeyPairBoolData> getSelectedItems() {
        List<KeyPairBoolData> selectedItems = new ArrayList<>();
        for (KeyPairBoolData item : items) {
            if (item.isSelected()) {
                if (item.getId() != 0)
                    selectedItems.add(item);
            }
        }
        return selectedItems;
    }

    public List<Integer> getSelectedIds() {
        List<Integer> selectedItemsIds = new ArrayList<>();
        for (KeyPairBoolData item : items) {
            if (item.isSelected()) {
                selectedItemsIds.add(item.getId());
            }
        }
        return selectedItemsIds;
    }


    @Override
    public void onCancel(DialogInterface dialog) {

        StringBuilder spinnerBuffer = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                if (items.get(i).getId() == 0) {
                    spinnerBuffer.append(items.get(i).getName());
                    spinnerBuffer.append(", ");
                    break;
                } else {
                    spinnerBuffer.append(items.get(i).getName());
                    spinnerBuffer.append(", ");
                }

            }
        }

        String spinnerText = spinnerBuffer.toString();
        if (spinnerText.length() > 2)
            spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
        else
            spinnerText = defaultText;

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[]{spinnerText});
        setAdapter(adapterSpinner);

        if (adapter != null)
            adapter.notifyDataSetChanged();

        listener.onItemsSelected(getSelectedItems());
        mOpened = false;

    }

    @Override
    public boolean performClick() {

        builder = new AlertDialog.Builder(getContext());
        builder.setTitle(spinnerTitle);

        final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        final View view = inflater.inflate(R.layout.alert_dialog_listview_search, null);
        builder.setView(view);

        final ListView listView = (ListView) view.findViewById(R.id.alertSearchListView);
        Button btnOk = (Button) view.findViewById(R.id.btnOk);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setFastScrollEnabled(false);
        adapter = new MyAdapter(getContext(), items);
        listView.setAdapter(adapter);
        btnOk.setVisibility(GONE);
//        final TextView emptyText = (TextView) view.findViewById(R.id.empty);
//        listView.setEmptyView(emptyText);

//        final EditText editText = (EditText) view.findViewById(R.id.alertSearchEditText);
//        editText.addTextChangedListener(new TextWatcher() {
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                adapter.getFilter().filter(s.toString());
//            }
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//            }
//        });


        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                dialog.cancel();

//
            }
        });

        builder.setOnCancelListener(this);
        if (!mOpened) {
            ad = builder.create();
            ad = builder.show();
            mOpened = true;
        }
        Button buttonbackground1 = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonbackground1.setBackgroundColor(btnColor);


        return true;
    }

    public void setItems(List<KeyPairBoolData> items, int position, SpinnerListener listener) {

        this.items = items;
        this.listener = listener;

        StringBuilder spinnerBuffer = new StringBuilder();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isSelected()) {
                if (items.get(i).getId() == 0) {
                    spinnerBuffer.append(items.get(i).getName());
                    spinnerBuffer.append(", ");
                    break;
                } else {
                    spinnerBuffer.append(items.get(i).getName());
                    spinnerBuffer.append(", ");
                }

            }
        }
        if (spinnerBuffer.length() > 2)
            defaultText = spinnerBuffer.toString().substring(0, spinnerBuffer.toString().length() - 2);

        //ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[]{defaultText});
        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(getContext(), R.layout.textview_for_spinner, new String[]{defaultText});
        setAdapter(adapterSpinner);

        if (position != -1) {
            items.get(position).setSelected(true);
            //listener.onItemsSelected(items);
            onCancel(null);
        }
    }


    public class MyAdapter extends BaseAdapter {

        List<KeyPairBoolData> arrayList;
        List<KeyPairBoolData> mOriginalValues; // Original Values
        LayoutInflater inflater;

        public MyAdapter(Context context, List<KeyPairBoolData> arrayList) {
            this.arrayList = arrayList;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.item_listview_multiple, parent, false);
                holder.textView = (TextView) convertView.findViewById(R.id.alertTextView);
                holder.checkBox = (CheckBox) convertView.findViewById(R.id.alertCheckbox);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            final KeyPairBoolData data = arrayList.get(position);

            holder.textView.setText(data.getName());
            holder.textView.setTypeface(null, Typeface.NORMAL);
            holder.checkBox.setChecked(data.isSelected());

            convertView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {

                    if (data.isSelected()) { // unselect
                        if (data.getId() == 0) {
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).getId() != 0)
                                    items.get(i).setSelected(false);
                            }
                            notifyDataSetChanged();
                        } else if (data.getId() != 0) {
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).getId() == 0 && items.get(i).isSelected()) {
                                    items.get(i).setSelected(false);
                                    break;
                                }
                            }
                            notifyDataSetChanged();
                        }

                    } else { // selected
                        if (data.getId() == 0) {
                            for (int i = 0; i < items.size(); i++) {
                                if (items.get(i).getId() != 0)
                                    items.get(i).setSelected(true);
                            }
                            notifyDataSetChanged();
                        }
                    }
                    final ViewHolder temp = (ViewHolder) v.getTag();
                    temp.checkBox.setChecked(!temp.checkBox.isChecked());

                    data.setSelected(!data.isSelected());


                }
            });
            holder.checkBox.setTag(holder);

            return convertView;
        }

    }

}
