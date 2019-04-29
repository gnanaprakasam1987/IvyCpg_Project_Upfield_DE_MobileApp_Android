package com.ivy.cpg.view.task;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskCreation extends IvyBaseActivityNoActionBar implements OnClickListener {

    private EditText taskView;
    private EditText taskTitle;
    private Button save;
    private BusinessModel bmodel;

    private int channelId, retailerid;

    private boolean fromHomeScreen = false;
    private boolean isRetailerTask = false;
    private String screenTitle = "";


    private int taskChannelId;
    private String taskTitleDec, taskDetailDesc;
    private int mSelectedUserId = 0;
    ArrayList<UserMasterBO> sellerUserList;
    private TaskHelper taskHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_creation);
        bmodel = (BusinessModel) getApplicationContext();
        taskHelper = TaskHelper.getInstance(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        setSupportActionBar(toolbar);
        if (toolbar != null && getSupportActionBar() != null) {
            TextView mScreenTitleTV = findViewById(R.id.tv_toolbar_title);
            mScreenTitleTV.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
            getSupportActionBar().setTitle(null);
            mScreenTitleTV.setText(getResources().getString(R.string.task_creation));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (extras != null) {
            if (extras.containsKey("fromHomeScreen")) {
                fromHomeScreen = extras.getBoolean("fromHomeScreen");
            }

            if (extras.containsKey("IsRetailerwisetask")) {
                isRetailerTask = extras.getBoolean("IsRetailerwisetask");
            }

            if (extras.containsKey("screentitle")) {
                screenTitle = extras.getString("screentitle");
            }
        }

        taskView = findViewById(R.id.taskView);
        taskTitle = findViewById(R.id.tv);
        final Spinner channelSpinner = findViewById(R.id.channel);
        channelSpinner.setEnabled(false);
        final Spinner retailerSpinner = findViewById(R.id.spinner_seller);
        retailerSpinner.setEnabled(false);
        final Spinner sellerSpinner = findViewById(R.id.spinner_seller);
        sellerSpinner.setEnabled(true);
        save = findViewById(R.id.saveTask);
        bmodel.setContext(this);
        save.setOnClickListener(this);

        TextView task_title = findViewById(R.id.task_title_tv);
        TextView applicable_tv = findViewById(R.id.applicable_tv);
        task_title.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        applicable_tv.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        taskTitle.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        taskView.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        save.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));


        RadioGroup rb = findViewById(R.id.rg_selection);
        final RadioButton seller_rb = findViewById(R.id.seller);
        final RadioButton channelwise_rb = findViewById(R.id.Channelwise);
        final RadioButton retailerwise_rb = findViewById(R.id.Retailerwise);
        seller_rb.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        TypedArray typearr = this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        final int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
        final int secondary_color = typearr.getColor(R.styleable.MyTextView_textColorSecondary, 0);
        seller_rb.setTextColor(color);
        channelwise_rb.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        retailerwise_rb.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.seller) {
                    seller_rb.setTextColor(color);
                    channelwise_rb.setTextColor(secondary_color);
                    retailerwise_rb.setTextColor(secondary_color);
                    channelSpinner.setSelection(0);
                    channelSpinner.setEnabled(false);
                    retailerSpinner.setSelection(0);
                    retailerSpinner.setEnabled(false);
                    sellerSpinner.setEnabled(true);
                    taskHelper.mode = "seller";
                } else if (checkedId == R.id.Channelwise) {
                    seller_rb.setTextColor(secondary_color);
                    channelwise_rb.setTextColor(color);
                    retailerwise_rb.setTextColor(secondary_color);
                    channelSpinner.setEnabled(true);
                    retailerSpinner.setSelection(0);
                    retailerSpinner.setEnabled(false);
                    sellerSpinner.setSelection(0);
                    sellerSpinner.setEnabled(false);
                    taskHelper.mode = "channel";
                } else if (checkedId == R.id.Retailerwise) {
                    seller_rb.setTextColor(secondary_color);
                    channelwise_rb.setTextColor(secondary_color);
                    retailerwise_rb.setTextColor(color);
                    channelSpinner.setSelection(0);
                    channelSpinner.setEnabled(false);
                    retailerSpinner.setEnabled(true);
                    sellerSpinner.setSelection(0);
                    sellerSpinner.setEnabled(false);
                    taskHelper.mode = "retailer";
                }
            }
        });

        //allow only create task only for retailer if not from seller Task
        if (!fromHomeScreen) {
            rb.setVisibility(View.GONE);
           // (this.findViewById(R.id.task_spinner_layouts)).setVisibility(View.GONE);
            applicable_tv.setVisibility(View.GONE);
            taskHelper.mode = "retailer";
        }

        ArrayAdapter<ChannelBO> channelAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        HashMap<String, ChannelBO> channelhashbo = new HashMap<>();
        channelAdapter.add(new ChannelBO(0, getResources().getString(R.string.all_channel)));
        int siz = bmodel.getRetailerMaster().size();
        for (int ii = 0; ii < siz; ii++) {
            for (ChannelBO temp : bmodel.channelMasterHelper.getChannelMaster()) {
                if (temp.getChannelId() == bmodel.getRetailerMaster().get(ii).getChannelID()) {
                    if (((bmodel
                            .getRetailerMaster().get(ii).getIsToday() == 1)) ||
                            (bmodel.getRetailerMaster().get(ii).getIsDeviated() != null
                                    && bmodel.getRetailerMaster().get(ii).getIsDeviated().equals("Y"))) {


                        ChannelBO temp2 = channelhashbo.get(temp.getChannelId() + "");
                        if (temp2 == null) {
                            channelAdapter.add(temp);
                            channelhashbo.put(temp.getChannelId() + "", temp);
                        }


                    }
                }
            }
        }

        channelAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        channelSpinner.setAdapter(channelAdapter);
        channelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ((TextView) view.findViewById(android.R.id.text1)).setGravity(Gravity.START);
                ChannelBO chBo = (ChannelBO) parent.getSelectedItem();
                if (chBo.getChannelName().equalsIgnoreCase(getResources().getString(R.string.all_channel))) {
                    channelId = -1;
                } else {
                    channelId = chBo.getChannelId();
                }

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        bmodel.channelMasterHelper.getRetailers();
        try {
            ArrayAdapter<RetailerMasterBO> retailerAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_bluetext_layout,
                    bmodel.channelMasterHelper.getRetailerMaster());
            retailerAdapter.insert(new RetailerMasterBO(0, getResources().getString(R.string.all_retailer)), 0);
            retailerAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            retailerSpinner.setAdapter(retailerAdapter);
            retailerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    ((TextView) view.findViewById(android.R.id.text1)).setGravity(Gravity.START);
                    RetailerMasterBO reBo = (RetailerMasterBO) parent.getSelectedItem();
                    if (reBo.getTretailerName().equalsIgnoreCase(getResources().getString(R.string.all_retailer))) {
                        retailerid = -2;
                    } else {
                        retailerid = reBo.getTretailerId();
                    }
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }

        sellerUserList = new ArrayList<>();
        sellerUserList.add(0, new UserMasterBO(0, "Select Seller"));
        sellerUserList.addAll(bmodel.userMasterHelper.downloadAllUser());
        for (UserMasterBO userMasterBO : sellerUserList)
            if (userMasterBO.getUserid() == bmodel.userMasterHelper.getUserMasterBO().getUserid()) {
                userMasterBO.setUserName("Self");
                break;
            }

        ArrayAdapter<UserMasterBO> sellerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, sellerUserList);
        sellerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        sellerSpinner.setAdapter(sellerAdapter);
        sellerSpinner.setSelection(0);
        sellerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ((TextView) view.findViewById(android.R.id.text1)).setGravity(Gravity.START);
                mSelectedUserId = position;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_new_task).setVisible(false);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (fromHomeScreen)
                startActivity(new Intent(TaskCreation.this,
                        HomeScreenActivity.class).putExtra("menuCode", "MENU_TASK_NEW"));
            else
                startActivity(new Intent(TaskCreation.this,
                        Task.class).putExtra("IsRetailerwisetask", isRetailerTask)
                        .putExtra("screentitle", screenTitle));
            finish();
            return true;
        }
        return false;
    }

    public void onClick(View comp) {
        Button bt = (Button) comp;
        if (bt.equals(save)) {

            taskDetailDesc = bmodel.validateInput(taskView.getText().toString());
            taskTitleDec = bmodel.validateInput(taskTitle.getText().toString());

            if (!validate())
                return;

            switch (taskHelper.mode) {
                case "seller":
                    if (mSelectedUserId == 0)
                        taskChannelId = bmodel.userMasterHelper.getUserMasterBO().getUserid();
                    else
                        taskChannelId = sellerUserList.get(mSelectedUserId).getUserid();

                    break;
                case "retailer":
                    if (fromHomeScreen)
                        taskChannelId = retailerid;
                    else
                        taskChannelId = SDUtil.convertToInt(bmodel.getRetailerMasterBO().getRetailerID());
                    break;
                default:
                    taskChannelId = channelId;
                    break;
            }

            new SaveNewTask().execute();

        }


    }

    @SuppressLint("StaticFieldLeak")
    class SaveNewTask extends AsyncTask<Void, Integer, Integer> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(TaskCreation.this);

            customProgressDialog(builder, getResources().getString(R.string.saving_new_task));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                taskHelper.saveTask(taskChannelId, taskTitleDec,
                        taskDetailDesc);
            } catch (Exception e) {
                Commons.printException(e);
            }
            return 1; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer status) {
            // result is the value returned from doInBackground

            alertDialog.dismiss();
            taskView.setText("");
            taskTitle.setText("");
            taskTitleDec = "";
            taskDetailDesc = "";
            taskChannelId = 0;
            taskHelper.mode = "seller";
            Toast.makeText(TaskCreation.this,
                    getResources().getString(R.string.new_task_saved),
                    Toast.LENGTH_SHORT).show();
            if (fromHomeScreen)
                startActivity(new Intent(TaskCreation.this,
                        HomeScreenActivity.class).putExtra("menuCode", "MENU_TASK_NEW"));
            else
                startActivity(new Intent(TaskCreation.this,
                        Task.class).putExtra("IsRetailerwisetask", isRetailerTask)
                        .putExtra("screentitle", screenTitle));

            finish();

        }

    }

    private boolean validate() {
        boolean ok = true;

        if (taskTitle.getText().toString().equals("")) {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_task_title),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (taskView.getText().toString().equals("")) {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_task_description),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    public void onBackPressed() {
        // do something on back.
    }


}
