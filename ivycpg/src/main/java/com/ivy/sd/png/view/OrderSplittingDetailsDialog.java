/**
 * 
 */
package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderSplittingDetailsBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * @author sivakumar.j
 * 
 */
public class OrderSplittingDetailsDialog extends Dialog {

	private BusinessModel bmodel;
	private Context context;
	private Button saveButton = null;
	private TextView poTextView = null;
	private ListView listView = null;
	private TextView deliveryDateTextView;

	private EditText remarksTextView;;

	RelativeLayout rl;
	MenuBaseAdapter adapter = null;

	EditText poNumberEditText = null;
	EditText remarkEditText = null;
	DatePicker datePicker = null;
	// TextView showDatePickerTextView=null;
	ImageView showDatePickerTextView = null;

	ImageView datePickerImageView = null;
	private int green_color;

	public interface SaveClickListener {
		public void saveProcess();

		public void showProgressDialog(String message);

		public void dismissProgressDialog();

		public void resettingListViews();

		public void showDatePickerDialogForSelectDate();

		public void dismissDatePickerDialogForSelectDate();

	}

	private SaveClickListener saveListener = null;
	public View convertView;

	public void setSaveClickInterface(SaveClickListener listener) {
		this.saveListener = listener;

		saveButton.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				bmodel.orderSplitHelper.isOrderSplitDialogExecuted = true;
				if (bmodel.orderSplitHelper.isAnyOrderTickedForSplit()) {
					if (checkForDateAndPoNumber()) {
						new MovingRightToLeftSideLoader().execute();
					}
				} else {
					new MovingRightToLeftSideLoader().execute();
				}
			}

		});

		this.showDatePickerTextView
				.setOnClickListener(new android.view.View.OnClickListener() {
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub

						Commons.print(DataMembers.SD+ ",date picker clicked");
						bmodel.setSelectedDateFromDatePickerDialog(null);
						saveListener.showDatePickerDialogForSelectDate();
					}

				});
	}

	public OrderSplittingDetailsDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		this.context = context;
		bmodel = (BusinessModel) context.getApplicationContext();

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setCancelable(true);

		rl = (RelativeLayout) LayoutInflater.from(context).inflate(
				R.layout.dialog_order_splitting_details_new, null);
		setContentView(rl);

		saveButton = (Button) this.findViewById(R.id.saveBTN);

		poTextView = (TextView) this.findViewById(R.id.po_number_text_view);
		Commons.print(DataMembers.SD+ ",po = "
				+ bmodel.orderSplitHelper
						.getCurrentlySelectedOrderSplittingMasterBOForEdit()
						.getPo());
		poTextView.setText(bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit().getPo());

		deliveryDateTextView = (TextView) this
				.findViewById(R.id.delivery_date_text_view);

		/*
		 * if(bmodel.getSelectedDateFromDatePickerDialog()==null)
		 * deliveryDateTextView.setText(""); else
		 * deliveryDateTextView.setText(bmodel
		 * .getSelectedDateFromDatePickerDialog());
		 */
		deliveryDateTextView.setText(this.getCurrentDate());

		remarksTextView = (EditText) this.findViewById(R.id.remarks_text_view);
		remarksTextView.setText(bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.getRemarks());

		this.poNumberEditText = (EditText) this
				.findViewById(R.id.po_number_text_view);
		this.datePicker = (DatePicker) this.findViewById(R.id.date_picker);

		// this.showDatePickerTextView=(TextView)this.findViewById(R.id.show_date_picker);
		this.showDatePickerTextView = (ImageView) this
				.findViewById(R.id.show_date_picker_new);

		listView = (ListView) this.findViewById(R.id.listView1);
		// bmodel.orderSplitHelper.getCurrentlySelectedOrderSplittingMasterBOForEdit().resetBooleanInOrderSplittingDetailsBOList();
		adapter = new MenuBaseAdapter(bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.getOrderSplittingDetailsBOList());
		listView.setAdapter(adapter);
	}

	public OrderSplittingDetailsDialog(int green_color, Context context) {
		super(context);
		// TODO Auto-generated constructor stub

		this.context = context;
		bmodel = (BusinessModel) context.getApplicationContext();

		this.green_color = green_color;

		green_color = this.context.getResources().getColor(
				R.color.order_split_green_color);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setCancelable(true);

		rl = (RelativeLayout) LayoutInflater.from(context).inflate(
				R.layout.dialog_order_splitting_details_new, null);
		setContentView(rl);

		saveButton = (Button) this.findViewById(R.id.saveBTN);

		poTextView = (TextView) this.findViewById(R.id.po_number_text_view);
		Commons.print(DataMembers.SD+ ",po = "
				+ bmodel.orderSplitHelper
						.getCurrentlySelectedOrderSplittingMasterBOForEdit()
						.getPo());
		poTextView.setText(bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit().getPo());

		deliveryDateTextView = (TextView) this
				.findViewById(R.id.delivery_date_text_view);

		/*
		 * if(bmodel.getSelectedDateFromDatePickerDialog()==null)
		 * deliveryDateTextView.setText(""); else
		 * deliveryDateTextView.setText(bmodel
		 * .getSelectedDateFromDatePickerDialog());
		 */
		if (bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.getDeliveryDate() == null)
			deliveryDateTextView.setText(this.getCurrentDate());
		else
			deliveryDateTextView.setText(bmodel.orderSplitHelper
					.getCurrentlySelectedOrderSplittingMasterBOForEdit()
					.getDeliveryDate());

		remarksTextView = (EditText) this.findViewById(R.id.remarks_text_view);
		remarksTextView.setText(bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.getRemarks());

		this.poNumberEditText = (EditText) this
				.findViewById(R.id.po_number_text_view);
		this.datePicker = (DatePicker) this.findViewById(R.id.date_picker);

		// this.showDatePickerTextView=(TextView)this.findViewById(R.id.show_date_picker);
		this.showDatePickerTextView = (ImageView) this
				.findViewById(R.id.show_date_picker_new);

		listView = (ListView) this.findViewById(R.id.listView1);
		// bmodel.orderSplitHelper.getCurrentlySelectedOrderSplittingMasterBOForEdit().resetBooleanInOrderSplittingDetailsBOList();
		adapter = new MenuBaseAdapter(bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.getOrderSplittingDetailsBOList());
		listView.setAdapter(adapter);
	}

	public void onStop() {
		this.adapter = null;
		saveButton = null;
		listView = null;
		deliveryDateTextView = remarksTextView = null;

		if (rl != null) {
			rl.removeAllViews();
		}

		rl = null;
	}

	class ViewHolder {
		private OrderSplittingDetailsBO orderSplittingDetailsBO;
		private int position;
		private TextView skuNumberTextView, desciptionTextView,
				qtyCaseTextView, qtyPcTextView;
		private CheckBox checkBox;
	}

	class MenuBaseAdapter extends BaseAdapter {
		List<OrderSplittingDetailsBO> orderSplittingDetailsBOList = null;

		public MenuBaseAdapter(List<OrderSplittingDetailsBO> ob) {
			orderSplittingDetailsBOList = ob;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return orderSplittingDetailsBOList.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub

			final ViewHolder holder;

			if (convertView == null) {
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater
						.inflate(
								R.layout.row_order_splitting_details_dialog_list_item_menu,
								parent, false);
				holder = new ViewHolder();

				holder.checkBox = (CheckBox) convertView
						.findViewById(R.id.list_item_check_box);
				holder.desciptionTextView = (TextView) convertView
						.findViewById(R.id.list_item_description);
				holder.qtyCaseTextView = (TextView) convertView
						.findViewById(R.id.list_item_qty_cases);
				holder.qtyPcTextView = (TextView) convertView
						.findViewById(R.id.list_item_qty_pieces);
				holder.skuNumberTextView = (TextView) convertView
						.findViewById(R.id.list_item_sku_code);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			holder.position = position;
			holder.orderSplittingDetailsBO = orderSplittingDetailsBOList
					.get(holder.position);

			holder.desciptionTextView.setText(holder.orderSplittingDetailsBO
					.getDescription());
			holder.qtyCaseTextView.setText(holder.orderSplittingDetailsBO
					.getCaseQty() + "");
			holder.qtyPcTextView.setText(holder.orderSplittingDetailsBO
					.getPieceqty() + "");
			holder.skuNumberTextView.setText(holder.orderSplittingDetailsBO
					.getMbarcode());

			// /*
			holder.checkBox
					.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

						@Override
						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub

							holder.orderSplittingDetailsBO
									.setTicked_in_dialog_check_box(!(isChecked));
						}

					});// */

			/*
			 * convertView.setOnClickListener ( new
			 * android.view.View.OnClickListener() {
			 * 
			 * @Override public void onClick(View v) { // TODO Auto-generated
			 * method stub
			 * if(holder.orderSplittingDetailsBO.isTicked_in_dialog_check_box())
			 * {
			 * holder.orderSplittingDetailsBO.setTicked_in_dialog_check_box(false
			 * ); v.setBackgroundResource(0); } else {
			 * holder.orderSplittingDetailsBO
			 * .setTicked_in_dialog_check_box(true);
			 * v.setBackgroundColor(context
			 * .getResources().getColor(R.color.GREEN)); } } } );
			 */

			holder.checkBox.setChecked(!(holder.orderSplittingDetailsBO
					.isTicked_in_dialog_check_box()));
			/*
			 * if(holder.orderSplittingDetailsBO.isTicked_in_dialog_check_box())
			 * 
			 * {
			 * //holder.orderSplittingDetailsBO.setTicked_in_dialog_check_box(false
			 * ); convertView.setBackgroundResource(0); } else {
			 * //holder.orderSplittingDetailsBO
			 * .setTicked_in_dialog_check_box(true);
			 * //convertView.setBackgroundResource(getGreen_color());
			 * convertView
			 * .setBackgroundColor(context.getResources().getColor(R.color
			 * .GREEN)); }
			 */

			return convertView;
		}
	}

	class MovingRightToLeftSideLoader extends AsyncTask<Void, Void, Void> {

		protected void onPreExecute() {
			saveListener.showProgressDialog("Loading ...");
		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			saveListener.saveProcess();
			return null;

		}

		protected void onPostExecute(Void result) {
			saveListener.dismissProgressDialog();
			saveListener.resettingListViews();
			dismiss();

		}

	}

	public boolean checkForDateAndPoNumber() {
		bmodel.setSelectedDateFromDatePickerDialog(null);

		String remarks1 = this.remarksTextView.getText().toString();
		// bmodel.setRemarksForOrderSplit();

		// bmodel.orderSplitHelper.getCurrentlySelectedOrderSplittingMasterBOForEdit().setRemarks(this.remarksTextView.getText().toString());

		String poNumber = this.poNumberEditText.getText().toString();
		/*
		 * if((poNumber==null)||(poNumber.length()<1)) { Toast.makeText(context,
		 * R.string.prompt_invalid_po_number,Toast.LENGTH_SHORT).show(); return
		 * false; }
		 */
		bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit().setPo(
						poNumber);

		String dateStr = deliveryDateTextView.getText().toString();
		bmodel.setSelectedDateFromDatePickerDialog(dateStr);

		/*
		 * int day = datePicker.getDayOfMonth(); int month =
		 * datePicker.getMonth() + 1; int year = datePicker.getYear();
		 * dateStr=day+"/"+month+"/"+year;
		 */

		if ((dateStr == null) || (dateStr.length() < 1)) {
			Toast.makeText(context, R.string.prompt_invalid_date,
					Toast.LENGTH_SHORT).show();
			return false;
		}

		bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.setDeliveryDate(dateStr);

		/*
		 * if((remarks1==null)||(remarks1.length()<1)) { Toast.makeText(context,
		 * R.string.prompt_invalid_remarks,Toast.LENGTH_SHORT).show(); return
		 * false; }
		 */
		//bmodel.setRemarksForOrderSplit(remarks1);
		bmodel.orderSplitHelper
				.getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.setRemarks(remarks1);

		remarks1 = null;
		dateStr = null;
		poNumber = null;
		return true;
	}

	public void setDeliverDate() {
		if (bmodel.getSelectedDateFromDatePickerDialog() == null)
			deliveryDateTextView.setText("");
		else
			deliveryDateTextView.setText(bmodel
					.getSelectedDateFromDatePickerDialog());
	}

	public String getCurrentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy",
				Locale.ENGLISH);
		return sdf.format(Calendar.getInstance().getTime());
	}

	public void checkForAllItemsIsSelected() {

	}

	/**
	 * @return the green_color
	 */
	public int getGreen_color() {
		return green_color;
	}

	/**
	 * @param green_color
	 *            the green_color to set
	 */
	public void setGreen_color(int green_color) {
		this.green_color = green_color;
	}

}
