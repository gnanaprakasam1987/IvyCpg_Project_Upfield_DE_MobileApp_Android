package com.ivy.cpg.view.van.odameter;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.van.vanstockapply.VanLoadMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.regex.Pattern;

public class OdaMeterScreen extends IvyBaseActivityNoActionBar implements OnClickListener {

    private BusinessModel bmodel;
    private EditText tripStarting;
    private ImageView ivStartTrip, tempImageView, ivEndTrip;
    private EditText tripEnding;

    private Context mContext;
    private TextView distanceCoveredEt;
    private String imageFileName = "", photoNamePath;
    private double startingvalue;
    private double endingvalue;
    private double distanceCovered;
    private VanLoadMasterBO product;
    private Button startjourney;
    private Button endjourney;
    private TypedArray typearr;
    private static final String TAG = "OdaMeterScreen";

    private static final int CAMERA_REQUEST_CODE = 1;
    private boolean isStartImg = false;
    private OdameterHelper odameterHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_odameter);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        mContext = getApplicationContext();
        Intent i = getIntent();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        odameterHelper = OdameterHelper.getInstance(this.getApplicationContext());

        Toolbar toolbar = findViewById(R.id.toolbar);
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

        tripStarting = findViewById(R.id.trip_starting_reading);
        // capture image starting
        ivStartTrip = findViewById(R.id.starttripimgiv);


        tripStarting.setInputType(InputType.TYPE_CLASS_NUMBER);
        tripStarting.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tripStarting.setKeyListener(DigitsKeyListener.getInstance(false, true));

        tripEnding = findViewById(R.id.trip_ending_reading);
        ivEndTrip = findViewById(R.id.endtripimgiv);
        tripEnding.setInputType(InputType.TYPE_CLASS_NUMBER);
        tripEnding.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        tripEnding.setKeyListener(DigitsKeyListener.getInstance(false, true));
        distanceCoveredEt = findViewById(R.id.distance_covered);
        TextView datevalue = findViewById(R.id.datevalue);
        TextView timevalue = findViewById(R.id.timevalue);
        TextView timevaluestart = findViewById(R.id.timevaluestart);
        TextView endtimevalue = findViewById(R.id.endtimevalue);
        TextView timeend = findViewById(R.id.timeend);
        TextView enddatevalue = findViewById(R.id.enddatevalue);
        TextView vannovalue = findViewById(R.id.vannovalue);
        startjourney = findViewById(R.id.startjourney);
        endjourney = findViewById(R.id.endjourney);
        LinearLayout endingtriplayout = findViewById(R.id.endingtriplayout);
        RelativeLayout distancelayout = findViewById(R.id.distancelayout);
        RelativeLayout enddatetime_layout = findViewById(R.id.enddatetime_layout);

        CustomDigitalClock clk1 = findViewById(R.id.digitalClock1);
        CustomDigitalClock clk2 = findViewById(R.id.digitalClock2);

        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.vanno).getTag()) != null)
                ((TextView) findViewById(R.id.vanno))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.vanno).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }

        photoNamePath = FileUtils.photoFolderPath + "/";
        Commons.print("Photo Path, " + "" + photoNamePath);

        startjourney.setOnClickListener(this);
        endjourney.setOnClickListener(this);

        if (!bmodel.configurationMasterHelper.SHOW_END_JOURNEY)
            endjourney.setVisibility(View.GONE);
        else {
            if (bmodel.getMissedCallRetailers().size() != 0 && bmodel.configurationMasterHelper.SHOW_CLOSE_DAY_VALID) {
                endjourney.setVisibility(View.GONE);
                tripEnding.setEnabled(false);
                ivEndTrip.setEnabled(false);
            } else {
                endjourney.setVisibility(View.VISIBLE);
                tripEnding.setEnabled(true);
                ivEndTrip.setEnabled(true);
            }
        }
