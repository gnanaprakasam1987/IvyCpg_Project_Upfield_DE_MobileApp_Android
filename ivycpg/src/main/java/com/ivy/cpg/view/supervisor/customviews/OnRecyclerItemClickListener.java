package com.ivy.cpg.view.supervisor.customviews;

import android.view.View;

import com.ivy.cpg.view.supervisor.mvp.models.ManagerialBO;

/**
 * Created by ramkumard on 28/2/19.
 */

public interface OnRecyclerItemClickListener {
    void onItemClick(View view, ManagerialBO item, int position);
}
