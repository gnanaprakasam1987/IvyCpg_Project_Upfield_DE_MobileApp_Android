package com.ivy.sd.png.view.van;

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

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.PlanogramMasterHelper;
import com.ivy.sd.png.provider.SalesReturnHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.DamageStockFragmentActivity;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.PlanogramActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Vector;



public class LoadManagementScreen extends IvyBaseActivityNoActionBar {
    private static final String OUR_INTENT_ACTION = "com.ivy.sd.png.view.van.LoadManagementScreen.RECVR";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private static final String ACTION_SOFTSCANTRIGGER = "com.motorolasolutions.emdk.datawedge.api.ACTION_SOFTSCANTRIGGER";
    private static final String EXTRA_PARAM = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
    private static final String DWAPI_TOGGLE_SCANNING = "TOGGLE_SCANNING";
    private static final HashMap<String, Integer> menuIcons = new HashMap<>();
    private static final String MENU_STOCK_PROPOSAL = "MENU_STOCK_PROPOSAL";
    private static final String MENU_MANUAL_VAN_LOAD = "MENU_MANUAL_VAN_LOAD";
    private static final String MENU_ODAMETER = "MENU_ODAMETER";
    private static final String MENU_STOCK_VIEW = "MENU_STOCK_VIEW";
    private static final String MENU_VANLOAD_STOCK_VIEW = "MENU_VANLOAD_STOCK_VIEW";
    private static final String MENU_VAN_UNLOAD = "MENU_VAN_UNLOAD";
    private static final String MENU_VAN_PLANOGRAM = "MENU_VAN_PLANOGRAM";
    private BusinessModel bmodel;
    private Intent vanloadintent;
    private Intent stockViewIntent;
    private Intent vanloadstockview;
    private Intent currenStockViewBatchWiseIntent;
    private TextView mSelectedListBTN;
    private String mSelectedBarCodemodule;
    private AlertDialog alertDialog;
    private Toolbar toolbar;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_load_management);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        try {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);

            if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                    && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                checkAndRequestPermissionAtRunTime(3);
            }

            try {
                LinearLayout bg = (LinearLayout) findViewById(R.id.root);
                File f = new File(
                        getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
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

            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
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
                        bmodel.configurationMasterHelper.getLoadmanagementtitle());
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

            Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper
                    .downloadLoadManagementMenu();
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");
            } else {
                bmodel.productHelper
                        .downloadProductFilter("MENU_LOAD_MANAGEMENT");
            }

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
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    private void gotoNextActivity(ConfigureBO menuItem) {
        Intent stockpropintent;
        Intent odameterintent;
        Intent damagedSalesReturnIntent;

        switch (menuItem.getConfigCode()) {
            case MENU_STOCK_PROPOSAL:

                stockpropintent = new Intent(LoadManagementScreen.this,
                        StockProposalScreen.class);
                stockpropintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockpropintent.putExtra("screentitle", menuItem.getMenuName());
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_STOCK_PROPOSAL");
                else
                    bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                            "MENU_STOCK_PROPOSAL");


                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_STOCK_PROPOSAL), 2);
                startActivity(stockpropintent);
                break;
            case MENU_MANUAL_VAN_LOAD:

                vanloadintent = new Intent(LoadManagementScreen.this,
                        ManualVanLoadActivity.class);
                vanloadintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                vanloadintent.putExtra("screentitle", menuItem.getMenuName());

                new DownloadManualVanLoad().execute();

                break;
            case MENU_ODAMETER:

                    odameterintent = new Intent(LoadManagementScreen.this,
                            OdaMeterScreen.class);
                    odameterintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    odameterintent.putExtra("screentitle", menuItem.getMenuName());
                    startActivity(odameterintent);

                break;
            case MENU_STOCK_VIEW:

                stockViewIntent = new Intent(LoadManagementScreen.this,
                        StockViewActivity.class);
                stockViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockViewIntent.putExtra("screentitle", menuItem.getMenuName());
                new DownloadCurrentStock().execute();

                break;
            case MENU_VANLOAD_STOCK_VIEW:

                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    checkIsAllowed(
                            MENU_VANLOAD_STOCK_VIEW,
                            bmodel.configurationMasterHelper.SHOW_VANBARCODE_VALIDATION);
                } else if (bmodel.configurationMasterHelper.SHOW_VANBARCODE_VALIDATION) {
                    checkBarCode(MENU_VANLOAD_STOCK_VIEW);
                } else {
                    vanLoadSubRoutine(menuItem.getMenuName());
                }

                break;
            case MENU_VAN_UNLOAD:
                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");
                else
                    bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                            "MENU_VAN_UNLOAD");

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);

                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    checkIsAllowed(
                            MENU_VAN_UNLOAD,
                            bmodel.configurationMasterHelper.SHOW_VANBARCODE_VALIDATION);
                } else if (bmodel.configurationMasterHelper.SHOW_VANBARCODE_VALIDATION) {
                    checkBarCode(MENU_VAN_UNLOAD);
                } else {
                    vanUnLoadSubRoutine(menuItem.getMenuName());
                }

                break;
            case MENU_VAN_PLANOGRAM:
                bmodel.mSelectedActivityName = menuItem.getMenuName();
                PlanogramMasterHelper mPlanoGramMasterHelper = PlanogramMasterHelper.getInstance(this);
                mPlanoGramMasterHelper
                        .downloadlevels(MENU_VAN_PLANOGRAM, "0");
                mPlanoGramMasterHelper.downloadPlanogram(MENU_VAN_PLANOGRAM
                        , false, false, false, 0, 0);
                bmodel.productHelper.downloadPlanogramProdutLocations(MENU_VAN_PLANOGRAM, bmodel.getRetailerMasterBO().getRetailerID(), null);
                mPlanoGramMasterHelper.loadPlanoGramInEditMode("0");
                if (bmodel.productHelper.getChildLevelBo() != null && bmodel.productHelper.getChildLevelBo().size() > 0) {
                    Intent in = new Intent(LoadManagementScreen.this,
                            PlanogramActivity.class);
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
                SalesReturnHelper.getInstance(this).loadDamagedProductReport();
                damagedSalesReturnIntent = new Intent(LoadManagementScreen.this,
                        DamageStockFragmentActivity.class);
                damagedSalesReturnIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                damagedSalesReturnIntent.putExtra("screentitle",
                        menuItem.getMenuName());
                startActivity(damagedSalesReturnIntent);
                break;
            case StandardListMasterConstants.MENU_CURRENT_STOCK_VIEW_BATCH:
                currenStockViewBatchWiseIntent = new Intent(
                        LoadManagementScreen.this,
                        CurrentStockBatchViewActivity.class);
                currenStockViewBatchWiseIntent
                        .setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                currenStockViewBatchWiseIntent.putExtra("screentitle",
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
        vanloadstockview = new Intent(LoadManagementScreen.this,
                VanLoadStockView_activity.class);
        vanloadstockview.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanloadstockview.putExtra("screentitle", menuName);
        new DownloadStockViewApply().execute();
    }

    public void checkIsAllowed(String menuString, boolean isValidateBarCode) {
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
                    if (isValidateBarCode)
                        checkBarCode(menuString);
                    else if (MENU_VANLOAD_STOCK_VIEW.equals(menuString))
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

    public void checkBarCode(String menuString) {
        mSelectedBarCodemodule = menuString;

        Intent i = new Intent();
        i.setAction(ACTION_SOFTSCANTRIGGER);
        i.putExtra(EXTRA_PARAM, DWAPI_TOGGLE_SCANNING);
        LoadManagementScreen.this.sendBroadcast(i);
    }

    @Override
    public void onNewIntent(Intent i) {
        try {
            if (i != null && mSelectedBarCodemodule != null) {
                checkBarcodeData(i);
                mSelectedBarCodemodule = null;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void checkBarcodeData(Intent i) {
        String mScannedData, mBarCode = "";
        if (i.getAction().contentEquals(OUR_INTENT_ACTION)) {
            mScannedData = i.getStringExtra(DATA_STRING_TAG);
            if (mScannedData == null)
                mScannedData = "";

            DBUtil db = new DBUtil(LoadManagementScreen.this,
                    DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT barcode FROM WarehouseActivityMapping WHERE activity_code = "
                            + DatabaseUtils
                            .sqlEscapeString(mSelectedBarCodemodule));

            if (c != null) {
                if (c.moveToNext()) {
                    mBarCode = c.getString(0);
                }
                c.close();
            }
            db.closeDB();

            if ("".equals(mBarCode))
                showToastMessageForBarcode(-1);
            else if ("".equals(mScannedData))
                showToastMessageForBarcode(-2);
            else if (mScannedData.equals(mBarCode)) {
                if (MENU_VANLOAD_STOCK_VIEW.equals(mSelectedBarCodemodule))
                    vanLoadSubRoutine(MENU_VANLOAD_STOCK_VIEW);
                else if (MENU_VAN_UNLOAD.equals(mSelectedBarCodemodule))
                    vanUnLoadSubRoutine(MENU_VAN_UNLOAD);
            } else
                showToastMessageForBarcode(-3);
        }
    }

    private void showToastMessageForBarcode(int status) {
        String strTitle = "";
        if (status == -1)
            strTitle = getResources().getString(
                    R.string.warehouse_barcode_not_assigned);
        else if (status == -2)
            strTitle = getResources().getString(
                    R.string.not_able_to_scan_barcode);
        else if (status == -3)
            strTitle = getResources().getString(R.string.barcode_not_matched);

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
        if (bmodel.configurationMasterHelper.SHOW_GCM_NOTIFICATION)
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
            if (bmodel.isOnline()) {
                new DownloadNewStock().execute();
            } else {
                bmodel.showAlert(
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
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.stock_download_successfully), 0);

                } else {
                    String errorDownlodCode = bundle
                            .getString(SynchronizationHelper.ERROR_CODE);
                    String errorDownloadMessage = bmodel.synchronizationHelper
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
        bmodel.synchronizationHelper.downloadVanloadFromServer();
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
                        if (mSelectedListBTN != null)
                            mSelectedListBTN.setSelected(false);

                     //   mSelectedListBTN = holder.menuBTN;
                     //   mSelectedListBTN.setSelected(true);

                        gotoNextActivity(holder.config);
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.config = configTemp;
            holder.menuBTN.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
                    bmodel.vanmodulehelper.downloadSubDepots();
                }

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_MANUAL_VAN_LOAD");
                else
                    bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                            "MENU_MANUAL_VAN_LOAD");

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_MANUAL_VAN_LOAD), 2);

                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

                    bmodel.productHelper.downlaodReturnableProducts("MENU_LOAD_MANAGEMENT");
                    bmodel.productHelper.downloadBomMaster();
                    bmodel.productHelper.downloadGenericProductID();
                    bmodel.vanmodulehelper.loadVanLoadReturnProductValidation();

                }

                OrderHeader ordHeadBO = new OrderHeader();
                bmodel.setOrderHeaderBO(ordHeadBO);
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
            startActivity(vanloadintent);

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
                bmodel.configurationMasterHelper.downloadSIHAppliedById();
                bmodel.stockreportmasterhelper.downloadStockReportMaster();
                bmodel.stockreportmasterhelper.downloadBatchwiseVanlod();
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
            startActivity(vanloadstockview);
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
                bmodel.synchronizationHelper.updateAuthenticateToken();

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
            downloadVanload();
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

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");
                } else {
                    bmodel.productHelper
                            .downloadProductFilter("MENU_LOAD_MANAGEMENT");
                }

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_STOCK_VIEW");
                else
                    bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                            "MENU_STOCK_VIEW");

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

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");
                } else {
                    bmodel.productHelper
                            .downloadProductFilter("MENU_LOAD_MANAGEMENT");
                }

                if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                    bmodel.productHelper.loadProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");
                else
                    bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                            "MENU_CUR_STK_BATCH");

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
            startActivity(currenStockViewBatchWiseIntent);

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