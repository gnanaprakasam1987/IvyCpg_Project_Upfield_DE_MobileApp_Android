package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class EmptyReturnFragment extends IvyBaseFragment implements BrandDialogInterface {

    private BusinessModel bmodel;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ListView lv;
    private ArrayList<ProductMasterBO> mylist;
    private EditText QUANTITY;
    private String append = "";
    View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_empty_return,
                container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("Empty Return");
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
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

            menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
            menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
            menu.findItem(R.id.menu_location_filter).setVisible(!drawerOpen);

            menu.findItem(R.id.menu_location_filter).setVisible(false);
            menu.findItem(R.id.menu_spl_filter).setVisible(false);
            menu.findItem(R.id.menu_remarks).setVisible(false);

            if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                menu.findItem(R.id.menu_product_filter).setVisible(false);

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
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(
                bmodel.mSelectedActivityName);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setIcon(
                R.drawable.icon_stock);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                ((AppCompatActivity) getActivity()).getSupportActionBar()
                        .setTitle(bmodel.mSelectedActivityName);
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

        lv = (ListView) getView().findViewById(R.id.lvwplist);
        lv.setCacheColorHint(0);

        onLoadModule();

        super.onStart();

    }

    private void onLoadModule() {
        Vector<ProductMasterBO> items;

        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
            items = bmodel.mEmptyReturnHelper.getProductType();
        else
            items = bmodel.productHelper.getProductMaster();

        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        mylist = new ArrayList<>();

        for (ProductMasterBO sku : items) {
            if (sku.getIsReturnable() == 1) {
                if (bmodel.mEmptyReturnHelper.mSelectedFilter == sku.getParentid()
                        || bmodel.mEmptyReturnHelper.mSelectedFilter == -1) {
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
            super(getActivity(), R.layout.row_empty_return, items);
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

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());

                convertView = inflater.inflate(R.layout.row_empty_return, parent, false);

                holder.mSKU = (TextView) convertView.findViewById(R.id.sku);

                holder.mQty = (EditText) convertView.findViewById(R.id.qty);
                holder.mQty.setInputType(InputType.TYPE_NULL);

                holder.mQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        holder.mSKUBO.setRetPieceQty(Integer.parseInt(qty));
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

            holder.mSKU.setText(holder.mSKUBO.getProductName());
            holder.mQty.setText(holder.mSKUBO.getRetPieceQty() + "");

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
            if (bmodel.mEmptyReturnHelper.hasDataTosave())
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
                bmodel.mEmptyReturnHelper.saveEmptyReturn();
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                bmodel.updateIsVisitedFlag();
                bmodel.saveModuleCompletion("MENU_EMPTY_RETURN");

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

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
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

    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();

            if (frag != null)
                ft.detach(frag);

            Bundle bundle = new Bundle();
            bundle.putString("filterName", "Brand");
            bundle.putString("filterHeader", bmodel.productHelper
                    .getChildLevelBo().get(0).getProductLevel());
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getChildLevelBo());
            if (bmodel.productHelper.getParentLevelBo() != null
                    && bmodel.productHelper.getParentLevelBo().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getParentLevelBo());
            } else {
                bundle.putBoolean("isFormBrand", false);
                bundle.putString("isFrom", "STK");
            }

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
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

                    int s = SDUtil.convertToInt(QUANTITY.getText()
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
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        bmodel.mEmptyReturnHelper.mSelectedFilter = id;
        mDrawerLayout.closeDrawers();
        onLoadModule();

    }

    @Override
    public void updategeneraltext(String filtertext) {

    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {

    }
    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId,ArrayList<Integer>mAttributeProducts,String filtertext) {

    }
}