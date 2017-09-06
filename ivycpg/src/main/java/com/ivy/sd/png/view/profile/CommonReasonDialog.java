package com.ivy.sd.png.view.profile;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.StateListDrawable;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by hanifa.m on 3/22/2017.
 */

public class CommonReasonDialog extends Dialog {
    private BusinessModel bmodel;
    private Context context;
    private String listLoad;
    private TextView reasonVisitTxt, headerText;
    private Button addReason, cancelReason, addReason1;
    private AddNonVisitListener addNonVisitListener;
    private ReasonMaster temp;
    private boolean isdialog = false;

    public CommonReasonDialog(Context context) {
        super(context);

    }

    public CommonReasonDialog(final Context context, final String listLoad) {
        super(context);

        this.context = context;
        this.listLoad = listLoad;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        bmodel = (BusinessModel) context.getApplicationContext();
        /*rl = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_order_processing, null);*/
        setContentView(R.layout.custom_dialog_nonvisit_reason);
        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7inch" tablet
        if (sizeLarge == 3)
            isdialog = true;

        if (isdialog)
            getWindow().setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        else if (!isdialog)
            getWindow().setLayout(1000, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.setCancelable(false);

        reasonVisitTxt = (TextView) findViewById(R.id.reason_text);
        headerText = (TextView) findViewById(R.id.textView9);
        addReason = (Button) findViewById(R.id.add_reason);
        addReason1 = (Button) findViewById(R.id.add_reason1);
        cancelReason = (Button) findViewById(R.id.cancel_reason);

        //set typface
        reasonVisitTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        headerText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        cancelReason.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        addReason.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        addReason1.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        cancelReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        addReason.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (listLoad.equals("nonVisit")) {
                        temp = selected_reason;
                        NonproductivereasonBO nonproductive = new NonproductivereasonBO();
                        nonproductive.setReasonid(temp.getReasonID());
                        nonproductive.setReasontype("NV");
                        nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO()
                                .getDownloadDate());
                        bmodel.saveNonproductivereason(nonproductive);
                        bmodel.getRetailerMasterBO().setHasNoVisitReason(true);
                        addNonVisitListener.addReatailerReason();
                        dismiss();
                    } else if (listLoad.equals("deviate")) {
                        bmodel.reasonHelper.setDeviate(bmodel.retailerMasterBO.getRetailerID(),
                                selected_reason, bmodel.retailerMasterBO.getBeatID());
                        addNonVisitListener.addReatailerReason();
                        dismiss();
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }

            }
        });

        RecyclerView reason_recycler = (RecyclerView) findViewById(R.id.reason_recycler);
        reason_recycler.setLayoutManager(new GridLayoutManager(context, 2));
        if (listLoad.equals("nonVisit")) {
            headerText.setText(context.getResources().getString(R.string.reason_non_visit));
            reasonVisitTxt.setVisibility(View.GONE);
            reason_recycler.setAdapter(new ReasonAdapter(bmodel.reasonHelper.getNonVisitReasonMaster()));
        } else if (listLoad.equals("deviate")) {
            reasonVisitTxt.setVisibility(View.VISIBLE);
            reason_recycler.setAdapter(new ReasonAdapter(bmodel.reasonHelper.getDeviatedReturnMaster()));
        }

    }

    private ReasonMaster selected_reason;

    class ReasonAdapter extends RecyclerView.Adapter<ReasonAdapter.ViewHolder> {

        private ArrayList<ReasonMaster> items;
        private int lastCheckedPosition = -1;

        public ReasonAdapter(ArrayList<ReasonMaster> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.common_reason_popup_recycler_items, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            holder.reasonObj = items.get(position);
            holder.reason_radio_btn.setText(holder.reasonObj.getReasonDesc());


//            holder.reason_radio_btn.setButtonDrawable(holder.mState1);

            holder.reason_radio_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addReason1.setVisibility(View.GONE);
                    addReason.setVisibility(View.VISIBLE);
                    addReason.setEnabled(true);
                    addReason.setClickable(true);
                    lastCheckedPosition = getItemViewType(position);
                    selected_reason = holder.reasonObj;
                    notifyDataSetChanged();
                }
            });
            holder.reason_radio_btn.setChecked(position == lastCheckedPosition);


        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            AppCompatRadioButton reason_radio_btn;
            ReasonMaster reasonObj;
            StateListDrawable mState1;

            public ViewHolder(View v) {
                super(v);
                reason_radio_btn = (AppCompatRadioButton) v.findViewById(R.id.reason_radio_btn);
                reason_radio_btn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
//                mState1 = new StateListDrawable();
//
//                mState1.addState(new int[]{android.R.attr.state_pressed},
//                        context.getResources().getDrawable(R.drawable.radiobutton_green));
//                mState1.addState(new int[]{android.R.attr.state_focused},
//                        context.getResources().getDrawable(R.drawable.radiobutton_green));
//                mState1.addState(new int[]{android.R.attr.state_checked},
//                        context.getResources().getDrawable(R.drawable.radiobutton_green));
//                mState1.addState(new int[]{},
//                        context.getResources().getDrawable(R.drawable.radiobutton_grey));
            }
        }
    }


    public interface AddNonVisitListener {
        void addReatailerReason();
    }


    public void setNonvisitListener(AddNonVisitListener listener) {
        this.addNonVisitListener = listener;
    }
}
