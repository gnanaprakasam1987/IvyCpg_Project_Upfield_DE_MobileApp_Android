package com.ivy.cpg.view.reports.webviewreport;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * Created by rajkumar.s on 29-04-2016.
 */
public class SOreportFragment extends Fragment {
    View view;
    BusinessModel bmodel;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.so_report_fragment, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        try {
            WebView browser = (WebView) getActivity().findViewById(R.id.webview);
            browser.getSettings().setJavaScriptEnabled(true);
            browser.getSettings().setAllowFileAccess(false);
            browser.getSettings().setDomStorageEnabled(true);
            browser.getSettings().setAllowUniversalAccessFromFileURLs(true);
            browser.loadUrl(WebViewReportHelper.getInstance(getActivity().getApplicationContext()).getWebReportUrl() + bmodel.userMasterHelper.getUserMasterBO().getUserid());//"http://192.168.1.155/IvyCPG/IvyCPG_Piramal_Ind_Web/dayreport/Index");
            browser.setWebViewClient(new WebViewClient());
        }
        catch (Exception e){
            Commons.printException(e);
        }

    }
}
