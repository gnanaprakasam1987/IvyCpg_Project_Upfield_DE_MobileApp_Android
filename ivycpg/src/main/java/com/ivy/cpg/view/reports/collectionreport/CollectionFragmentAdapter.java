package com.ivy.cpg.view.reports.collectionreport;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatDrawableManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;

import java.util.List;

public class CollectionFragmentAdapter extends BaseExpandableListAdapter {

    private BusinessModel bModel;
    private Context mContext;
    public CollectionReportHelper collectionReportHelper;
    private CollectionReportFragmentNew reportFragment;

    public CollectionFragmentAdapter(Context context, BusinessModel businessModel, CollectionReportFragmentNew reportFragment) {
        this.mContext = context;
        this.bModel = businessModel;
        CollectionComponent collectionComponent = DaggerCollectionComponent.builder().
                collectionModule(new CollectionModule((BusinessModel) mContext.getApplicationContext())).build();
        collectionReportHelper = collectionComponent.provideCollectionReportHelper();
        collectionReportHelper.loadCollectionReport();
        this.reportFragment = reportFragment;
    }

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
            List<PaymentBO> payment =collectionReportHelper.getChildPaymentList()
                    .get(groupPosition);


            if (row == null) {

                row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_collection_report_child,
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
                childHolder.img_delete = row.findViewById(R.id.imgDelete);
                childHolder.view_bottom_line = row.findViewById(R.id.bottom_line);

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
                            .setText(DateTimeUtils.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
                                    childHolder.childpos).getChequeDate(),
                                    ConfigurationMasterHelper.outDateFormat));
                } catch (Exception e) {
                    childHolder.paymentOrChqDateTv
                            .setText("");
                    Commons.printException(e);
                }
                childHolder.chqRefTitleTv.setText(mContext.getResources()
                        .getString(
                                R.string.chq_no));
                childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(childHolder.childpos).getChequeNumber());
                childHolder.chqRefTitleTv.setVisibility(View.VISIBLE);
                childHolder.chqRefTv.setVisibility(View.VISIBLE);
                childHolder.line1.setVisibility(View.VISIBLE);
            } else if (childHolder.paymentChldObj.get(childHolder.childpos).getCashMode()
                    .equals(StandardListMasterConstants.CREDIT_NOTE)) {
                childHolder.chqRefTitleTv.setText(mContext.getResources()
                        .getString(
                                R.string.ref_no));
                childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                        childHolder.childpos).getReferenceNumber());
                try {
                    childHolder.paymentOrChqDateTv
                            .setText(DateTimeUtils.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
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
                childHolder.chqRefTitleTv.setText(mContext.getResources()
                        .getString(
                                R.string.ref_no));
                childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                        childHolder.childpos).getChequeNumber());
                try {
                    childHolder.paymentOrChqDateTv
                            .setText(DateTimeUtils.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
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
                childHolder.chqRefTitleTv.setText(mContext.getResources()
                        .getString(
                                R.string.ref_no));
                childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                        childHolder.childpos).getChequeNumber());
                try {
                    childHolder.paymentOrChqDateTv
                            .setText(DateTimeUtils.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
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
                childHolder.chqRefTitleTv.setText(mContext.getResources()
                        .getString(
                                R.string.ref_no));
                childHolder.chqRefTv.setText(childHolder.paymentChldObj.get(
                        childHolder.childpos).getChequeNumber());
                try {
                    childHolder.paymentOrChqDateTv
                            .setText(DateTimeUtils.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
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
                            .setText(DateTimeUtils.convertFromServerDateToRequestedFormat(childHolder.paymentChldObj.get(
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
                    childHolder.modeTv.setText(mContext.getResources()
                            .getString(R.string.advance_payment));
                } else {
                    String paid_mode = childHolder.paymentChldObj.get(
                            childHolder.childpos).getListName();
                    childHolder.modeTv.setText(paid_mode);
                }

            } else {
                String paid_mode = childHolder.paymentChldObj.get(
                        childHolder.childpos).getListName();
                childHolder.modeTv.setText(paid_mode);
            }

            if (childPosition == (payment.size() - 1)) {
                childHolder.top_line.setVisibility(View.GONE);
            } else {
                childHolder.top_line.setVisibility(View.VISIBLE);
            }

            if (bModel.configurationMasterHelper.IS_COLLECTION_DELETE)
                childHolder.img_delete.setVisibility(View.VISIBLE);
            else
                childHolder.img_delete.setVisibility(View.GONE);

            childHolder.img_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteAlert(childPosition, (PaymentBO) getGroup(groupPosition), groupPosition);
                }
            });

            if (isLastChild && groupPosition != (collectionReportHelper
                    .getParentPaymentList().size()-1))
                childHolder.view_bottom_line.setVisibility(View.VISIBLE);
            else
                childHolder.view_bottom_line.setVisibility(View.GONE);

            row.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        }
        return row;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (collectionReportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId() != null
                && collectionReportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId().startsWith("AP"))
            return 0;
        else
            return collectionReportHelper.getChildPaymentList().get(groupPosition).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return collectionReportHelper.getParentPaymentList()
                .get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return collectionReportHelper.getParentPaymentList().size();
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
        if (collectionReportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId() != null
                && collectionReportHelper.getParentPaymentList().get(groupPosition).getAdvancePaymentId().startsWith("AP"))
            return 1;
        else
            return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final GroupViewHolder holder;
        PaymentBO paymentGroup = collectionReportHelper
                .getParentPaymentList().get(groupPosition);

        View row = convertView;
        if (row == null) {
            holder = new GroupViewHolder();
            if (getGroupType(groupPosition) == 1) {
                row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_advance_payment_report, parent,
                        false);
                holder.retailerNameTv = row
                        .findViewById(R.id.retailerNameTv);
                holder.adv_cash_modeTV = row.findViewById(R.id.cashMode);
                holder.advTitle = row.findViewById(R.id.advTitle);
                holder.date = row.findViewById(R.id.date);
                holder.invoiceAmt = row.findViewById(R.id.invoiceAmt);
                holder.invDateTv = row.findViewById(R.id.invDateTv);
                holder.adv_amountTV = row.findViewById(R.id.adv_amount);
                holder.view_divider = row.findViewById(R.id.bottom_line);
            } else {
                row = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_collection_report, parent,
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
                holder.view_divider = row.findViewById(R.id.bottom_line);

                @SuppressLint("RestrictedApi")
                Drawable drawable = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.activity_icon_next);
                Bitmap imageBitmap = fromDrawableToBitmap(drawable);
                holder.down_arrow.setImageBitmap(getRotatedBitmap(imageBitmap, 90));
            }
            row.setTag(holder);
        } else {
            holder = (GroupViewHolder) row.getTag();
        }

        try {
            if (isExpanded) {
                @SuppressLint("RestrictedApi")
                Drawable drawable = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.activity_icon_next);
                Bitmap imageBitmap = fromDrawableToBitmap(drawable);
                holder.down_arrow.setImageBitmap(getRotatedBitmap(imageBitmap, -90));
                holder.line.setVisibility(View.VISIBLE);
                holder.view_divider.setVisibility(View.GONE);
            } else {
                @SuppressLint("RestrictedApi")
                Drawable drawable = AppCompatDrawableManager.get().getDrawable(mContext, R.drawable.activity_icon_next);
                Bitmap imageBitmap = fromDrawableToBitmap(drawable);
                holder.down_arrow.setImageBitmap(getRotatedBitmap(imageBitmap, 90));
                holder.line.setVisibility(View.GONE);
                if (groupPosition != (collectionReportHelper
                        .getParentPaymentList().size()-1))
                holder.view_divider.setVisibility(View.VISIBLE);
                else
                    holder.view_divider.setVisibility(View.GONE);
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
                holder.invDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.paymentObj.getAdvancePaymentDate(),
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
                holder.invDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.paymentObj.getInvoiceDate(),
                        ConfigurationMasterHelper.outDateFormat));
            } catch (Exception e) {
                holder.invDateTv.setText("");
                Commons.printException(e);
            }
        }

        row.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
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

    class GroupViewHolder {
        TextView invNoTv, retailerNameTv, invAmtTv, invDateTv, outAmtTv, invoice, invoice_amt, invoice_date, os_amt;
        TextView adv_amountTV, adv_cash_modeTV, advTitle, date, invoiceAmt;
        ImageView down_arrow;
        PaymentBO paymentObj;
        LinearLayout line;
        View view_divider;
    }

    class ChildViewHolder {
        TextView modeTv, paymentOrChqDateTv, chqRefTitleTv, chqRefTv,
                paidAmtTv, disAmtTV, amountPaidTv, discAmtTv;
        ImageView top_line;
        ImageView img_delete;
        LinearLayout collectionDiscLL, line1, line2;
        List<PaymentBO> paymentChldObj;
        View view_bottom_line;
        int childpos;
    }

    private void showDeleteAlert(int childpos, PaymentBO paymentBO, int groupPos) {

        final CommonDialog commonDialog = new CommonDialog(mContext.getApplicationContext(),
                mContext,
                "",
                mContext.getResources().getString(R.string.delete_alert_common),
                false,
                mContext.getResources().getString(R.string.yes),
                mContext.getResources().getString(R.string.no),
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        PaymentBO childBO = collectionReportHelper.getChildPaymentList().get(groupPos).get(childpos);
                        if (collectionReportHelper.deletePayment(childBO.getUid())) {
                            reportFragment.updateViews(childBO);
                            paymentBO.setBalance(paymentBO.getBalance() + childBO.getAmount());
                            collectionReportHelper.getChildPaymentList().get(groupPos).remove(childpos);

                            if (collectionReportHelper.getChildPaymentList().get(groupPos).isEmpty()) {
                                reportFragment.collectionListView.collapseGroup(groupPos);
                                collectionReportHelper.getParentPaymentList().remove(groupPos);
                            }

                            if (collectionReportHelper.getParentPaymentList().isEmpty())
                                ((Activity)mContext).finish();
                            else
                                notifyDataSetChanged();

                        }
                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

}



