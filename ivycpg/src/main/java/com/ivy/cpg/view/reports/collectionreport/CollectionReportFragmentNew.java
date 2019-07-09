package com.ivy.cpg.view.reports.collectionreport;


import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatDrawableManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PaymentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.StandardListMasterConstants;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class CollectionReportFragmentNew extends IvyBaseFragment implements ICollectionReportView {

    private Unbinder unbinder;
    private BusinessModel bModel;

    private ICollectionReportModelPresenter mCollectionReportModelPresenter;


    Double totalCash = 0.0;
    Double totalCheque = 0.0;
    Double totalColl = 0.0;
    Double totalDD = 0.0;
    Double totalRTGS = 0.0;
    Double total_mob_payment = 0.0;
    Double totalCn = 0.0;
    Double totalAd = 0.0;

    TextView tv_collected;
    TextView tv_cash;
    TextView tv_cheque;
    TextView tv_dd;
    TextView tv_cn;
    TextView tv_rtgs;
    TextView tv_mob_pymt;
    TextView tv_ad;

    CollectionReportHelper reportHelper;

    @BindView(R.id.collection_listview)
    ExpandableListView collectionListView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        initializeBusinessModel();
    }

    private void initializeBusinessModel() {
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());
    }

    public void initializeModel() {
        mCollectionReportModelPresenter = new CollectionReportModel(getActivity(), CollectionReportFragmentNew.this);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.collection_report_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);
        initializeModel();
        mCollectionReportModelPresenter.loadCollectionReport();
        mCollectionReportModelPresenter.setUpAdapter();
        reportHelper =  mCollectionReportModelPresenter.getReportHelper();
        updateDetails(reportHelper);
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
        tv_collected = view.findViewById(R.id.tot);
        tv_cash = view.findViewById(R.id.totcash);
        tv_cheque = view.findViewById(R.id.tocheque);
        tv_dd = view.findViewById(R.id.total_dd);
        tv_cn = view.findViewById(R.id.totCn);
        tv_rtgs = view.findViewById(R.id.total_rtgs);
        tv_mob_pymt = view.findViewById(R.id.total_mob_payment);
        tv_ad = view.findViewById(R.id.totAd);

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

    private void updateDetails(CollectionReportHelper collectionReportHelper) {
        if (collectionReportHelper.getLstPaymentBObyGroupId() != null)
            for (String groupid : collectionReportHelper.getLstPaymentBObyGroupId().keySet()) {
                for (int i = 0; i < collectionReportHelper.getLstPaymentBObyGroupId().get(groupid).size(); i++) {
                    PaymentBO payBO = collectionReportHelper.getLstPaymentBObyGroupId().get(groupid).get(i);
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void setAdapter(CollectionFragmentAdapter collectionAdapter) {
        collectionListView.setAdapter(collectionAdapter);
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

        CollectionReportHelper reportHelper =  mCollectionReportModelPresenter.getReportHelper();
        if (i == R.id.menu_print) {
            if (reportHelper.getParentPaymentList() != null && reportHelper.getParentPaymentList().size() > 0) {

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

    public void updateViews(PaymentBO payBO) {


        switch (payBO.getCashMode()) {
            case StandardListMasterConstants.CASH:
                totalCash = totalCash - payBO.getAmount();
                break;
            case StandardListMasterConstants.CHEQUE:
                totalCheque = totalCheque - payBO.getAmount();
                break;
            case StandardListMasterConstants.DEMAND_DRAFT:
                totalDD = totalDD - payBO.getAmount();
                break;
            case StandardListMasterConstants.RTGS:
                totalRTGS = totalRTGS - payBO.getAmount();
                break;
            case StandardListMasterConstants.MOBILE_PAYMENT:
                total_mob_payment = total_mob_payment - payBO.getAmount();
                break;
            case StandardListMasterConstants.CREDIT_NOTE:
                if (payBO.getReferenceNumber().startsWith("AP"))
                    totalAd = totalAd - payBO.getAmount();
                else
                    totalCn = totalCn - payBO.getAmount();
                break;
        }


        totalColl = totalCash + totalCheque + totalDD + totalRTGS + total_mob_payment;

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
    }
}
