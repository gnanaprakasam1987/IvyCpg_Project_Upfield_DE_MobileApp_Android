package com.ivy.utils.view.Dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;

import java.util.ArrayList;

public class ReasonCaptureDialog extends Dialog implements ReasonAdapter.onItemClickListener {

    private OnButtonClickListener onButtonClickListener;
    private int selectedReasonId = 0;
    private int lastSelectedPosition = -1;
    private ReasonAdapter reasonAdapter;
    private String titleMsg;
    private String reasonTitle;

    public ReasonCaptureDialog(ReasonCaptureDialogBuilder reasonCaptureDialogBuilder) {
        super(reasonCaptureDialogBuilder.context);
        Context context = reasonCaptureDialogBuilder.context;
        ArrayList<ReasonMaster> reasonMasterArrayList = reasonCaptureDialogBuilder.reasonMasterArrayList;
        this.titleMsg = reasonCaptureDialogBuilder.titleMsg;
        this.reasonTitle = reasonCaptureDialogBuilder.reasonTitle;
        this.onButtonClickListener = reasonCaptureDialogBuilder.buttonClickListener;
        setContentView(R.layout.custom_dialog_nonvisit_reason);
        this.setCancelable(false);

        TextView reasonTitleTxt = findViewById(R.id.reason_text);
        TextView headerText = findViewById(R.id.textView9);
        findViewById(R.id.add_reason1).setVisibility(View.GONE);
        findViewById(R.id.add_reason).setVisibility(View.VISIBLE);


        if (titleMsg != null) {
            headerText.setText(titleMsg);
            findViewById(R.id.header_divider).setVisibility(View.VISIBLE);
        } else {
            headerText.setVisibility(View.GONE);
            findViewById(R.id.header_divider).setVisibility(View.GONE);
        }

        if (reasonTitle != null)
            reasonTitleTxt.setText(reasonTitle);
        else
            reasonTitleTxt.setVisibility(View.GONE);

        findViewById(R.id.add_reason).setOnClickListener(v -> {
            lastSelectedPosition = -1;
            onButtonClickListener.addReason(selectedReasonId);
        });

        findViewById(R.id.cancel_reason).setOnClickListener(
                v -> onButtonClickListener.onDismiss());

        RecyclerView reason_recycler = findViewById(R.id.reason_recycler);
        reason_recycler.setLayoutManager(new GridLayoutManager(context, 2));
        reasonAdapter = new ReasonAdapter(context, reasonMasterArrayList, this, lastSelectedPosition);
        reason_recycler.setAdapter(reasonAdapter);

    }

    public ReasonCaptureDialog setDialogTitles(String titleMsg, String reasonTitle) {
        this.titleMsg = titleMsg;
        this.reasonTitle = reasonTitle;
        return this;
    }

    @Override
    public void updateSelectedItem(ReasonMaster selectedReasonBo, int lastSelectedPosition) {
        this.lastSelectedPosition = lastSelectedPosition;
        selectedReasonId = SDUtil.convertToInt(selectedReasonBo.getReasonID());
        reasonAdapter.notifyDataSetChanged();
    }

    public interface OnButtonClickListener {
        void addReason(int selectedResId);

        void onDismiss();
    }

    public static class ReasonCaptureDialogBuilder {

        private String titleMsg;
        private String reasonTitle;
        private Context context;
        private ArrayList<ReasonMaster> reasonMasterArrayList;
        private OnButtonClickListener buttonClickListener;

        public ReasonCaptureDialogBuilder(Context context, ArrayList<ReasonMaster> reasonMasterArrayList) {
            this.context = context;
            this.reasonMasterArrayList = reasonMasterArrayList;
        }


        public ReasonCaptureDialogBuilder setDialogTitles(String titleMsg, String reasonTitle) {
            this.titleMsg = titleMsg;
            this.reasonTitle = reasonTitle;
            return this;
        }

        public ReasonCaptureDialogBuilder headerTitle(String titleMsg) {
            this.titleMsg = titleMsg;
            return this;
        }

        public ReasonCaptureDialogBuilder reasonTitle(String reasonTitle) {
            this.reasonTitle = reasonTitle;
            return this;
        }


        public ReasonCaptureDialogBuilder setOnButtonClickListener(OnButtonClickListener listener) {
            this.buttonClickListener = listener;
            return this;
        }

        public ReasonCaptureDialog buildDialog() {
            return new ReasonCaptureDialog(this);
        }

    }
}
