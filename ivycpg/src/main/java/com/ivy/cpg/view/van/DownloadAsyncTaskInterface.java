package com.ivy.cpg.view.van;

import android.app.AlertDialog;

/**
 * Created by Hanifa on 20/8/18.
 */

public interface DownloadAsyncTaskInterface {
    void showProgress(AlertDialog.Builder builder);
    void hideProgress();
    void intentCall(String menuCode,String menuName);
    void loadMethods(String menuCode,String menuName);
}
