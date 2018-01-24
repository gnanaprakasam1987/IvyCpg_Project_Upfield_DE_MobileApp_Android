package com.ivy.sd.png.view.reports;

import android.annotation.SuppressLint;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.ReportonorderbookingBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

/**
 * Created by vikraman.a on 05-05-2017.
 */

public class ReportMenufragment extends IvyBaseFragment {
    private View view;

    private BusinessModel bmodel;
    private static final HashMap<String, Integer> menuIcons = new HashMap<String, Integer>();
    private Vector<ConfigureBO> menuDB = new Vector<ConfigureBO>();
    private ActionBar actionBar;
    private ArrayList<ReportonorderbookingBO> mylist;
    private Vector<InvoiceReportBO> mylist1;
    private Vector<ConfigureBO> mDayList;

    @SuppressLint("NewApi")

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragmentnew_reportmenu, container, false);

        try {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());

            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

            if (actionBar != null) {
                actionBar.setDisplayShowTitleEnabled(false);
                actionBar.setIcon(null);
                actionBar.setElevation(0);
                //  actionBar.setStackedBackgroundDrawable((new ColorDrawable(ContextCompat.getColor(getActivity(),R.color.toolbar_ret_bg))));
            }

            setScreenTitle(bmodel.configurationMasterHelper.getTradecoveragetitle());

            if (getArguments().getString("screentitle") != null)
                setScreenTitle(getArguments().getString("screentitle"));
            try {
                LinearLayout bg = (LinearLayout) getActivity().findViewById(R.id.root);
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


            menuIcons.put(StandardListMasterConstants.MENU_ORDER_REPORT,
                    R.drawable.icon_stock);
            menuIcons.put(StandardListMasterConstants.MENU_PREVIOUS_ORDER_REPORT,
                    R.drawable.icon_order);
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
            menuIcons.put(StandardListMasterConstants.MENU_BEGINNING_STOCK_REPORT,
                    R.drawable.icon_collection);
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
            menuIcons.put(StandardListMasterConstants.MENU_CS_RPT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT,
                    R.drawable.icon_reports);
            menuIcons.put(StandardListMasterConstants.MENU_SELLER_PERFOMANCE_REPORT,
                    R.drawable.icon_reports);

            menuIcons.put(StandardListMasterConstants.MENU_PRDVOL_RPT,
                    R.drawable.icon_reports);
            // Load the HHTTable
            menuDB = bmodel.configurationMasterHelper
                    .downloadNewActivityMenu(StandardListMasterConstants.REPORT_MENU);


            ListView listView = (ListView) view.findViewById(R.id.listView1);
            listView.setCacheColorHint(0);
            listView.setAdapter(new MenuBaseAdapter(menuDB));

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return view;
    }

    private boolean isCreated;

    protected void gotoSelectedFragement(ConfigureBO config) {
        if (config.getConfigCode().equals(StandardListMasterConstants.MENU_INVOICE_REPORT)) {
            if (bmodel.reportHelper.downloadInvoicereport().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PND_INVOICE_REPORT)) {
            bmodel.collectionHelper.updateInvoiceDiscountAmount();
            bmodel.downloadInvoice();
            if (bmodel.getInvoiceHeaderBO().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_ORDER_REPORT)) {

            if (bmodel.reportHelper.downloadOrderreport().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_CURRENT_STOCK_REPORT)) {

            if (bmodel.reportHelper.downloadCurrentStockReport().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_DAY_REPORT)) {

            if (bmodel.configurationMasterHelper.downloadDayReportList().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SKU_REPORT)) {

            if (bmodel.reportHelper.downloadSKUReport().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_CS_RPT)) {
            bmodel.mCounterSalesHelper.loadVisitedCustomer();
            if (bmodel.mCounterSalesHelper.getCSCustomerVisitedUID().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_INVENTORY_RPT)) {
            intoreportacti(config);
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT)) {
            intoreportacti(config);
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SELLER_PERFOMANCE_REPORT)) {
            if (bmodel.reportHelper.isPerformReport()) {
                intoreportacti(config);
            } else {
                String Url = bmodel.reportHelper.getPerformRptUrl();
                if (Url != null && Url.length() > 0) {
                    new PerformRptDownloadData(config, Url).execute();
                } else {
                    Toast.makeText(getActivity(), "Download Url Not Available", Toast.LENGTH_LONG).show();
                }
            }

        }else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SALES_REPORT)) {
            if (bmodel.reportHelper.getSalesReturnRetailerList().size() >= 1) {
                intoreportacti(config);
            } else {
                Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
            }
        }  else {
            intoreportacti(config);
        }

    }

    private void intoreportacti(ConfigureBO config) {
        Intent intent = new Intent(getActivity(), ReportActivity.class);
        Bundle bun = new Bundle();
        bun.putSerializable("config", config);
        intent.putExtras(bun);

        startActivity(intent);
        //isCreated = false;
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
                convertView = inflater.inflate(R.layout.custom_loadmgt_list_item, parent,
                        false);
                holder = new ViewHolder();
                holder.menuIcon = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_ib);

                holder.menuBTN = (TextView) convertView
                        .findViewById(R.id.list_item_menu_tv_loadmgt);


                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.config = configTemp;

            holder.menuBTN.setTextColor(ContextCompat.getColor(getActivity(), R.color.black_bg1));
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
                    if (holder.menuCode.equals("MENU_AUDITSCORE_REPORT") || holder.menuCode.equals("MENU_DAY_PERFORMA")
                            || holder.menuCode.equals("MENU_FOCUS_REPORT") || holder.menuCode.equals("MENU_MSL_REPORT") || holder.menuCode.equals("MENU_POSM_REPORT")
                            || holder.menuCode.equals("MENU_SCHEME_REPORT") || holder.menuCode.equals("MENU_STORERANK_REPORT") || holder.menuCode.equals("MENU_TASKEXEC_REPORT")
                            || holder.menuCode.equals("MENU_TIME_REPORT") || holder.menuCode.equals("MENU_REPORT01")) {

                        Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();

                    } else {
                        //isCreated = true;
                        gotoSelectedFragement(holder.config);
                    }


                }
            });


            if (position % 2 == 0)
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.history_list_bg));

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

    class PerformRptDownloadData extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;
        ConfigureBO config;
        String Url;
        private ProgressDialog progressDialogue;

        PerformRptDownloadData(ConfigureBO config, String Url) {
            this.config = config;
            this.Url = Url;

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
            bmodel.synchronizationHelper.updateAuthenticateToken();
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
                                    SDUtil.now(SDUtil.DATE_TIME_NEW));
                            editor.commit();

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
                    if (bmodel.reportHelper.isPerformReport()) {
                        Intent intent = new Intent(getActivity(), ReportActivity.class);
                        Bundle bun = new Bundle();
                        bun.putSerializable("config", config);
                        intent.putExtras(bun);
                        startActivity(intent);
                    } else {
                        Toast.makeText(getActivity(), "Data Not Available", Toast.LENGTH_LONG).show();
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
}
