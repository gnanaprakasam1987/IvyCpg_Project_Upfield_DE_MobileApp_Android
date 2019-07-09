package com.ivy.ui.dashboard.util;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import com.ivy.cpg.view.dashboard.DashBoardBO;

import java.util.List;

public class DashboardListDiffUtil extends DiffUtil.Callback {


    private List<DashBoardBO> mOldList;
    private List<DashBoardBO> mNewList;


    public DashboardListDiffUtil(List<DashBoardBO> oldList, List<DashBoardBO> newList) {
        this.mOldList = oldList;
        this.mNewList = newList;
    }


    @Override
    public int getOldListSize() {
        return mOldList != null ? mOldList.size() : 0;    }

    @Override
    public int getNewListSize() {
        return mNewList != null ? mNewList.size() : 0;
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return !mNewList.get(newItemPosition).getText().equalsIgnoreCase(mOldList.get(oldItemPosition).getText());
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return !mNewList.get(newItemPosition).getText().equalsIgnoreCase(mOldList.get(oldItemPosition).getText());
    }


    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {

        DashBoardBO oldDashboardBo = mOldList.get(oldItemPosition);
        DashBoardBO newDashboardBo = mNewList.get(newItemPosition);

        Bundle diff = new Bundle();

        if(!newDashboardBo.getText().equalsIgnoreCase(oldDashboardBo.getText()))
            diff.putString("TEXT",newDashboardBo.getText());

        if (diff.size() == 0) {
            return null;
        }

        return diff;
    }
}
