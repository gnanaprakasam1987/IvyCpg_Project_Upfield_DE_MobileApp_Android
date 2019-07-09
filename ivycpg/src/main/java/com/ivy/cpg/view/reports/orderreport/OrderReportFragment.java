package com.ivy.cpg.view.reports.orderreport;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.reports.dayreport.DaggerReportComponent;
import com.ivy.cpg.view.reports.dayreport.ReportComponent;
import com.ivy.cpg.view.reports.dayreport.ReportModule;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.JExcelHelper;
import com.ivy.sd.png.util.Commons;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class OrderReportFragment extends IvyBaseFragment implements IOrderReportView, View.OnClickListener, AdapterView.OnItemClickListener {
    private IOrderReportModelPresenter mOrderReportModelPresenter;
    private Unbinder unbinder;

    private BusinessModel businessModel;
    private JExcelHelper jExcelHelper;

    @BindView(R.id.txttotal)
    TextView text_totalOrderValue;

    @BindView(R.id.txt_dist_pre_post)
    TextView text_averagePreOrPost;

    @BindView(R.id.btn_export)
    Button xlsExport;

    @BindView(R.id.list)
    ListView listView;

    @BindView(R.id.container_volume)
    LinearLayout volumeContainer;

    @BindView(R.id.lab_totalVolume)
    TextView totalWeightLabel;

    @BindView(R.id.txt_totalVolume_val)
    TextView totalVolumeValue;

    @BindView(R.id.view2)
    View dividerVolume;

    @BindView(R.id.txt_totalWeight)
    TextView text_totalWeight;


    private ArrayList<OrderReportBO> list;

    private OrderReportHelper reportHelper;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApplication();
        mOrderReportModelPresenter = new OrderReportModel(getActivity(), OrderReportFragment.this);

    }

    private void initializeApplication() {
        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());
        ReportComponent reportComponent = DaggerReportComponent.builder().reportModule(new ReportModule((BusinessModel) getActivity().getApplicationContext())).build();
        reportHelper = reportComponent.provideOrderReportHelper();
        jExcelHelper = JExcelHelper.getInstance(getActivity());
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("Start" + System.currentTimeMillis());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_order_report, container, false);
        unbinder = ButterKnife.bind(this, view);


        if (businessModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        TextView averageLines = view.findViewById(R.id.txtavglines);
        TextView text_LPC = view.findViewById(R.id.lpc);
        TextView totalLines = view.findViewById(R.id.txttotallines);
        TextView tv_lbl_total_lines = view.findViewById(R.id.lbl_total_lines);

        if (businessModel.configurationMasterHelper.IS_EXPORT_ORDER_REPORT) {
            xlsExport.setVisibility(View.VISIBLE);
        }

        if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL) {
            xlsExport.setText(getResources().getString(R.string.export_and_email));
        } else if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_SHARE) {
            xlsExport.setText(getResources().getString(R.string.export_and_share));
        } else {
            xlsExport.setText(getResources().getString(R.string.export));
        }


        listView.setCacheColorHint(0);
        listView.setOnItemClickListener(this);
        xlsExport.setOnClickListener(this);


        TextView text_totalValueTitle = view.findViewById(R.id.totalvaluetitle);
        TextView lab_dist_pre_post = view.findViewById(R.id.lab_dist_pre_post);

      /*  text_LPC.setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
        text_totalValueTitle.setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
        lab_dist_pre_post.setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
*/
        //list = businessModel.reportHelper.downloadOrderreport();

        list = mOrderReportModelPresenter.getOrderReport();

        updateOrderGrid();

        int mLPC = reportHelper.getavglinesfororderbooking("OrderHeader");

        if (businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {
            double mTotalOutlets = reportHelper
                    .getorderbookingCount("OrderHeader");
            double result = mLPC / mTotalOutlets;
            String resultS = result + "";
            if (resultS.equals(getResources().getString(R.string.nan))) {
                averageLines.setText(String.valueOf(0));
            } else {
                averageLines.setText(SDUtil.roundIt(result, 2));
            }

        }

        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES)
            totalLines.setText(String.valueOf(mLPC));
        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            if (businessModel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
                int totalQty = 0;
                for (OrderReportBO bo : list)
                    totalQty = totalQty + reportHelper.getTotalQtyfororder(bo.getOrderID());
                totalLines.setText(String.valueOf(totalQty));
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_qty));
            } else {
                totalLines.setText(String.valueOf(mLPC));
                tv_lbl_total_lines.setText(getResources().getString(R.string.tot_line));
            }

        }

        if (businessModel.configurationMasterHelper.SHOW_LPC_ORDER) {
            view.findViewById(R.id.lbl_avg_lines).setVisibility(View.VISIBLE);
            averageLines.setVisibility(View.VISIBLE);
        }

        if (businessModel.configurationMasterHelper.SHOW_TOTAL_LINES) {
            totalLines.setVisibility(View.VISIBLE);
            view.findViewById(R.id.lbl_total_lines).setVisibility(View.VISIBLE);
            view.findViewById(R.id.view00).setVisibility(View.VISIBLE);
        }

        if (!businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            view.findViewById(R.id.view1).setVisibility(View.GONE);
            view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
            view.findViewById(R.id.dist).setVisibility(View.GONE);
            //((TextView) view.findViewById(R.id.outna)).setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
        }

        if (!businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
            view.findViewById(R.id.weighttitle).setVisibility(View.GONE);
        else {
            try {
                if (businessModel.labelsMasterHelper.applyLabels(getActivity().findViewById(
                        R.id.weighttitle).getTag()) != null)
                    ((TextView) getActivity().findViewById(R.id.weighttitle))
                            .setText(businessModel.labelsMasterHelper
                                    .applyLabels(getActivity().findViewById(R.id.weighttitle)
                                            .getTag()));
               // ((TextView) view.findViewById(R.id.weighttitle)).setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }


        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.weighttitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.weighttitle))
                        .setText(businessModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.weighttitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.outna).getTag()) != null)
                ((TextView) view.findViewById(R.id.outna))
                        .setText(businessModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.outna).getTag()));
            //((TextView) view.findViewById(R.id.outna)).setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));

        } catch (Exception e) {
            Commons.printException(e);
        }

        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.lpc).getTag()) != null)
                ((TextView) view.findViewById(R.id.lpc))
                        .setText(businessModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.lpc).getTag()));
           // ((TextView) view.findViewById(R.id.lpc)).setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));

        } catch (Exception e) {
            Commons.printException(e);
        }

        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.outid).getTag()) != null)
                ((TextView) view.findViewById(R.id.outid))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.outid)
                                        .getTag()));
            //((TextView) view.findViewById(R.id.outid)).setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));

        } catch (Exception e) {
            Commons.printException(e);
        }


        try {
            if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.lab_total_value).getTag()) != null)
                ((TextView) view.findViewById(R.id.lab_total_value))
                        .setText(businessModel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.lab_total_value)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        if (!businessModel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
            text_totalOrderValue.setVisibility(View.GONE);
            text_totalValueTitle.setVisibility(View.GONE);
            view.findViewById(R.id.view0).setVisibility(View.GONE);
        }

        if (businessModel.configurationMasterHelper.SHOW_TOTAL_ACHIEVED_VOLUME) {
            showVolume();
        }
        System.out.println("Start" + System.currentTimeMillis());

        if (businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
            view.findViewById(R.id.ll_totweight).setVisibility(View.VISIBLE);
            view.findViewById(R.id.view3).setVisibility(View.VISIBLE);
            try {
                if (businessModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.lbl_totweigh).getTag()) != null)
                    ((TextView) view.findViewById(R.id.lbl_totweigh))
                            .setText(businessModel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.lbl_totweigh).getTag()));
               // ((TextView) view.findViewById(R.id.lbl_totweigh)).setTypeface(FontUtils.getFontRoboto(getActivity(),FontUtils.FontType.LIGHT));

            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        return view;

    }

    private void showVolume() {
        volumeContainer.setVisibility(View.VISIBLE);
        dividerVolume.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View comp) {
        Button vw = (Button) comp;
        if (vw == xlsExport) {
            new XlsExport().execute();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            OrderReportBO ret = list.get(position);
            Intent intent = new Intent();
            intent.putExtra("OBJ", ret);
            intent.putExtra("isFromOrder", true);
            intent.putExtra("TotalValue", ret.getOrderTotal());
            intent.putExtra("TotalLines", ret.getLPC());
            intent.putExtra("TotalWeight", ret.getWeight());
            intent.setClass(getActivity(), OrderReportDetail.class);
            startActivityForResult(intent, 0);


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void updateOrderGrid() {
        double mTotalValue = 0;
        int pre = 0, post = 0;
        float mTotalWeight = 0;

        // Show alert if error loading data.
        if (list == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }
        // Show alert if no order exist.
        if (list.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_orders_available),
                    Toast.LENGTH_SHORT).show();
            xlsExport.setVisibility(View.GONE);
            return;
        }

        // Calculate the total order value.
        for (OrderReportBO ret : list) {
            mTotalValue = mTotalValue + SDUtil.convertToDouble(SDUtil.format(ret.getOrderTotal(),
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    0, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));
            mTotalWeight = mTotalWeight + ret.getWeight();
        }

        if (businessModel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
            // Calculate the total order value.
            for (OrderReportBO ret : list) {
                try {
                    String str[] = ret.getDist().split("/");
                    pre = pre + SDUtil.convertToInt(str[0]);
                    post = post + SDUtil.convertToInt(str[1]);
                } catch (Exception e) {
                    Commons.printException(e);
                }

            }
            float mPreAverage = 0, mPostAverage = 0;
            if (list.size() > 0) {
                if (pre > 0) {
                    mPreAverage = (float) pre / (float) list.size();
                }
                if (post > 0) {
                    mPostAverage = (float) post / (float) list.size();
                }

                String value = SDUtil.format(mPreAverage, 1, 0) + "/"
                        + SDUtil.format(mPostAverage, 1, 0);
                text_averagePreOrPost.setText(value);

            } else {
                text_averagePreOrPost.setText("0/0");
            }

        }
        // Format and set on the label
        if (!businessModel.configurationMasterHelper.SHOW_NETAMOUNT_IN_REPORT)
            text_totalOrderValue.setText(SDUtil.format(mTotalValue,
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    businessModel.configurationMasterHelper.VALUE_COMMA_COUNT, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));
        else
            text_totalOrderValue.setText(SDUtil.format(reportHelper.getTotValues(getActivity().getApplicationContext()) - SalesReturnHelper.getInstance(getActivity()).getTotalSalesReturnValue(getActivity().getApplicationContext()),
                    businessModel.configurationMasterHelper.VALUE_PRECISION_COUNT,
                    businessModel.configurationMasterHelper.VALUE_COMMA_COUNT, businessModel.configurationMasterHelper.IS_DOT_FOR_GROUP));


        //cpg132-task13

        if (businessModel.configurationMasterHelper.SHOW_TOTAL_ACHIEVED_VOLUME) {
            int pcQty = 0;
            int caseQty = 0;
            int outQty = 0;


            for (OrderReportBO ret : list) {
                pcQty = pcQty + ret.getVolumePcsQty();
                caseQty = caseQty + ret.getVolumeCaseQty();
                outQty = outQty + ret.getVolumeOuterQty();
            }
            totalWeightLabel.setText(getString(R.string.total_vol));


            try {

                StringBuilder sb = new StringBuilder();
                String op = getString(R.string.item_piece);
                String oc = getString(R.string.item_case);
                String ou = getString(R.string.item_outer);

                if (businessModel.labelsMasterHelper
                        .applyLabels("item_piece") != null)
                    op = businessModel.labelsMasterHelper
                            .applyLabels("item_piece");
                if (businessModel.labelsMasterHelper
                        .applyLabels("item_case") != null)
                    oc = businessModel.labelsMasterHelper
                            .applyLabels("item_case");

                if (businessModel.labelsMasterHelper
                        .applyLabels("item_outer") != null)
                    ou = businessModel.labelsMasterHelper
                            .applyLabels("item_outer");


                if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS) {

                    sb.append(pcQty + " " + op);
                }


                if (businessModel.configurationMasterHelper.SHOW_ORDER_CASE) {

                    if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS)
                        sb.append(" : " + caseQty + " " + oc);
                    else
                        sb.append(caseQty + " " + oc);
                }

                if (businessModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (businessModel.configurationMasterHelper.SHOW_ORDER_PCS || businessModel.configurationMasterHelper.SHOW_ORDER_CASE)
                        sb.append(" : " + outQty + " " + ou);
                    else
                        sb.append(outQty + " " + ou);
                }

                totalVolumeValue.setText(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }


        }

        if (businessModel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
            text_totalWeight.setText(Utils.formatAsTwoDecimal((double) mTotalWeight));
        }


        // Load ListView
        //  com.ivy.cpg.view.reports.OrderReportFragment.OutletListAdapter mSchedule = new com.ivy.cpg.view.reports.OrderReportFragment.OutletListAdapter(list);
        OrderReportAdapter mSchedule = new OrderReportAdapter(list, getActivity(), businessModel, iOrderReportImageView);
        listView.setAdapter(mSchedule);

    }

    class XlsExport extends AsyncTask<Void, Void, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.exporting_orders));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {

                ArrayList<String> columnNames = new ArrayList<>();
                columnNames.add("Distributor");
                columnNames.add("UserCode");
                columnNames.add("UserName");
                columnNames.add("RetailerCode");
                columnNames.add("RetailerName");
                columnNames.add("OrderNo");
                columnNames.add("OrderDate");
                columnNames.add("SKUCode");
                columnNames.add("SKUDescription");
                columnNames.add("OrderQty(Piece)");
                columnNames.add("OrderQty(Case)");
                columnNames.add("OrderQty(Outer)");
                columnNames.add("DeliveryDate");

                reportHelper
                        .downloadOrderReportToExport();
                HashMap<String, ArrayList<ArrayList<String>>> mOrderDetailsByDistributorName = reportHelper
                        .getmOrderDetailsByDistributorName();


                for (String distributorName : mOrderDetailsByDistributorName.keySet()) {

                    ArrayList<JExcelHelper.ExcelBO> mExcelBOList = new ArrayList<>();
                    JExcelHelper.ExcelBO excel = jExcelHelper.new ExcelBO();
                    excel.setSheetName(distributorName);
                    excel.setColumnNames(columnNames);
                    excel.setColumnValues(mOrderDetailsByDistributorName.get(distributorName));
                    mExcelBOList.add(excel);
                    jExcelHelper.createExcel("OrderReport_" + distributorName + ".xls", mExcelBOList);
                }

                if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL)
                    reportHelper.downloadOrderEmailAccountCredentials();


            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);


            alertDialog.dismiss();
            if (result) {
                Toast.makeText(getActivity(), getResources().getString(R.string.successfully_exported),
                        Toast.LENGTH_SHORT).show();

                try {

                    if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_SHARE) {
                        Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

                        ArrayList<Uri> uriList = new ArrayList<>();
                        for (String distributorName : reportHelper
                                .getmOrderDetailsByDistributorName().keySet()) {
                            File newFile = new File(getActivity().getExternalFilesDir(null) + "", "OrderReport_" + distributorName + ".xls");
                            if (Build.VERSION.SDK_INT >= 24) {
                                uriList.add(FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", newFile));

                            } else {
                                uriList.add(Uri.fromFile(newFile));
                            }
                        }

                        sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

                        sharingIntent.setType("application/excel");
                        sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        //sharingIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});

                        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_order_report_using)));
                    } else if (businessModel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL) {

                        if (reportHelper.getUserName() != null && !reportHelper.getUserName().equals("")
                                && reportHelper.getUserPassword() != null && !reportHelper.getUserPassword().equals(""))
                            new SendMail(getActivity()
                                    , "Order Report", "PFA").execute();
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.invalid_credentials_mail_not_sent), Toast.LENGTH_LONG).show();


                    }
                } catch (Exception ex) {
                    Commons.printException(ex);
                }
            } else
                Toast.makeText(getActivity(), getResources().getString(R.string.export_failed),
                        Toast.LENGTH_SHORT).show();


        }

    }

    public class SendMail extends AsyncTask<Void, Void, Boolean> {

        Session session;

        Context mContext;
        private String subject;
        private String body;


        ProgressDialog progressDialog;

        public SendMail(Context ctx, String subject, String message) {
            this.mContext = ctx;

            this.subject = subject;
            this.body = message;


        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.sending_email), getResources().getString(R.string.please_wait_some_time), false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Properties props = System.getProperties();// new Properties();

            //Configuring properties for G-MAIL
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(reportHelper.getUserName(), reportHelper.getUserPassword());
                        }
                    });

            try {

                // sending distributor wise..
                for (String distributorName : reportHelper
                        .getmOrderDetailsByDistributorName().keySet()) {

                    //not allowed if email not available
                    if (reportHelper.getmEmailIdByDistributorName().get(distributorName) != null) {

                        Message message = new MimeMessage(session);
                        message.setFrom(new InternetAddress(reportHelper.getUserName()));
                        message.setRecipient(Message.RecipientType.TO, new InternetAddress(reportHelper.getmEmailIdByDistributorName().get(distributorName)));
                        message.setSubject(subject);
                        message.setText(body);
                        //  mm.setContent(message,"text/html; charset=utf-8");

                        BodyPart bodyPart = new MimeBodyPart();
                        bodyPart.setText(body);//Content(message,"text/html");

                        //Attachment
                        DataSource source = new FileDataSource(getActivity().getExternalFilesDir(null) + "/" + "OrderReport_" + distributorName + ".xls");
                        bodyPart.setDataHandler(new DataHandler(source));
                        bodyPart.setFileName("OrderReport_" + distributorName + ".xls");

                        MimeMultipart multiPart = new MimeMultipart();
                        multiPart.addBodyPart(bodyPart);
                        message.setContent(multiPart);

                        Thread.currentThread().setContextClassLoader(getActivity().getClassLoader());

                        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                        mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

                        //sending mail
                        Transport.send(message);
                    }
                }
            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSent) {
            super.onPostExecute(isSent);

            progressDialog.dismiss();

            if (isSent) {
                Toast.makeText(getActivity(), getResources().getString(R.string.email_sent),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.error_in_sending_email),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    /**
     * used to open selected image into image viewer
     */
    IOrderReportImageView iOrderReportImageView = new IOrderReportImageView() {
        @Override
        public void openImageView(String fileName) {
            if (fileName.trim().length() > 0) {
                try {
                    Uri path;
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);

                    if (Build.VERSION.SDK_INT >= 24) {
                        path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(fileName));
                        intent.setDataAndType(path, "image/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } else {
                        path = Uri.fromFile(new File(fileName));
                        intent.setDataAndType(path, "image/*");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    }
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Commons.printException("" + e);
                    Toast.makeText(
                            getActivity(),
                            getResources()
                                    .getString(
                                            R.string.no_application_available_to_view_video),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.unloadimage),
                        Toast.LENGTH_SHORT).show();
            }
        }
    };

}
