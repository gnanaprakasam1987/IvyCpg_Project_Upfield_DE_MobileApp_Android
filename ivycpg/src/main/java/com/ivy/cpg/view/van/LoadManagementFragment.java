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
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.dashboard.olddashboard.DashBoardActivity;
import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.cpg.view.reports.ReportActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.van.manualvanload.ManualVanLoadActivity;
import com.ivy.cpg.view.van.manualvanload.ManualVanLoadHelper;
import com.ivy.cpg.view.van.vanstockapply.VanLoadStockApplyActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.DamageStockFragmentActivity;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.PlanningVisitActivity;
import com.ivy.sd.png.view.WebViewActivity;
import com.ivy.cpg.view.webview.WebViewActivity;

import java.util.Vector;

/**
 * Created by hanifa.m on 4/19/2017.
 */

public class LoadManagementFragment extends IvyBaseFragment {

    private final String MENU_STOCK_PROPOSAL = "MENU_STOCK_PROPOSAL";
    private final String MENU_MANUAL_VAN_LOAD = "MENU_MANUAL_VAN_LOAD";
    private final String MENU_ODAMETER = "MENU_ODAMETER";
    private final String MENU_STOCK_VIEW = "MENU_STOCK_VIEW";
    private final String MENU_VANLOAD_STOCK_VIEW = "MENU_VANLOAD_STOCK_VIEW";
    private final String MENU_VAN_UNLOAD = "MENU_VAN_UNLOAD";
    private final String MENU_VAN_PLANOGRAM = "MENU_VAN_PLANOGRAM";
    private final String MENU_LOAD_WEBVIEW = "MENU_LOAD_WEBVIEW";
    private final String MENU_PLANNING = "MENU_PLANNING";
    private final String MENU_TASK_REPORT = "MENU_TASK_REPORT";
    private final String MENU_DASH_DAY = "MENU_DASH_DAY";
    private final String MENU_DAMAGE_STOCK = "MENU_DAMAGE_STOCK";


    private BusinessModel bmodel;


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

                new DownloadMethods(getActivity(), downloadInterface, menuItem.getConfigCode(), menuItem.getMenuName());
                break;

            case MENU_ODAMETER:

                Intent odameterintent = new Intent(getActivity(),
                        OdaMeterScreen.class);
                odameterintent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                odameterintent.putExtra("screentitle", menuItem.getMenuName());
                startActivity(odameterintent);

                break;

            case MENU_STOCK_VIEW:

                new DownloadMethods(getActivity(), downloadInterface, menuItem.getConfigCode(), menuItem.getMenuName());
                break;

            case MENU_VANLOAD_STOCK_VIEW:

                new DownloadMethods(getActivity(), downloadInterface, menuItem.getConfigCode(), menuItem.getMenuName());
                break;

            case MENU_VAN_UNLOAD:

                new DownloadMethods(getActivity(), downloadInterface, menuItem.getConfigCode(), menuItem.getMenuName());
                break;

            case MENU_VAN_PLANOGRAM:

                new DownloadMethods(getActivity(), downloadInterface, menuItem.getConfigCode(), menuItem.getMenuName());
                break;

            case MENU_DAMAGE_STOCK:

                new DownloadMethods(getActivity(), downloadInterface, menuItem.getConfigCode(), menuItem.getMenuName());
                break;

            case MENU_LOAD_WEBVIEW:

                if (bmodel.isOnline()) {
                    Intent i = new Intent(getActivity(), WebViewActivity.class);
                    i.putExtra("screentitle", menuItem.getMenuName());
                    i.putExtra("menucode", menuItem.getConfigCode());
                    startActivity(i);
                } else
                    Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();
                break;

            case MENU_PLANNING:

