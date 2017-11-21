package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.HashMap;

public class TaskCreation extends IvyBaseActivityNoActionBar implements OnClickListener {

    private EditText taskView;
    private EditText taskTitle;
    private Button close, save;
    private BusinessModel bmodel;

    private int channelId, retailerid;
    private LinearLayout ll, rll;

    private boolean fromHomeScreen = false;


    private int taskChannelId;
    private String taskTitleDec, taskDetailDesc;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task_creation);
        bmodel = (BusinessModel) getApplicationContext();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        setSupportActionBar(toolbar);
        if (toolbar != null && getSupportActionBar() != null) {
            TextView mScreenTitleTV = (TextView) findViewById(R.id.tv_toolbar_title);
            mScreenTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            getSupportActionBar().setTitle(null);
            mScreenTitleTV.setText(getResources().getString(R.string.task_creation));
            // getSupportActionBar().setIcon(R.drawable.icon_order);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (extras != null) {
            if (extras.containsKey("fromHomeScreen")) {
                fromHomeScreen = extras.getBoolean("fromHomeScreen");
            }
        }

        taskView = (EditText) findViewById(R.id.taskView);
        taskTitle = (EditText) findViewById(R.id.tv);
        Spinner channelSpinner = (Spinner) findViewById(R.id.channel);
        Spinner retailerSpinner = (Spinner) findViewById(R.id.retailer);
        close = (Button) findViewById(R.id.closeTask);
        save = (Button) findViewById(R.id.saveTask);
        bmodel.setContext(this);
        close.setOnClickListener(this);
        save.setOnClickListener(this);

        TextView task_title = (TextView) findViewById(R.id.task_title_tv);
        TextView applicable_tv = (TextView) findViewById(R.id.applicable_tv);
        task_title.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        applicable_tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        taskTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        taskView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        save.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.LIGHT));

        /*channelSpinner.setEnabled(false);
        retailerSpinner.setEnabled(false);*/
        ll = (LinearLayout) findViewById(R.id.allchannel);
        rll = (LinearLayout) findViewById(R.id.allretailer);
        RadioGroup rb = (RadioGroup) findViewById(R.id.rg);
        CheckBox focusCheck = (CheckBox) findViewById(R.id.allcheckbox);
        focusCheck.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        CheckBox retailercheck = (CheckBox) findViewById(R.id.allretaicheckbox);
        retailercheck.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        final RadioButton seller_rb = (RadioButton) findViewById(R.id.seller);
        final RadioButton channelwise_rb = (RadioButton) findViewById(R.id.Channelwise);
        final RadioButton retailerwise_rb = (RadioButton) findViewById(R.id.Retailerwise);
        seller_rb.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        TypedArray typearr = this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        final int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
        final int secondary_color = typearr.getColor(R.styleable.MyTextView_textColorSecondary, 0);
        seller_rb.setTextColor(color);
        channelwise_rb.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        retailerwise_rb.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.seller) {
                    seller_rb.setTextColor(color);
                    channelwise_rb.setTextColor(secondary_color);
                    retailerwise_rb.setTextColor(secondary_color);
                    ll.setVisibility(View.GONE);
                    rll.setVisibility(View.GONE);
                    bmodel.taskHelper.mode = "seller";
                } else if (checkedId == R.id.Channelwise) {
                    seller_rb.setTextColor(secondary_color);
                    channelwise_rb.setTextColor(color);
                    retailerwise_rb.setTextColor(secondary_color);
                    ll.setVisibility(View.VISIBLE);
                    rll.setVisibility(View.GONE);
                    bmodel.taskHelper.mode = "channel";
                } else if (checkedId == R.id.Retailerwise) {
                    seller_rb.setTextColor(secondary_color);
                    channelwise_rb.setTextColor(secondary_color);
                    retailerwise_rb.setTextColor(color);
                    ll.setVisibility(View.GONE);
                    rll.setVisibility(View.VISIBLE);
                    bmodel.taskHelper.mode = "retailer";
                }
            }
        });

        //allow only create task only for retailer if not from seller Task
        if (!fromHomeScreen) {
            rb.setVisibility(View.GONE);
            applicable_tv.setVisibility(View.GONE);
            bmodel.taskHelper.mode = "retailer";
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
                            .getRetailerMaster().get(ii).getIsToday() == 1)) || bmodel.getRetailerMaster().get(ii).getIsDeviated()
                            .equals("Y")) {


                        ChannelBO temp2 = channelhashbo.get(temp.getChannelId() + "");
                        System.out.println("temp=" + temp.getChannelId() + "," + temp.getChannelName());
                        if (temp2 == null) {
                            System.out.println("temp2=null");

                            channelAdapter.add(temp);
                            channelhashbo.put(temp.getChannelId() + "", temp);
                        } else {
                            System.out.println("temp2=!null");

                        }


                    }
                }
            }
        }

        /*channelAdapter1 = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new ChannelBO[]{new ChannelBO(0, "Select Channel")});*/

        channelAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        channelSpinner.setAdapter(channelAdapter);
        channelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
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
        //retailer spinner
        try {
            ArrayAdapter<RetailerMasterBO> retailerAdapter = new ArrayAdapter<>(this,
                    R.layout.spinner_bluetext_layout,
                    bmodel.channelMasterHelper.getRetailerMaster());
            retailerAdapter.insert(new RetailerMasterBO(0, getResources().getString(R.string.all_retailer)), 0);
            /*retailerAdapter1 = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item,
                    new RetailerMasterBO[]{new RetailerMasterBO(0, "Select Retailer")});*/

            retailerAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            retailerSpinner.setAdapter(retailerAdapter);
            retailerSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
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

        /*focusCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    channelSpinner.setAdapter(channelAdapter1);
                    channelSpinner.setEnabled(false);
                } else {
                    channelSpinner.setAdapter(channelAdapter);
                    channelSpinner.setEnabled(true);
                }

            }
        });*/
        /*retailercheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {

                if (isChecked) {
                    retailerSpinner.setAdapter(retailerAdapter1);
                    retailerSpinner.setEnabled(false);
                } else {
                    retailerSpinner.setAdapter(retailerAdapter);
                    retailerSpinner.setEnabled(true);
                }

            }
        });*/
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

            if (validate() == false)
                return;

            if (bmodel.taskHelper.mode.equals("seller")) {
                taskChannelId = 0;
            } else if (bmodel.taskHelper.mode.equals("retailer")) {
                if (fromHomeScreen)
                    taskChannelId = retailerid;
                else
                    taskChannelId = SDUtil.convertToInt(bmodel.getRetailerMasterBO().getRetailerID());
            } else {
                taskChannelId = channelId;
            }

            new SaveNewTask().execute();

        } else if (bt.equals(close)) {
            /*Intent myIntent = new Intent(TaskCreation.this, Task.class);
            myIntent.putExtra("IsRetailerwisetask", false);
			startActivity(myIntent);*/
            finish();
        }

    }

    class SaveNewTask extends AsyncTask<Void, Integer, Integer> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(TaskCreation.this);

            customProgressDialog(builder, TaskCreation.this, getResources().getString(R.string.saving_new_task));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                bmodel.taskHelper.saveTask(taskChannelId, taskTitleDec,
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
            bmodel.taskHelper.mode = "seller";
            Toast.makeText(TaskCreation.this,
                    getResources().getString(R.string.new_task_saved),
                    Toast.LENGTH_SHORT).show();


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
        return;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
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
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

}
