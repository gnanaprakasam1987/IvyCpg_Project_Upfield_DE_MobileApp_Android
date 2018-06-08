package com.ivy.cpg.view.reports.dayreport;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class DayReportFragment extends IvyBaseFragment implements IDayReportView {

    private IDayReportModelPresenter dayReportModelPresenter;
    @Inject
    public BusinessModel bModel;
    public static final String ZEBRA_3INCH = "3";

    @BindView(R.id.gridview)
    GridView dayReportGrid;

    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBusinessModel();
        initModel();

    }

    private void initModel() {
        dayReportModelPresenter = new DayReportModel(getActivity(), DayReportFragment.this, bModel);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_daily_report_new, container,
                false);

        unbinder = ButterKnife.bind(this, view);
        if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        try {
            if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.nametv).getTag()) != null)
                ((TextView) view.findViewById(R.id.nametv)).
                        setText(bModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.nametv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }


        try {
            if (bModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.valuetv).getTag()) != null)
                ((TextView) view.findViewById(R.id.valuetv))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.valuetv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        dayReportModelPresenter.downloadData();

        return view;
    }


    private void initializeBusinessModel() {
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());


    }

    //private Connection zebraPrinterConnection;
    private AlertDialog alertDialog;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_dayreport, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bModel.configurationMasterHelper.IS_DAY_REPORT_PRINT)
            menu.findItem(R.id.menu_print).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_print) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, "Printing....");
            alertDialog = builder.create();
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    DayReportPrintHelper dayReportPrintHelper = new DayReportPrintHelper(bModel, getActivity(), alertDialog, dayReportModelPresenter);
                    dayReportPrintHelper.doConnection(ZEBRA_3INCH);
                    Looper.loop();
                    //noinspection ConstantConditions
                    Looper.myLooper().quit();
                }
            }).start();
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }


    @Override
    public void setAdapter(MyAdapter myAdapter) {
        dayReportGrid.setAdapter(myAdapter);
    }

    @Override
    public void onDestroy() {

        unbinder.unbind();
        bModel = null;
        dayReportModelPresenter.destroy();
        super.onDestroy();


    }


   /* private void doConnection(String printerName) {
        try {
            bModel.vanmodulehelper.downloadSubDepots();
            ZebraPrinter printer = connect();
            if (printer != null) {
                printInvoice(printerName);
            } else {
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Printer not connected ..", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void printInvoice(String printerName) {

        try {
            if (printerName.equals(ZEBRA_3INCH)) {

                zebraPrinterConnection.write(dayReportModelPresenter.printDataFor3InchPrinter());
                alertDialog.dismiss();

                bModel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 0);
            }

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) zebraPrinterConnection)
                        .getFriendlyName();

                Commons.print(TAG + "friendlyName : " + friendlyName);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            Commons.printException(e);

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            disconnect();
        }
    }


    private static final String TAG = "DailyReportFragmentNew";
    // boolean isPrinterLanguageDetected = false;

    public ZebraPrinter connect() {

        zebraPrinterConnection = new BluetoothConnection(getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(getActivity(), getMacAddressFieldText());

        Commons.print(TAG + "PRINT MAC : " + getMacAddressFieldText());

        try {
            zebraPrinterConnection.open();
        } catch (ConnectionException e) {
            Commons.printException(e);
            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {
            Commons.printException(e);
        }

        ZebraPrinter printer = null;

        //  isPrinterLanguageDetected = false;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                PrinterLanguage pl = printer.getPrinterControlLanguage();

                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
                // isPrinterLanguageDetected = true;
            } catch (ConnectionException e) {

                Commons.print(TAG + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException(e);
                // isPrinterLanguageDetected = false;
            }
        }

        return printer;
    }


    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            SharedPreferences pref = getActivity().getSharedPreferences("PRINT",
                    Context.MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");

        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }


    private void disconnect() {
        try {
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
                zebraPrinterConnection = null;
            }
        } catch (ConnectionException e) {
            Commons.printException(e);
        }
    }*/
}
