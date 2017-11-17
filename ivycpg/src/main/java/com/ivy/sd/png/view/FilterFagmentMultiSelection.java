package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ParentLevelBo;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

@SuppressLint("ValidFragment")
public class FilterFagmentMultiSelection<E> extends Fragment implements
		OnClickListener, OnItemClickListener {

	private Context context;
	private GridView brandOrSplGridView, categoryGridView;
	private String buttonName;
	private Button cancelButton, allButton, mbtn_ok;
	private BrandDialogInterface brandInterface;
	private Vector itm;
	private BusinessModel bmodel;
	String isFrom;
	View view;
	HashMap<String, String> mselectedFilterMap;
	TextView mpre_select_filter;
	//boolean hideBrandFilter = false;
	// List<Integer> catgoryintlist = new ArrayList<Integer>();
	private ArrayList<Object> mSelectedObjects = new ArrayList<Object>();
	HashMap<Integer, String> brandhmap = new HashMap<Integer, String>();
	HashMap<Integer, String> hMapcatogry = new HashMap<Integer, String>();
	private TextView pLevelFilterheading, cLevelFilterHeading;
	private String parentTitle, childTitle;

	public FilterFagmentMultiSelection() {

	}

	public FilterFagmentMultiSelection(HashMap<String, String> selectedfilter) {
		mselectedFilterMap = selectedfilter;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		view = inflater.inflate(R.layout.filter_dialog, container, false);
		this.context = getActivity();
		bmodel = (BusinessModel) context.getApplicationContext();

		brandOrSplGridView = (GridView) view.findViewById(R.id.brandGrid);
		categoryGridView = (GridView) view.findViewById(R.id.categoryGrid);
		
		pLevelFilterheading = (TextView) view
				.findViewById(R.id.chooseCategoryTitle);
		cLevelFilterHeading = (TextView) view
				.findViewById(R.id.chooseBrandTitle);

		boolean isBrandFilter = false;

		try {
			buttonName = getArguments().getString("filterName");
			isBrandFilter = getArguments().getBoolean("isFormBrand");
			parentTitle = getArguments().getString("pfilterHeader");
			childTitle = getArguments().getString("filterHeader");
			isFrom = getArguments().getString("isFrom");
			//hideBrandFilter = getArguments().getBoolean("hideBrandFilter");

			if (!isBrandFilter) {
				// itm = new Vector(getArguments().getStringArrayList(
				// "filterContent"));
				if (isFrom != null) {
					if (isFrom.equals("stockproposal")) {
						itm = (Vector) getArguments().get("filterContent");
					}
					else
					{
						itm = (Vector) getArguments().get("serilizeContent");
					}
				} else {
					itm = bmodel.configurationMasterHelper.downloadFilterList();
				}
			} else {
				itm = (Vector) getArguments().get("serilizeContent");
			}
		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
		}

		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.chooseCategoryTitle).getTag()) != null)
				((TextView) view.findViewById(R.id.chooseCategoryTitle))
						.setText(bmodel.labelsMasterHelper.applyLabels(view
								.findViewById(R.id.chooseCategoryTitle)
								.getTag()));
		} catch (Exception e) {
			Commons.printException(e);
		}

		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.chooseBrandTitle).getTag()) != null)
				((TextView) view.findViewById(R.id.chooseBrandTitle))
						.setText(bmodel.labelsMasterHelper.applyLabels(view
								.findViewById(R.id.chooseBrandTitle).getTag()));
		} catch (Exception e) {
			Commons.printException(e);
		}
		
		if (parentTitle != null)
			pLevelFilterheading.setText(getResources().getString(
					R.string.choose)
					+ " " + parentTitle);
		else
			pLevelFilterheading.setText(getResources().getString(
					R.string.choose));

		if (childTitle != null)
			cLevelFilterHeading.setText(getResources().getString(
					R.string.choose)
					+ " " + childTitle);
		else
			cLevelFilterHeading.setText(getResources().getString(
					R.string.choose));

		//if (!isBrandFilter || !bmodel.configurationMasterHelper.SHOW_CAT_FILTER) {
			if (!isBrandFilter ) {
			view.findViewById(R.id.chooseCategoryTitle)
					.setVisibility(View.GONE);
			categoryGridView.setVisibility(View.GONE);
		}
		/*if (hideBrandFilter) {
			view.findViewById(R.id.chooseBrandTitle).setVisibility(View.GONE);
			brandOrSplGridView.setVisibility(View.GONE);
		}*/
		if (buttonName.equals("General")) {
			TextView tv = (TextView) view.findViewById(R.id.chooseBrandTitle);
			tv.setText(getActivity().getResources().getString(
					R.string.choose_special_filter));
		}

		cancelButton = (Button) view.findViewById(R.id.btn_cancel);
		allButton = (Button) view.findViewById(R.id.btn_all);
		mbtn_ok = (Button) view.findViewById(R.id.btn_ok);
		mbtn_ok.setVisibility(View.VISIBLE);
		brandOrSplGridView.setOnItemClickListener(this);
		categoryGridView.setOnItemClickListener(this);

		mbtn_ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				/*if (hideBrandFilter == false) {
					brandFilterorder();
				} else {*/
					//catgoryFilterorder();
					brandFilterorder();
				//}
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				// Perform action on click
				brandInterface.updateCancel();

			}
		});

		allButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				resetMultiSelectionBO();
				// Perform action on click
				if (buttonName.equals("Brand")) {

					brandInterface.updateBrandText("Brand", -1);
					mselectedFilterMap.put("Brand", "All");
					mselectedFilterMap.put("Category", "All");
				}
				if (buttonName.equals("General")) {
					brandInterface.updateGeneralText("General");
					mselectedFilterMap.put("General", "All");
					mselectedFilterMap.put("Brand", "All");
					mselectedFilterMap.put("Category", "All");
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
		for (int i = 0; i < itm.size(); i++) {
			mylist.add((E) itm.get(i));
		}

		Commons.print("Mylist.Size" + "ListSize" + mylist.size());

		MyGridAdapter mSchedule = new MyGridAdapter(mylist);
		brandOrSplGridView.setAdapter(mSchedule);

		try {
			updateBrandFilter(Integer.parseInt(mselectedFilterMap
					.get("Category")));
		} catch (Exception e) {
			System.out.println(e + "");
		}

		return view;
	}

	private void resetMultiSelectionBO() {
		try {
			if(bmodel.productHelper.getChildLevelBo()!=null){
				int size=bmodel.productHelper.getChildLevelBo().size();
			for (int i = 0; i <size; i++) {
				ChildLevelBo childBO = (ChildLevelBo) bmodel.productHelper.getChildLevelBo().get(i);
				childBO.setMchecked(false);
			}
			}
			if( bmodel.productHelper.getParentLevelBo()!=null){
				int size= bmodel.productHelper.getParentLevelBo().size();
			for (int i = 0; i <size; i++) {
				ParentLevelBo mcategoryBO = (ParentLevelBo) bmodel.productHelper.getParentLevelBo().get(i);
				mcategoryBO.setMchecked(false);
			}
			}
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Fragment#onAttach(android.app.Activity)
	 */
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		if (activity instanceof BrandDialogInterface) {
			this.brandInterface = (BrandDialogInterface) activity;
		}
	}

	public void brandFilterorder() {
		List<String> listbrandname = new ArrayList<String>();
		List<Integer> brandintlist = new ArrayList<Integer>();
		Iterator<Integer> keySetIterator = brandhmap.keySet().iterator();
		while (keySetIterator.hasNext()) {
			Integer key = keySetIterator.next();
			listbrandname.add(brandhmap.get(key));
			brandintlist.add(key);
		}
		if (brandintlist.size() > 0) {
		brandInterface.updateMultiSelectionBrand(listbrandname, brandintlist);
		}else{
			brandInterface.updateBrandText("Brand", -1);
			mselectedFilterMap.put("Brand", "All");
			mselectedFilterMap.put("Category", "All");
		}
	}

	public void catgoryFilterorder() {

		List<Integer> intlist = new ArrayList<Integer>();
		Iterator<Integer> keySetIterator = hMapcatogry.keySet().iterator();
		while (keySetIterator.hasNext()) {
			Integer key = keySetIterator.next();
			intlist.add(key);

		}
		if (intlist.size() > 0) {
			brandInterface.updateMultiSelectionCategory(intlist);

		} else {
			brandInterface.updateBrandText("Brand", -1);
			mselectedFilterMap.put("Brand", "All");
			mselectedFilterMap.put("Category", "All");
		}
	}

	private void updateBrandFilter(int parentId) {
		try {

			Commons.print("updateBrandFilter>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>,"+ ""+parentId);
			ArrayList<E> mylist = new ArrayList<E>();
			for (int i = 0; i < itm.size(); i++) {
				ChildLevelBo childBO = (ChildLevelBo) itm.get(i);
				if (childBO.getParentid() == parentId)
					mylist.add((E) childBO);
			}

			MyGridAdapter mSchedule = new MyGridAdapter(mylist);
			brandOrSplGridView.setAdapter(mSchedule);
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	private void updateBrandFilterlist(List<Integer> b) {
		try {

			Commons.print("updateBrandFilterlist>>>>>>>>>>>>>>>>>>>>>>>>>>>,"+ ""+b);
			ArrayList<E> mylist = new ArrayList<E>();
			for (int i = 0; i < itm.size(); i++) {
				ChildLevelBo childBO = (ChildLevelBo) itm.get(i);
				if (b.contains(childBO.getParentid()))
					mylist.add((E) childBO);
			}

			MyGridAdapter mSchedule = new MyGridAdapter(mylist);
			brandOrSplGridView.setAdapter(mSchedule);
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	// BrandMasterBO brandBO;
	// ConfigureBO configBO;

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

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;

			/*
			 * if (buttonName.equals("Brand")) { brandBO = (BrandMasterBO)
			 * items.get(position); } else { configBO = (ConfigureBO)
			 * items.get(position); }
			 */
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater
						.inflate(R.layout.filter_grid_item_multi_selection, parent, false);
				holder = new ViewHolder();
				holder.ref = position;

				if (buttonName.equals("Brand")) {
					holder.childBO = (ChildLevelBo) items.get(holder.ref);
				} else {
					holder.configBO = (ConfigureBO) items.get(holder.ref);
				}

				holder.text = (TextView) row.findViewById(R.id.grid_item_text);
				holder.cb = (CheckBox) row.findViewById(R.id.checkBox1);
				// holder.cb.setVisibility(View.VISIBLE);
				mbtn_ok.setVisibility(View.VISIBLE);
				if (buttonName.equals("Brand")) {
					holder.text.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							if (holder.childBO.isMchecked() == true) {

								holder.childBO.setMchecked(false);
								holder.cb.setChecked(false);
								holder.text
										.setBackgroundDrawable(getActivity()
												.getResources()
												.getDrawable(
														R.drawable.filter_button_bg_normal));
							} else {
								holder.childBO.setMchecked(true);
								holder.cb.setChecked(true);
								holder.text.setBackgroundColor(getActivity()
										.getResources().getColor(
												R.color.blue_btn_txt));
							}

						}
					});
					holder.cb
							.setOnCheckedChangeListener(new OnCheckedChangeListener() {

								@Override
								public void onCheckedChanged(
										CompoundButton buttonView,
										boolean isChecked) {
									if (isChecked) {
										holder.childBO.setMchecked(isChecked);
										brandhmap.put(holder.id, holder.text
												.getText().toString());
									} else {
										brandhmap.remove(holder.id);
										holder.childBO.setMchecked(false);
									}

								}
							});
				}
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			holder.ref = position;
			if (buttonName.equals("Brand")) {
				holder.childBO = (ChildLevelBo) items.get(holder.ref);
			} else {
				holder.configBO = (ConfigureBO) items.get(holder.ref);
			}
			if (buttonName.equals("Brand")) {

				holder.text.setText(holder.childBO.getPlevelName() + "");
				holder.id = holder.childBO.getProductid();
				holder.cb.setChecked(holder.childBO.isMchecked());
				if ((holder.id + "").equals(mselectedFilterMap.get("Brand"))) {
					holder.text.setBackgroundColor(getActivity().getResources()
							.getColor(R.color.blue_btn_txt));
				} else {
					holder.text.setBackgroundDrawable(getActivity()
							.getResources().getDrawable(
									R.drawable.filter_button_bg_normal));

					if (holder.childBO.isMchecked() == true) {
						brandhmap.put(holder.id, holder.text.getText()
								.toString());
						holder.text.setBackgroundColor(getActivity()
								.getResources().getColor(R.color.blue_btn_txt));
					} else {
						brandhmap.remove(holder.id);
						holder.text.setBackgroundDrawable(getActivity()
								.getResources().getDrawable(
										R.drawable.filter_button_bg_normal));
					}

				}

			}

			else if (buttonName.equals("General")) {
				try {
					// if (isFrom.equalsIgnoreCase("stockproposal")
					// && configBO.getMenuName().equalsIgnoreCase("ordered"))
					// holder.text.setVisibility(View.GONE);
					// else
					mbtn_ok.setVisibility(View.GONE);
					holder.cb.setVisibility(View.GONE);
					holder.text.setText(holder.configBO.getMenuName());

					holder.Spe_filt_id = holder.configBO.getConfigCode();

					if (holder.Spe_filt_id.equals(mselectedFilterMap
							.get("General"))) {
						holder.text.setBackgroundColor(getActivity()
								.getResources().getColor(R.color.blue_btn_txt));
					} else {
						holder.text.setBackgroundDrawable(getActivity()
								.getResources().getDrawable(
										R.drawable.filter_button_bg_normal));

					}
				} catch (Exception e) {
					Commons.printException(e);
				}
			}

			holder.type = buttonName;

			return (row);
		}
	}

	class ViewHolder {
		TextView text;
		CheckBox cb;

		int id;
		ChildLevelBo childBO;
		ConfigureBO configBO;
		ParentLevelBo parentLevelBO;
		int ref;
		String type, Spe_filt_id;
		boolean isSelected;
	}

	public HashMap<Integer, String> getCheckedValues() {
		return brandhmap;
	}

	// CategoryMasterBO categoryBO;

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

			// categoryBO = (CategoryMasterBO) items.get(position);

			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater
						.inflate(R.layout.filter_grid_item_multi_selection, parent, false);
				holder = new ViewHolder();
				holder.ref = position;
				holder.parentLevelBO = (ParentLevelBo) items.get(holder.ref);
				holder.text = (TextView) row.findViewById(R.id.grid_item_text);
				holder.cb = (CheckBox) row.findViewById(R.id.checkBox1);
				// holder.cb.setVisibility(View.VISIBLE);

				holder.text.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						if (holder.parentLevelBO.isMchecked() == true) {

							holder.parentLevelBO.setMchecked(false);
							holder.cb.setChecked(false);
							holder.text
									.setBackgroundDrawable(getActivity()
											.getResources()
											.getDrawable(
													R.drawable.filter_button_bg_normal));

						} else {
							holder.parentLevelBO.setMchecked(true);
							holder.cb.setChecked(true);
							holder.text.setBackgroundColor(getActivity()
									.getResources().getColor(
											R.color.blue_btn_txt));
						}

					}
				});
				holder.cb
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(
									CompoundButton buttonView, boolean isChecked) {
								if (isChecked) {
									holder.parentLevelBO.setMchecked(isChecked);
									hMapcatogry.put(holder.id, holder.text
											.getText().toString());
									List<Integer> intlist = new ArrayList<Integer>();
									Iterator<Integer> keySetIterator = hMapcatogry
											.keySet().iterator();
									while (keySetIterator.hasNext()) {
										Integer key = keySetIterator.next();
										intlist.add(key);

									}
									updateBrandFilterlist(intlist);

								} else {
									hMapcatogry.remove(holder.id);
									List<Integer> intlist = new ArrayList<Integer>();
									Iterator<Integer> keySetIterator = hMapcatogry
											.keySet().iterator();
									while (keySetIterator.hasNext()) {
										Integer key = keySetIterator.next();
										intlist.add(key);

									}
									holder.parentLevelBO.setMchecked(false);
									clearBrandCatgory(holder.id);
									updateBrandFilterlist(intlist);
								}

							}
						});
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			holder.ref = position;

			holder.parentLevelBO = (ParentLevelBo) items.get(holder.ref);

			holder.text.setText(holder.parentLevelBO.getPl_levelName() + "");
			holder.id = holder.parentLevelBO.getPl_productid();
			holder.cb.setChecked(holder.parentLevelBO.isMchecked());
			holder.type = "Category";
			if ((holder.id + "").equals(mselectedFilterMap.get("Category"))) {
				mpre_select_filter = holder.text;
				holder.text.setBackgroundColor(getActivity().getResources()
						.getColor(R.color.blue_btn_txt));
				updateBrandFilter(holder.id);

			} else {
				holder.text.setBackgroundDrawable(getActivity().getResources()
						.getDrawable(R.drawable.filter_button_bg_normal));
				// updateBrandFilter(holder.id);

				if (holder.parentLevelBO.isMchecked() == true) {
					hMapcatogry
							.put(holder.id, holder.text.getText().toString());
					holder.text.setBackgroundColor(getActivity().getResources()
							.getColor(R.color.blue_btn_txt));
					// catgoryintlist.add(holder.id);
					List<Integer> intlist = new ArrayList<Integer>();
					Iterator<Integer> keySetIterator = hMapcatogry.keySet()
							.iterator();
					while (keySetIterator.hasNext()) {
						Integer key = keySetIterator.next();
						intlist.add(key);

					}
					updateBrandFilterlist(intlist);
				} else {
					hMapcatogry.remove(holder.id);
					holder.text.setBackgroundDrawable(getActivity()
							.getResources().getDrawable(
									R.drawable.filter_button_bg_normal));

				}
			}

			return (row);
		}
	}

	public void clearBrandCatgory(int cb) {
		for (int i = 0; i < itm.size(); i++) {
			ChildLevelBo childBO = (ChildLevelBo) itm.get(i);
			if (childBO.getParentid() == cb) {
				brandhmap.remove(childBO.getProductid());
				childBO.setMchecked(false);

			}
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		resetMultiSelectionBO();
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		// TODO Auto-generated method stub

		ViewHolder holder = (ViewHolder) arg1.getTag();
		// set color for the selected item
		holder.text.setBackgroundColor(getActivity().getResources().getColor(
				R.color.blue_btn_txt));

		if (holder.type.equals("Brand") || holder.type.equals("General")) {
			// Brand or Spl filter
			if (buttonName.equals("Brand")) {
				System.out.println("onitem" + holder.id);
				mselectedFilterMap.put("Brand", holder.id + "");
				brandInterface.updateBrandText((String) holder.text.getText(),
						holder.id);

			}

			else if (buttonName.equals("General")) {
				mselectedFilterMap.put("General", (String) holder.Spe_filt_id);
				brandInterface.updateGeneralText((String) holder.Spe_filt_id);
				mselectedFilterMap.put("Brand", "All");
				mselectedFilterMap.put("Category", "All");

			}
		} else {
			// Category Filter is clicked

			// set previously selected filter to normal color
			if (mpre_select_filter != null) {
				mpre_select_filter.setBackgroundDrawable(getActivity()
						.getResources().getDrawable(
								R.drawable.filter_button_bg_normal));

			}
			// store selected filter in hashmap
			mselectedFilterMap.put("Category", holder.id + "");

			mpre_select_filter = holder.text;
			/*if (hideBrandFilter)
				brandInterface.updateBrandText((String) holder.text.getText(),
						holder.id);
			else*/
				updateBrandFilter(holder.id);
		}
	}

}
