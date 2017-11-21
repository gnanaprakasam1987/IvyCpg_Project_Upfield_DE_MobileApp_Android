package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.ParentLevelBo;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

public class FilterDialogue<E> extends Dialog implements OnClickListener,
		OnItemClickListener {

	private Context context;
	private GridView brandOrSplGridView, categoryGridView;
	private String buttonName;
	private Button cancelButton, allButton;
	private BrandDialogInterface brandInterface;
	private Vector itm;
	private BusinessModel bmodel;

	public FilterDialogue(Context context, Vector items, String pdname,
			final BrandDialogInterface bdinterface, boolean isBrandFilter) {
		super(context);
		this.context = context;
		buttonName = pdname;
		brandInterface = bdinterface;
		itm = items;
		bmodel = (BusinessModel) context.getApplicationContext();

		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.filter_dialog);

		brandOrSplGridView = (GridView) findViewById(R.id.brandGrid);
		categoryGridView = (GridView) findViewById(R.id.categoryGrid);

		if (!isBrandFilter) {
			findViewById(R.id.chooseCategoryTitle).setVisibility(View.GONE);
			categoryGridView.setVisibility(View.GONE);
		}
		
		if (buttonName.equals("General")) {
			TextView tv=(TextView) findViewById(R.id.chooseBrandTitle);
			tv.setText("Choose Special Filter");
		}

		cancelButton = (Button) findViewById(R.id.btn_cancel);
		allButton = (Button) findViewById(R.id.btn_all);
		
		brandOrSplGridView.setOnItemClickListener(this);
		categoryGridView.setOnItemClickListener(this);

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				bdinterface.updateCancel();
				FilterDialogue.this.dismiss();
			}
		});

		allButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				if (buttonName.equals("Brand")) {
					bdinterface.updateBrandText("Brand", -1);
					FilterDialogue.this.dismiss();
				}
				if (buttonName.equals("General")) {
					bdinterface.updateGeneralText("General");
					FilterDialogue.this.dismiss();
				}

			}
		});

		// Set Data to category Filter
		if (isBrandFilter) {
			CategoryGridAdapter categoryAdapter = new CategoryGridAdapter(
					bmodel.productHelper.getParentLevelBo());
			categoryGridView.setAdapter(categoryAdapter);
		}

		ArrayList<E> mylist = new ArrayList<E>();
		for (int i = 0; i < items.size(); i++) {
			mylist.add((E) items.get(i));
		}

		MyGridAdapter mSchedule = new MyGridAdapter(mylist);
		brandOrSplGridView.setAdapter(mSchedule);

	}

	private void updateBrandFilter(int categoryId) {
		try {
			ArrayList<E> mylist = new ArrayList<E>();
			for (int i = 0; i < itm.size(); i++) {
				ChildLevelBo childBO = (ChildLevelBo) itm.get(i);
				if (childBO.getParentid()== categoryId)
					mylist.add((E) childBO);
			}

			MyGridAdapter mSchedule = new MyGridAdapter(mylist);
			brandOrSplGridView.setAdapter(mSchedule);
		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
		}
	}

	ChildLevelBo childBO;

	class MyGridAdapter extends ArrayAdapter {
		ArrayList items;

		public MyGridAdapter(ArrayList items) {
			super(context, R.layout.filter_grid_item, items);
			this.items = items;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return items.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			if (buttonName.equals("Brand")) {
				childBO = (ChildLevelBo) items.get(position);
			}
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater
						.inflate(R.layout.filter_grid_item, parent, false);
				holder = new ViewHolder();
				holder.text = (TextView) row.findViewById(R.id.grid_item_text);
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			if (buttonName.equals("Brand")) {

				holder.text.setText(childBO.getPlevelName() + "");
				holder.id = childBO.getProductid();
			}

			else if (buttonName.equals("General")) {

				holder.text.setText(items.get(position) + "");
				holder.id = 0;
			}

			holder.type = buttonName;

			return (row);
		}
	}

	class ViewHolder {
		TextView text;
		int id;
		String type;
	}

	ParentLevelBo categoryBO;

	class CategoryGridAdapter extends ArrayAdapter<ParentLevelBo> {
		Vector<ParentLevelBo> items;

		public CategoryGridAdapter(Vector<ParentLevelBo> items) {
			super(context, R.layout.filter_grid_item, items);
			this.items = items;
		}

		public ParentLevelBo getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return items.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			categoryBO = (ParentLevelBo) items.get(position);

			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater
						.inflate(R.layout.filter_grid_item, parent, false);
				holder = new ViewHolder();
				holder.text = (TextView) row.findViewById(R.id.grid_item_text);
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			holder.text.setText(categoryBO.getPl_levelName() + "");
			holder.id = categoryBO.getPl_productid();
			holder.type = "Category";

			return (row);
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		this.dismiss();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub

		ViewHolder holder = (ViewHolder) arg1.getTag();

		if (holder.type.equals("Brand") || holder.type.equals("General")) {

			// Brand or Spl filter


			if (buttonName.equals("Brand")) {

				brandInterface.updateBrandText((String) holder.text.getText(), holder.id);
				FilterDialogue.this.dismiss();
			}

			else if (buttonName.equals("General")) {

				brandInterface.updateGeneralText((String) holder.text.getText());
				FilterDialogue.this.dismiss();
			}
		} else {
			// Category Filter is clicked
			updateBrandFilter(holder.id);
		}
	}
}
