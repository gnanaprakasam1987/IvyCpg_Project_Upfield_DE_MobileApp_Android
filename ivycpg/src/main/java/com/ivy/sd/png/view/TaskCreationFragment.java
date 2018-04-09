package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

public class TaskCreationFragment extends IvyBaseFragment {

    private EditText taskView;
    private EditText taskTitle;
    private BusinessModel bmodel;
    private ArrayAdapter<ChannelBO> channelAdapter;
    private ArrayAdapter<ChannelBO> channelAdapter1;
    private Spinner channelSpinner;
    private int channelId;
    private CheckBox focusCheck;
    private LinearLayout ll;
    private String mode = "seller";

    private int taskChannelId;
    private String taskTitleDec, taskDetailDesc;

    @Override
    public void onStart() {

        super.onStart();
        taskView = getView().findViewById(R.id.taskView);
        taskTitle = getView().findViewById(R.id.tv);
        channelSpinner = getView().findViewById(R.id.channel);

        //	close.setOnClickListener((OnClickListener) getActivity());
        //	save.setOnClickListener((OnClickListener) getActivity());

        channelSpinner.setEnabled(false);

        ll = getView().findViewById(R.id.allchannel);
        RadioGroup rb = getView().findViewById(R.id.rg);
        rb.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.seller) {
                    ll.setVisibility(View.GONE);
                    mode = "seller";
                }
                if (checkedId == R.id.Channelwise) {
                    ll.setVisibility(View.VISIBLE);
                    mode = "channel";
                }
            }
        });

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        channelAdapter = new ArrayAdapter<ChannelBO>(getActivity(),
                android.R.layout.simple_spinner_item,
                bmodel.channelMasterHelper.getChannelMaster());

        channelAdapter1 = new ArrayAdapter<ChannelBO>(getActivity(),
                android.R.layout.simple_spinner_item,
                new ChannelBO[]{new ChannelBO(0, "Select Channel")});

        channelAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        channelSpinner.setAdapter(channelAdapter1);
        channelSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ChannelBO chBo = (ChannelBO) parent.getSelectedItem();
                channelId = chBo.getChannelId();

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        focusCheck = getView().findViewById(R.id.allcheckbox);
        focusCheck.setOnCheckedChangeListener(new OnCheckedChangeListener() {

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
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_creation,
                container, false);
        setHasOptionsMenu(true);
        return view;
    }

    class SaveNewTask extends AsyncTask<Void, Integer, Integer> {

        //private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
					DataMembers.SD,
					getResources().getString(R.string.saving_new_task), true,
					false);*/

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving_new_task));
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
            //	progressDialogue.dismiss();
            taskView.setText("");
            taskTitle.setText("");
            taskTitleDec = "";
            taskDetailDesc = "";
            taskChannelId = 0;

            Toast.makeText(getActivity(),
                    getResources().getString(R.string.new_task_saved),
                    Toast.LENGTH_SHORT).show();

//			Intent myIntent = new Intent(getActivity(), Task.class);
//			myIntent.putExtra("IsRetailerwisetask", false);
//			startActivity(myIntent);
            getActivity().finish();

        }

    }

    private boolean validate() {
        boolean ok = true;

        if (taskTitle.getText().toString().equals("")) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.enter_task_title),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        } else if (taskView.getText().toString().equals("")) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.enter_task_description),
                    Toast.LENGTH_SHORT).show();
            ok = false;
        }
        return ok;
    }

    public void onBackPressed() {
        // do something on back.
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getActivity().getMenuInflater().inflate(R.menu.menu_task, menu);
        return true;

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_new_task).setVisible(false);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {//			Intent myIntent = new Intent(getActivity(), Task.class);
//			myIntent.putExtra("IsRetailerwisetask", false);
//			startActivity(myIntent);
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_next) {
            taskDetailDesc = taskView.getText().toString();
            taskTitleDec = taskTitle.getText().toString();

            if (!validate())
                return true;

            if (mode.equals("seller")) {
                taskChannelId = 0;
            } else {
                if (focusCheck.isChecked()) {
                    taskChannelId = -1;
                } else {
                    taskChannelId = channelId;
                }
            }

            new SaveNewTask().execute();
            return true;
        }

        return false;
    }


}
