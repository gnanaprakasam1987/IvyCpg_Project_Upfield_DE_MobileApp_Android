package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

/**
 * Created by subramanian.r on 23-03-2016.
 */
class CustomKeyBoard extends Dialog implements View.OnClickListener {
    private Context mContext;
    private TextView tv_value;
    private Button decimal_point;
    private String value = "0";

    private EditText mSelectedView;

    private boolean isDialogCreated;
    private int limit =6; // default 6 charteres can entered

    //user for quantity values
    public CustomKeyBoard(Context context, EditText tv) {
        super(context);
        this.mContext=context;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_keyboard);

        initializeViews(tv);
    }

    //used for money values
    public CustomKeyBoard(Context context, EditText tv, boolean isDecimalAllowed,int limit) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.custom_keyboard);

        initializeViews(tv);

        if (isDecimalAllowed)
            decimal_point.setVisibility(View.VISIBLE);

        this.limit = limit;
    }

    public boolean isDialogCreated() {
        return isDialogCreated;
    }

    private void initializeViews(EditText tv) {
        isDialogCreated = true;

        mSelectedView = tv;
        if (tv.getText().toString().length() > 0 && !"0.0".equals(tv.getText().toString()))
            this.value = tv.getText().toString();

        tv_value = (TextView) findViewById(R.id.typed_value);
        tv_value.setText(value);

        Button number_one = (Button) findViewById(R.id.num_one);
        Button number_two = (Button) findViewById(R.id.num_two);
        Button number_three = (Button) findViewById(R.id.num_three);
        Button number_four = (Button) findViewById(R.id.num_four);
        Button number_five = (Button) findViewById(R.id.num_five);
        Button number_six = (Button) findViewById(R.id.num_six);
        Button number_seven = (Button) findViewById(R.id.num_seven);
        Button number_eight = (Button) findViewById(R.id.num_eight);
        Button number_nine = (Button) findViewById(R.id.num_nine);
        Button number_zero = (Button) findViewById(R.id.num_zero);
        decimal_point = (Button) findViewById(R.id.dec_dot);

        Button btn_cancel = (Button) findViewById(R.id.cancel);
        Button btn_ok = (Button) findViewById(R.id.ok);
        ImageButton btn_delete = (ImageButton) findViewById(R.id.delete);

        number_one.setOnClickListener(this);
        number_two.setOnClickListener(this);
        number_three.setOnClickListener(this);
        number_four.setOnClickListener(this);
        number_five.setOnClickListener(this);
        number_six.setOnClickListener(this);
        number_seven.setOnClickListener(this);
        number_eight.setOnClickListener(this);
        number_nine.setOnClickListener(this);
        number_zero.setOnClickListener(this);
        decimal_point.setOnClickListener(this);

        btn_cancel.setOnClickListener(this);
        btn_ok.setOnClickListener(this);
        btn_delete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.ok) {
            //update value
            if (value.endsWith("."))
                value = value.substring(0, value.length() - 1);
            mSelectedView.setText(value);

            dismiss();
            updateOrderTotalValue();
            isDialogCreated = false;
        } else if (id == R.id.cancel) {
            dismiss();
            isDialogCreated = false;
        } else if (id == R.id.delete) {
            value = value.substring(0, value.length() - 1);
            if ("".equals(value))
                value = "0";
            tv_value.setText(value);
        } else {
            addValue(v.getTag().toString());
        }
    }

    private void addValue(String typedValue) {
        if (value.length() < limit) {
            if (".".equals(typedValue) && !value.contains(".") && value.length() < limit-1) {
                if ("".equals(value))
                    value = "0.";
                else
                    value = value + typedValue;
            } else if (!".".equals(typedValue)) {
                if ("0".equals(value)) {
                    value = typedValue;
                } else {
                    value = value + typedValue;
                }
            }

            tv_value.setText(value);
        }
    }

    private void updateOrderTotalValue(){
        if(mContext instanceof StockAndOrder){
            StockAndOrder act = (StockAndOrder)mContext;
            act.updateValue();
        }
    }
}
