package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

/**
 * Created by nivetha.s on 06-04-2016.
 */
public class InOutReasonDialog extends Dialog {

    Spinner reason_spnr;
    ArrayAdapter<ReasonMaster> dataAdapter;
    OnDismissListener listener;
    Context context;
    private OnMyDialogResult Result;
    private BusinessModel bmodel;

    protected InOutReasonDialog(final Context context,OnMyDialogResult mDialogResult) {
        super(context);
        this.context = context;
        bmodel = (BusinessModel) context.getApplicationContext();
//        bmodel.setContext(context);
        this.Result = mDialogResult;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.in_out_reason_dialog);

        reason_spnr = (Spinner) findViewById(R.id.reason_spnr);
        reason_spnr.setVisibility(View.VISIBLE);
        dataAdapter = new ArrayAdapter<ReasonMaster>(context,
                R.layout.spinner_bluetext_layout);
        dataAdapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_reason)));
        loadInOutReason();
        dataAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        reason_spnr.setAdapter(dataAdapter);
        Button btn_ok=(Button) findViewById(R.id.btn_ok);
        TextView titleBar=(TextView) findViewById(R.id.titleBar);
        TextView must_sell_message_tv=(TextView) findViewById(R.id.must_sell_message_tv);
        btn_ok
                .setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Commons.print((ReasonMaster) reason_spnr
                                .getSelectedItem()
                                + " "
                                + reason_spnr.getSelectedItemPosition());
                        if (!((ReasonMaster) reason_spnr.getSelectedItem()).getReasonID()
                                .equals("0")) {

                            Result.cancel(((ReasonMaster) reason_spnr
                                    .getSelectedItem()).getReasonID());
                        }

                    }
                });
        btn_ok.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        titleBar.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        must_sell_message_tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
    }

    public void loadInOutReason() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            /*Cursor c = db.selectSQL("select reasonid,reasondescription from "
                    + DataMembers.tbl_ReasonMaster
                    + " where reasonCategory='ATR'");*/

            String s = "SELECT ListId, ListName FROM StandardListMaster WHERE ListType = 'REASON'"
                    + " AND ParentId = (SELECT ListId FROM StandardListMaster WHERE ListType ='REASON_TYPE' AND ListCode = 'ATR')";
            Cursor c = db.selectSQL(s);
            if (c != null) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    dataAdapter.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public interface OnMyDialogResult {


        void cancel(String reasonid);
    }

    public void setDialogResult(OnMyDialogResult onMyDialogResult) {
        Result = onMyDialogResult;

    }
}
