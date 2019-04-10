package com.ivy.cpg.view.competitor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetetorPOSMBO;
import com.ivy.sd.png.bo.CompetitorBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.CompetitorTrackingHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SubCompetitorTrackingActivity extends IvyBaseActivityNoActionBar {
    BusinessModel bmodel;
    private Toolbar toolbar;
    private String screenTitle;
    private static Button btn = null;
    public int companyid, competitorid, trackinglistId;
    static String outPutDateFormat;
    private String imageName, imagePath;
    private CompetitorBO masterObj = null;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    // Disable Motorola ET1 Scanner Plugin
    private final String ACTION_SCANNERINPUTPLUGIN = "com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
    private final String EXTRA_PARAMETER = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
    private final String DISABLE_PLUGIN = "DISABLE_PLUGIN";
    private static final int CAMERA_REQUEST_CODE = 1;
    private InputMethodManager inputManager;
    private EditText mSelectedET;
    private String append = "";

    MyAdapter mSchedule;
    private ListView lvwplist;
    private EditText et_feedback;
    private Button btnSave;
    private String calledBy = "0";
    private CompetitorTrackingHelper competitorTrackingHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_competitor_tracking);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getSupportActionBar().setElevation(0);
            }
        }
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        competitorTrackingHelper = CompetitorTrackingHelper.getInstance(this);

        outPutDateFormat = bmodel.configurationMasterHelper.outDateFormat;

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        screenTitle = getIntent().getStringExtra("screentitle");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setScreenTitle(screenTitle);

        companyid = getIntent().getIntExtra("companyId", 0);
        competitorid = getIntent().getIntExtra("competitorId", 0);
        calledBy = getIntent().getStringExtra("from") != null ? getIntent().getStringExtra("from") : "0";

        initializeView();
        process();


    }

    @Override
    public void onStart() {
        super.onStart();
        loadReasons();
    }

    private void initializeView() {
        et_feedback = findViewById(R.id.edt_feedback);
        lvwplist = findViewById(R.id.list);
        btnSave = findViewById(R.id.btn_save);
        lvwplist.setCacheColorHint(0);

        if (!bmodel.configurationMasterHelper.SHOW_COMP_QTY)
            (findViewById(R.id.tvTitleQty)).setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
            (findViewById(R.id.tvTitlePhoto)).setVisibility(View.GONE);
        }
    }

    private void process() {
        for (CompetitorBO competitorBO : competitorTrackingHelper.getCompetitorMaster()) {
            if (competitorBO.getCompanyID() == companyid && competitorBO.getCompetitorpid() == competitorid)
                masterObj = competitorBO;
        }
        if (masterObj != null) {
            et_feedback.setText(masterObj.getFeedBack());
            if (!masterObj.getCompetitoreason().isEmpty()) {
                mSchedule = new MyAdapter(masterObj.getCompetitoreason());
                lvwplist.setAdapter(mSchedule);

            }
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (hasData()) {
                    new SaveAsyncTask().execute();
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.no_data_tosave), 0);
                }
            }
        });

        et_feedback.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2,
                                      int arg3) {

            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1,
                                          int arg2, int arg3) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String qty = s.toString();

                if (qty.length() > 0)
                    et_feedback.setSelection(qty.length());

                if (masterObj != null) {
                    masterObj.setFeedBack(qty);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_promo, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {


            menu.findItem(R.id.menu_photo).setVisible(!bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR);
            menu.findItem(R.id.menu_add).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
            menu.findItem(R.id.menu_next).setVisible(false);


            if (bmodel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButonClick();
            return true;
        } else if (i == R.id.menu_photo)

        {
            if (competitorTrackingHelper.getNoOfImages())
                Toast.makeText(
                        this,
                        getResources()
                                .getString(
                                        R.string.its_highly_recommend_you_to_upload_the_images_before_capturing_new_image),
                        Toast.LENGTH_SHORT).show();
            else
                takephoto(bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR);
            return true;
        } else if (i == R.id.menu_remarks)

        {
            android.support.v4.app.FragmentManager ft = getSupportFragmentManager();
            RemarksDialog dialog = new RemarksDialog(
                    HomeScreenTwo.MENU_COMPETITOR);
            dialog.setCancelable(false);
            dialog.show(ft, HomeScreenTwo.MENU_COMPETITOR);
            return true;
        }
        return super.

                onOptionsItemSelected(item);
    }

    private class MyAdapter extends ArrayAdapter<CompetetorPOSMBO> {
        private ArrayList<CompetetorPOSMBO> items;

        public MyAdapter(ArrayList<CompetetorPOSMBO> items) {
            super(SubCompetitorTrackingActivity.this, R.layout.row_sub_comp, items);
            this.items = items;

        }

        public CompetetorPOSMBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(SubCompetitorTrackingActivity.this);
                convertView = inflater.inflate(
                        R.layout.row_sub_comp, null);

                holder.checkBox = convertView
                        .findViewById(R.id.chk_comp_activity);
                holder.tvTrackingList = convertView
                        .findViewById(R.id.tv_trackinglist);
                holder.btnFromDate = convertView
                        .findViewById(R.id.btn_fromdate);
                holder.btnToDate = convertView
                        .findViewById(R.id.btn_todate);
                holder.btnPhoto = convertView
                        .findViewById(R.id.btn_photo);
                holder.edtComFeedback = convertView
                        .findViewById(R.id.edt_competitor_feedback);
                holder.edtQty = convertView
                        .findViewById(R.id.et_qty);
                holder.edtRemark = convertView
                        .findViewById(R.id.et_remark);
                holder.spnReason = convertView
                        .findViewById(R.id.spn_reason);

                if (!bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
                    (convertView.findViewById(R.id.ll_photoView)).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_TIME_VIEW) {
                    (convertView.findViewById(R.id.ll_from_date)).setVisibility(View.GONE);
                    (convertView.findViewById(R.id.ll_to_date)).setVisibility(View.GONE);
                }

                if (!bmodel.configurationMasterHelper.SHOW_SPINNER) {
                    (convertView.findViewById(R.id.ll_spin)).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_COMP_FEEDBACK) {
                    convertView.findViewById(R.id.ll_hg).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_COMP_QTY) {
                    convertView.findViewById(R.id.et_qty).setVisibility(View.GONE);
                }

                holder.checkBox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                if (!isChecked) {
                                    holder.edtQty.setText("0");
                                    holder.edtQty.setEnabled(false);
                                    holder.edtRemark.setText("");
                                    holder.edtRemark.setEnabled(false);
                                    holder.btnPhoto.setEnabled(false);
                                    holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                                    holder.edtComFeedback.setText("");
                                    holder.edtComFeedback.setEnabled(false);
                                    holder.btnFromDate.setEnabled(false);
                                    holder.btnToDate.setEnabled(false);
                                    holder.spnReason.setEnabled(false);
                                } else {
                                    if ((holder.mCompTrackBO.getImageName() != null)
                                            && (!"".equals(holder.mCompTrackBO.getImageName()))
                                            && (!"null".equals(holder.mCompTrackBO.getImageName()))) {
                                        holder.btnPhoto.setEnabled(true);
                                        setPictureToImageView(holder.mCompTrackBO.getImageName(), holder.btnPhoto);
                                    } else {
                                        holder.btnPhoto.setEnabled(true);
                                        holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));

                                    }
                                    holder.edtQty.setEnabled(true);
                                    holder.edtRemark.setEnabled(true);
                                    holder.edtComFeedback.setEnabled(true);
                                    holder.btnFromDate.setEnabled(true);
                                    holder.btnToDate.setEnabled(true);
                                    holder.spnReason.setEnabled(true);
                                }
                                holder.mCompTrackBO.setExecuted(isChecked);

                            }

                        });

                holder.btnFromDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        btn = holder.btnFromDate;
                        btn.setTag(holder.mCompTrackBO);
                        DialogFragment newFragment = new DatePickerFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("date", holder.mCompTrackBO.getToDate());
                        newFragment.setArguments(bundle);
                        newFragment.show(getSupportFragmentManager(), "datePicker1");
                    }
                });
                holder.btnToDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        btn = holder.btnToDate;
                        btn.setTag(holder.mCompTrackBO);
                        DialogFragment newFragment = new DatePickerFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString("date", holder.mCompTrackBO.getToDate());
                        newFragment.setArguments(bundle);
                        newFragment.show(getSupportFragmentManager(), "datePicker2");
                    }
                });

                holder.edtComFeedback.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String text = s.toString();
                        holder.mCompTrackBO.setFeedBack(text);

                    }
                });

                holder.btnPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        trackinglistId = holder.mCompTrackBO.getId();
                        takephoto(bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR);
                    }
                });

                holder.edtQty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        holder.edtQty.onTouchEvent(event);
                        holder.edtQty.requestFocus();
                        if (holder.edtQty.getText().length() > 0)
                            holder.edtQty.setSelection(holder.edtQty.getText().length());
                        return true;
                    }
                });

                holder.edtQty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String str = s.toString();
                        if (str.length() > 0)
                            holder.edtQty.setSelection(str.length());
                        if (!str.equals("")) {
                            int qty = SDUtil
                                    .convertToInt(holder.edtQty
                                            .getText().toString());

                            holder.mCompTrackBO.setQty(qty);
                        } else {
                            holder.mCompTrackBO.setQty(0);
                        }

                    }
                });
                holder.edtRemark.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        holder.edtRemark.onTouchEvent(event);
                        holder.edtRemark.requestFocus();
                        if (holder.edtRemark.getText().length() > 0)
                            holder.edtRemark.setSelection(holder.edtRemark.getText().length());
                        return true;
                    }
                });

                holder.edtRemark.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String str = s.toString();
                        if (str.length() > 0)
                            holder.edtRemark.setSelection(str.length());
                        if (!str.equals(""))

                            holder.mCompTrackBO.setRemarks(str);
                        else
                            holder.mCompTrackBO.setRemarks("");
                    }
                });

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mCompTrackBO = items.get(position);


            holder.checkBox.setChecked(holder.mCompTrackBO.isExecuted());
            holder.tvTrackingList.setText(holder.mCompTrackBO.getName());
            holder.edtComFeedback.setText(holder.mCompTrackBO.getFeedBack() + "");
            holder.edtQty.setText(holder.mCompTrackBO.getQty() + "");
            holder.edtRemark.setText(holder.mCompTrackBO.getRemarks() + "");
            if (!holder.mCompTrackBO.isExecuted()) {
                holder.btnFromDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                holder.mCompTrackBO.setFromDate(holder.btnFromDate.getText()
                        .toString());

                holder.btnToDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(
                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                holder.mCompTrackBO.setToDate(holder.btnToDate.getText()
                        .toString());
                //holder.mCompTrackBO.setReasonID(resonId);
                holder.edtQty.setEnabled(false);
                holder.edtRemark.setEnabled(false);
                holder.btnPhoto.setEnabled(false);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.edtComFeedback.setEnabled(false);
                holder.btnFromDate.setEnabled(false);
                holder.btnToDate.setEnabled(false);
                holder.spnReason.setEnabled(false);
            } else {
                holder.btnFromDate
                        .setText((holder.mCompTrackBO.getFromDate() == null) ? (DateTimeUtils
                                .convertFromServerDateToRequestedFormat(
                                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                        outPutDateFormat))
                                : holder.mCompTrackBO.getFromDate());
                holder.btnToDate
                        .setText((holder.mCompTrackBO.getToDate() == null) ? (DateTimeUtils
                                .convertFromServerDateToRequestedFormat(
                                        DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                        outPutDateFormat))
                                : holder.mCompTrackBO.getToDate());
                holder.edtQty.setEnabled(true);
                holder.edtRemark.setEnabled(true);
                holder.btnPhoto.setEnabled(true);
                holder.edtComFeedback.setEnabled(true);
                holder.btnFromDate.setEnabled(true);
                holder.btnToDate.setEnabled(true);
                holder.spnReason.setEnabled(true);

                if ((holder.mCompTrackBO.getImageName() != null)
                        && (!"".equals(holder.mCompTrackBO.getImageName()))
                        && (!"null".equals(holder.mCompTrackBO.getImageName()))) {
                    setPictureToImageView(holder.mCompTrackBO.getImageName(), holder.btnPhoto);
                } else {
                    holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                }
            }

            holder.spnReason.setAdapter(spinnerAdapter);
            holder.spnReason
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int position, long id) {

                            ReasonMaster reString = (ReasonMaster) holder.spnReason
                                    .getSelectedItem();

                            if (reString.getReasonID().equals("-1")) {
                                holder.mCompTrackBO.setReasonID(0);
                            } else {
                                holder.mCompTrackBO.setReasonID(SDUtil
                                        .convertToInt(reString.getReasonID()));
                            }

                        }

                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

            if (holder.mCompTrackBO != null)
                holder.spnReason.setSelection(getReasonIndex(holder.mCompTrackBO.getReasonID()
                        + ""));

            holder.spnReason.setSelected(true);

            return convertView;
        }
    }

    class ViewHolder {
        CompetetorPOSMBO mCompTrackBO;
        CheckBox checkBox;
        TextView tvTrackingList;
        Button btnFromDate, btnToDate;
        ImageView btnPhoto;
        EditText edtComFeedback;
        EditText edtQty;
        EditText edtRemark;
        Spinner spnReason;
    }

    private void setPictureToImageView(String imageName, final ImageView imageView) {
        Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera_blue_24dp);
        Glide.with(SubCompetitorTrackingActivity.this)
                .load(FileUtils.photoFolderPath + "/" + imageName)
                .asBitmap()
                .centerCrop()
                .placeholder(new BitmapDrawable(getResources(), defaultIcon))
                .into(new BitmapImageViewTarget(imageView) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        imageView.setImageDrawable(new BitmapDrawable(getResources(), getCircularBitmapFrom(resource)));
                    }
                });
    }

    private void takephoto(boolean isCompPhoto) {
        // Photo capture
        if (bmodel.isExternalStorageAvailable()) {
            String path_prefix;
            if (isCompPhoto)
                path_prefix = "CT_" + bmodel.retailerMasterBO.getRetailerID() + "_"
                        + trackinglistId + "_"
                        + competitorid + "_"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid();
            else
                path_prefix = "CT_" + bmodel.retailerMasterBO.getRetailerID() + "_"
                        + competitorid + "_"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid();

            imageName = path_prefix
                    + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";
            imagePath = "Competitor/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;

            Commons.print(imageName);
            String fnameStarts = path_prefix + "_" + Commons.now(Commons.DATE);

            boolean nfiles_there = bmodel.checkForNFilesInFolder(
                    FileUtils.photoFolderPath, 1, fnameStarts);

            if (nfiles_there) {
                if (isCompPhoto)
                    showFileDeleteAlert(trackinglistId + "", fnameStarts);
                else
                    showFileDeleteAlert(competitorid + "", fnameStarts);
            } else {
                try {
                    Intent i = new Intent();
                    i.setAction(ACTION_SCANNERINPUTPLUGIN);
                    i.putExtra(EXTRA_PARAMETER, DISABLE_PLUGIN);
                    this.sendBroadcast(i);
                    Thread.sleep(100);

                    String _path = FileUtils.photoFolderPath + "/" + imageName;
                    bmodel.getPhotosTakeninCurrentCompetitorTracking().put(trackinglistId + "", _path);

                    Intent intent = new Intent(this,
                            CameraActivity.class);
                    intent.putExtra(CameraActivity.QUALITY,
                            40);
                    intent.putExtra(CameraActivity.PATH,
                            _path);
                    intent.putExtra(
                            CameraActivity.ISSAVEREQUIRED,
                            false);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);

                } catch (Exception e) {
                    Commons.printException(e);
                }
                // holder.capture.requestFocus();
            }

        } else {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                if (masterObj != null) {
                    if (bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
                        for (CompetetorPOSMBO cmp : masterObj.getCompetitoreason()) {
                            if (cmp.getId() == trackinglistId) {
                                cmp.setImageName(imageName);
                                cmp.setImagePath(imagePath);
                            }
                        }
                        mSchedule.notifyDataSetChanged();
                    } else {
                        masterObj.setImageName(imageName);
                        masterObj.setImagePath(imagePath);
                    }


                    Commons.print(imageName);
                }
            } else {
                Commons.print("CompetitorTracking _ Camers Activity Canceled");
            }
        }
    }

    public void showFileDeleteAlert(final String bbid,
                                    final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        ArrayList<CompetitorBO> items = competitorTrackingHelper
                                .getCompetitorMaster();
                        if (bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
                            for (int i = 0; i < items.size(); i++) {
                                CompetitorBO ATB = (CompetitorBO) items.get(i);
                                if (ATB.getCompetitorpid() == SDUtil.convertToInt(bbid)) {
                                    ArrayList<CompetetorPOSMBO> trackingList = ATB.getCompetitoreason();
                                    for (CompetetorPOSMBO cmp : trackingList) {
                                        if (cmp.getId() == SDUtil.convertToInt(bbid)) {
                                            cmp.setImageName("");
                                            cmp.setImagePath("");
                                        }
                                    }
                                }
                            }

                        } else {
                            for (int i = 0; i < items.size(); i++) {
                                CompetitorBO ATB = (CompetitorBO) items.get(i);
                                if (ATB.getCompetitorpid() == SDUtil.convertToInt(bbid)) {
                                    ATB.setImageName("");
                                    ATB.setImagePath("");
                                }
                            }
                        }
                        competitorTrackingHelper
                                .deleteImageName(imageNameStarts);
                        competitorTrackingHelper.deleteFiles(
                                FileUtils.photoFolderPath, imageNameStarts);
                        dialog.dismiss();

                        Intent intent = new Intent(SubCompetitorTrackingActivity.this,
                                CameraActivity.class);
                        String _path = FileUtils.photoFolderPath + "/" + imageName;
                        intent.putExtra(
                                CameraActivity.QUALITY, 40);
                        intent.putExtra(
                                CameraActivity.PATH, _path);
                        intent.putExtra(
                                CameraActivity.ISSAVEREQUIRED,
                                false);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);

                        return;
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        return;
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }// end of showChangeAlert

    private Bitmap getCircularBitmapFrom(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return null;
        }
        float radius = source.getWidth() > source.getHeight() ? ((float) source
                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
                source.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
                radius, paint);

        return bitmap;
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                competitorTrackingHelper.saveCompetitor();
                if (!calledBy.equals("3"))
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_COMPETITOR, true);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
					true, false);*/
            builder = new AlertDialog.Builder(SubCompetitorTrackingActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            alertDialog.dismiss();
            //	progressDialogue.dismiss();
            bmodel.getPhotosTakeninCurrentCompetitorTracking().clear();
            Toast.makeText(SubCompetitorTrackingActivity.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();

            finish();


        }

    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {
        String date;
        Date selectionDate = null;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getArguments() != null) {
                date = getArguments().getString("date", "");
            }
            /* param   - date (selected date will return in this string obj)
             * return  - To update selectionDate into date picker dialog
             * default - Show current date in date picker dialog
             */
            if (!date.isEmpty())
                selectionDate = DateTimeUtils.convertStringToDateObject(
                        date, outPutDateFormat);

            Calendar c = Calendar.getInstance();

            if (selectionDate != null)
                c.setTime(selectionDate);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            CompetetorPOSMBO bo = (CompetetorPOSMBO) btn.getTag();
            if (this.getTag().equals("datePicker1")) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    bo.setFromDate(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                    btn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    bo.setFromDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    btn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else if (this.getTag().equals("datePicker2")) {
                if (bo.getFromDate() != null && bo.getFromDate().length() > 0) {
                    Date dateMfg = DateTimeUtils.convertStringToDateObject(
                            bo.getFromDate(), outPutDateFormat);
                    if (dateMfg != null && selectedDate.getTime() != null
                            && dateMfg.after(selectedDate.getTime())) {
                        Toast.makeText(getActivity(), R.string.competitor_date,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        bo.setToDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        btn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                } else {
                    bo.setToDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    btn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            }
        }
    }

    public boolean hasData() {

        if (masterObj == null)
            return false;


        ArrayList<CompetetorPOSMBO> trackinglist = masterObj
                .getCompetitoreason();
        for (CompetetorPOSMBO temp : trackinglist) {
            if (temp.isExecuted()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Initialize Adapter and add reason
     */
    private void loadReasons() {
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        ReasonMaster reason = new ReasonMaster();
        reason.setReasonID("1");
        reason.setReasonDesc(getResources().getString(R.string.other_reason));
        reason.setReasonCategory("COMP_RSN");

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if ("COMP_RSN".equalsIgnoreCase(temp.getReasonCategory())
                    || "NONE".equalsIgnoreCase(temp.getReasonCategory()))
                spinnerAdapter.add(temp);
        }

        if (!(spinnerAdapter.getCount() > 0)) {
            ReasonMaster reasonMasterBo = new ReasonMaster();
            reasonMasterBo.setReasonDesc(getResources().getString(R.string.select_reason));
            reasonMasterBo.setReasonID("-1");
            spinnerAdapter.add(reasonMasterBo);
        }
        spinnerAdapter.add(reason);
    }

    /**
     * Get the selected reason id, iterate and get position and set in the
     * spinner item
     *
     * @param reasonId Reason Id
     * @return Index of reason Id
     */
    private int getReasonIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster reasonBO = spinnerAdapter.getItem(i);
            if (reasonBO != null) {
                if (reasonBO.getReasonID().equals(reasonId))
                    return i;
            }
        }
        return -1;
    }

    private void onBackButonClick() {

        if (hasData()) {
            showBackDialog();
        } else {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void showBackDialog() {
        CommonDialog dialog = new CommonDialog(this, getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

            }
        }, getResources().getString(R.string.cancel), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }
}
