package com.ivy.cpg.view.van;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
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

import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.DamageStockFragmentActivity;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Vector;


public class LoadManagementScreen extends IvyBaseActivityNoActionBar {

    private static final String MENU_STOCK_PROPOSAL = "MENU_STOCK_PROPOSAL";
    private static final String MENU_MANUAL_VAN_LOAD = "MENU_MANUAL_VAN_LOAD";
    private static final String MENU_ODAMETER = "MENU_ODAMETER";
    private static final String MENU_STOCK_VIEW = "MENU_STOCK_VIEW";
    private static final String MENU_VANLOAD_STOCK_VIEW = "MENU_VANLOAD_STOCK_VIEW";
    private static final String MENU_VAN_UNLOAD = "MENU_VAN_UNLOAD";
    private static final String MENU_VAN_PLANOGRAM = "MENU_VAN_PLANOGRAM";


    private static final HashMap<String, Integer> menuIcons = new HashMap<>();


    private BusinessModel mBModel;

    private Intent vanLoadIntent;
    private Intent stockViewIntent;
    private Intent vanLoadStockView;
    private Intent currentStockViewBatchWiseIntent;

    private AlertDialog alertDialog;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        try {
            mBModel = (BusinessModel) getApplicationContext();
            mBModel.setContext(this);

            if (mBModel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                    && mBModel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                checkAndRequestPermissionAtRunTime(3);
            }

            try {
                LinearLayout bg = (LinearLayout) findViewById(R.id.root);
                File f = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + mBModel.userMasterHelper.getUserMasterBO()
                                .getUserid() + "APP");
                if (f.isDirectory()) {
                    File files[] = f.listFiles(new FilenameFilter() {
                        public boolean accept(File directory, String fileName) {
                            return fileName.startsWith("bg_menu");
                        }
                    });
                    for (File temp : files) {
                        Bitmap bitmapImage = BitmapFactory.decodeFile(temp
                                .getAbsolutePath());
                        Drawable bgrImage = new BitmapDrawable(this.getResources(), bitmapImage);
                        int sdk = android.os.Build.VERSION.SDK_INT;
                        if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                            bg.setBackgroundDrawable(bgrImage);
                        } else {
                            bg.setBackground(bgrImage);
                        }
                        break;
                    }

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }

