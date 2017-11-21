package com.ivy.sd.png.view.van;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.VanLoadMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.regex.Pattern;

public class OdaMeterScreen extends IvyBaseActivityNoActionBar implements OnClickListener {

    private BusinessModel bmodel;
    private EditText tripStarting;
    private EditText tripEnding;
    private TextView distanceCoveredEt;

    private double startingvalue;
    private double endingvalue;
    private double distanceCovered;
    private VanLoadMasterBO product;
    private Button startjourney;
    private Button endjourney;
    private TypedArray typearr;
    private Toolbar toolbar;
    private TextView datevalue, timevalue, timevaluestart, endtimevalue, timeend, enddatevalue;
    private CustomDigitalClock clk1, clk2;
    private RelativeLayout endingtriplayout, distancelayout;
    private LinearLayout enddatetime_layout;
    private Intent loadActivity;
    private boolean isFromPlanning = false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_odameter);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        Intent i = getIntent();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            checkAndRequestPermissionAtRunTime(3);
        }

        typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(i.getStringExtra("screentitle"));
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        isFromPlanning = getIntent().getBooleanExtra("planingsub", false);
        tripStarting = (EditText) findViewById(R.id.trip_starting_reading);

        tripStarting.setInputType(InputType.TYPE_CLASS_NUMBER);
        tripStarting.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tripStarting.setKeyListener(DigitsKeyListener.getInstance(false, true));

        tripEnding = (EditText) findViewById(R.id.trip_ending_reading);

        tripEnding.setInputType(InputType.TYPE_CLASS_NUMBER);
        tripEnding.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tripEnding.setKeyListener(DigitsKeyListener.getInstance(false, true));
        distanceCoveredEt = (TextView) findViewById(R.id.distance_covered);
        datevalue = (TextView) findViewById(R.id.datevalue);
        timevalue = (TextView) findViewById(R.id.timevalue);
        timevaluestart = (TextView) findViewById(R.id.timevaluestart);
        endtimevalue = (TextView) findViewById(R.id.endtimevalue);
        timeend = (TextView) findViewById(R.id.timeend);
        enddatevalue = (TextView) findViewById(R.id.enddatevalue);
        TextView vanno = (TextView) findViewById(R.id.vanno);
        TextView vannovalue = (TextView) findViewById(R.id.vannovalue);
        startjourney = (Button) findViewById(R.id.startjourney);
        endjourney = (Button) findViewById(R.id.endjourney);
        endingtriplayout = (RelativeLayout) findViewById(R.id.endingtriplayout);
        distancelayout = (RelativeLayout) findViewById(R.id.distancelayout);
        enddatetime_layout = (LinearLayout) findViewById(R.id.enddatetime_layout);
        clk1 = (CustomDigitalClock) findViewById(R.id.digitalClock1);
        clk2 = (CustomDigitalClock) findViewById(R.id.digitalClock2);


        vanno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        vannovalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) this.findViewById(R.id.datetxtview)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) this.findViewById(R.id.timetxtview)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) this.findViewById(R.id.starttriptxtview)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) this.findViewById(R.id.endtriptxtview)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) this.findViewById(R.id.distencetxtview)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        datevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        timevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        timevaluestart.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        endtimevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        timeend.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        enddatevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        clk1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        clk2.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        startjourney.setOnClickListener(this);
        endjourney.setOnClickListener(this);

        if (!bmodel.configurationMasterHelper.SHOW_END_JOURNEY)
            endjourney.setVisibility(View.GONE);
        else {
            if (bmodel.getMissedCallRetailers().size() != 0 && bmodel.configurationMasterHelper.SHOW_CLOSE_DAY_VALID) {
                endjourney.setVisibility(View.GONE);
                tripEnding.setEnabled(false);
            } else {
                endjourney.setVisibility(View.VISIBLE);
                tripEnding.setEnabled(true);
            }
        }


        datevalue.setText("" + DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        timevalue.setText("" + SDUtil.now(SDUtil.TIME));
        enddatevalue.setText("" + DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        endtimevalue.setText("" + SDUtil.now(SDUtil.TIME));
        vannovalue.setText(" " + bmodel.userMasterHelper.getUserMasterBO().getVanno());
        findViewById(R.id.calcdot).setVisibility(View.VISIBLE);

        product = bmodel.vanmodulehelper.downloadOdameter();

        if (product == null)
            product = new VanLoadMasterBO();
        else {
            String tv = product.getOdameterstart() + "";
            tripStarting.setText(tv);
            tv = product.getOdameterend() + "";
            tripEnding.setText(tv);
            startingvalue = SDUtil.convertToDouble(tripStarting.getText().toString());
            endingvalue = SDUtil.convertToDouble(tripEnding.getText().toString());
            distanceCovered = endingvalue > startingvalue ? (endingvalue - startingvalue) : 0;
            tv = distanceCovered + "";
            distanceCoveredEt.setText(tv);
        }

        if (product.getIsstarted() == 0 && product.getIsended() == 0) {

            startjourney.setVisibility(View.VISIBLE);
            tripStarting.setFocusable(true);
            endingtriplayout.setVisibility(View.GONE);
            distancelayout.setVisibility(View.GONE);
            endjourney.setVisibility(View.GONE);
            enddatetime_layout.setVisibility(View.GONE);
            clk1.setVisibility(View.VISIBLE);
            timevaluestart.setVisibility(View.GONE);
            timevalue.setVisibility(View.GONE);
            tripStarting.setText("0.0");
            tripStarting.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    tripStarting.setCursorVisible(true);
                    tripStarting.selectAll();
                    tripStarting.onTouchEvent(event);
                    // tripStarting.setText("");
                    return true;
                }
            });


        }

        if (product.getIsstarted() == 1 && product.getIsended() == 0) {

            String[] CurrentString = product.getStartdatetime().toString().split(" ");

            timevaluestart.setText(CurrentString[1]);
            datevalue.setText(CurrentString[0]);

            timevaluestart.setVisibility(View.VISIBLE);
            timevalue.setVisibility(View.GONE);
            clk1.setVisibility(View.GONE);

            tripEnding.setFocusable(true);

            startjourney.setVisibility(View.GONE);
            tripStarting.setEnabled(false);
            tripStarting.setFocusable(false);
            clk2.setVisibility(View.VISIBLE);
            endingtriplayout.setVisibility(View.VISIBLE);
            distancelayout.setVisibility(View.VISIBLE);
            endjourney.setVisibility(View.VISIBLE);
            enddatetime_layout.setVisibility(View.VISIBLE);
            endtimevalue.setVisibility(View.GONE);
            tripEnding.setEnabled(true);
            tripEnding.setText("0.0");
            tripEnding.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    tripEnding.setCursorVisible(true);
                    tripEnding.selectAll();
                    tripEnding.onTouchEvent(event);
                    //tripEnding.setText("");
                    return true;
                }
            });

        }
        if (product.getIsstarted() == 1 && product.getIsended() == 1) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            String CurrentString = product.getStartdatetime().toString();
            String CurrentString1 = product.getEndtime().toString();
            String[] separated = CurrentString.split(" ");
            timevaluestart.setText(separated[1]);
            datevalue.setText(separated[0]);
            String[] separated1 = CurrentString1.split(" ");
            timeend.setText(separated1[1]);
            enddatevalue.setText(separated1[0]);

            timevaluestart.setVisibility(View.VISIBLE);
            tripStarting.setFocusable(false);
            timevalue.setVisibility(View.GONE);
            timeend.setVisibility(View.VISIBLE);
            endtimevalue.setVisibility(View.GONE);
            clk2.setVisibility(View.GONE);
            clk1.setVisibility(View.GONE);
            endjourney.setVisibility(View.GONE);
            startjourney.setVisibility(View.GONE);
            tripEnding.setEnabled(false);
            tripEnding.setFocusable(false);
            tripStarting.setEnabled(false);
            if (endingvalue > startingvalue)
                distanceCovered = endingvalue - startingvalue;
            else
                distanceCovered = 0;

            double distance = SDUtil.convertToDouble(String.valueOf(distanceCovered));
            // String tvDistanceCoveredEt = distance + "";
            distanceCoveredEt.setText(String.format("%.2f", distance));
        }

        try {
            checkRegex(tripStarting);
            tripStarting.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    // TO DO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    // TO DO Auto-generated method stub
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String value;
                    if (s.toString().isEmpty())
                        value = "0";
                    else
                        value = s.toString();
                    startingvalue = SDUtil.convertToDouble(value);
                    Commons.print("Planning ," + " starting value :" + startingvalue);
                    tripStartInsideTry(value);
                    product.setOdameterstart(startingvalue);

                }
            });

        } catch (Exception e) {
            Commons.printException("" + e);
        }


        try {
            checkRegex(tripEnding);
            tripEnding.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    // TO DO Auto-generated method stub

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    // TO DO Auto-generated method stub

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String value;
                    if (s.toString().isEmpty())
                        value = "0";
                    else
                        value = s.toString();
                    endingvalue = SDUtil.convertToDouble(value);
                    tripEndInsideTry(value);
                    product.setOdameterend(endingvalue);
                }
            });
        } catch (Exception e) {
            Commons.printException("" + e);
        }


    }

    //to allow single digit after decimal
    private void checkRegex(EditText editText) {
        final String reg = "\\d{0,5}(\\.(\\d{0,0})?)?";
        try {
            InputFilter filter = new InputFilter() {
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    String destString = String.valueOf(dest);
                    if (!Pattern.compile(reg).matcher(destString).matches()) {
                        return "";
                    }

                    return null;
                }
            };
            editText.setFilters(new InputFilter[]{filter});


        } catch (Exception ex) {
            Commons.printException("regex check", ex);
        }
    }

    private void tripEndInsideTry(String value) {
        try {
            /*int indexOFdec = value.indexOf(".");

            if (indexOFdec >= 0) {
                if (value.substring(indexOFdec).length() > 2) {

                    Toast.makeText(
                            getApplicationContext(),
                            getResources().getString(
                                    R.string.value_exceeded),
                            Toast.LENGTH_SHORT).show();
                    tripEnding.setText(value.substring(0,
                            value.length() - 1));
                    endingvalue = SDUtil.convertToDouble(value
                            .substring(0, value.length() - 1));

                }
            }*/
        /*    if (value.length() > 6 && indexOFdec < 0) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.value_exceeded),
                        Toast.LENGTH_SHORT).show();
                tripEnding.setText(value.substring(0,
                        value.length() - 1));
                endingvalue = SDUtil.convertToDouble(value
                        .substring(0, value.length() - 1));

            }*/
            if (endingvalue > startingvalue)
                distanceCovered = endingvalue - startingvalue;
            else
                distanceCovered = 0;

            /*String tvDistanceCoveredEt = distanceCovered + "";
            distanceCoveredEt.setText(tvDistanceCoveredEt);*/
            double distance = SDUtil.convertToDouble(String.valueOf(distanceCovered));
            // String tvDistanceCoveredEt = distance + "";
            distanceCoveredEt.setText(String.format("%.2f", distance));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void tripStartInsideTry(String value) {
        try {
            /*int indexOFdec = value.indexOf('.');

            if (indexOFdec >= 0 && value.substring(indexOFdec).length() > 2) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.value_exceeded),
                        Toast.LENGTH_SHORT).show();
                tripStarting.setText(value.substring(0,
                        value.length() - 1));
                startingvalue = SDUtil.convertToDouble(value
                        .substring(0, value.length() - 1));

            }*/
          /*  if (value.length() > 6 && indexOFdec < 0) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.value_exceeded),
                        Toast.LENGTH_SHORT).show();
                tripStarting.setText(value.substring(0,
                        value.length() - 1));
                startingvalue = SDUtil.convertToDouble(value
                        .substring(0, value.length() - 1));

            }*/
            if (endingvalue > startingvalue)
                distanceCovered = endingvalue - startingvalue;
            else
                distanceCovered = 0;

            //  String tvDistanceCoveredEt = distanceCovered + "";
            //  distanceCoveredEt.setText(tvDistanceCoveredEt);


            double distance = SDUtil.convertToDouble(String.valueOf(distanceCovered));
            // String tvDistanceCoveredEt = distance + "";
            distanceCoveredEt.setText(String.format("%.2f", distance));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(OdaMeterScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(R.string.doyouwantgoback))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        loadActivity = new Intent(OdaMeterScreen.this, HomeScreenActivity.class);
                                        if (isFromPlanning)
                                            loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
                                        else
                                            loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                                        startActivity(loadActivity);
                                        finish();
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

            case 1:

                AlertDialog.Builder builder1 = new AlertDialog.Builder(OdaMeterScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.saved_successfully))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        loadActivity = new Intent(OdaMeterScreen.this, HomeScreenActivity.class);
                                        if (isFromPlanning)
                                            loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
                                        else
                                            loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                                        startActivity(loadActivity);
                                        finish();

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;

            case 2:

                AlertDialog.Builder builder2 = new AlertDialog.Builder(OdaMeterScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.odameter_entry))
                        .setMessage(
                                getResources()
                                        .getString(
                                                R.string.ending_reading_should_be_greater_than_starting_reading))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        dialog.dismiss();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder2);
                break;

            case 3:

                AlertDialog.Builder builder3 = new AlertDialog.Builder(OdaMeterScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.odameter_entry))
                        .setMessage(
                                getResources().getString(
                                        R.string.start_reading_should_not_be_empty))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        dialog.dismiss();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder3);
                break;
            case 4:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(OdaMeterScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.odameter_entry))
                        .setMessage(
                                getResources().getString(
                                        R.string.end_reading_should_not_be_empty))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        dialog.dismiss();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder4);
                break;
        }
        return null;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_odameter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_save).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            showDialog(0);
            return true;
        } else if (i == R.id.menu_save) {
            if (SDUtil.convertToDouble(tripEnding.getText().toString()) < SDUtil
                    .convertToDouble(tripStarting.getText().toString())) {
                Commons.print("validation if," + "if");
                showDialog(2);
            } else if (SDUtil.convertToDouble(tripEnding.getText().toString()) == 0
                    && SDUtil
                    .convertToDouble(tripStarting.getText().toString()) == 0) {
                showDialog(3);
            } else {

                new SaveOdameter().execute();

            }
            return true;
        }
        return true;

    }

    public void saveOdameter(VanLoadMasterBO mylist) {
        try {

            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.executeQ("DELETE from Odameter");

            String columns = "uid,start,end,isstarted,startlatitude,startlongitude,starttime,date";

            String values = QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID))
                    + ","
                    + mylist.getOdameterstart()
                    + ","
                    + mylist.getOdameterend()
                    + ","
                    + 1
                    + ","
                    + LocationUtil.latitude
                    + ","
                    + LocationUtil.longitude
                    + ","
                    + QT(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat)
                    + " "
                    + SDUtil.now(SDUtil.TIME))
                    + ","
                    + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getDownloadDate());

            String sql = "insert into " + "Odameter" + "(" + columns
                    + ") values(" + values + ")";
            db.executeQ(sql);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

  /*  public void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String qty = QUANTITY.getText() + append;
            QUANTITY.setText(qty);
        } else
            QUANTITY.setText(append);
    }*/

   /* public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "0";
                    }
                }

                QUANTITY.setText(s);
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (!s.contains(".")) {
                    String qty = s + ".";
                    QUANTITY.setText(qty);
                }
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }*/

    @Override
    public void onClick(View v) {
        Button view = (Button) v;

        if (view == startjourney) {
            if (!(Double.compare(Double.valueOf(SDUtil.convertToDouble(tripStarting.getText().toString())), Double.valueOf(0)) > 0)) {
                showDialog(3);
            } else {
                new SaveOdameter().execute();
                startjourney.setEnabled(false);
                startjourney.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            }
        }
        if (view == endjourney) {
            bmodel.endjourneyclicked = true;
            if (!(Double.compare(Double.valueOf(SDUtil.convertToDouble(tripStarting.getText().toString())), Double.valueOf(0)) > 0))
                showDialog(4);
            else {
                if (SDUtil.convertToDouble(tripEnding.getText().toString()) < SDUtil
                        .convertToDouble(tripStarting.getText().toString()))
                    showDialog(2);
                else {
                    new UpdateOdameter().execute();
                    endjourney.setEnabled(false);
                    endjourney.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
                }
            }
        }
    }

    public void UpdateOdaMeter(VanLoadMasterBO mylist) {
        try {
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select  count(uid) from Odameter");
            String sql1, sql;
            if (c != null) {
                while (c.moveToNext())
                    if (c.getInt(0) == 0) {
                        sql1 = "insert into odameter(end,endtime,endlatitude,endlongitude,isended,upload) values("
                                + mylist.getOdameterend()
                                + ","
                                + QT(SDUtil.now(SDUtil.TIME))
                                + ","
                                + LocationUtil.latitude
                                + ","
                                + LocationUtil.longitude + "," + 1 + ",N)";
                        db.executeQ(sql1);
                    } else {
                        sql = "update Odameter set end="
                                + mylist.getOdameterend()
                                + ",endtime="
                                + QT(DateUtil.convertFromServerDateToRequestedFormat(SDUtil.now(SDUtil.DATE_GLOBAL), ConfigurationMasterHelper.outDateFormat)
                                + " "
                                + SDUtil.now(SDUtil.TIME))
                                + ",endlatitude=" + LocationUtil.latitude
                                + ",endlongitude=" + LocationUtil.longitude
                                + ",isended=" + 1 + ",upload='N'";
                        db.executeQ(sql);
                    }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    class SaveOdameter extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(OdaMeterScreen.this);

            customProgressDialog(builder,  getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                saveOdameter(product);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            bmodel.startjourneyclicked = true;
            alertDialog.dismiss();
            showDialog(1);
        }

    }

    class UpdateOdameter extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(OdaMeterScreen.this);

            customProgressDialog(builder,  getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                UpdateOdaMeter(product);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            showDialog(1);
        }

    }

}
