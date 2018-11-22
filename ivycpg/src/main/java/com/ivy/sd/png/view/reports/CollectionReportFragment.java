package com.ivy.sd.png.view.reports;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatDrawableManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.cpg.view.reports.collectionreport.CollectionReportDialog;

import java.util.List;

@Deprecated
public class CollectionReportFragment extends Fragment {

    ExpandableListView collectionListView;

    BusinessModel bModel;

    Double totalCash = 0.0;
    Double totalCheque = 0.0;
    Double totalColl = 0.0;
    Double totalDD = 0.0;
    Double totalRTGS = 0.0;
    Double total_mob_payment = 0.0;
    Double totalCn = 0.0;
    Double totalAd = 0.0;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    private void initializeBusinessModel() {
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());
        bModel.reportHelper.loadCollectionReport();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.collection_report_fragment, container, false);

        initializeBusinessModel();

        collectionListView = view.findViewById(R.id.collection_listview);
        CollectionFragmentAdapter collectionadapter = new CollectionFragmentAdapter();
        collectionListView.setAdapter(collectionadapter);

        updateDetails();
        initFooter(view);

        return view;
    }

    private void initFooter(View view) {
        final LinearLayout ll_cash = view.findViewById(R.id.ll_cash);
        final LinearLayout ll_cheque = view.findViewById(R.id.ll_cheque);
        final LinearLayout ll_dd = view.findViewById(R.id.ll_dd);
        final LinearLayout ll_credit_note = view.findViewById(R.id.ll_creditNote);
        final LinearLayout ll_adPayment = view.findViewById(R.id.ll_adPayment);
        final LinearLayout ll_rtgs = view.findViewById(R.id.ll_rtgs);
        final LinearLayout ll_mob_pymt = view.findViewById(R.id.ll_mob_pymt);
        final ImageView imageView = view.findViewById(R.id.imageView);
        TextView tv_collected = view.findViewById(R.id.tot);
        TextView tv_cash = view.findViewById(R.id.totcash);
        TextView tv_cheque = view.findViewById(R.id.tocheque);
        TextView tv_dd = view.findViewById(R.id.total_dd);
        TextView tv_cn = view.findViewById(R.id.totCn);
        TextView tv_rtgs = view.findViewById(R.id.total_rtgs);
        TextView tv_mob_pymt = view.findViewById(R.id.total_mob_payment);
        TextView tv_ad = view.findViewById(R.id.totAd);

        if (totalCash != null)
            tv_cash.setText(bModel.formatValue(totalCash));
        if (totalCheque != null)
            tv_cheque.setText(bModel.formatValue(totalCheque));
        if (totalDD != null)
            tv_dd.setText(bModel.formatValue(totalDD));
        if (totalRTGS != null)
            tv_rtgs.setText(bModel.formatValue(totalRTGS));
        if (total_mob_payment != null)
            tv_mob_pymt.setText(bModel.formatValue(total_mob_payment));
        if (totalCn != null)
            tv_cn.setText(bModel.formatValue(totalCn));
        if (totalAd != null)
            tv_ad.setText(bModel.formatValue(totalAd));
        if (totalColl != null)
            tv_collected.setText(bModel.formatValue(totalColl));

        @SuppressLint("RestrictedApi")
        Drawable drawable = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.activity_icon_next);
        final Bitmap imageBitmap = fromDrawableToBitmap(drawable);
        imageView.setColorFilter(ContextCompat.getColor(getActivity(), R.color.white));
        imageView.setImageBitmap(getRotatedBitmap(imageBitmap, 90));
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ll_cash.getVisibility() == View.GONE) {
                    imageView.setImageBitmap(getRotatedBitmap(imageBitmap, -90));
                    if (totalCash > 0)
                        ll_cash.setVisibility(View.VISIBLE);
                    if (totalCheque > 0)
                        ll_cheque.setVisibility(View.VISIBLE);
                    if (totalDD > 0)
                        ll_dd.setVisibility(View.VISIBLE);
                    if (totalAd > 0)
                        ll_adPayment.setVisibility(View.VISIBLE);
                    if (totalCn > 0)
                        ll_credit_note.setVisibility(View.VISIBLE);
                    if (totalRTGS > 0)
                        ll_rtgs.setVisibility(View.VISIBLE);
                    if (total_mob_payment > 0)
                        ll_mob_pymt.setVisibility(View.VISIBLE);
                } else {
                    imageView.setImageBitmap(getRotatedBitmap(imageBitmap, 90));
                    ll_cash.setVisibility(View.GONE);
                    ll_cheque.setVisibility(View.GONE);
                    ll_dd.setVisibility(View.GONE);
                    ll_adPayment.setVisibility(View.GONE);
                    ll_credit_note.setVisibility(View.GONE);
                    ll_rtgs.setVisibility(View.GONE);
                    ll_mob_pymt.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getActivity().getMenuInflater().inflate(R.menu.menu_invoice_report, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bModel.configurationMasterHelper.SHOW_PRINT_BUTTON) {
            menu.findItem(R.id.menu_print).setVisible(true);
        } else {
            menu.findItem(R.id.menu_print).setVisible(false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_print) {
            if (bModel.reportHelper.getParentPaymentList() != null && bModel.reportHelper.getParentPaymentList().size() > 0) {

                CollectionReportDialog fragment = new CollectionReportDialog();
                fragment.setCancelable(false);
                fragment.show(getFragmentManager(), "CollectionReportFragment");
                return true;
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.no_data_print), Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private class CollectionFragmentAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ChildViewHolder childHolder;
            View row = convertView;
            if (getGroupType(groupPosition) == 0) {
                List<PaymentBO> payment = bModel.reportHelper.getChildPaymentList()
                        .get(groupPosition);


                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater.inflate(R.layout.row_collection_report_child,
                            parent, false);
                    childHolder = new ChildViewHolder();
                    childHolder.modeTv = row.findViewById(R.id.modeTv);
                    childHolder.paymentOrChqDateTv = row
                            .findViewById(R.id.paymentOrChqDateTv);
                    childHolder.chqRefTitleTv = row
                            .findViewById(R.id.chqRefTitleTv);
                    childHolder.chqRefTv = row
                            .findViewById(R.id.chqRefTv);
                    childHolder.paidAmtTv = row
                            .findViewById(R.id.paidAmtTv);
                    childHolder.disAmtTV = row.findViewById(R.id.disAmtTv);
                    childHolder.amountPaidTv = row.findViewById(R.id.amountPaidTv);
                    childHolder.discAmtTv = row.findViewById(R.id.discAmtTv);
                    childHolder.collectionDiscLL = row.findViewById(R.id.ll_collection_disc);
                    childHolder.line1 = row.findViewById(R.id.line1);
                    childHolder.line2 = row.findViewById(R.id.line2);
                    childHolder.top_line = row.findViewById(R.id.top_line);

                    childHolder.modeTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    childHolder.paymentOrChqDateTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    childHolder.chqRefTitleTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    childHolder.amountPaidTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    childHolder.discAmtTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    childHolder.chqRefTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    childHolder.paidAmtTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    childHolder.disAmtTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                    if (bModel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                        childHolder.collectionDiscLL.setVisibility(View.VISIBLE);
                        childHolder.line2.setVisibility(View.VISIBLE);
                    }

                    row.setTag(childHolder);
                } else {
                    childHolder = (ChildViewHolder) row.getTag();
                }
                childHolder.paymentChldObj = payment;
                childHolder.childpos = childPosition;
                childHolder.disAmtTV.setText(bModel.formatValue(childHolder.paymentChldObj.get(childHolder.childpos).getAppliedDiscountAmount()));
                if (childHolder.paymentChldObj.get(childHolder.childpos)
                        .getCashMode().equals(StandardListMasterConstants.CHEQUE)) {

                    try {
                        childHolder.paymentOrChqDateTv
                                .setText(DateUtil.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                        childHolder.childpos).getChequeDate(),
                                        ConfigurationMasterHelper.outDateFormat));
                    } catch (Exception e) {
                        childHolder.paymentOrChqDateTv
                                .setText("");
                        Commons.printException(e);
                    }
                    childHolder.chqRefTitleTv.setText(getResources().getString(
                            R.string.chq_no));
                    childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(childHolder.childpos).getChequeNumber());
                    childHolder.chqRefTitleTv.setVisibility(View.VISIBLE);
                    childHolder.chqRefTv.setVisibility(View.VISIBLE);
                    childHolder.line1.setVisibility(View.VISIBLE);
                } else if (childHolder.paymentChldObj.get(childHolder.childpos).getCashMode()
                        .equals(StandardListMasterConstants.CREDIT_NOTE)) {
                    childHolder.chqRefTitleTv.setText(getResources().getString(
                            R.string.ref_no));
                    childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                            childHolder.childpos).getReferenceNumber());
                    try {
                        childHolder.paymentOrChqDateTv
                                .setText(DateUtil.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                        childHolder.childpos).getCollectionDate(),
                                        ConfigurationMasterHelper.outDateFormat));
                    } catch (Exception e) {
                        childHolder.paymentOrChqDateTv.setText("");
                        Commons.printException(e);
                    }
                    childHolder.chqRefTitleTv.setVisibility(View.VISIBLE);
                    childHolder.chqRefTv.setVisibility(View.VISIBLE);
                    childHolder.line1.setVisibility(View.VISIBLE);
                } else if (childHolder.paymentChldObj.get(childHolder.childpos)
                        .getCashMode()
                        .equals(StandardListMasterConstants.RTGS)) {
                    childHolder.chqRefTitleTv.setText(getResources().getString(
                            R.string.ref_no));
                    childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                            childHolder.childpos).getChequeNumber());
                    try {
                        childHolder.paymentOrChqDateTv
                                .setText(DateUtil.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                        childHolder.childpos).getCollectionDate(),
                                        ConfigurationMasterHelper.outDateFormat));
                    } catch (Exception e) {
                        childHolder.paymentOrChqDateTv
                                .setText("");
                        Commons.printException(e);
                    }
                    childHolder.chqRefTitleTv.setVisibility(View.VISIBLE);
                    childHolder.chqRefTv.setVisibility(View.VISIBLE);
                    childHolder.line1.setVisibility(View.VISIBLE);
                } else if (childHolder.paymentChldObj.get(childHolder.childpos)
                        .getCashMode()
                        .equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                    childHolder.chqRefTitleTv.setText(getResources().getString(
                            R.string.ref_no));
                    childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                            childHolder.childpos).getChequeNumber());
                    try {
                        childHolder.paymentOrChqDateTv
                                .setText(DateUtil.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                        childHolder.childpos).getCollectionDate(), ConfigurationMasterHelper.outDateFormat));
                    } catch (Exception e) {
                        childHolder.paymentOrChqDateTv
                                .setText("");
                        Commons.printException(e);
                    }
                    childHolder.chqRefTitleTv.setVisibility(View.VISIBLE);
                    childHolder.line1.setVisibility(View.VISIBLE);
                    childHolder.chqRefTv.setVisibility(View.VISIBLE);
                } else if (childHolder.paymentChldObj.get(childHolder.childpos)
                        .getCashMode()
                        .equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                    childHolder.chqRefTitleTv.setText(getResources().getString(
                            R.string.ref_no));
                    childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                            childHolder.childpos).getChequeNumber());
                    try {
                        childHolder.paymentOrChqDateTv
                                .setText(DateUtil.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                        childHolder.childpos).getCollectionDate(),
                                        ConfigurationMasterHelper.outDateFormat));
                    } catch (Exception e) {
                        childHolder.paymentOrChqDateTv
                                .setText("");
                        Commons.printException(e);
                    }
                    childHolder.chqRefTitleTv.setVisibility(View.VISIBLE);
                    childHolder.line1.setVisibility(View.VISIBLE);
                    childHolder.chqRefTv.setVisibility(View.VISIBLE);
                } else {
                    try {
                        childHolder.paymentOrChqDateTv
                                .setText(DateUtil.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                        childHolder.childpos).getCollectionDate(),
                                        ConfigurationMasterHelper.outDateFormat));
                    } catch (Exception e) {
                        childHolder.paymentOrChqDateTv
                                .setText("");
                        Commons.printException(e);
                    }
                    childHolder.chqRefTitleTv.setVisibility(View.INVISIBLE);
                    childHolder.chqRefTv.setVisibility(View.INVISIBLE);
                    childHolder.line1.setVisibility(View.INVISIBLE);

                }

                childHolder.paidAmtTv.setText(bModel.formatValue(childHolder.paymentChldObj.get(
                        childHolder.childpos).getAmount()));
                if (childHolder.paymentChldObj.get(
                        childHolder.childpos).getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                    if (childHolder.paymentChldObj.get(
                            childHolder.childpos).getReferenceNumber().startsWith("AP")) {
                        childHolder.modeTv.setText(getResources().getString(R.string.advance_payment));
                    } else {
                        String paid_mode = getResources().getString(R.string.paid_by) + " " + childHolder.paymentChldObj.get(
                                childHolder.childpos).getListName();
                        childHolder.modeTv.setText(paid_mode);
                    }

                } else {
                    String paid_mode = getResources().getString(R.string.paid_by) + " " + childHolder.paymentChldObj.get(
                            childHolder.childpos).getListName();
                    childHolder.modeTv.setText(paid_mode);
                }

                if (childPosition == (payment.size() - 1)) {
                    childHolder.top_line.setVisibility(View.GONE);
                } else {
                    childHolder.top_line.setVisibility(View.VISIBLE);
                }
            }
            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            if (bModel.reportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId() != null
                    && bModel.reportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId().startsWith("AP"))
                return 0;
            else
                return bModel.reportHelper.getChildPaymentList().get(groupPosition).size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return bModel.reportHelper.getParentPaymentList()
                    .get(groupPosition);
        }

        @Override
        public int getGroupCount() {
            return bModel.reportHelper.getParentPaymentList().size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public int getGroupTypeCount() {
            return 2;
        }

        @Override
        public int getGroupType(int groupPosition) {
            if (bModel.reportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId() != null
                    && bModel.reportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId().startsWith("AP"))
                return 1;
            else
                return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final GroupViewHolder holder;
            PaymentBO paymentGroup = bModel.reportHelper
                    .getParentPaymentList().get(groupPosition);

            View row = convertView;
            if (row == null) {
                holder = new GroupViewHolder();
                LayoutInflater inflater = getActivity().getLayoutInflater();
                if (getGroupType(groupPosition) == 1) {
                    row = inflater.inflate(R.layout.row_advance_payment_report, parent,
                            false);
                    holder.retailerNameTv = row
                            .findViewById(R.id.retailerNameTv);
                    holder.adv_cash_modeTV = row.findViewById(R.id.cashMode);
                    holder.advTitle = row.findViewById(R.id.advTitle);
                    holder.date = row.findViewById(R.id.date);
                    holder.invoiceAmt = row.findViewById(R.id.invoiceAmt);
                    holder.invDateTv = row.findViewById(R.id.invDateTv);
                    holder.adv_amountTV = row.findViewById(R.id.adv_amount);

                    holder.retailerNameTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.advTitle.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.date.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.invoiceAmt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.adv_cash_modeTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.invDateTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.adv_amountTV.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                } else {
                    row = inflater.inflate(R.layout.row_collection_report, parent,
                            false);

                    holder.retailerNameTv = row
                            .findViewById(R.id.retailerNameTv);
                    holder.invAmtTv = row.findViewById(R.id.invAmtTv);
                    holder.invNoTv = row.findViewById(R.id.invNoTv);
                    holder.outAmtTv = row.findViewById(R.id.outAmtTv);
                    holder.invDateTv = row.findViewById(R.id.invDateTv);
                    holder.invoice = row.findViewById(R.id.invoice_tv);
                    holder.invoice_amt = row.findViewById(R.id.invoice_amt);
                    holder.invoice_date = row.findViewById(R.id.invoice_date);
                    holder.os_amt = row.findViewById(R.id.os_amt);
                    holder.down_arrow = row.findViewById(R.id.img_arrow);
                    holder.line = row.findViewById(R.id.line);

                    @SuppressLint("RestrictedApi")
                    Drawable drawable = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.activity_icon_next);
                    Bitmap imageBitmap = fromDrawableToBitmap(drawable);
                    holder.down_arrow.setImageBitmap(getRotatedBitmap(imageBitmap, 90));

                    holder.retailerNameTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.invoice.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.invNoTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.invoice_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.invAmtTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.invoice_date.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.invDateTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.os_amt.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.outAmtTv.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }
                row.setTag(holder);
            } else {
                holder = (GroupViewHolder) row.getTag();
            }

            try {
                if (isExpanded) {
                    @SuppressLint("RestrictedApi")
                    Drawable drawable = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.activity_icon_next);
                    Bitmap imageBitmap = fromDrawableToBitmap(drawable);
                    holder.down_arrow.setImageBitmap(getRotatedBitmap(imageBitmap, -90));
                    holder.line.setVisibility(View.VISIBLE);
                } else {
                    @SuppressLint("RestrictedApi")
                    Drawable drawable = AppCompatDrawableManager.get().getDrawable(getContext(), R.drawable.activity_icon_next);
                    Bitmap imageBitmap = fromDrawableToBitmap(drawable);
                    holder.down_arrow.setImageBitmap(getRotatedBitmap(imageBitmap, 90));
                    holder.line.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
            holder.paymentObj = paymentGroup;
            holder.retailerNameTv.setText(holder.paymentObj.getRetailerName());

            if (getGroupType(groupPosition) == 1) {
                holder.adv_cash_modeTV.setText(holder.paymentObj.getListName());
                holder.adv_amountTV.setText(bModel.formatValue(holder.paymentObj.getAmount()));
                try {
                    holder.invDateTv.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.paymentObj.getAdvancePaymentDate(),
                            ConfigurationMasterHelper.outDateFormat));
                } catch (Exception e) {
                    holder.invDateTv.setText("");
                    Commons.printException(e);
                }
            } else {
                holder.invAmtTv.setText(bModel.formatValue(holder.paymentObj
                        .getInvoiceAmount()));
                holder.invNoTv.setText(holder.paymentObj.getBillNumber());
                holder.outAmtTv.setText(bModel.formatValue(holder.paymentObj
                        .getBalance()));

                try {
                    holder.invDateTv.setText(DateUtil.convertFromServerDateToRequestedFormat(holder.paymentObj.getInvoiceDate(),
                            ConfigurationMasterHelper.outDateFormat));
                } catch (Exception e) {
                    holder.invDateTv.setText("");
                    Commons.printException(e);
                }
            }

            if (groupPosition % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            }
            return row;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

    }

    class GroupViewHolder {
        TextView invNoTv, retailerNameTv, invAmtTv, invDateTv, outAmtTv, invoice, invoice_amt, invoice_date, os_amt;
        TextView adv_amountTV, adv_cash_modeTV, advTitle, date, invoiceAmt;
        ImageView down_arrow;
        PaymentBO paymentObj;
        LinearLayout line;
    }

    class ChildViewHolder {
        TextView modeTv, paymentOrChqDateTv, chqRefTitleTv, chqRefTv,
                paidAmtTv, disAmtTV, amountPaidTv, discAmtTv;
        ImageView top_line;
        LinearLayout collectionDiscLL, line1, line2;
        List<PaymentBO> paymentChldObj;
        int childpos;
    }

    private Bitmap fromDrawableToBitmap(Drawable drawable) {
        Bitmap imageBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(imageBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return imageBitmap;
    }

    private Bitmap getRotatedBitmap(Bitmap bitmap, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle, bitmap.getWidth(),
                bitmap.getHeight());
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);
    }

    private void updateDetails() {
        if (bModel.reportHelper.getLstPaymentBObyGroupId() != null)
            for (String groupid : bModel.reportHelper.getLstPaymentBObyGroupId().keySet()) {
                for (int i = 0; i < bModel.reportHelper.getLstPaymentBObyGroupId().get(groupid).size(); i++) {
                    PaymentBO payBO = bModel.reportHelper.getLstPaymentBObyGroupId().get(groupid).get(i);
                    if (payBO.getCashMode().equals(StandardListMasterConstants.CASH)) {
                        totalCash = totalCash + payBO.getAmount();
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.CHEQUE)) {
                        totalCheque = totalCheque + payBO.getAmount();
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.DEMAND_DRAFT)) {
                        totalDD += payBO.getAmount();
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.RTGS)) {
                        totalRTGS += payBO.getAmount();
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.MOBILE_PAYMENT)) {
                        total_mob_payment += payBO.getAmount();
                    } else if (payBO.getCashMode().equals(StandardListMasterConstants.CREDIT_NOTE)) {
                        if (payBO.getReferenceNumber().startsWith("AP"))
                            totalAd = +payBO.getAmount();
                        else
                            totalCn += payBO.getAmount();
                    }
                }
            }
        totalColl = totalCash + totalCheque + totalDD + totalRTGS + total_mob_payment;
    }
}
