package com.ivy.cpg.view.van;

import android.Manifest;
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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.ivy.cpg.view.van.vanstockapply.VanLoadStockApplyActivity;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.DamageStockFragmentActivity;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.WebViewActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by hanifa.m on 4/19/2017.
 */

public class LoadManagementFragment extends IvyBaseFragment {


    private static final String MENU_STOCK_PROPOSAL = "MENU_STOCK_PROPOSAL";
    private static final String MENU_MANUAL_VAN_LOAD = "MENU_MANUAL_VAN_LOAD";
    private static final String MENU_ODAMETER = "MENU_ODAMETER";
    private static final String MENU_STOCK_VIEW = "MENU_STOCK_VIEW";
    private static final String MENU_VANLOAD_STOCK_VIEW = "MENU_VANLOAD_STOCK_VIEW";
    private static final String MENU_VAN_UNLOAD = "MENU_VAN_UNLOAD";
    private static final String MENU_VAN_PLANOGRAM = "MENU_VAN_PLANOGRAM";
    private static final String MENU_LOAD_WEBVIEW = "MENU_LOAD_WEBVIEW";


    private BusinessModel bmodel;

    private Intent vanloadintent;
    private Intent stockViewIntent;
    private Intent vanloadstockview;

    private AlertDialog alertDialog;

    private View view;

