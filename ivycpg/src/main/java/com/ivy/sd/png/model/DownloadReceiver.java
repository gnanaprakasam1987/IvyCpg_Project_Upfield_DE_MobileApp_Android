package com.ivy.sd.png.model;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.ivy.countersales.CustomerVisitFragment;
import com.ivy.cpg.login.LoginScreen.MyReceiver;
import com.ivy.cpg.view.van.LoadManagementScreen;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.view.AdhocPlanningFragment;
import com.ivy.sd.png.view.NewOutletFragment.NewRetailerReceiver;
import com.ivy.sd.png.view.SynchronizationFragment;
import com.ivy.sd.png.view.TLAttendanceActivity;
import com.ivy.sd.png.view.profile.ProfileActivity;

public class DownloadReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Intent broadCIntent = new Intent();
        Bundle bundle = intent.getExtras();
        SynchronizationHelper.FROM_SCREEN fromWhere = (SynchronizationHelper.FROM_SCREEN) bundle.get("isFromWhere");

        switch (fromWhere) {
            case LOGIN:
                broadCIntent.setAction(MyReceiver.PROCESS_RESPONSE);
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
            case LOAD_MANAGEMENT:
                broadCIntent.setAction(LoadManagementScreen.Loadmanagemntreceiver.RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case RETAILER_SELECTION:
                broadCIntent.setAction(AdhocPlanningFragment.RetailerSelectionReceiver.RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
                break;
            case COUNTER_SALES_SELECTION:
                broadCIntent.setAction(CustomerVisitFragment.MyReceiver.PROCESS_RESPONSE);
                broadCIntent.putExtras(bundle);
                context.sendBroadcast(broadCIntent);
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
