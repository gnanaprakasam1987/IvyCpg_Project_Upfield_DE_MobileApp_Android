package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

/**
 * Created by dharmapriya.k on 1/24/2017,12:32 PM.
 */
public class SyncDownloadStatusDialog extends Dialog {
    public SyncDownloadStatusDialog(Context context, String msg, DisplayMetrics displaymetrics) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        /*rl = (RelativeLayout) LayoutInflater.from(context)
                .inflate(R.layout.dialog_order_processing, null);*/
        setContentView(R.layout.sync_success_popup);

        getWindow().setLayout(displaymetrics.widthPixels - 100, displaymetrics.heightPixels / 3);


        /*lp.copyFrom(window.getAttributes());
        lp.width = displaymetrics.widthPixels-50;
        lp.height = (int) (displaymetrics.heightPixels/1.1);
        window.setAttributes(lp);*/
        this.setCancelable(false);

        final RelativeLayout close = (RelativeLayout) this.findViewById(R.id.rl_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        TextView response_msg = (TextView) this.findViewById(R.id.response_msg);
        response_msg.setText(msg);
    }
}
