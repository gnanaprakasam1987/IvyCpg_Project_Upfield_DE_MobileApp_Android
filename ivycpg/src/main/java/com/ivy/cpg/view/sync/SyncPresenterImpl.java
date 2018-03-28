package com.ivy.cpg.view.sync;

import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;

/**
 * Created by rajkumar on 16/3/18.
 */

public class SyncPresenterImpl {

    private Context mContext;
    private SynchronizationHelper mSyncHelper;
    private BusinessModel mBModel;

    public SyncPresenterImpl(Context mContext, BusinessModel mBModel, SynchronizationHelper mSyncHelper) {
        this.mBModel = mBModel;
        this.mSyncHelper = mSyncHelper;
        this.mContext = mContext;
    }
}
