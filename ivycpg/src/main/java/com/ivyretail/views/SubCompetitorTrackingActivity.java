package com.ivyretail.views;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
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
import android.support.v4.content.ContextCompat;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetetorPOSMBO;
import com.ivy.sd.png.bo.CompetitorBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;

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

    private void initializeView() {
        et_feedback = (EditText) findViewById(R.id.edt_feedback);
        lvwplist = (ListView) findViewById(R.id.lvwplist);
        btnSave = (Button) findViewById(R.id.btn_save);
        lvwplist.setCacheColorHint(0);

        ((TextView) findViewById(R.id.tvTitleTrackingList)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvTitlePeriod)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvTitlePhoto)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvTitleAvaialabilty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        et_feedback.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        if (!bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
            ((TextView) findViewById(R.id.tvTitlePhoto)).setVisibility(View.GONE);
        }
    }

    private void process() {
        for (CompetitorBO competitorBO : bmodel.competitorTrackingHelper.getCompetitorMaster()) {
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
            menu.findItem(R.id.menu_product_filter).setVisible(false);
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
            finish();

            return true;
        } else if (i == R.id.menu_photo)

        {
            if (bmodel.competitorTrackingHelper.getNoOfImages())
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
                convertView = (View) inflater.inflate(
                        R.layout.row_sub_comp, null);

                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.chk_comp_activity);
                holder.tvTrackingList = (TextView) convertView
                        .findViewById(R.id.tv_trackinglist);
                holder.btnFromDate = (Button) convertView
                        .findViewById(R.id.btn_fromdate);
                holder.btnToDate = (Button) convertView
                        .findViewById(R.id.btn_todate);
                holder.btnPhoto = (ImageView) convertView
                        .findViewById(R.id.btn_photo);
                holder.edtComFeedback = (EditText) convertView
                        .findViewById(R.id.edt_competitor_feedback);
                holder.edtQty = (EditText) convertView
                        .findViewById(R.id.et_qty);

                if (!bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
                    ((LinearLayout) convertView.findViewById(R.id.ll_photoView)).setVisibility(View.GONE);
                }

                holder.checkBox
                        .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                if (!isChecked) {
                                    holder.btnPhoto.setEnabled(false);
                                    holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                                    holder.edtComFeedback.setText("");
                                    holder.edtComFeedback.setEnabled(false);
                                    holder.btnFromDate.setEnabled(false);
                                    holder.btnToDate.setEnabled(false);
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
                                    holder.edtComFeedback.setEnabled(true);
                                    holder.btnFromDate.setEnabled(true);
                                    holder.btnToDate.setEnabled(true);
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
                        newFragment.show(getSupportFragmentManager(), "datePicker1");
                    }
                });
                holder.btnToDate.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        btn = holder.btnToDate;
                        btn.setTag(holder.mCompTrackBO);
                        DialogFragment newFragment = new DatePickerFragment();
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

                        /*if (bmodel.competitorTrackingHelper.getNoOfImages())
                            Toast.makeText(
                                    SubCompetitorTrackingActivity.this,
                                    getResources()
                                            .getString(
                                                    R.string.its_highly_recommend_you_to_upload_the_images_before_capturing_new_image),
                                    Toast.LENGTH_SHORT).show();
                        else {*/
                            trackinglistId = holder.mCompTrackBO.getId();
                            takephoto(bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR);
                        //}
                    }
                });

                holder.edtQty.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        holder.edtQty.onTouchEvent(event);
                        holder.edtQty.selectAll();
                        holder.edtQty.requestFocus();
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

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mCompTrackBO = items.get(position);

            //typefaces
            holder.tvTrackingList.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.btnFromDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.btnToDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            holder.edtComFeedback.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.edtQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) convertView.findViewById(R.id.tv_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            holder.checkBox.setChecked(holder.mCompTrackBO.isExecuted());
            holder.tvTrackingList.setText(holder.mCompTrackBO.getName());
            holder.edtComFeedback.setText(holder.mCompTrackBO.getFeedBack() + "");
            holder.edtQty.setText(holder.mCompTrackBO.getQty() + "");
            if (!holder.mCompTrackBO.isExecuted()) {
                holder.btnFromDate.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.mCompTrackBO.setFromDate(holder.btnFromDate.getText()
                        .toString());

                holder.btnToDate.setText(DateUtil.convertFromServerDateToRequestedFormat(
                        SDUtil.now(SDUtil.DATE_GLOBAL), outPutDateFormat));
                holder.mCompTrackBO.setToDate(holder.btnToDate.getText()
                        .toString());
                holder.btnPhoto.setEnabled(false);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                holder.edtComFeedback.setEnabled(false);
                holder.btnFromDate.setEnabled(false);
                holder.btnToDate.setEnabled(false);
            } else {
                holder.btnFromDate
                        .setText((holder.mCompTrackBO.getFromDate() == null) ? (DateUtil
                                .convertFromServerDateToRequestedFormat(
                                        SDUtil.now(SDUtil.DATE_GLOBAL),
                                        outPutDateFormat))
                                : holder.mCompTrackBO.getFromDate());
                holder.btnToDate
                        .setText((holder.mCompTrackBO.getToDate() == null) ? (DateUtil
                                .convertFromServerDateToRequestedFormat(
                                        SDUtil.now(SDUtil.DATE_GLOBAL),
                                        outPutDateFormat))
                                : holder.mCompTrackBO.getToDate());
                holder.btnPhoto.setEnabled(true);
                holder.btnPhoto.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                holder.edtComFeedback.setEnabled(true);
                holder.btnFromDate.setEnabled(true);
                holder.btnToDate.setEnabled(true);
            }

            if ((holder.mCompTrackBO.getImageName() != null)
                    && (!"".equals(holder.mCompTrackBO.getImageName()))
                    && (!"null".equals(holder.mCompTrackBO.getImageName()))) {
                //  Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera);
                setPictureToImageView(holder.mCompTrackBO.getImageName(), holder.btnPhoto);
//                Glide.with(SubCompetitorTrackingActivity.this).load(HomeScreenFragment.photoPath + "/" + holder.mCompTrackBO.getImageName()).asBitmap().centerCrop().placeholder(new BitmapDrawable(getResources(), defaultIcon)).into(new BitmapImageViewTarget(holder.btnPhoto) {
//                    @Override
//                    protected void setResource(Bitmap resource) {
//                        holder.btnPhoto.setImageDrawable(new BitmapDrawable(getResources(), getCircularBitmapFrom(resource)));
//                    }
//                });

            } else {
                holder.btnPhoto.setImageDrawable(ContextCompat.getDrawable(SubCompetitorTrackingActivity.this, R.drawable.ic_photo_camera));
            }
            TypedArray typearr = SubCompetitorTrackingActivity.this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                convertView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }
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
    }

    private void setPictureToImageView(String imageName, final ImageView imageView) {
        Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera_blue_24dp);
        Glide.with(SubCompetitorTrackingActivity.this)
                .load(HomeScreenFragment.photoPath + "/" + imageName)
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
                    HomeScreenFragment.folder.getPath(), 1, fnameStarts);

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

                    Intent intent = new Intent(this,
                            CameraActivity.class);
                    String _path = HomeScreenFragment.photoPath + "/" + imageName;
                    intent.putExtra(getResources().getString(R.string.quality),
                            40);
                    intent.putExtra(getResources().getString(R.string.path),
                            _path);
                    intent.putExtra(
                            getResources().getString(R.string.saverequired),
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

                        ArrayList<CompetitorBO> items = bmodel.competitorTrackingHelper
                                .getCompetitorMaster();
                        if (bmodel.configurationMasterHelper.IS_PHOTO_COMPETITOR) {
                            for (int i = 0; i < items.size(); i++) {
                                CompetitorBO ATB = (CompetitorBO) items.get(i);
                                if (ATB.getCompetitorpid() == Integer
                                        .parseInt(bbid)) {
                                    ArrayList<CompetetorPOSMBO> trackingList = ATB.getCompetitoreason();
                                    for (CompetetorPOSMBO cmp : trackingList) {
                                        if (cmp.getId() == Integer.parseInt(bbid)) {
                                            cmp.setImageName("");
                                            cmp.setImagePath("");
                                        }
                                    }
                                }
                            }

                        } else {
                            for (int i = 0; i < items.size(); i++) {
                                CompetitorBO ATB = (CompetitorBO) items.get(i);
                                if (ATB.getCompetitorpid() == Integer
                                        .parseInt(bbid)) {
                                    ATB.setImageName("");
                                    ATB.setImagePath("");
                                }
                            }
                        }
                        bmodel.competitorTrackingHelper
                                .deleteImageName(imageNameStarts);
                        bmodel.competitorTrackingHelper.deleteFiles(
                                HomeScreenFragment.folder.getPath(), imageNameStarts);
                        dialog.dismiss();

                        Intent intent = new Intent(SubCompetitorTrackingActivity.this,
                                CameraActivity.class);
                        String _path = HomeScreenFragment.photoPath + "/" + imageName;
                        intent.putExtra(
                                getResources().getString(R.string.quality), 40);
                        intent.putExtra(
                                getResources().getString(R.string.path), _path);
                        intent.putExtra(
                                getResources().getString(R.string.saverequired),
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
                bmodel.competitorTrackingHelper.saveCompetitor();
                bmodel.updateIsVisitedFlag();
                if (!calledBy.equals("3"))
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_COMPETITOR);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
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

            bmodel.customProgressDialog(alertDialog, builder, SubCompetitorTrackingActivity.this, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            alertDialog.dismiss();
            //	progressDialogue.dismiss();
            Toast.makeText(SubCompetitorTrackingActivity.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();

            finish();


        }

    }

    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);
            CompetetorPOSMBO bo = (CompetetorPOSMBO) btn.getTag();
            if (this.getTag().equals("datePicker1")) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(getActivity(),
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    bo.setFromDate(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    bo.setFromDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else if (this.getTag().equals("datePicker2")) {
                if (bo.getFromDate() != null && bo.getFromDate().length() > 0) {
                    Date dateMfg = DateUtil.convertStringToDateObject(
                            bo.getFromDate(), outPutDateFormat);
                    if (dateMfg != null && selectedDate.getTime() != null
                            && dateMfg.after(selectedDate.getTime())) {
                        Toast.makeText(getActivity(), R.string.competitor_date,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        bo.setToDate(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                } else {
                    bo.setToDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(
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

  /*  private void eff() {
        String s = mSelectedET.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = mSelectedET.getText() + append;
            mSelectedET.setText(strQuantity);
        } else
            mSelectedET.setText(append);
    }

    public void numberPressed(View vw) {
        int val;
        if (mSelectedET == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                int s = SDUtil.convertToInt(mSelectedET.getText()
                        .toString());
                s = s / 10;
                String strS = s + "";
                mSelectedET.setText(strS);
                val = s;


            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt(append);
            }



        }
    }*/
}
