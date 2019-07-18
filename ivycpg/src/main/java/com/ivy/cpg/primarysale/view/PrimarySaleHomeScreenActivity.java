package com.ivy.cpg.primarysale.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;

import java.util.HashMap;
import java.util.Vector;

public class PrimarySaleHomeScreenActivity extends IvyBaseActivityNoActionBar {

    // Used to map icons
    private static final HashMap<String, Integer> menuIcons = new HashMap<>();
    public boolean isClicked;
    private BusinessModel bmodel;
    //add menu items in this obj
    private Vector<ConfigureBO> leftmenuDB = new Vector<>();
    private ListView listView;
    //menus of stockist order
    private String MENU_PS_INVSTATUS = "MENU_PS_INVSTATUS";
    private String MENU_PS_STKORD = "MENU_PS_STKORD";
    private String MENU_PS_STK = "MENU_PS_STK";
    private String MENU_PS_CLOSECALL = "MENU_PS_CLOSECALL";
    private TypedArray typearr;

    private Toolbar toolbar;

    @Override
    protected boolean onPrepareOptionsPanel(View view, Menu menu) {
        return super.onPrepareOptionsPanel(view, menu);
    }

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stockist_home);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        setSupportActionBar(toolbar);

        // Set title to toolbar

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setIcon(null);
        setScreenTitle(bmodel.distributorMasterHelper.getDistributor().getDName());


        getSupportActionBar().setIcon(null);
        // Used to hide the app logo icon from actionbar
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        // Hiding the old title
        listView = (ListView) findViewById(R.id.listView1);


        menuIcons.put(MENU_PS_INVSTATUS, R.drawable.icon_visit);
        menuIcons.put(MENU_PS_STKORD, R.drawable.icon_order);
        menuIcons.put(MENU_PS_STK, R.drawable.icon_new_retailer);
        menuIcons.put(MENU_PS_CLOSECALL, R.drawable.icon_stock);


        loadStockistMenu();


    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    /**
     * load menus of stockist order
     */
    private void loadStockistMenu() {

        for (ConfigureBO con : bmodel.configurationMasterHelper.getStockistMenu())
            if (con.getHasLink() == 1)
                leftmenuDB.add(con);


        listView.setCacheColorHint(0);
        listView.setAdapter(new LeftMenuBaseAdapter(leftmenuDB));

    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        supportInvalidateOptionsMenu();
        // bmodel.orderSplitHelper.setLast_split_master_index(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:

                startActivity(new Intent(PrimarySaleHomeScreenActivity.this,
                        PrimarySaleActivity.class));
                finish();
                return true;
        }
        return false;
    }

    private void gotoNextActivity(String menuName) {
        if (menuName.equals(MENU_PS_INVSTATUS)) {// invoice status
            if (!isClicked) {
                isClicked = true;
                bmodel.disInvoiceDetailsHelper.downloadInvoiceDetails(bmodel.distributorMasterHelper.getDistributor().getDId());
                if (bmodel.disInvoiceDetailsHelper.getInvoices() != null
                        && bmodel.disInvoiceDetailsHelper.getInvoices().size() != 0) {
                    bmodel.reasonHelper.downloadPrimSaleReasonList();
                    bmodel.distTimeStampHeaderHelper.saveTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menuName);
                    Intent i = new Intent(PrimarySaleHomeScreenActivity.this, InvoiceStatusActivity.class);
                    startActivityForResult(i, 998);

                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                    isClicked = false;
                }

            }
        } else if (menuName.equals(MENU_PS_STKORD)) // stock & order
        {
            if (!isClicked) {
                isClicked = true;
                new LoadDistributorStockAndOrder(menuName).execute();
            }

        } else if (menuName.equals(MENU_PS_STK)) // stock check
        {
            if (!isClicked) {
                isClicked = true;
                new LoadDistributorStockCheck(menuName).execute();


            }
        } else if (menuName.equals(MENU_PS_CLOSECALL)) // close call
        {

            String time = DateTimeUtils.now(DateTimeUtils.TIME);
            bmodel.distTimeStampHeaderHelper.updateTimeStamp(time);
            /*startActivity(new Intent(PrimarySaleHomeScreenActivity.this,
                    PrimarySaleActivity.class));*/
            finish();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 998)
            if (resultCode == RESULT_OK)
                isClicked = false;

            else if (resultCode == 0)
                isClicked = false;
    }

    class LeftMenuBaseAdapter extends BaseAdapter {

        Vector<ConfigureBO> items;
        ConfigureBO configTemp;

        public LeftMenuBaseAdapter(Vector<ConfigureBO> menuDB) {
            this.items = menuDB;
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
            configTemp = items.get(position);
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.activity_stockist_home_list_item, parent,
                        false);
                holder = new ViewHolder();

                holder.iconIV = (ImageView) convertView.findViewById(R.id.list_item_icon_iv);
                holder.icon_ll = (LinearLayout) convertView.findViewById(R.id.icon_ll);
                holder.img_arrow = (ImageView) convertView.findViewById(R.id.img_arrow);
                holder.activityname = (TextView) convertView.findViewById(R.id.activityName);
                holder.activityname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        bmodel.mSelectedActivityName = holder.config.getMenuName();
                        gotoNextActivity(holder.config.getConfigCode());
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.config = configTemp;
            holder.activityname.setText(holder.config.getMenuName());
            Integer i = menuIcons.get(holder.config.getConfigCode());
            if (i != null)
                holder.iconIV.setImageResource(i);
            else
                holder.iconIV.setImageResource(menuIcons.get(MENU_PS_STK));
            if (holder.config.isDone()) {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_completed);
                holder.iconIV.setColorFilter(Color.argb(255, 255, 255, 255));

            } else {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.iconIV.setColorFilter(Color.argb(0, 0, 0, 0));
            }


            return convertView;
        }

        class ViewHolder {
            ConfigureBO config;
            private ImageView iconIV, img_arrow;
            private TextView activityname;
            private LinearLayout icon_ll;
        }
    }

    private class LoadDistributorStockCheck extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        private String menuName;

        public LoadDistributorStockCheck(String menuName) {
            this.menuName = menuName;
        }

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(PrimarySaleHomeScreenActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                // Download Products
                bmodel.productHelper
                        .downloadDistributorProducts(menuName);
                //Download Filter Levels and Products
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(menuName));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        bmodel.productHelper.getFilterProductLevels(),true));

                bmodel.configurationMasterHelper.loadPrimarySaleStockCheckAndOrderConfiguration();
                /** Load the stock check if opened in edit mode. **/
                bmodel.distributorMasterHelper.setIsEditDistributorStockCheck(false);
                if (bmodel.distributorMasterHelper.hasAlreadyDistributorStockChecked(bmodel.distributorMasterHelper.getDistributor().getDId())) {
                    bmodel.distributorMasterHelper.setIsEditDistributorStockCheck(true);
                    bmodel.distributorMasterHelper.loadDistributedStockCheckedProducts(bmodel.distributorMasterHelper.getDistributor().getDId());
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            return true;
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();

            if (bmodel.productHelper.getProductMaster() == null || bmodel.productHelper.getProductMaster().size() == 0) {
                bmodel.showAlert(
                        getResources().getString(R.string.data_not_mapped),
                        0);
                isClicked = false;
                return;

            } else {
                bmodel.distTimeStampHeaderHelper.saveTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menuName);

                Intent i = new Intent(PrimarySaleHomeScreenActivity.this,
                        PrimarySaleStockCheckFragmentActivity.class);
                startActivityForResult(i, 998);
            }

        }
    }

    private class LoadDistributorStockAndOrder extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        private String menuName;

        public LoadDistributorStockAndOrder(String menuName) {
            this.menuName = menuName;
        }

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(PrimarySaleHomeScreenActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                // Download Products
                bmodel.productHelper
                        .downloadDistributorProducts(menuName);
                //Download Filter Levels and Products
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(menuName));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        bmodel.productHelper.getFilterProductLevels(),true));

                bmodel.configurationMasterHelper.loadPrimarySaleStockCheckAndOrderConfiguration();

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(menuName), 1);

                /** Load the stock check if opened in edit mode. **/
                bmodel.distributorMasterHelper.setIsEditDistributorStockCheck(false);
                if (bmodel.configurationMasterHelper.SHOW_DIST_STOCK && bmodel.distributorMasterHelper.hasAlreadyDistributorStockChecked(bmodel.distributorMasterHelper.getDistributor().getDId())) {
                    bmodel.distributorMasterHelper.setIsEditDistributorStockCheck(true);
                    bmodel.distributorMasterHelper.loadDistributedStockCheckedProducts(bmodel.distributorMasterHelper.getDistributor().getDId());
                }

                bmodel.distributorMasterHelper.setIsEditDistributorOrder(false);
                if (bmodel.distributorMasterHelper.hasAlreadyDistributorOrdered(bmodel.distributorMasterHelper.getDistributor().getDId())) {
                    bmodel.distributorMasterHelper.setIsEditDistributorOrder(true);
                    bmodel.distributorMasterHelper.loadDistributedOrderedProducts(bmodel.distributorMasterHelper.getDistributor().getDId(), null);
                } else {
                    bmodel.setOrderHeaderBO(null);
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            return true;
        }


        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            if (bmodel.productHelper.getProductMaster() == null || bmodel.productHelper.getProductMaster().size() == 0) {
                bmodel.showAlert(
                        getResources().getString(R.string.data_not_mapped),
                        0);
                isClicked = false;
                return;
            } else {
                bmodel.distTimeStampHeaderHelper.saveTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                        DateTimeUtils.now(DateTimeUtils.TIME), menuName);
                if (bmodel.distributorMasterHelper.isEditDistributorOrder()) {
                    Intent i = new Intent(PrimarySaleHomeScreenActivity.this,
                            PrimarySaleOrderSummaryActivity.class);
                    startActivityForResult(i, 998);
                } else {
                    Intent i = new Intent(PrimarySaleHomeScreenActivity.this,
                            PrimarySaleStockAndOrderFragmentActivity.class);
                    startActivityForResult(i, 998);
                }
            }
        }
    }
}