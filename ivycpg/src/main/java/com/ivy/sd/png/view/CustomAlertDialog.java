package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

public class CustomAlertDialog extends Dialog implements
		android.view.View.OnClickListener {
	public Activity c;
	public Dialog d;
	public Button yes, cancel, deletestockandorder;
	private BusinessModel bmodel;
	//private ProgressDialog pd;
	private AlertDialog.Builder builder;
	private AlertDialog alertDialog;
	public CustomAlertDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		this.c = (Activity) context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(com.ivy.sd.png.asean.view.R.layout.custom_dialog);

		bmodel = (BusinessModel) c.getApplicationContext();
		bmodel.setContext(c);
		yes = (Button) findViewById(com.ivy.sd.png.asean.view.R.id.btn_yes);
		cancel = (Button) findViewById(com.ivy.sd.png.asean.view.R.id.btn_no);
		deletestockandorder = (Button) findViewById(com.ivy.sd.png.asean.view.R.id.btn_delstockandorder);
		yes.setOnClickListener(this);
		cancel.setOnClickListener(this);
		deletestockandorder.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		int i = v.getId();
		if (i == R.id.btn_no) {
			dismiss();

		} else if (i == R.id.btn_yes) {
			try {
				builder = new AlertDialog.Builder(c);

				bmodel.customProgressDialog(alertDialog, builder, c, c.getResources()
						.getString(
								R.string.deleting_order));
				alertDialog = builder.create();
				alertDialog.show();
				/*pd = ProgressDialog
						.show(c,
								DataMembers.SD,
								c.getResources()
										.getString(
												com.ivy.sd.png.asean.view.R.string.deleting_order),
								true, false);*/

				new MyThread(c, DataMembers.DELETE_ORDER).start();
				//	pd.dismiss();
				alertDialog.dismiss();
				bmodel = (BusinessModel) c.getApplicationContext();
				bmodel.showAlert(
						c.getResources().getString(
								R.string.order_deleted_sucessfully)
								+ bmodel.getOrderid(),
						DataMembers.NOTIFY_ORDER_SAVED);
			} catch (Exception e) {
				Commons.printException(e);
			}

		} else if (i == R.id.btn_delstockandorder) {
			try {
				/*pd = ProgressDialog.show(c, DataMembers.SD, c.getResources()
						.getString(R.string.deleting_order), true, false);
*/
				builder = new AlertDialog.Builder(c);

				bmodel.customProgressDialog(alertDialog, builder, c, c.getResources().getString(R.string.deleting_order));
				alertDialog = builder.create();
				alertDialog.show();

				new deleteStockAndOrder().execute();
				new MyThread(c, DataMembers.DELETE_ORDER).start();
				//pd.dismiss();
				alertDialog.dismiss();
				bmodel = (BusinessModel) c.getApplicationContext();
				bmodel.showAlert(
						c.getResources().getString(
								R.string.order_deleted_sucessfully)
								+ bmodel.getOrderid(),
						DataMembers.NOTIFY_ORDER_SAVED);
			} catch (Exception e) {
				Commons.printException(e);
			}

		}
	}

	class deleteStockAndOrder extends AsyncTask<Integer, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Integer... params) {
			deleteStockAndOrder();
			return null;
		}

	}

	public void deleteStockAndOrder() {
		try {
			DBUtil db = new DBUtil(c.getApplicationContext(),
					DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.createDataBase();
			db.openDataBase();

			String id;
			Cursor closingStockCursor = db
					.selectSQL("select StockID from ClosingStockHeader where RetailerID="
							+ bmodel.getRetailerMasterBO().getRetailerID() + "");
			if (closingStockCursor.getCount() > 0) {
				closingStockCursor.moveToNext();
				id = bmodel.QT(closingStockCursor.getString(0));
				db.deleteSQL("ClosingStockHeader", "StockID=" + id
						+ " and upload='N'", false);
				db.deleteSQL("ClosingStockDetail", "StockID=" + id
						+ " and upload='N'", false);
			}
			closingStockCursor.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

}