            if (mBModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                Toast.makeText(
                        this,
                        getResources()
                                .getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show();
                finish();
            }


            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setTitle(
                        mBModel.configurationMasterHelper.getLoadmanagementtitle());
                getSupportActionBar().setIcon(R.drawable.icon_stock);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            menuIcons.put(MENU_STOCK_PROPOSAL, R.drawable.icon_order);
            menuIcons.put(MENU_MANUAL_VAN_LOAD, R.drawable.icon_vanload);
            menuIcons.put(MENU_ODAMETER, R.drawable.icon_odameter);
            menuIcons.put(MENU_STOCK_VIEW, R.drawable.icon_stock);
            menuIcons.put(MENU_VANLOAD_STOCK_VIEW, R.drawable.icon_stock);
            menuIcons.put(MENU_VAN_UNLOAD, R.drawable.icon_vanload);
            menuIcons.put(MENU_VAN_PLANOGRAM, R.drawable.icon_vanload);
            menuIcons.put(StandardListMasterConstants.MENU_DAMAGE_STOCK,
                    R.drawable.icon_stock);

            Vector<ConfigureBO> menuDB = mBModel.configurationMasterHelper
                    .downloadLoadManagementMenu();

            mBModel.productHelper
                    .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");


            ListView listView = (ListView) findViewById(R.id.listView1);
            listView.setCacheColorHint(0);
            listView.setAdapter(new MenuBaseAdapter(menuDB));

            registerReceiver();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    /**
     * This method will load only the configured menus. Stock Proposal and New
     * Retailer module are configured from backend.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        if (mBModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (mBModel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && mBModel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                mBModel.locationUtil.startLocationListener();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mBModel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && mBModel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                mBModel.locationUtil.stopLocationListener();
        }
    }

    private void gotoNextActivity(ConfigureBO menuItem) {
        Intent stockProposalIntent;
        Intent odometerIntent;
        Intent damagedSalesReturnIntent;

        switch (menuItem.getConfigCode()) {
            case MENU_STOCK_PROPOSAL:

                stockProposalIntent = new Intent(LoadManagementScreen.this,
                        StockProposalScreen.class);
                stockProposalIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockProposalIntent.putExtra("screentitle", menuItem.getMenuName());

                mBModel.productHelper.loadProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_STOCK_PROPOSAL");


                mBModel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_STOCK_PROPOSAL), 2);
                startActivity(stockProposalIntent);

                break;
            case MENU_MANUAL_VAN_LOAD:

                vanLoadIntent = new Intent(LoadManagementScreen.this,
                        ManualVanLoadActivity.class);
                vanLoadIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                vanLoadIntent.putExtra("screentitle", menuItem.getMenuName());

                new DownloadManualVanLoad().execute();

                break;
            case MENU_ODAMETER:

                odometerIntent = new Intent(LoadManagementScreen.this,
                        OdaMeterScreen.class);
                odometerIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                odometerIntent.putExtra("screentitle", menuItem.getMenuName());
                startActivity(odometerIntent);

                break;
            case MENU_STOCK_VIEW:
                mBModel.configurationMasterHelper.loadStockUOMConfiguration();
                stockViewIntent = new Intent(LoadManagementScreen.this,
                        StockViewActivity.class);
                stockViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockViewIntent.putExtra("screentitle", menuItem.getMenuName());
                new DownloadCurrentStock().execute();

                break;
            case MENU_VANLOAD_STOCK_VIEW:

                if (mBModel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    checkIsAllowed(
                            MENU_VANLOAD_STOCK_VIEW);
                } else {
                    vanLoadSubRoutine(menuItem.getMenuName());
                }

                break;
            case MENU_VAN_UNLOAD:
                mBModel.productHelper.loadProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");

                mBModel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);

                if (mBModel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    checkIsAllowed(
                            MENU_VAN_UNLOAD);
                } else {
                    vanUnLoadSubRoutine(menuItem.getMenuName());
                }

                break;
            case MENU_VAN_PLANOGRAM:
                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(this);
                mPlanoGramHelper.mSelectedActivityName = menuItem.getMenuName();
                mPlanoGramHelper.loadConfigurations(getApplicationContext());
                mPlanoGramHelper.downloadLevels(getApplicationContext(), MENU_VAN_PLANOGRAM, "0");
                mPlanoGramHelper.downloadPlanoGram(getApplicationContext(), MENU_VAN_PLANOGRAM);
                mPlanoGramHelper.downloadPlanoGramProductLocations(getApplicationContext(), MENU_VAN_PLANOGRAM, mBModel.getRetailerMasterBO().getRetailerID(), null);
                mPlanoGramHelper.loadPlanoGramInEditMode(getApplicationContext(), "0");
                if (mBModel.productHelper.getChildLevelBo() != null && mBModel.productHelper.getChildLevelBo().size() > 0) {
                    Intent in = new Intent(LoadManagementScreen.this,
                            PlanoGramActivity.class);
                    in.putExtra("from", "1");
                    startActivity(in);
                    finish();
                } else {
                    Toast.makeText(LoadManagementScreen.this,
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case StandardListMasterConstants.MENU_DAMAGE_STOCK:
                SalesReturnHelper.getInstance(this).loadDamagedProductReport(getApplicationContext());
                damagedSalesReturnIntent = new Intent(LoadManagementScreen.this,
                        DamageStockFragmentActivity.class);
                damagedSalesReturnIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                damagedSalesReturnIntent.putExtra("screentitle",
                        menuItem.getMenuName());
                startActivity(damagedSalesReturnIntent);
                break;
            case StandardListMasterConstants.MENU_CURRENT_STOCK_VIEW_BATCH:
                currentStockViewBatchWiseIntent = new Intent(
                        LoadManagementScreen.this,
                        CurrentStockBatchViewActivity.class);
                currentStockViewBatchWiseIntent
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                currentStockViewBatchWiseIntent.putExtra("screentitle",
                        menuItem.getMenuName());
                new DownloadCurrentStockBAtchWise().execute();
                break;
            default:
                break;
        }

    }

    private void vanUnLoadSubRoutine(String menuName) {
        Intent vanunload;
        vanunload = new Intent(LoadManagementScreen.this,
                VanUnloadActivity.class);
        vanunload.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanunload.putExtra("screentitle", menuName);
        startActivity(vanunload);

    }

    private void vanLoadSubRoutine(String menuName) {
        vanLoadStockView = new Intent(LoadManagementScreen.this,
                VanLoadStockView_activity.class);
        vanLoadStockView.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanLoadStockView.putExtra("screentitle", menuName);
        new DownloadStockViewApply().execute();
    }

    public void checkIsAllowed(String menuString) {
        try {
            DBUtil db = new DBUtil(LoadManagementScreen.this,
                    DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT latitude, longitude FROM WarehouseActivityMapping WHERE activity_code = "
                            + DatabaseUtils.sqlEscapeString(menuString));
            double wareLatitude = 0;
            double wareLongitude = 0;
            if (c != null) {
                if (c.moveToNext()) {
                    wareLatitude = c.getDouble(0);
                    wareLongitude = c.getDouble(1);
                }
                c.close();
            }
            db.closeDB();

            if (wareLatitude == 0 && wareLongitude == 0) {
                showToastMessage(-1);
            } else if (LocationUtil.latitude == 0
                    && LocationUtil.longitude == 0) {
                showToastMessage(-2);
            } else {

                float distance = LocationUtil.calculateDistance(wareLatitude,
                        wareLongitude);
                if (distance <= ConfigurationMasterHelper.vanDistance) {
                    if (MENU_VANLOAD_STOCK_VIEW.equals(menuString))
                        vanLoadSubRoutine(MENU_VANLOAD_STOCK_VIEW);
                    else if (MENU_VAN_UNLOAD.equals(menuString))
                        vanUnLoadSubRoutine(MENU_VAN_UNLOAD);
                } else {
                    showToastMessage(distance);
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    private void showToastMessage(float distance) {
        String strTitle;
        if (distance == -1)
            strTitle = getResources().getString(
                    R.string.warehouse_location_mismatch);
        else if (distance == -2)
            strTitle = getResources().getString(
                    R.string.not_able_to_find_user_location);
        else
            strTitle = getResources().getString(R.string.you_are) + " "
                    + distance + getResources().getString(R.string.mts_away);
        Toast.makeText(LoadManagementScreen.this, strTitle, Toast.LENGTH_SHORT)
                .show();
    }









    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view -view
     */
    private void unbindDrawables(View view) {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_monthly_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_clear).setVisible(false);
        menu.findItem(R.id.menu_lock).setVisible(false);
        menu.findItem(R.id.menu_save_draft).setVisible(false);
        if (mBModel.configurationMasterHelper.SHOW_GCM_NOTIFICATION)
            menu.findItem(R.id.menu_refresh).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(LoadManagementScreen.this,
                    HomeScreenActivity.class));
            finish();

