package com.ivy.cpg.view.van;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import com.ivy.cpg.view.dashboard.olddashboard.DashBoardActivity;
import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.cpg.view.reports.ReportActivity;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.cpg.view.van.damagestock.DamageStockFragmentActivity;
import com.ivy.cpg.view.van.damagestock.DamageStockHelper;
import com.ivy.cpg.view.van.manualvanload.ManualVanLoadActivity;
import com.ivy.cpg.view.van.manualvanload.ManualVanLoadHelper;
import com.ivy.cpg.view.van.odameter.OdaMeterScreen;
import com.ivy.cpg.view.van.stockproposal.StockProposalScreen;
import com.ivy.cpg.view.van.stockview.StockViewActivity;
import com.ivy.cpg.view.van.vanstockapply.VanLoadStockApplyActivity;
import com.ivy.cpg.view.van.vanunload.VanUnLoadModuleHelper;
import com.ivy.cpg.view.van.vanunload.VanUnloadActivity;
import com.ivy.cpg.view.webview.WebViewActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.PlanningVisitActivity;
import com.ivy.utils.FontUtils;
import com.ivy.utils.NetworkUtils;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.Vector;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

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
    private static final String MENU_SURVEY01_SW = "MENU_SURVEY01_SW";


    private BusinessModel bmodel;
    private AlertDialog alertDialog;
    private View view;
    private boolean isClick = false;

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
                showMessage(getString(R.string.sessionout_loginagain));
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


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            showMessage(getString(R.string.sessionout_loginagain));
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


    private void gotoNextActivity(ConfigureBO menuItem) {

        switch (menuItem.getConfigCode()) {
            case MENU_STOCK_PROPOSAL:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();

                }
                break;

            case MENU_MANUAL_VAN_LOAD:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }
                break;

            case MENU_ODAMETER:
                if (!isClick) {
                    isClick = true;
                    navigateToActivity(menuItem.getMenuName(), menuItem.getConfigCode(), OdaMeterScreen.class);
                }
                break;

            case MENU_STOCK_VIEW:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }
                break;

            case MENU_VANLOAD_STOCK_VIEW:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }
                break;

            case MENU_VAN_UNLOAD:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }
                break;

            case MENU_VAN_PLANOGRAM:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }
                break;

            case MENU_DAMAGE_STOCK:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }
                break;

            case MENU_LOAD_WEBVIEW:
                if (!isClick) {
                    isClick = true;
                    if (NetworkUtils.isNetworkConnected(getActivity())) {
                        navigateToActivity(menuItem.getMenuName(), menuItem.getConfigCode(), WebViewActivity.class);
                    } else {
                        isClick = false;
                        showMessage(getString(R.string.please_connect_to_internet));
                    }
                }
                break;

            case MENU_PLANNING:

                if (!isClick) {
                    isClick = true;
                    if (bmodel.synchronizationHelper.isDayClosed()) {
                        showMessage(getString(R.string.day_closed));
                        isClick = false;
                    } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                        showMessage(getString(R.string.please_redownload));
                        isClick = false;
                    } else {
                        bmodel.distributorMasterHelper.downloadDistributorsList();
                        bmodel.configurationMasterHelper
                                .setTradecoveragetitle(menuItem.getMenuName());

                        navigateToActivity(menuItem.getMenuName(), menuItem.getConfigCode(), PlanningVisitActivity.class);
                    }
                }

                break;

            case MENU_TASK_REPORT:
                if (!isClick) {
                    isClick = true;
                    ConfigureBO configureBO = new ConfigureBO();
                    configureBO.setMenuName(menuItem.getMenuName());
                    configureBO.setConfigCode(MENU_TASK_REPORT);

                    Intent intent = new Intent(getActivity(), ReportActivity.class);
                    Bundle bun = new Bundle();
                    bun.putSerializable("config", configureBO);
                    intent.putExtras(bun);
                    startActivity(intent);
                }

                break;

            case MENU_DASH_DAY:
                if (!isClick) {
                    isClick = true;
                    navigateToActivity(menuItem.getMenuName(), menuItem.getConfigCode(), DashBoardActivity.class);
                }
                break;
            case MENU_SURVEY01_SW:
                if (!isClick) {
                    isClick = true;
                    new DownloadMethodsAsyncTask(getActivity(), downloadAsyncTaskInterface, menuItem.getConfigCode(), menuItem.getMenuName()).execute();
                }

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
            holder.menuBTN.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
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
            if (NetworkUtils.isNetworkConnected(getActivity())) {
                downloadVanloadData();
            } else {
                bmodel.showAlert(getString(R.string.no_network_connection), 0);
            }
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * download method call based on menu code wise
     */
    float distance = 0;
    DownloadAsyncTaskInterface downloadAsyncTaskInterface = new DownloadAsyncTaskInterface() {
        @Override
        public void showProgress(AlertDialog.Builder builder) {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        public void hideProgress() {
            dismissAlertDialog();
            isClick = false;
            showMessage(getString(R.string.unable_to_load_data));
        }

        @Override
        public void intentCall(String menuCode, String menuName) {
            dismissAlertDialog();
            isClick = false;
            switch (menuCode) {

                case MENU_STOCK_PROPOSAL:
                    if (bmodel.productHelper.getLoadMgmtProducts().size() > 0) {
                        navigateToActivity(menuName, menuCode, StockProposalScreen.class);
                    } else {
                        showMessage(getString(R.string.data_not_mapped));
                    }
                    break;

                case MENU_MANUAL_VAN_LOAD:
                    if (bmodel.productHelper.getLoadMgmtProducts().size() > 0) {
                        navigateToActivity(menuName, menuCode, ManualVanLoadActivity.class);
                    } else {
                        showMessage(getString(R.string.data_not_mapped));
                    }
                    break;

                case MENU_VANLOAD_STOCK_VIEW:
                    if (bmodel.stockreportmasterhelper.getStockReportMaster()!=null &&
                            bmodel.stockreportmasterhelper.getStockReportMaster().size() > 0) {
                        navigateToActivity(menuName, menuCode, VanLoadStockApplyActivity.class);
                    } else {
                        if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION
                                && bmodel.stockreportmasterhelper.getStockReportMaster() == null) {
                            if (distance == -1)
                                showToastMessage(-1);
                            else if (distance == -2 || distance == -3)
                                showToastMessage(distance);
                            else
                                showToastMessage(distance);
                        } else
                            showMessage(getString(R.string.data_not_mapped));
                    }
                    break;

                case MENU_VAN_UNLOAD:
                    if (bmodel.productHelper.getLoadMgmtProducts() != null &&
                            bmodel.productHelper.getLoadMgmtProducts().size() > 0) {
                        bmodel.getRetailerMasterBO().setDistributorId(0);
                        navigateToActivity(menuName, menuCode, VanUnloadActivity.class);
                    } else {
                        if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION
                                && bmodel.stockreportmasterhelper.getStockReportMaster() == null) {
                            if (distance == -1)
                                showToastMessage(-1);
                            else if (distance == -2 || distance == -3)
                                showToastMessage(distance);
                            else
                                showToastMessage(distance);
                        } else
                            showMessage(getString(R.string.data_not_mapped));
                    }
                    break;

                case MENU_STOCK_VIEW:
                    if (bmodel.productHelper.getLoadMgmtProducts().size() > 0) {
                        navigateToActivity(menuName, menuCode, StockViewActivity.class);
                    } else {
                        showMessage(getString(R.string.data_not_mapped));
                    }
                    break;

                case MENU_VAN_PLANOGRAM:
                    PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity().getApplicationContext());
                    if (mPlanoGramHelper.getmChildLevelBo() != null && mPlanoGramHelper.getmChildLevelBo().size() > 0) {
                        navigateToActivity(menuName, menuCode, PlanoGramActivity.class);
                    } else {
                        showMessage(getString(R.string.data_not_mapped));
                    }
                    break;

                case MENU_DAMAGE_STOCK:
                    if (DamageStockHelper.getInstance(getActivity().
                            getApplicationContext()).
                            getDamagedSalesReport().size() > 0) {
                        navigateToActivity(menuName, menuCode, DamageStockFragmentActivity.class);
                    } else {
                        showMessage(getString(R.string.data_not_mapped));
                    }
                    break;
                case MENU_SURVEY01_SW:
                    SurveyHelperNew surveyHelperNew=SurveyHelperNew.getInstance(getActivity().getApplicationContext());
                    if (surveyHelperNew.getSurvey() != null
                            && surveyHelperNew.getSurvey().size() > 0) {
                        bmodel.mSelectedActivityName = menuName;
                        bmodel.mSelectedActivityConfigCode = menuCode;
                        surveyHelperNew.loadSurveyConfig(MENU_SURVEY01_SW);
                        navigateToActivity(MENU_SURVEY01_SW, menuName, SurveyActivityNew.class);
                    } else {
                        showMessage(getString(R.string.data_not_mapped));
                    }
                    break;
                default:
                    break;
            }

        }

        @Override
        public void loadMethods(String menuCode, String menuName) {

            if (!menuCode.equals(MENU_VANLOAD_STOCK_VIEW)
                    || !menuCode.equals(MENU_DAMAGE_STOCK)) {
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_LOAD_MANAGEMENT"));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        bmodel.productHelper.getFilterProductLevels(),false));
            }

            if (menuCode.equals(MENU_VANLOAD_STOCK_VIEW)
                    || menuCode.equals(MENU_VAN_UNLOAD)) {
                if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION)
                    distance = bmodel.loadManagementHelper.checkIsAllowed(menuCode);
            }


            switch (menuCode) {
                case MENU_STOCK_PROPOSAL:
                    loadStockProposalData(menuCode);
                    break;

                case MENU_MANUAL_VAN_LOAD:
                    ManualVanLoadHelper.getInstance(getActivity().getApplicationContext()).loadManuvalVanLoadData(menuCode);
                    break;

                case MENU_VANLOAD_STOCK_VIEW:
                    if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                        if (distance > -1
                                && distance <= ConfigurationMasterHelper.vanDistance)
                            vanLoadSubRoutine();

                    } else {
                        vanLoadSubRoutine();
                    }
                    updateModuleWiseTimeStampDetails(menuCode);
                    break;

                case MENU_VAN_UNLOAD:
                    if (bmodel.configurationMasterHelper.SHOW_VANGPS_VALIDATION) {
                        if (distance > -1
                                && distance <= ConfigurationMasterHelper.vanDistance) {
                            bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                                    "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");

                            bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);
                        }
                    } else {
                        bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                                "MENU_LOAD_MANAGEMENT", "MENU_VAN_UNLOAD");

                        bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_VAN_UNLOAD), 2);
                    }
                    VanUnLoadModuleHelper unLoadModuleHelper = VanUnLoadModuleHelper.getInstance(getActivity().getApplicationContext());

                    // Non Salable return product product set object to reason wise
                    if (bmodel.configurationMasterHelper.SHOW_NON_SALABLE_UNLOAD)
                        unLoadModuleHelper.setNonSalableReturnProduct(bmodel.productHelper.getLoadMgmtProducts(), getActivity());

                    unLoadModuleHelper.getUnloadHistory(getActivity());

                    updateModuleWiseTimeStampDetails(menuCode);
                    break;

                case MENU_STOCK_VIEW:
                    bmodel.configurationMasterHelper
                            .loadStockUOMConfiguration();
                    bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                            "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");
                    //updateModuleWiseTimeStampDetails(menuCode);
                    break;

                case MENU_VAN_PLANOGRAM:
                    loadPlanogramData(menuName);
                    updateModuleWiseTimeStampDetails(menuCode);
                    break;

                case MENU_DAMAGE_STOCK:
                    DamageStockHelper.getInstance(getActivity().getApplicationContext()).loadDamagedProductReport(getActivity());
                    updateModuleWiseTimeStampDetails(menuCode);
                    break;
                case MENU_SURVEY01_SW:
                    loadSellerSurveyData(menuCode);
                    updateModuleWiseTimeStampDetails(menuCode);
                    break;
            }


        }
    };

    /**
     * Navigate to given activity Name
     *
     * @param menuName
     * @param menuCode
     * @param activityName
     */
    private void navigateToActivity(String menuName, String menuCode, Class activityName) {
        isClick = false;
        Intent intent = new Intent(getActivity(), activityName);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("screentitle", menuName);
        intent.putExtra("from", "1");
        intent.putExtra("isFromLodMgt", true);
        intent.putExtra("menuCode", menuCode);
        intent.putExtra("isPlanningSub", true);
        startActivity(intent);
    }

    /**
     * dismiss alert Dialog
     */
    private void dismissAlertDialog() {
        if (alertDialog.isShowing())
            alertDialog.dismiss();
    }

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
            strTitle = getString(
                    R.string.warehouse_location_mismatch);
        else if (distance == -2 || distance == -3)
            strTitle = getString(
                    R.string.not_able_to_find_user_location);
        else
            strTitle = getString(R.string.you_are) + " "
                    + distance + getString(R.string.mts_away);
        showMessage(strTitle);
    }

    /**
     * Download VanLoad Stock from server
     */
    private void downloadVanloadData() {
        downloadAsyncTaskInterface.showProgress(new AlertDialog.Builder(getActivity()));
        AppSchedulerProvider appSchedulerProvider = new AppSchedulerProvider();
        new CompositeDisposable().add(bmodel.loadManagementHelper.stockRefresh(getActivity())
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String errcode) {
                        updateStockRefresh(errcode);
                    }
                }));
    }

    private void updateStockRefresh(String errorCode) {
        dismissAlertDialog();
        if (errorCode != null && errorCode
                .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
            if (getActivity() != null)
                bmodel.showAlert(
                        getString(
                                R.string.stock_download_successfully), 0);
        }else{
            String errorDownloadMessage = bmodel.synchronizationHelper
                    .getErrormessageByErrorCode().get(errorCode);
            if(errorDownloadMessage!=null)
                showMessage(errorDownloadMessage);
            else if(errorCode !=null && errorCode.length()>0)
                showMessage(errorCode);

        }
    }


    /**
     * Download Planogram Data's
     *
     * @param menuName
     */
    private void loadPlanogramData(String menuName) {
        PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
        mPlanoGramHelper.loadConfigurations(getContext().getApplicationContext());
        mPlanoGramHelper.mSelectedActivityName = menuName;
        mPlanoGramHelper
                .downloadLevels(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM, "0");
        mPlanoGramHelper.downloadPlanoGram(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM);
        mPlanoGramHelper.downloadPlanoGramProductLocations(getContext().getApplicationContext(), MENU_VAN_PLANOGRAM, bmodel.getRetailerMasterBO().getRetailerID(), null);
        mPlanoGramHelper.loadPlanoGramInEditMode(getContext().getApplicationContext(), "0");
    }

    /**
     * Load Stock proposal Data's
     *
     * @param menuCode
     */
    private void loadStockProposalData(String menuCode) {

        bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(menuCode));
        bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                bmodel.productHelper.getFilterProductLevels(),false));

        bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                menuCode, menuCode);

        bmodel.updateProductUOM(menuCode, 2);
        bmodel.stockProposalModuleHelper.loadInitiative();
        bmodel.stockProposalModuleHelper.loadSBDData();
        bmodel.stockProposalModuleHelper.loadPurchased();
    }

    private void loadSellerSurveyData(String menuCode){

            SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
            surveyHelperNew.setFromHomeScreen(true);
            surveyHelperNew.downloadModuleId("SPECIAL");
            surveyHelperNew.downloadQuestionDetails(menuCode);

            if (bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY) {
                bmodel.productHelper.setFilterProductLevelsRex(bmodel.productHelper.downloadFilterLevel(menuCode));
                bmodel.productHelper.setFilterProductsByLevelIdRex(bmodel.productHelper.downloadFilterLevelProducts(
                        bmodel.productHelper.getRetailerModuleSequenceValues(),false));
            }
    }
}
