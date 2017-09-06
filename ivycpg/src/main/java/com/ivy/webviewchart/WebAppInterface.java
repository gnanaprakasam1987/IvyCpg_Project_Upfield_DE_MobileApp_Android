package com.ivy.webviewchart;

import android.content.Context;
import android.os.Handler;
import android.webkit.JavascriptInterface;

import com.ivy.sd.png.util.Commons;

interface JavascriptCallback {

}

public class WebAppInterface implements JavascriptCallback {
	Context mContext;
	Handler mWebHandler;

    /** Instantiate the interface and set the context 
     * @param handler */
    public WebAppInterface(Context c, Handler handler) {
        mContext = c;
        mWebHandler = handler;
    }

    @JavascriptInterface
    public void notifyJavaInterface(String message) {
    	try {
			if(message.equalsIgnoreCase("DomLoaded"))	mWebHandler.sendEmptyMessage(0);
			if(message.equalsIgnoreCase("SemiCircleDomLoaded"))	mWebHandler.sendEmptyMessage(1);
			if(message.equalsIgnoreCase("ColumnstackedPercentDomLoaded"))	mWebHandler.sendEmptyMessage(2);
			if(message.equalsIgnoreCase("ChartLoaded")) mWebHandler.sendEmptyMessage(3);
		} catch (Exception e) {
			Commons.printException(e);
		}
    }
}