                if (bmodel.synchronizationHelper.isDayClosed()) {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.day_closed),
                            Toast.LENGTH_SHORT).show();
                } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                    Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                    getResources().getString(R.string.please_redownload),
                            Toast.LENGTH_SHORT).show();
                } else {
                    bmodel.distributorMasterHelper.downloadDistributorsList();
                    bmodel.configurationMasterHelper
                            .setTradecoveragetitle(menuItem.getMenuName());
                    Intent i = new Intent(getActivity(),
                            PlanningVisitActivity.class);
                    i.putExtra("isPlanningSub", true);
                    startActivity(i);
                }

                break;

            case MENU_TASK_REPORT:

                ConfigureBO configureBO = new ConfigureBO();
                configureBO.setMenuName(menuItem.getMenuName());
                configureBO.setConfigCode(MENU_TASK_REPORT);

                Intent intent = new Intent(getActivity(), ReportActivity.class);
                Bundle bun = new Bundle();
                bun.putSerializable("config", configureBO);
                intent.putExtras(bun);
                startActivity(intent);

                break;

            case MENU_DASH_DAY:

                Intent i = new Intent(getActivity(),
                        DashBoardActivity.class);
                i.putExtra("screentitle", menuItem.getMenuName());
                startActivity(i);

                break;

            default:
                break;
        }

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
                new DownloadMethods(getActivity(), downloadInterface, "NewStock", "");
            } else {
                bmodel.showAlert(
                        getResources()
                                .getString(R.string.no_network_connection), 0);
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * download method call based on menu code wise
     */

    DownloadInterface downloadInterface = new DownloadInterface() {
        @Override
        public void showProgress(AlertDialog.Builder builder, String message) {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        public void hideProgress() {
            if (alertDialog.isShowing())
                alertDialog.dismiss();
            Toast.makeText(getActivity(),
                    getActivity().getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void intentCall(String menuCode, String menuName) {
            if (alertDialog.isShowing())
                alertDialog.dismiss();
            Intent intent = null;
            if (menuCode.equals(MENU_MANUAL_VAN_LOAD)) {
                intent = new Intent(getActivity(),
                        ManualVanLoadActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("screentitle", menuName);
                startActivity(intent);
            } else if (menuCode.equals(MENU_VANLOAD_STOCK_VIEW)) {
                intent = new Intent(getActivity(),
                        VanLoadStockApplyActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("screentitle", menuName);
                startActivity(intent);
            } else if (menuCode.equals(MENU_VAN_UNLOAD)) {
                intent = new Intent(getActivity(),
                        VanUnloadActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("screentitle", menuName);
                startActivity(intent);
            } else if (menuCode.equals(MENU_STOCK_VIEW)) {
                intent = new Intent(getActivity(),
                        StockViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("screentitle", menuName);
                startActivity(intent);
            } else if (menuCode.equals(MENU_VAN_PLANOGRAM)) {
                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
                if (mPlanoGramHelper.getmChildLevelBo() != null && mPlanoGramHelper.getmChildLevelBo().size() > 0) {
                    intent = new Intent(getActivity(),
                            PlanoGramActivity.class);
                    intent.putExtra("from", "1");
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                }
            } else if (menuCode.equals(MENU_DAMAGE_STOCK)) {
                intent = new Intent(getActivity(),
                        DamageStockFragmentActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("screentitle", menuName);
                startActivity(intent);
            } else if (menuCode.equals("NewStock")) {

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

        @Override
        public void loadMethods(String menuCode, String menuName) {

            if (!menuCode.equals("NewStock")
                    || !menuCode.equals(MENU_VANLOAD_STOCK_VIEW)
                    || !menuCode.equals(MENU_DAMAGE_STOCK)) {
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_LOAD_MANAGEMENT"));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts("MENU_LOAD_MANAGEMENT",
                        bmodel.productHelper.getFilterProductLevels()));
            }

            float distance = 0;
            if (menuCode.equals(MENU_VANLOAD_STOCK_VIEW)
                    || menuCode.equals(MENU_VAN_UNLOAD)) {
                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION)
                    distance = bmodel.loadManagementHelper.checkIsAllowed(menuCode);
            }


            if (menuCode.equals(MENU_MANUAL_VAN_LOAD)) {

                ManualVanLoadHelper.getInstance(getActivity().getApplicationContext()).loadManuvalVanLoadData(menuCode);

            } else if (menuCode.equals(MENU_VANLOAD_STOCK_VIEW)) {
                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    if (distance == -1)
                        showToastMessage(-1);
                    else if (distance == -2 || distance == -3)
                        showToastMessage(distance);
                    else if (distance <= ConfigurationMasterHelper.vanDistance)
                        vanLoadSubRoutine();
                    else
                        showToastMessage(distance);
                } else {
                    vanLoadSubRoutine();
                }
                updateModuleWiseTimeStampDetails(menuCode);
            } else if (menuCode.equals(MENU_VAN_UNLOAD)) {

                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                    if (distance == -1)
                        showToastMessage(-1);
                    else if (distance == -2 || distance == -3)
                        showToastMessage(distance);
                    else if (distance <= ConfigurationMasterHelper.vanDistance) {
                        bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                                "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");

                        bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);
                    } else
                        showToastMessage(distance);
                } else {
                    bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");

                    bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);
                }
                updateModuleWiseTimeStampDetails(menuCode);
            } else if (menuCode.equals(MENU_STOCK_VIEW)) {
                bmodel.configurationMasterHelper
                        .loadStockUOMConfiguration();
                bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");
                //updateModuleWiseTimeStampDetails(menuCode);
            } else if (menuCode.equals(MENU_VAN_PLANOGRAM)) {
                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
                mPlanoGramHelper.loadConfigurations(getContext().getApplicationContext());
                mPlanoGramHelper.mSelectedActivityName = menuName;
                mPlanoGramHelper
                        .downloadLevels(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM, "0");
                mPlanoGramHelper.downloadPlanoGram(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM);
                mPlanoGramHelper.downloadPlanoGramProductLocations(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM, bmodel.getRetailerMasterBO().getRetailerID(), null);
                mPlanoGramHelper.loadPlanoGramInEditMode(getContext().getApplicationContext(), "0");
                updateModuleWiseTimeStampDetails(menuCode);
            } else if (menuCode.equals(MENU_DAMAGE_STOCK)) {
                SalesReturnHelper.getInstance(getActivity()).loadDamagedProductReport(getContext().getApplicationContext());
                updateModuleWiseTimeStampDetails(menuCode);
            } else if (menuCode.equals("NewStock")) {
                try {

                    bmodel.synchronizationHelper.updateAuthenticateToken(false);

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }


        }
    };

    /**
     * insert module wise time stamp details
     *
     * @param menuCode
     */
    private void updateModuleWiseTimeStampDetails(String menuCode) {
        bmodel.moduleTimeStampHelper.setTid("MTS" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));
        bmodel.moduleTimeStampHelper.setModuleCode(menuCode);
        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("In");

    }

    /**
     * Prepare data for Manual Stock apply
     */
    private void vanLoadSubRoutine() {
        bmodel.configurationMasterHelper.downloadSIHAppliedById();
        bmodel.stockreportmasterhelper.downloadStockReportMaster();
        bmodel.stockreportmasterhelper.downloadBatchwiseVanlod();
    }

    /**
     * used to show toast msg based on given GPS distance value
     *
     * @param distance
     */
    private void showToastMessage(float distance) {
        String strTitle;
        if (distance == -1)
            strTitle = getResources().getString(
                    R.string.warehouse_location_mismatch);
        else if (distance == -2 || distance == -3)
            strTitle = getResources().getString(
                    R.string.not_able_to_find_user_location);
        else
            strTitle = getResources().getString(R.string.you_are) + " "
                    + distance + getResources().getString(R.string.mts_away);
        Toast.makeText(getActivity(), strTitle, Toast.LENGTH_SHORT)
                .show();
    }

    /**
     * Download VanLoad Stock from server
     */
    private void downloadVanload() {
        bmodel.synchronizationHelper.downloadVanloadFromServer();
    }


    public class Loadmanagemntreceiver extends BroadcastReceiver {
        public static final String RESPONSE = "com.ivy.intent.action.LoadManagement";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            updateReceiver(arg1);
        }

    }

}
