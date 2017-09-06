package com.ivy.sd.print;

import android.os.AsyncTask;
import android.util.Log;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.aem.api.IAemScrybe;
import com.ivy.sd.png.util.Commons;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nagaganesh.n on 10/7/2016.
 */
public class ScribePrinter extends AsyncTask<Void, Void, Boolean> implements IAemScrybe {

    private ScribeListener onScribeListener;
    private AEMScrybeDevice m_AemScrybeDevice;
    private AEMPrinter m_AemPrinter = null;

    public ScribePrinter(ScribeListener onScribeListener) {
        this.onScribeListener = onScribeListener;
        this.m_AemScrybeDevice = new AEMScrybeDevice(ScribePrinter.this);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        ArrayList<String> aemPrinterList = m_AemScrybeDevice.getPairedPrinters();
        if (aemPrinterList != null) {
            for (int i = 0; i < aemPrinterList.size(); i++) {
                Log.v("Check", aemPrinterList.get(i));
            }
            if (aemPrinterList.size() > 0)
                m_AemPrinter = connect(aemPrinterList.get(0).toString());
        }

        if (m_AemPrinter != null) {
            return true;
        }

        return false;
    }

    @Override
    protected void onPostExecute(Boolean printerStatus) {
        super.onPostExecute(printerStatus);
        onScribeListener.isScribeResponse(m_AemPrinter, m_AemScrybeDevice, printerStatus);
    }

    @Override
    public void onDiscoveryComplete(ArrayList<String> arrayList) {

    }

    private AEMPrinter connect(String printerName) {
        try {
            Log.v("Check", "connecting");
            m_AemScrybeDevice.connectToPrinter(printerName);
            m_AemPrinter = m_AemScrybeDevice.getAemPrinter();
            Log.v("Check", "connected");

        } catch (IOException e) {
            Commons.printException(""+e);
            disConnectScribe();
            m_AemPrinter = null;
            return m_AemPrinter;
        }

        return m_AemPrinter;
    }

    private void disConnectScribe() {
        if (m_AemScrybeDevice != null) {
            try {
                m_AemScrybeDevice.disConnectPrinter();
            } catch (IOException e) {
                Commons.printException(""+e);
            }
        }
    }

    public interface ScribeListener {
        void isScribeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected);
    }
}