            return true;
        } else if (i == R.id.menu_refresh) {
            if (mBModel.isOnline()) {
                new DownloadNewStock().execute();
            } else {
                mBModel.showAlert(
                        getResources()
                                .getString(R.string.no_network_connection), 0);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void registerReceiver() {
        Loadmanagemntreceiver mLoadmanagementReceiver;
        IntentFilter filter = new IntentFilter(
                Loadmanagemntreceiver.RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mLoadmanagementReceiver = new Loadmanagemntreceiver();
        registerReceiver(mLoadmanagementReceiver, filter);
    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);

        switch (method) {
            case SynchronizationHelper.VANLOAD_DOWNLOAD:
                if (errorCode != null && errorCode
                        .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    //	pd.dismiss();
                    alertDialog.dismiss();
                    mBModel.showAlert(
                            getResources().getString(
                                    R.string.stock_download_successfully), 0);

                } else {
                    String errorDownlodCode = bundle
                            .getString(SynchronizationHelper.ERROR_CODE);
                    String errorDownloadMessage = mBModel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorDownlodCode);
                    if (errorDownloadMessage != null) {
                        Toast.makeText(LoadManagementScreen.this, errorDownloadMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                    alertDialog.dismiss();
                    break;
                }
                break;
            default:
                break;
        }

    }

    private void downloadVanload() {
        mBModel.synchronizationHelper.downloadVanloadFromServer();
    }

    class MenuBaseAdapter extends BaseAdapter {

        Vector<ConfigureBO> items;

        MenuBaseAdapter(Vector<ConfigureBO> menuDB) {
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
            ConfigureBO configTemp = items.get(position);
            final ViewHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_item_menu, parent,
                        false);
                holder = new ViewHolder();
                holder.menuIcon = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = (TextView) convertView
                        .findViewById(R.id.list_item_menu_tv_new);

                holder.menuBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        gotoNextActivity(holder.config);
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.config = configTemp;
            holder.menuBTN.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.menuBTN.setText(configTemp.getMenuName());

            Integer i = menuIcons.get(configTemp.getConfigCode());
            if (i != null)
                holder.menuIcon.setImageResource(i);
            else
                holder.menuIcon.setImageResource(menuIcons
                        .get(MENU_STOCK_PROPOSAL));

            return convertView;
        }

        class ViewHolder {
            private ConfigureBO config;
            private ImageView menuIcon;
            private TextView menuBTN;
        }
    }

    class DownloadManualVanLoad extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(LoadManagementScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (mBModel.configurationMasterHelper.SHOW_SUBDEPOT) {
                    mBModel.vanmodulehelper.downloadSubDepots();
                }

                mBModel.productHelper.loadProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_MANUAL_VAN_LOAD");


                mBModel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_MANUAL_VAN_LOAD), 2);

                if (mBModel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

                    mBModel.productHelper.downlaodReturnableProducts("MENU_LOAD_MANAGEMENT");
                    mBModel.productHelper.downloadBomMaster();
                    mBModel.productHelper.downloadGenericProductID();
                    mBModel.vanmodulehelper.loadVanLoadReturnProductValidation();

                }

                OrderHeader ordHeadBO = new OrderHeader();
                mBModel.setOrderHeaderBO(ordHeadBO);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TO DO Auto-generated method stub

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            startActivity(vanLoadIntent);

        }

    }

    class DownloadStockViewApply extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(LoadManagementScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                mBModel.configurationMasterHelper.downloadSIHAppliedById();
                mBModel.stockreportmasterhelper.downloadStockReportMaster();
                mBModel.stockreportmasterhelper.downloadBatchwiseVanlod();
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TO DO Auto-generated method stub

        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            startActivity(vanLoadStockView);
        }

    }


    class DownloadNewStock extends AsyncTask<Integer, Integer, Integer> {

        private int downloadStatus = 0;
        private AlertDialog.Builder builder;


        protected void onPreExecute() {
            builder = new AlertDialog.Builder(LoadManagementScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                mBModel.synchronizationHelper.updateAuthenticateToken(false);

            } catch (Exception e) {
                Commons.printException("" + e);
                return downloadStatus;
            }
            return downloadStatus;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TO DO Auto-generated method stub

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (mBModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                downloadVanload();
            } else {
                String errorMsg = mBModel.synchronizationHelper.getErrormessageByErrorCode().get(mBModel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(LoadManagementScreen.this, errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LoadManagementScreen.this, getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class DownloadCurrentStock extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(LoadManagementScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                mBModel.productHelper
                        .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");


                mBModel.productHelper.loadProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_STOCK_VIEW");


            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TO DO Auto-generated method stub

        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            startActivity(stockViewIntent);
        }

    }

    class DownloadCurrentStockBAtchWise extends
            AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(LoadManagementScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                mBModel.productHelper
                        .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");


                mBModel.productHelper.loadProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");

            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TO DO Auto-generated method stub

        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            startActivity(currentStockViewBatchWiseIntent);

        }


    }

    public class Loadmanagemntreceiver extends BroadcastReceiver {
        public static final String RESPONSE = "com.ivy.intent.action.LoadManagement";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            updateReceiver(arg1);
        }

    }


}