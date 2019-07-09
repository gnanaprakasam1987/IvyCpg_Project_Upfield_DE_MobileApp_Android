package com.ivy.cpg.view.order;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

public class AmountSplitUpDialog extends DialogFragment {

    private BusinessModel bmodel;
    View view;

    private ImageView closeBTN;

    private double totalValue, companyDis, distributorDis, totalSchemeDiscValue, tax;

    private TextView tvOrderValue, tv_comy_disc, tv_dist_disc, tv_total_value, tv_scheme, tv_discount, tv_tax;
    private String schemes, discounts;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        view = inflater.inflate(R.layout.fragment_amt_split_up, container, false);

        totalValue = getArguments().getDouble("totalOrderValue");
        companyDis = getArguments().getDouble("cmy_disc");
        distributorDis = getArguments().getDouble("dist_disc");
        totalSchemeDiscValue = getArguments().getDouble("scheme_disc");
        schemes = getArguments().getString("scheme_name");
        discounts = getArguments().getString("disc_name");
        tax = getArguments().getDouble("tax_value");

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        closeBTN = view.findViewById(R.id.closeBTN);
        tvOrderValue = view.findViewById(R.id.tvOrderValue);
        tv_comy_disc = view.findViewById(R.id.tv_comy_disc);
        tv_dist_disc = view.findViewById(R.id.tv_dist_disc);
        tv_total_value = view.findViewById(R.id.tv_total_value);
        tv_scheme = view.findViewById(R.id.tv_scheme);
        tv_discount = view.findViewById(R.id.tv_discount);
        tv_tax = view.findViewById(R.id.tv_taxValue);

        (view.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        ((TextView) view.findViewById(R.id.tvTitle)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tvTitleDeduction)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.lbl_total)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.lblOrderValue)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.lbl_comy_disc)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.lbl_dist_disc)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.lbl_tax)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        tvOrderValue.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        tv_comy_disc.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        tv_dist_disc.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        tv_total_value.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        tv_scheme.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.THIN));
        tv_discount.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.THIN));
        tv_tax.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.lbl_comy_disc).getTag()) != null)
                ((TextView) view.findViewById(R.id.lbl_comy_disc))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.lbl_comy_disc)
                                        .getTag()));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.lbl_dist_disc).getTag()) != null)
                ((TextView) view.findViewById(R.id.lbl_dist_disc))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.lbl_dist_disc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        return view;
    }


    public boolean isShowing() {
        if (getDialog() != null) {
            return true;
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }

        int dialogHeight = (int) getActivity().getResources().getDimension(R.dimen.dialog_height); // specify a value here

        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

        tv_comy_disc.setText("- " + bmodel.formatValue(companyDis + totalSchemeDiscValue));
        tv_dist_disc.setText("- " + bmodel.formatValue(distributorDis));
        tv_total_value.setText(bmodel.formatValue(totalValue));
        tvOrderValue.setText(bmodel.formatValue((totalValue + distributorDis + companyDis + totalSchemeDiscValue) - tax));
        tv_scheme.setText(schemes);
        tv_discount.setText(discounts);
        tv_tax.setText(bmodel.formatValue(tax));

        closeBTN.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });


    }

    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }


}
