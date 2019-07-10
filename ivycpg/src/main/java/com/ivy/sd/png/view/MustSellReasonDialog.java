package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

public class MustSellReasonDialog extends Dialog {
	BusinessModel bmodel;
	Spinner reason_spnr;
	ArrayAdapter<ReasonMaster> dataAdapter;
	Context context;
	private OnMustSellReasonSelectedListener mustSellReasonSelectedListener;

    public MustSellReasonDialog(Context context, boolean cancelable,
                                OnCancelListener cancelListener, final BusinessModel bmodel) {
        super(context, cancelable, cancelListener);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.must_sell_dialog);
		this.bmodel = bmodel;
		this.bmodel.setContext((Activity) bmodel.getContext());
		this.context = context;
		if (bmodel.configurationMasterHelper.IS_MUST_SELL_REASON) {
			((TextView) findViewById(R.id.must_sell_message_tv))
					.setText(R.string.reason_must_sell);
			reason_spnr = (Spinner) findViewById(R.id.reason_spnr);
			reason_spnr.setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btn_ok)).setText(context
					.getResources().getString(R.string.cancel));
			((Button) findViewById(R.id.btn_continue))
					.setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btn_continue)).setText(context.getResources().getString(R.string.ok));
			dataAdapter = new ArrayAdapter<ReasonMaster>(context,
					android.R.layout.simple_spinner_item);
            dataAdapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_reason)));
            loadMustSellReason();
            dataAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			reason_spnr.setAdapter(dataAdapter);
			((Button) findViewById(R.id.btn_continue))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							Commons.print((ReasonMaster) reason_spnr
									.getSelectedItem()
									+ " "
									+ reason_spnr.getSelectedItemPosition());
							if (!((ReasonMaster) reason_spnr.getSelectedItem()).getReasonID()
									.equals("0")) {
								bmodel.getOrderHeaderBO().setRemark(((ReasonMaster) reason_spnr
										.getSelectedItem()).getReasonID());
								if(mustSellReasonSelectedListener!=null)
									mustSellReasonSelectedListener.onReasonSelected(((ReasonMaster) reason_spnr
											.getSelectedItem()).getReasonID());
								cancel();
							}

						}
					});
		} else if (bmodel.configurationMasterHelper.IS_MUST_SELL_SKIP) {
			((TextView) findViewById(R.id.must_sell_message_tv))
					.setText(R.string.skip_must_sell);
			((Button) findViewById(R.id.btn_continue))
					.setVisibility(View.VISIBLE);
			((Button) findViewById(R.id.btn_continue))
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							cancel();
						}
					});
		}

		((Button) findViewById(R.id.btn_ok))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
					}
				});
	}

	private void loadMustSellReason() {
		try {
			ReasonMaster reason;
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.openDataBase();
			Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(StandardListMasterConstants.MUSTSELL_REASON_TYPE));
			if (c != null) {
				while (c.moveToNext()) {
					reason = new ReasonMaster();
					reason.setReasonID(c.getString(0));
					reason.setReasonDesc(c.getString(1));
					dataAdapter.add(reason);
				}
				c.close();
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}


	public OnMustSellReasonSelectedListener getMustSellReasonSelectedListener() {
		return mustSellReasonSelectedListener;
	}

	public void setMustSellReasonSelectedListener(OnMustSellReasonSelectedListener mustSellReasonSelectedListener) {
		this.mustSellReasonSelectedListener = mustSellReasonSelectedListener;
	}

	interface OnMustSellReasonSelectedListener{
    	void onReasonSelected(String reasonId);
	}

}