//
        if (!bmodel.configurationMasterHelper.SHOW_PHOTO_ODAMETER) {
            ivStartTrip.setVisibility(View.GONE);
            ivEndTrip.setVisibility(View.GONE);
        } else {
            ivStartTrip.setVisibility(View.VISIBLE);
            ivEndTrip.setVisibility(View.VISIBLE);
        }


        datevalue.setText("" + DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        timevalue.setText("" + DateTimeUtils.now(DateTimeUtils.TIME));
        enddatevalue.setText("" + DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        endtimevalue.setText("" + DateTimeUtils.now(DateTimeUtils.TIME));
        vannovalue.setText(" " + bmodel.userMasterHelper.getUserMasterBO().getVanno());
        //findViewById(R.id.calcdot).setVisibility(View.VISIBLE);

        product = LoadManagementHelper.getInstance(this).downloadOdameter();

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

            if (bmodel.configurationMasterHelper.SHOW_PHOTO_ODAMETER) {

                if (product.getStartTripImg() != null)

                    Glide.with(getApplicationContext())
                            .load(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + product.getStartTripImg())
                            .asBitmap()
                            .centerCrop()
                            .placeholder(R.drawable.ic_photo_camera)
                            .transform(bmodel.circleTransform)
                            .into(new BitmapImageViewTarget(ivStartTrip));

                if (product.getEndTripImg() != null)

                    Glide.with(getApplicationContext())
                            .load(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + product.getEndTripImg())
                            .asBitmap()
                            .centerCrop()
                            .placeholder(R.drawable.ic_photo_camera)
                            .transform(bmodel.circleTransform)
                            .into(new BitmapImageViewTarget(ivEndTrip));

            }

        }

        if (product.getIsstarted() == 0 && product.getIsended() == 0) {

            startjourney.setVisibility(View.VISIBLE);
            tripStarting.setFocusable(true);
            ivStartTrip.setFocusable(true);
            endingtriplayout.setVisibility(View.GONE);
            ivEndTrip.setVisibility(View.GONE);
            distancelayout.setVisibility(View.GONE);
            endjourney.setVisibility(View.GONE);
            enddatetime_layout.setVisibility(View.GONE);
            clk1.setVisibility(View.VISIBLE);
            timevaluestart.setVisibility(View.GONE);
            timevalue.setVisibility(View.GONE);
            tripStarting.setText("");

            tripStarting.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    tripStarting.setCursorVisible(true);
                    tripStarting.onTouchEvent(event);
                    if (tripStarting.getText().length() > 0)
                        tripStarting.setSelection(tripStarting.getText().length());
                    // tripStarting.setText("");
                    return true;
                }
            });


        }

        if (product.getIsstarted() == 1 && product.getIsended() == 0) {

            String[] CurrentString = product.getStartdatetime().split(" ");

            timevaluestart.setText(CurrentString[1]);
            datevalue.setText("" + DateTimeUtils.convertFromServerDateToRequestedFormat(CurrentString[0],
                    ConfigurationMasterHelper.outDateFormat));

            timevaluestart.setVisibility(View.VISIBLE);
            timevalue.setVisibility(View.GONE);
            clk1.setVisibility(View.GONE);

            tripEnding.setFocusable(true);
            ivEndTrip.setFocusable(true);

            startjourney.setVisibility(View.GONE);
            tripStarting.setEnabled(false);
            tripStarting.setFocusable(false);

            ivStartTrip.setEnabled(false);
            ivStartTrip.setFocusable(false);

            clk2.setVisibility(View.VISIBLE);
            endingtriplayout.setVisibility(View.VISIBLE);
            if (bmodel.configurationMasterHelper.SHOW_PHOTO_ODAMETER)
                ivEndTrip.setVisibility(View.VISIBLE);
            distancelayout.setVisibility(View.VISIBLE);
            endjourney.setVisibility(View.VISIBLE);
            enddatetime_layout.setVisibility(View.VISIBLE);
            endtimevalue.setVisibility(View.GONE);
            tripEnding.setEnabled(true);
            ivEndTrip.setEnabled(true);
            tripEnding.setText("");
            tripEnding.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    tripEnding.setCursorVisible(true);
                    tripEnding.onTouchEvent(event);
                    if (tripEnding.getText().length() > 0)
                        tripEnding.setSelection(tripEnding.getText().length());
                    //tripEnding.setText("");
                    return true;
                }
            });

        }
        if (product.getIsstarted() == 1 && product.getIsended() == 1) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            String CurrentString = product.getStartdatetime();
            String CurrentString1 = product.getEndtime();
            String[] separated = CurrentString.split(" ");
            timevaluestart.setText(separated[1]);
            datevalue.setText("" + DateTimeUtils.convertFromServerDateToRequestedFormat(separated[0],
                    ConfigurationMasterHelper.outDateFormat));
            String[] separated1 = CurrentString1.split(" ");
            timeend.setText(separated1[1]);
            enddatevalue.setText("" + DateTimeUtils.convertFromServerDateToRequestedFormat(separated[0],
                    ConfigurationMasterHelper.outDateFormat));


            timevaluestart.setVisibility(View.VISIBLE);
            tripStarting.setFocusable(false);
            ivStartTrip.setFocusable(false);
            timevalue.setVisibility(View.GONE);
            timeend.setVisibility(View.VISIBLE);
            endtimevalue.setVisibility(View.GONE);
            clk2.setVisibility(View.GONE);
            clk1.setVisibility(View.GONE);
            endjourney.setVisibility(View.GONE);
            startjourney.setVisibility(View.GONE);
            tripEnding.setEnabled(false);
            tripEnding.setFocusable(false);
            ivEndTrip.setEnabled(false);
            ivEndTrip.setFocusable(false);
            tripStarting.setEnabled(false);
            ivStartTrip.setEnabled(false);
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

                    if (value.length() > 0 && !"0".equals(value))
                        tripStarting.setSelection(value.length());

                    startingvalue = SDUtil.convertToDouble(value);
                    Commons.print("Planning ," + " starting value :" + startingvalue);
                    updateTripDistanceCovered();
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
                    if (value.length() > 0 && !"0".equals(value))
                        tripEnding.setSelection(value.length());
                    endingvalue = SDUtil.convertToDouble(value);
                    updateTripDistanceCovered();
                    product.setOdameterend(endingvalue);
                }
            });
        } catch (Exception e) {
            Commons.printException("" + e);
        }


        if (bmodel.configurationMasterHelper.SHOW_PHOTO_ODAMETER) {

            ivStartTrip.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    tempImageView = ivStartTrip;
                    isStartImg = true;
                    takePhoto();
                }
            });
            ivEndTrip.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    tempImageView = ivEndTrip;
                    isStartImg = false;
                    takePhoto();
                }
            });
        }

    }


    private void takePhoto() {

        if (bmodel.isExternalStorageAvailable()) {


            boolean nFilesThere = bmodel
                    .checkForNFilesInFolder(
                            photoNamePath,
                            1, imageFileName);


            if (nFilesThere) {

                showFileDeleteAlertWithImage(imageFileName);
            } else {

                imageFileName = "ODA_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";


                String path = photoNamePath + "/" + imageFileName;
                try {
                    Intent intent = new Intent(mContext, CameraActivity.class);
                    intent.putExtra(CameraActivity.QUALITY, 40);
                    intent.putExtra(CameraActivity.PATH, path);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);

                } catch (Exception e) {
                    Commons.print("error opening camera");
                    Commons.printException(e);
                    // TODO: handle exception
                }
            }

        } else {
            Toast.makeText(
                    mContext,
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }

    }


    private void showFileDeleteAlertWithImage(final String imageNameStarts) {
        final CommonDialog commonDialog = new CommonDialog(OdaMeterScreen.this.getApplication(), //Context
                OdaMeterScreen.this, //Context
                "", //Title
                getResources().getString(R.string.word_already) + " " + 1 + " " + getResources().getString(R.string.word_photocaptured_delete_retake), //Message
                true, //ToDisplayImage
                getResources().getString(R.string.yes), //Positive Button
                getResources().getString(R.string.no), //Negative Button
                false, //MoveToNextActivity
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageNameStarts, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        bmodel.deleteFiles(photoNamePath,
                                imageNameStarts);
                        Intent intent = new Intent(getApplicationContext(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = photoNamePath + "/" + imageNameStarts;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
// dialog.dismiss();
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                Commons.print(TAG + ",Camers Activity : Sucessfully Captured.");

                //For adding server ref path to image name
                String path = "Odameter/"
                        + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate().replace("/", "") + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/";

                String imageUrl = (path + imageFileName);


                Glide.with(getApplicationContext())
                        .load(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageFileName)
                        .asBitmap()
                        .centerCrop()
                        .placeholder(R.drawable.ic_photo_camera)
                        .transform(bmodel.circleTransform)
                        .into(new BitmapImageViewTarget(tempImageView));

                if (isStartImg)
                    product.setStartTripImg(imageUrl);
                else
                    product.setEndTripImg(imageUrl);
            } else {
                Commons.print(TAG + ",Camers Activity : Canceled");
            }
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


    private void updateTripDistanceCovered() {

        if (endingvalue > startingvalue)
            distanceCovered = endingvalue - startingvalue;
        else
            distanceCovered = 0;

        double distance = SDUtil.convertToDouble(String.valueOf(distanceCovered));

        distanceCoveredEt.setText(String.format("%.2f", distance));
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
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        finish();
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
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
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
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
                        .setPositiveButton(getResources().getString(R.string.ok),
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


    @Override
    public void onClick(View v) {
        Button view = (Button) v;

        if (view == startjourney) {
            if (!(Double.compare(SDUtil.convertToDouble(tripStarting.getText().toString()), 0d) > 0)) {
                showDialog(3);
            } else {
                new SaveOdameter().execute();
                startjourney.setEnabled(false);
                startjourney.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            }
        }
        if (view == endjourney) {
            bmodel.endjourneyclicked = true;
            if (!(Double.compare(SDUtil.convertToDouble(tripStarting.getText().toString()), 0d) > 0))
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

    class SaveOdameter extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(OdaMeterScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                odameterHelper.saveOdameter(product);
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

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                odameterHelper.UpdateOdaMeter(product);
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
