package com.ivy.sd.png.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ivy.cpg.view.login.LoginBaseActivity;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.cpg.view.tradeCoverage.adhocPlanning.AdhocPlanningFragment;
import com.ivy.sd.png.view.NewOutletFragment.NewRetailerReceiver;
import com.ivy.sd.png.view.SynchronizationFragment;
import com.ivy.cpg.view.attendance.TLAttendanceActivity;
import com.ivy.cpg.view.profile.ProfileActivity;


public class DownloadReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent broadCIntent = new Intent();
        Bundle bundle = intent.getExtras();
        SynchronizationHelper.FROM_SCREEN fromWhere = (SynchronizationHelper.FROM_SCREEN) bundle.get("isFromWhere");

        switch (fromWhere) {
            case LOGIN:
                broadCIntent.setAction(LoginBaseActivity.MyReceiver.PROCESS_RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case SYNC:
                broadCIntent.setAction(SynchronizationFragment.SyncronizationReceiver.PROCESS_RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case NEW_RETAILER:
                broadCIntent.setAction(NewRetailerReceiver.PROCESS_RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case VISIT_SCREEN:
                broadCIntent.setAction(ProfileActivity.UserRetailerTransactionReceiver.PROCESS_RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case RETAILER_SELECTION:
                broadCIntent.setAction(AdhocPlanningFragment.RetailerSelectionReceiver.RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case COUNTER_SALES_SELECTION:

                break;
            case TL_ALLOCATION:
                broadCIntent.setAction(TLAttendanceActivity.TeamLeadReceiver.PROCESS_RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            default:
                break;
        }


    }

}
