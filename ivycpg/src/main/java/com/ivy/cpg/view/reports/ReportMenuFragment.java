package com.ivy.cpg.view.reports;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.reports.performancereport.OutletPerfomanceHelper;
import com.ivy.cpg.view.reports.soho.SalesReturnReportHelperSOHO;
import com.ivy.cpg.view.van.stockview.StockViewActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;


public class ReportMenuFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private static final HashMap<String, Integer> menuIcons = new HashMap<>();
    private ReportHelper reportHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_menu, container, false);

        try {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());
            reportHelper = ReportHelper.getInstance(getActivity());

            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (actionBar != null) {
                actionBar.setTitle(null);
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setIcon(null);
                actionBar.setElevation(0);
            }

            // Set screen title.
            setScreenTitle(bmodel.configurationMasterHelper.getTradecoveragetitle());

            if (getArguments().getString("screentitle") != null)
                setScreenTitle(getArguments().getString("screentitle"));
            else
                setScreenTitle(getResources().getString(R.string.report));


            setScreenBackground();


            menuIcons.put(StandardListMasterConstants.MENU_ORDER_REPORT,
                    R.drawable.icon_stock);
            menuIcons.put(StandardListMasterConstants.MENU_DAY_REPORT,
                    R.drawable.icon_new_retailer);
            menuIcons.put(StandardListMasterConstants.MENU_INVOICE_REPORT,
                    R.drawable.icon_dash);
            menuIcons.put(StandardListMasterConstants.MENU_PND_INVOICE_REPORT,
                    R.drawable.icon_dash);
            menuIcons.put(StandardListMasterConstants.MENU_SKU_REPORT,
                    R.drawable.icon_monthly_plan);
            menuIcons.put(StandardListMasterConstants.MENU_CURRENT_STOCK_REPORT,
                    R.drawable.icon_review_plan);

            menuIcons.put(StandardListMasterConstants.MENU_COLLECTION_REPORT,
                    R.drawable.icon_sbd);
            menuIcons.put(StandardListMasterConstants.MENU_CREDIT_NOTE_REPORT,
                    R.drawable.icon_sbd);
            menuIcons.put(StandardListMasterConstants.MENU_TASK_EXECUTION_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_EOD_STOCK_REPORT,
                    R.drawable.icon_stock_check);
            menuIcons.put(StandardListMasterConstants.MENU_TASK_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_QUESTION_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_DYN_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_LOG,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_SELLER_PERFOMANCE_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_ARCHV_RPT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_INV_SALES_RETURN_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_DELIVERY_RPT,
                    R.drawable.icon_reports);


            Vector<ConfigureBO> menuDB = bmodel.configurationMasterHelper
                    .downloadNewActivityMenu(StandardListMasterConstants.REPORT_MENU);


            ListView listView = view.findViewById(R.id.listView1);
            listView.setCacheColorHint(0);
            listView.setAdapter(new MenuBaseAdapter(menuDB));

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return view;
    }

    private void setScreenBackground() {
        try {
            LinearLayout bg = getActivity().findViewById(R.id.root);
            File f = new File(
                    getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
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
    }


    /**
     * @param config - ConfigureBO object
     *               move to corresponding fragment based on the configCode
     *               check BusinessModel object for valid data then only move to corresponding Screen
     *               else show corresponding error message
     */
    protected void gotoSelectedFragement(ConfigureBO config) {
        switch (config.getConfigCode()) {
            case StandardListMasterConstants.MENU_INVOICE_REPORT:
                if (reportHelper.downloadInvoicereport().size() >= 1) {
                    gotoReportActivity(config);
                } else {
                    showToast();
                }
                break;
            case StandardListMasterConstants.MENU_PND_INVOICE_REPORT:
                new UpdatePendingInvoice(config).execute();
                break;
            case StandardListMasterConstants.MENU_ORDER_REPORT:

                if (reportHelper.downloadOrderreport().size() >= 1) {
                    gotoReportActivity(config);
                } else {
                    showToast();
                }
                break;
            case StandardListMasterConstants.MENU_CURRENT_STOCK_REPORT:

                new DownloadCurrentStock(config.getMenuName()).execute();

                break;
            case StandardListMasterConstants.MENU_DAY_REPORT:

                if (bmodel.configurationMasterHelper.downloadDayReportList().size() >= 1) {
                    gotoReportActivity(config);
                } else {
                    showToast();
                }
                break;
            case StandardListMasterConstants.MENU_SELLER_PERFOMANCE_REPORT:
                OutletPerfomanceHelper perfomanceHelper = OutletPerfomanceHelper.getInstance(getActivity());
                if (perfomanceHelper.isPerformReport()) {
                    gotoReportActivity(config);
                } else {
                    String Url = perfomanceHelper.getPerformRptUrl();
                    if (Url != null && Url.length() > 0) {
                        new PerformRptDownloadData(config, Url, perfomanceHelper).execute();
                    } else {
                        Toast.makeText(getActivity(), "Download Url Not Available", Toast.LENGTH_LONG).show();
                    }
                }

                break;
            case StandardListMasterConstants.MENU_SALES_REPORT:
                if ((new SalesReturnReportHelperSOHO(getContext())).getSalesReturnRetailerList().size() >= 1) {
                    gotoReportActivity(config);
                } else {
                    showToast();
                }
                break;
            case StandardListMasterConstants.MENU_ARCHV_RPT:
                if (bmodel.isOnline()) {
                    gotoReportActivity(config);
                } else
                    showToast();
                break;

            case StandardListMasterConstants.MENU_INV_SALES_RETURN_REPORT:
                if (bmodel.isOnline()) {
                    gotoReportActivity(config);
                } else
                    showToast();
                break;
            case StandardListMasterConstants.MENU_COLLECTION_REPORT:
                if (reportHelper.hasPayment()) {
                    gotoReportActivity(config);
                } else
                    showToast();
                break;

            case StandardListMasterConstants.MENU_SKU_REPORT:
                if(reportHelper.hasOrder())
                    gotoReportActivity(config);
                else
                    showToast();
                break;

            case StandardListMasterConstants.MENU_ASSET_SERVICE_REQ_RPT:
                if(reportHelper.hasAssetServiceRequest())
                    gotoReportActivity(config);
                else
                    showToast();
                break;
            default:
                gotoReportActivity(config);
                break;
        }

    }

    private void showToast() {
        Toast.makeText(getActivity(), R.string.data_not_mapped, Toast.LENGTH_LONG).show();
    }

    /**
     * @param config - ConfigureBO object
     *               start the Report Activity with corresponding Config object
     */
    private void gotoReportActivity(ConfigureBO config) {
        Intent intent = new Intent(getActivity(), ReportActivity.class);
        Bundle bun = new Bundle();
        bun.putSerializable("config", config);
        bun.putString("FROM", "REPORT");
        intent.putExtras(bun);
        startActivity(intent);
    }


    /**
     * Prepare the adapter for show the menu Items
     * ViewHolder Pattern:   object hold the views for future reference instead of creating new views every time
     * prepare the view for menus
     * Move to corresponding screen based on list item click
     */

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
                convertView = inflater.inflate(R.layout.custom_newui_list_item, parent,
                        false);
                holder = new ViewHolder();
                holder.menuIcon = convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = convertView
                        .findViewById(R.id.list_item_menu_tv_loadmgt);


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.config = configTemp;

            holder.menuBTN.setText(configTemp.getMenuName());
            holder.menuBTN.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.position = position;
            holder.menuCode = configTemp.getConfigCode();

            holder.hasLink = configTemp.getHasLink();


            Integer i = menuIcons.get(configTemp.getConfigCode());
            if (i != null)
                holder.menuIcon.setImageResource(i);
            else
                holder.menuIcon.setImageResource(menuIcons
                        .get(StandardListMasterConstants.MENU_ORDER_REPORT));

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // Depreciated Reports.
                    if (holder.menuCode.equals("MENU_AUDITSCORE_REPORT") || holder.menuCode.equals("MENU_DAY_PERFORMA")
                            || holder.menuCode.equals("MENU_FOCUS_REPORT") || holder.menuCode.equals("MENU_MSL_REPORT") || holder.menuCode.equals("MENU_POSM_REPORT")
                            || holder.menuCode.equals("MENU_SCHEME_REPORT") || holder.menuCode.equals("MENU_STORERANK_REPORT")
                            || holder.menuCode.equals("MENU_TIME_REPORT") || holder.menuCode.equals("MENU_REPORT01") || holder.menuCode.equals("MENU_SUP_TEST_SCORE")) {

                        Toast.makeText(getActivity(), getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();

                    } else {
                        gotoSelectedFragement(holder.config);
                    }


                }
            });

            if (position % 2 == 0)
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));

            return convertView;
        }

        class ViewHolder {
            private ConfigureBO config;
            private ImageView menuIcon;
            private TextView menuBTN;

            String menuCode;
            int position;
            int hasLink;
        }
    }


    /**
     * setUp the updateAuthenticateToken() for post request
     * send the post request and store the "rpt_downtime" in SharedPreferences from the response object
     * <p>
     * based on result of  (bmodel.reportHelper.isPerformReport()) ) this will move ReportActivity
     * else show the alert for the error code
     */
    class PerformRptDownloadData extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;
        ConfigureBO config;
        String Url;
        OutletPerfomanceHelper outletPerfomanceHelper;
        private ProgressDialog progressDialogue;

        PerformRptDownloadData(ConfigureBO config, String Url, OutletPerfomanceHelper perfomanceHelper) {
            this.config = config;
            this.Url = Url;
            this.outletPerfomanceHelper = perfomanceHelper;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.downloading_rpt),
                    true, false);
            jsonObject = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken(false);
            String response = bmodel.synchronizationHelper.sendPostMethod(Url, jsonObject);
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(jsonObject, true);

                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .edit();
                            editor.putString("rpt_dwntime",
                                    DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW));
                            editor.apply();

                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print(jsonExpection.getMessage());
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            progressDialogue.dismiss();
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                if (errorCode
                        .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    if (outletPerfomanceHelper.isPerformReport()) {
                        Intent intent = new Intent(getActivity(), ReportActivity.class);
                        Bundle bun = new Bundle();
                        bun.putSerializable("config", config);
                        bun.putString("FROM", "REPORT");
                        intent.putExtras(bun);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
                    }

                } else {
                    String errorMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorCode);
                    if (errorMessage != null) {
                        bmodel.showAlert(errorMessage, 0);
                    }
                }
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
        private AlertDialog alertDialog;
        private String menuName = "";

        public DownloadCurrentStock(String menuName) {
            this.menuName = menuName;
        }


        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.configurationMasterHelper
                        .loadStockUOMConfiguration();
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_LOAD_MANAGEMENT"));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        bmodel.productHelper.getFilterProductLevels(),false));
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

            if (bmodel.productHelper.getLoadMgmtProducts().size() > 0) {
                Intent stockViewIntent = new Intent(getActivity(),
                        StockViewActivity.class);
                stockViewIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                stockViewIntent.putExtra("screentitle", menuName);
                startActivity(stockViewIntent);
            } else {
                showToast();
            }
        }

    }

    class UpdatePendingInvoice extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        private ConfigureBO config = null;

        public UpdatePendingInvoice(ConfigureBO config) {
            this.config = config;
        }


        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                CollectionHelper.getInstance(getActivity()).updateInvoiceDiscountAmount();
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
            gotoReportActivity(config);
        }

    }
}