    private Loadmanagemntreceiver mLoadmanagementReceiver;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_load_management, container, false);


        try {

            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
                setScreenTitle(bmodel.configurationMasterHelper.getLoadmanagementtitle());

            }

            if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                    && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                ((HomeScreenActivity) getActivity()).checkAndRequestPermissionAtRunTime(3);
            }

            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }


            Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper
                    .downloadLoadManagementMenu();

            bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_LOAD_MANAGEMENT"));
            bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts("MENU_LOAD_MANAGEMENT",
                    bmodel.productHelper.getFilterProductLevels()));

            ListView listView = view.findViewById(R.id.listView1);
            listView.setCacheColorHint(0);
            listView.setAdapter(new MenuBaseAdapter(menuDB));

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        registerReceiver();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }

    }


    @Override
    public void onPause() {
        super.onPause();

        getActivity().unregisterReceiver(mLoadmanagementReceiver);

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (view != null)
            unbindDrawables(view.findViewById(R.id.root));
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


    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                Loadmanagemntreceiver.RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mLoadmanagementReceiver = new Loadmanagemntreceiver();
        getActivity().registerReceiver(mLoadmanagementReceiver, filter);
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
                    if (getActivity() != null)
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.stock_download_successfully), 0);

                } else {
                    String errorDownlodCode = bundle
                            .getString(SynchronizationHelper.ERROR_CODE);
                    String errorDownloadMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorDownlodCode);
                    if (errorDownloadMessage != null) {
                        Toast.makeText(getActivity(), errorDownloadMessage,
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

    private void gotoNextActivity(ConfigureBO menuItem) {

        switch (menuItem.getConfigCode()) {
            case MENU_STOCK_PROPOSAL:

                Intent stockProposalIntent = new Intent(getActivity(),
                        StockProposalScreen.class);
                stockProposalIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockProposalIntent.putExtra("screentitle", menuItem.getMenuName());
                stockProposalIntent.putExtra("isFromLodMgt", true);
                stockProposalIntent.putExtra("menuCode",menuItem.getConfigCode());
                startActivity(stockProposalIntent);
                break;
            case MENU_MANUAL_VAN_LOAD:

                vanloadintent = new Intent(getActivity(),
                        ManualVanLoadActivity.class);
                vanloadintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                vanloadintent.putExtra("screentitle", menuItem.getMenuName());
                bmodel.moduleTimeStampHelper.setTid("MTS" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));
                bmodel.moduleTimeStampHelper.setModuleCode(menuItem.getConfigCode());
                bmodel.moduleTimeStampHelper.saveModuleTimeStamp("In");
                new DownloadManualVanLoad().execute();

                break;
            case MENU_ODAMETER:

                Intent odameterintent = new Intent(getActivity(),
                        OdaMeterScreen.class);
                odameterintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                odameterintent.putExtra("screentitle", menuItem.getMenuName());
                startActivity(odameterintent);

                break;
            case MENU_STOCK_VIEW:
                bmodel.configurationMasterHelper
                        .loadStockUOMConfiguration();
                stockViewIntent = new Intent(getActivity(),
                        StockViewActivity.class);
                stockViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockViewIntent.putExtra("screentitle", menuItem.getMenuName());
                new DownloadCurrentStock().execute();

                break;
            case MENU_VANLOAD_STOCK_VIEW:

                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    checkIsAllowed(MENU_VANLOAD_STOCK_VIEW);
                } else {
                    vanLoadSubRoutine(menuItem.getMenuName());
                }

                break;
            case MENU_VAN_UNLOAD:
                bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);

                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    checkIsAllowed(
                            MENU_VAN_UNLOAD);
                } else {
                    vanUnLoadSubRoutine(menuItem.getMenuName());
                }

                break;
            case MENU_VAN_PLANOGRAM:
                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
                mPlanoGramHelper.loadConfigurations(getContext().getApplicationContext());
                mPlanoGramHelper.mSelectedActivityName = menuItem.getMenuName();
                mPlanoGramHelper
                        .downloadLevels(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM, "0");
                mPlanoGramHelper.downloadPlanoGram(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM);
                mPlanoGramHelper.downloadPlanoGramProductLocations(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM, bmodel.getRetailerMasterBO().getRetailerID(), null);
                mPlanoGramHelper.loadPlanoGramInEditMode(getContext().getApplicationContext(), "0");
                if (mPlanoGramHelper.getmChildLevelBo() != null && mPlanoGramHelper.getmChildLevelBo().size() > 0) {
                    Intent in = new Intent(getActivity(),
                            PlanoGramActivity.class);
                    in.putExtra("from", "1");
                    startActivity(in);
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case StandardListMasterConstants.MENU_DAMAGE_STOCK:
                SalesReturnHelper.getInstance(getActivity()).loadDamagedProductReport(getContext().getApplicationContext());
                Intent damagedSalesReturnIntent = new Intent(getActivity(),
                        DamageStockFragmentActivity.class);
                damagedSalesReturnIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                damagedSalesReturnIntent.putExtra("screentitle",
                        menuItem.getMenuName());
                startActivity(damagedSalesReturnIntent);
                break;

            case MENU_LOAD_WEBVIEW:
                if (bmodel.isOnline()) {
                    Intent i = new Intent(getActivity(), WebViewActivity.class);
                    i.putExtra("screentitle", menuItem.getMenuName());
                    i.putExtra("menucode", menuItem.getConfigCode());
                    startActivity(i);
                    getActivity().finish();
                } else
                    Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();
                break;

            default:
                break;
        }

    }

    private void vanUnLoadSubRoutine(String menuName) {
        Intent vanunload;
        vanunload = new Intent(getActivity(),
                VanUnloadActivity.class);
        vanunload.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanunload.putExtra("screentitle", menuName);
        bmodel.moduleTimeStampHelper.setTid("MTS" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));
        bmodel.moduleTimeStampHelper.setModuleCode(MENU_VAN_UNLOAD);
        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("In");
        startActivity(vanunload);

    }

    private void vanLoadSubRoutine(String menuName) {
        vanloadstockview = new Intent(getActivity(),
                VanLoadStockApplyActivity.class);
        vanloadstockview.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        vanloadstockview.putExtra("screentitle", menuName);
        new DownloadStockViewApply().execute();
    }

    public void checkIsAllowed(String menuString) {
        try {
            DBUtil db = new DBUtil(getActivity(),
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
        Toast.makeText(getActivity(), strTitle, Toast.LENGTH_SHORT)
                .show();
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

                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.custom_newui_list_item, parent, false);
                holder = new ViewHolder();

                holder.menuBTN = convertView
                        .findViewById(R.id.list_item_menu_tv_loadmgt);

                convertView.setOnClickListener(new View.OnClickListener() {

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
            holder.menuBTN.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.menuBTN.setText(configTemp.getMenuName());

            return convertView;
        }

        class ViewHolder {
            private ConfigureBO config;
            private TextView menuBTN;
        }
    }

    class DownloadManualVanLoad extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        //  private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
                    bmodel.loadManagementHelper.downloadSubDepots();
                }

                bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_MANUAL_VAN_LOAD");

                bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_MANUAL_VAN_LOAD), 2);

                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {

                    bmodel.productHelper.downlaodReturnableProducts("MENU_LOAD_MANAGEMENT");
                    bmodel.productHelper.downloadBomMaster();
                    bmodel.productHelper.downloadGenericProductID();
                    bmodel.loadManagementHelper.loadVanLoadReturnProductValidation();

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
        //   private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

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
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                bmodel.synchronizationHelper.updateAuthenticateToken(false);

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
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                downloadVanload();
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    class DownloadCurrentStock extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        //   private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_LOAD_MANAGEMENT"));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts("MENU_LOAD_MANAGEMENT",
                        bmodel.productHelper.getFilterProductLevels()));
                bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
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
            startActivity(stockViewIntent);
        }

    }



    public class Loadmanagemntreceiver extends BroadcastReceiver {
        public static final String RESPONSE = "com.ivy.intent.action.LoadManagement";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            updateReceiver(arg1);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_monthly_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_clear).setVisible(false);
        menu.findItem(R.id.menu_lock).setVisible(false);
        menu.findItem(R.id.menu_save_draft).setVisible(false);
        if (bmodel.configurationMasterHelper.SHOW_GCM_NOTIFICATION)
            menu.findItem(R.id.menu_refresh).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == R.id.menu_refresh) {
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

}
