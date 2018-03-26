package com.ivy.sd.png.model;

import android.app.Activity;
import android.os.Handler;

/**
 * Created by rajkumar on 20/3/18.
 */

public class CallAnalysisUploadThread extends Thread {

    private Activity ctx;
    private int opt;
    Handler handler;


    public CallAnalysisUploadThread(Activity ctx, int opt) {
        this.ctx = ctx;
        this.opt = opt;
    }

    public CallAnalysisUploadThread(Activity ctx, int opt, Handler handler) {
        this.ctx = ctx;
        this.handler = handler;
        this.opt = opt;

    }
}
