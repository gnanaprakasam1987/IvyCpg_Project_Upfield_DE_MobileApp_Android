/**
 * 
 */
package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderSplitMasterBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.List;

/**
 * @author sivakumar.j
 *
 */
public class DialogForOrderProcessing extends Dialog {

	private BusinessModel bmodel;
	
	private Button syncButton=null;
	private Context context;
	private ListView listView=null;
	
	
	
	RelativeLayout rl;
	MenuBaseAdapter adapter=null;	

	EditText poNumberEditText=null;
	EditText remarkEditText=null;
	DatePicker datePicker=null;
	//TextView showDatePickerTextView=null;
	ImageView showDatePickerTextView=null;
	
	ImageView datePickerImageView=null;
	
	
	private android.view.View.OnClickListener buttonClickListener=null;
	
	
	//public View convertView;	
	
	public DialogForOrderProcessing(Context context) 
	{
		super(context);
		// TODO Auto-generated constructor stub
				
		this.context = context;
		bmodel = (BusinessModel) context.getApplicationContext();				
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		rl = (RelativeLayout) LayoutInflater.from(context)
				.inflate(R.layout.dialog_order_processing, null);
		setContentView(rl);		
		
		getWindow().setLayout(1000,550);
		this.setCancelable(false);
		
		syncButton=(Button)this.findViewById(R.id.syncBTN);			
		listView=(ListView)this.findViewById(R.id.listView1);
		//bmodel.orderSplitHelper.getCurrentlySelectedOrderSplittingMasterBOForEdit().resetBooleanInOrderSplittingDetailsBOList();
		adapter=new MenuBaseAdapter(bmodel.orderSplitHelper.getOrderSplitMasterBOList());
		listView.setAdapter(adapter);
	}	
	
	public void onStop()
	{	
		this.adapter=null;		
		syncButton=null;listView=null;		
				
		if(rl!=null)
		{
			rl.removeAllViews();
		}
		
		rl=null;		
	}
	
	

	class ViewHolder 
	{
		private OrderSplitMasterBO orderSplitMasterBO;
		private int position;
		private TextView numberOfskuTextView,customerNameTextView,orderNumberTextView,poNumberTextView;		
		private CheckBox checkBox;
	}
	
	class MenuBaseAdapter extends BaseAdapter 
	{
		List<OrderSplitMasterBO> orderSplitMasterBOList=null;  //Order split bo list
		
		public MenuBaseAdapter(List<OrderSplitMasterBO> ob)
		{
			orderSplitMasterBOList=ob;
		}
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return orderSplitMasterBOList.size();
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
			
			if (convertView == null) 
			{
				LayoutInflater inflater = getLayoutInflater();
				convertView = inflater.inflate(R.layout.row_order_processing_details_dialog_list_item_menu, parent,
						false);
				holder = new ViewHolder();
				
				holder.checkBox=(CheckBox)convertView.findViewById(R.id.list_item_check_box);
				holder.customerNameTextView=(TextView)convertView.findViewById(R.id.list_item_customer_name);
				holder.orderNumberTextView=(TextView)convertView.findViewById(R.id.list_item_order_no);
				holder.poNumberTextView=(TextView)convertView.findViewById(R.id.list_item_po_no);
				holder.numberOfskuTextView=(TextView)convertView.findViewById(R.id.list_item_n_sku);
				
				convertView.setTag(holder);
			}
			else
			{
				holder=(ViewHolder)convertView.getTag();
			}
			
			holder.position=position;
			holder.orderSplitMasterBO=orderSplitMasterBOList.get(holder.position);
			
			holder.customerNameTextView.setText(holder.orderSplitMasterBO.getRetailerName());
			//holder.customerNameTextView.setText("11111");
			holder.orderNumberTextView.setText(holder.orderSplitMasterBO.getOrderID()+"");
			holder.poNumberTextView.setText(holder.orderSplitMasterBO.getPo()+"");
			holder.numberOfskuTextView.setText(holder.orderSplitMasterBO.getLinesPerCall()+"");
			
			///*
			holder.checkBox.setOnCheckedChangeListener
			(			
				new CompoundButton.OnCheckedChangeListener()
				{

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) 
					{
						// TODO Auto-generated method stub						
						
						if(isChecked)
							holder.orderSplitMasterBO.setProcessed(OrderSplitMasterBO.ORDER_PROCESSED);
						else
							holder.orderSplitMasterBO.setProcessed(OrderSplitMasterBO.ORDER_NOT_PROCESSED);
					}
					
				}		
			);//*/		
			
			
			holder.checkBox.setChecked(((holder.orderSplitMasterBO.getProcessed()==OrderSplitMasterBO.ORDER_PROCESSED))?(((true))):(((false))));
			/*
			convertView.setOnClickListener
			(
				new android.view.View.OnClickListener() 
				{
					
					@Override
					public void onClick(View v) 
					{
						// TODO Auto-generated method stub
						if(holder.orderSplittingDetailsBO.isTicked_in_dialog_check_box())
						{
							holder.orderSplittingDetailsBO.setTicked_in_dialog_check_box(false);
							v.setBackgroundResource(0);
						}
						else
						{
							holder.orderSplittingDetailsBO.setTicked_in_dialog_check_box(true);
							v.setBackgroundColor(context.getResources().getColor(R.color.GREEN));
						}
					}
				}
			);*/
			
			
			//holder.checkBox.setChecked(!(holder.orderSplittingDetailsBO.isTicked_in_dialog_check_box()));
			/*
			if(holder.orderSplittingDetailsBO.isTicked_in_dialog_check_box())
				
			{
				//holder.orderSplittingDetailsBO.setTicked_in_dialog_check_box(false);
				convertView.setBackgroundResource(0);
			}
			else
			{
				//holder.orderSplittingDetailsBO.setTicked_in_dialog_check_box(true);
				//convertView.setBackgroundResource(getGreen_color());
				convertView.setBackgroundColor(context.getResources().getColor(R.color.GREEN));
			}
			*/
			
			return convertView;
		}		
	}	
	
	/**
	 * @return the buttonClickListener
	 */
	public android.view.View.OnClickListener getButtonClickListener() {
		return buttonClickListener;
	}

	/**
	 * @param buttonClickListener the buttonClickListener to set
	 */
	public void setButtonClickListener(android.view.View.OnClickListener buttonClickListener) {
		this.buttonClickListener = buttonClickListener;
		if(this.syncButton!=null)
			this.syncButton.setOnClickListener(buttonClickListener);
	}
	
}
 