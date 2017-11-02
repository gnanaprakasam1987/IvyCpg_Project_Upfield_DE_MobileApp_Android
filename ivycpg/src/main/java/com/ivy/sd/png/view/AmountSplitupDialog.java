package com.ivy.sd.png.view;

import android.content.DialogInterface;
import android.os.Bundle;
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
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

public class AmountSplitupDialog extends DialogFragment {

    private BusinessModel bmodel;
    View view;

    private ImageView closeBTN;

    private double totalValue, companyDis, distributorDis, totalSchemeDiscValue;

    private TextView tvOrderValue, tv_comy_disc, tv_dist_disc, tv_total_value;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
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

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        closeBTN = (ImageView) view.findViewById(R.id.closeBTN);
        tvOrderValue = (TextView) view.findViewById(R.id.tvOrderValue);
        tv_comy_disc = (TextView) view.findViewById(R.id.tv_comy_disc);
        tv_dist_disc = (TextView) view.findViewById(R.id.tv_dist_disc);
        tv_total_value = (TextView) view.findViewById(R.id.tv_total_value);

        ((View) view.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);


        ((TextView) view.findViewById(R.id.tvTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        ((TextView) view.findViewById(R.id.tvTitleDeduction)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.lbl_total)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        ((TextView) view.findViewById(R.id.lblOrderValue)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) view.findViewById(R.id.lbl_comy_disc)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) view.findViewById(R.id.lbl_dist_disc)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        tvOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_comy_disc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_dist_disc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_total_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

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
        tvOrderValue.setText(bmodel.formatValue(totalValue + distributorDis + companyDis));

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
