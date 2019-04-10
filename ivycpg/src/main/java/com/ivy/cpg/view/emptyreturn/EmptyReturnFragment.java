package com.ivy.cpg.view.emptyreturn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class EmptyReturnFragment extends IvyBaseFragment implements BrandDialogInterface, FiveLevelFilterCallBack {

    private BusinessModel bmodel;
    private DrawerLayout mDrawerLayout;
    private ListView lv;
    private EditText QUANTITY;
    private String append = "";
    View view;
    private ActionBar actionBar;
    private Context context;
    private EmptyReturnHelper emptyReturnHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_empty_return,
                container, false);
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(getActivity());
        emptyReturnHelper = EmptyReturnHelper.getInstance(getActivity());
        setHasOptionsMenu(true);
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

            menu.findItem(R.id.menu_location_filter).setVisible(false);
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
            onBackButonClick();
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

        if (getActivity() != null)
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setTitle(null);
            actionBar.setIcon(null);
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                actionBar.setTitle(bmodel.mSelectedActivityName);
                getActivity().invalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {

                actionBar.setTitle(getResources().getString(R.string.filter));
                getActivity().invalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        lv = view.findViewById(R.id.list);
        lv.setCacheColorHint(0);

        onLoadModule();

        super.onStart();

    }

    private void onLoadModule() {
        Vector<ProductMasterBO> items;

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            items = emptyReturnHelper.getProductType();
        else
            items = bmodel.productHelper.getProductMaster();

        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        ArrayList<ProductMasterBO> mylist = new ArrayList<>();

        for (ProductMasterBO sku : items) {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            if (sku.getIsReturnable() == 1) {
                if (emptyReturnHelper.mSelectedFilter == sku.getParentid()
                        || emptyReturnHelper.mSelectedFilter == -1) {
                    mylist.add(sku);
                }
            }
        }

        MyAdapter adapter = new MyAdapter(mylist);
        lv.setAdapter(adapter);
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(context, R.layout.row_empty_return, items);
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

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(context);

                convertView = inflater.inflate(R.layout.row_empty_return, parent, false);

                holder.mSKU = convertView.findViewById(R.id.sku);

                holder.mQty = convertView.findViewById(R.id.qty);
                holder.mQty.setInputType(InputType.TYPE_NULL);

                holder.mQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.mQty.setSelection(qty.length());
                        holder.mSKUBO.setRetPieceQty(SDUtil.convertToInt(qty));
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
                        holder.mQty.requestFocus();

                        if (holder.mQty.getText().length() > 0)
                            holder.mQty.setSelection(holder.mQty.getText().length());
                        return true;
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mSKUBO = items.get(position);
            holder.position = position;

            holder.mSKU.setText(holder.mSKUBO.getProductName());
            holder.mQty.setText(String.valueOf(holder.mSKUBO.getRetPieceQty()));

            return convertView;
        }
    }

    class ViewHolder {
        ProductMasterBO mSKUBO;
        int position;
        TextView mSKU;
        EditText mQty;
    }

    private void nextButtonClick() {
        try {
            if (emptyReturnHelper.hasDataTosave())
                new SaveAsyncTask().execute();
            else
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(R.string.no_data_tosave),
                        Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class SaveAsyncTask extends AsyncTask<Void, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                emptyReturnHelper.saveEmptyReturn();
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                bmodel.saveModuleCompletion("MENU_EMPTY_RETURN", true);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
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

                //startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                //getActivity().finish();
            }

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
                if (QUANTITY.getId() == 1) {
                    String s = QUANTITY.getText().toString();

                    if (!s.isEmpty()) {
                        s = s.substring(0, s.length() - 1);

                        if (s.length() == 0) {
                            s = "0.0";
                        }
                    }

                    QUANTITY.setText(s);
                } else {

                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    QUANTITY.setText(String.valueOf(s));
                }


            } else {
                Button ed = ((Activity) context).findViewById(vw.getId());
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
        emptyReturnHelper.mSelectedFilter = id;
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

    private void onBackButonClick() {

        if (emptyReturnHelper.hasDataTosave()) {
            showAlert();
        } else {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                startActivityAndFinish(HomeScreenTwo.class);
            }
        }
    }

    private void showAlert() {
        CommonDialog dialog = new CommonDialog(getActivity(), getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                startActivityAndFinish(HomeScreenTwo.class);
            }
        }, getResources().getString(R.string.cancel), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }
}