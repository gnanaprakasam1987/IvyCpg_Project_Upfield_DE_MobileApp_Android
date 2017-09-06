package com.ivy.sd.png.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.OrderSummary.OrderRemarksClickListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class OrderRemarkDialog extends Dialog implements OnClickListener {

	private BusinessModel bmodel;

	private EditText mEdtPO;
	private EditText mEdtRemark;
	private Button mBtnDate,mBtnClose;

	Date date;
	private String mnextDate;
	public String mdate_selected;
	private Context con;
	private OrderRemarksClickListener mRmarkListner;

	public OrderRemarkDialog(Context context,OrderRemarksClickListener rmkListner) {
		super(context);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_order_remarks);

		mEdtPO = (EditText) findViewById(R.id.edt_po);
		mEdtRemark = (EditText) findViewById(R.id.edt_remark);
		mBtnDate = (Button) findViewById(R.id.Btn_deliveryDate);
		mBtnClose  = (Button)findViewById(R.id.closeButton);

		bmodel = (BusinessModel) context.getApplicationContext();
		con = context;
		mRmarkListner = rmkListner;

		getNextDate();
		if (bmodel.isEdit()) {
			mBtnDate.setText(Utils.formatDateAsUserRequired(bmodel
					.getDeliveryDate(bmodel.getRetailerMasterBO()
							.getRetailerID()), "yyyy/MM/dd", "MM/dd/yyyy"));
		} else {
			mBtnDate.setText(mnextDate + "");
		}
		mEdtPO.setText(bmodel.getOrderHeaderBO().getPO());
		mEdtRemark.setText(bmodel.getOrderHeaderBO().getRemark());
		mBtnDate.setOnClickListener(this);
		mBtnClose.setOnClickListener(this);

	}

	private String getNextDate() {
		Calendar origDay = Calendar.getInstance();
		Calendar nextDay = (Calendar) origDay.clone();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		nextDay.add(Calendar.DAY_OF_YEAR, 1);
		return mnextDate = sdf.format(nextDay.getTime()) + "";

	}

	protected Dialog onCreateDialog() {

		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_YEAR, 1);
		int cyear = c.get(Calendar.YEAR);
		int cmonth = c.get(Calendar.MONTH);
		int cday = c.get(Calendar.DAY_OF_MONTH);

		// todayDate = cday + "/" + cmonth + "/" + cyear;
		mnextDate = (cmonth + 1)
				+ "/" + (cday)
				+ "/" + cyear;

		return new DatePickerDialog(con, mDateSetListener, cyear, cmonth, cday);

	}

	private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			mdate_selected = (monthOfYear + 1)
					+ "/"
					+ dayOfMonth
					+ "/" + year;
			mBtnDate.setText(mdate_selected);

			Calendar currentcal = Calendar.getInstance();
			Calendar cal = Calendar.getInstance();
			cal.set(year, monthOfYear, dayOfMonth);

			if (currentcal.after(cal)) {
				Toast.makeText(
						con.getApplicationContext(),
						con.getResources().getString(
								R.string.Please_select_next_day),
						Toast.LENGTH_SHORT).show();
				mBtnDate.setText(mnextDate);
			}
		}
	};

	@Override
	public void onClick(View v) {
		if (v == mBtnDate){
			onCreateDialog().show();
		}else if(v == mBtnClose){
			bmodel.getOrderHeaderBO().setPO(mEdtPO.getText().toString());
			bmodel.getOrderHeaderBO().setRemark(mEdtRemark.getText().toString());
			bmodel.getOrderHeaderBO().setDeliveryDate(Utils.formatDateAsUserRequired(
					mBtnDate.getText().toString(), "MM/dd/yyyy",
					"yyyy/MM/dd"));
//			mRmarkListner.onRemarkClicked();
			bmodel.setOrderHeaderNote(mEdtRemark.getText().toString());
			mEdtPO.setText("");
			mEdtRemark.setText("");
			dismiss();
		}

	}

}
