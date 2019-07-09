package com.ivy.cpg.view.emptyreconcil;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.legacy.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationHelper.SKUTypeBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class EmptyReconciliationFragment extends IvyBaseFragment implements
        BrandDialogInterface,FiveLevelFilterCallBack {

    private BusinessModel bmodel;
    private PriceTrackingHelper priceTrackingHelper;

    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ListView lv;

    private ArrayList<ProductMasterBO> mylist;
    private String strBarCodeSearch = "ALL";

    private EditText QUANTITY;
    private String append = "";

    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    View view;
    private EmptyReconciliationHelper emptyReconciliationHelper;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        view = inflater.inflate(R.layout.fragment_empty_reconciliation,
                container, false);

        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        emptyReconciliationHelper = EmptyReconciliationHelper.getInstance(getActivity());
        setHasOptionsMenu(true);
        /*
         * if (bmodel.configurationMasterHelper.SELECT_DEFAULT_FILTER) {
		 * selectByDefaultFirstFilter(); } else { if
		 * (bmodel.mPriceChangeCheckHelper.mSelectedFilter == 0)
		 * bmodel.mPriceChangeCheckHelper.mSelectedFilter = -1; if
		 * (bmodel.mPriceChangeCheckHelper.mSelectedParentFilter == 0)
		 * bmodel.mPriceChangeCheckHelper.mSelectedParentFilter = -1; }
		 */
        priceTrackingHelper = PriceTrackingHelper.getInstance(getContext());
        if (priceTrackingHelper.mSelectedFilter == 0)
            priceTrackingHelper.mSelectedFilter = -1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_inventory, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);

        try {
            boolean drawerOpen = false;

            if (mDrawerLayout != null)
                drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
            menu.findItem(R.id.menu_location_filter).setVisible(!drawerOpen);

            menu.findItem(R.id.menu_location_filter).setIcon(
                    R.drawable.ic_action_description);
            menu.findItem(R.id.menu_location_filter).setVisible(true);
            menu.findItem(R.id.menu_spl_filter).setVisible(false);
            menu.findItem(R.id.menu_remarks).setVisible(false);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                getActivity().finish();
            }
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        }else if (i == R.id.menu_location_filter) {
            View anchor = getActivity().findViewById(item.getItemId());
            showPopup(getActivity(), anchor);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showPopup(final Activity context, View anchor) {

        // Get Anchor View Location
        int[] locations = new int[2];
        anchor.getLocationOnScreen(locations);
        Point p = new Point();
        p.x = locations[0];
        p.y = locations[1];

        // Prepare Arrow
        ImageView popupArrow = new ImageView(getActivity());
        popupArrow.setBackgroundResource(R.drawable.quickaction_arrow_up);

        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        Drawable d = getResources()
                .getDrawable(R.drawable.quickaction_arrow_up);
        int w = d.getIntrinsicWidth();

        int popupArrowXPosition = p.x + (anchor.getWidth() / 2) - (w / 2);
        int popupArrowYPosition = p.y + (anchor.getHeight() / 4);

        param.setMargins(popupArrowXPosition, popupArrowYPosition, 0, 0);

        popupArrow.setLayoutParams(param);

        // Prepare ListView
        ListView listView = new ListView(getActivity());
        listView.setBackgroundResource(R.drawable.rounded_corners);

        LinearLayout.LayoutParams param1 = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        param1.setMargins(20, 0, 20, 20);

        listView.setLayoutParams(param1);

        ArrayList<String> listItems = new ArrayList<>();

        for (SKUTypeBO type : emptyReconciliationHelper.getSkuTypeBO()) {

            listItems.add(type.getTypeName() + " - " + type.getQty());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_list_item_1, listItems);

        listView.setAdapter(adapter);

        // Add Arrow and ListView into ParentLayout
        LinearLayout parentLayout = new LinearLayout(getActivity());
        parentLayout.setOrientation(LinearLayout.VERTICAL);
        parentLayout.addView(popupArrow);
        parentLayout.addView(listView);

        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(context);
        popup.setContentView(parentLayout);
        popup.setWidth(LayoutParams.MATCH_PARENT);
        popup.setHeight(LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);

        // Offset to align the PopUp
        int OFFSET_X = 0;
        int OFFSET_Y = anchor.getHeight() / 2;

        // Clear the default translucent background
        popup.setBackgroundDrawable(new BitmapDrawable());

        // Displaying the popup at the specified location, + offsets.
        popup.showAtLocation(anchor, Gravity.NO_GRAVITY, p.x + OFFSET_X, p.y
                + OFFSET_Y);

    }

    @Override
    public void onStart() {


        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                getArguments().getString("screentitle"));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(
                R.drawable.icon_stock);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_action_bottle_return, /* nav drawer image to replace 'Up' caret */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                ((AppCompatActivity) getActivity()).getSupportActionBar()
                        .setTitle(
                                getArguments().getString(
                                        "screentitle"));
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {

                ((AppCompatActivity) getActivity()).getSupportActionBar()
                        .setTitle(getResources().getString(R.string.filter));
                getActivity().supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");

        if (getView().findViewById(R.id.calcdot) != null)
            (getView().findViewById(R.id.calcdot))
                    .setVisibility(View.VISIBLE);

        lv = (ListView) getView().findViewById(R.id.list);
        lv.setCacheColorHint(0);

        onLoadModule();

        super.onStart();

    }

    private void nextButtonClick() {
        try {
            if (emptyReconciliationHelper.isDataToSave()) {
                if (emptyReconciliationHelper.checkDataTosave())
                    new SaveAsyncTask().execute();
                else
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.exceed_allocation),
                            Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.no_data_tosave),
                        Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        //    private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                emptyReconciliationHelper.saveTransaction();
                /*
                 * bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
				 * .now(SDUtil.TIME)); bmodel.updateIsVisitedFlag();
				 */

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
           /* progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
                    true, false);*/
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            try {
               /* if (progressDialogue != null)
                    progressDialogue.dismiss();*/
                if (alertDialog != null)
                    alertDialog.dismiss();
            } catch (Exception e) {
                Commons.printException(e);
            }
            if (result == Boolean.TRUE) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();

                startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                getActivity().finish();
            }

        }

    }

    private void onLoadModule() {
        ArrayList<ProductMasterBO> items = emptyReconciliationHelper
                .getProductBO();

        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<ProductMasterBO>();

        if (strBarCodeSearch.equalsIgnoreCase("ALL")) {
            for (ProductMasterBO sku : items) {
                if (priceTrackingHelper.mSelectedFilter == sku
                        .getParentid()
                        || priceTrackingHelper.mSelectedFilter == -1)
                    mylist.add(sku);
            }
        } else {
            for (ProductMasterBO sku : items) {
                if (sku.getBarCode().equalsIgnoreCase(strBarCodeSearch)) {
                    mylist.add(sku);
                    break;
                }
            }
        }

        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);

    }

    class ViewHolder {
        ProductMasterBO mSKUBO;
        int position;
        TextView mBarCode;
        TextView mSKU;
        TextView mType;
        EditText mQty;
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.row_empty_reconciliation, items);
            this.items = items;
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity()
                        .getBaseContext());

                convertView = inflater.inflate(
                        R.layout.row_empty_reconciliation, null);

                holder.mBarCode = (TextView) convertView
                        .findViewById(R.id.barcode);

                holder.mSKU = (TextView) convertView.findViewById(R.id.sku);
                holder.mSKU.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.mType = (TextView) convertView.findViewById(R.id.type);

                holder.mQty = (EditText) convertView.findViewById(R.id.qty);
                holder.mQty
                        .setInputType(InputType.TYPE_NULL);

                holder.mQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        holder.mSKUBO.setRetPieceQty(SDUtil.convertToInt(qty));
                        double totalAmount = SDUtil.convertToInt(qty)
                                * SDUtil.convertToDouble(holder.mSKUBO.getPrice());
                        holder.mSKUBO.setTotalamount(totalAmount);
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                });

                holder.mQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.mQty;
                        QUANTITY.setId(0);
                        int inType = holder.mQty.getInputType(); // backup
                        holder.mQty.setInputType(InputType.TYPE_NULL); // disable
                        holder.mQty.onTouchEvent(event); // call native
                        holder.mQty.setInputType(inType); // restore
                        holder.mQty.selectAll();
                        holder.mQty.requestFocus();
                        return true;
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mSKUBO = items.get(position);
            holder.position = position;

            holder.mBarCode.setText(holder.mSKUBO.getBarCode());
            holder.mSKU.setText(holder.mSKUBO.getProductName());
            holder.mType.setText(holder.mSKUBO.getTypeName());
            holder.mQty.setText(holder.mSKUBO.getRetPieceQty() + "");

            return convertView;
        }
    }

    @SuppressLint("ResourceType")
    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
				/*
				 * int s = SDUtil.convertToInt((String) QUANTITY.getText()
				 * .toString()); s = s / 10; QUANTITY.setText(s + "");
				 */

                if (QUANTITY.getId() == 1) {
                    String s = QUANTITY.getText().toString();

                    if (s != null) {
                        if (!s.isEmpty()) {
                            s = s.substring(0, s.length() - 1);

                            if (s.length() == 0) {
                                s = "0.0";
                            }
                        }

                        QUANTITY.setText(s);
                    }
                } else {

                    int s = SDUtil.convertToInt((String) QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(s + "");
                }

            } else {
                Button ed = (Button) getActivity().findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        strBarCodeSearch = "ALL";
        priceTrackingHelper.mSelectedFilter = id;
        mDrawerLayout.closeDrawers();
        onLoadModule();
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

    }

}
