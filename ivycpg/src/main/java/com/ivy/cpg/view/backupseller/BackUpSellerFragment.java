package com.ivy.cpg.view.backupseller;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by ramkumard on 6/4/18
 */

public class BackUpSellerFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private View view;
    private AlertDialog alertDialog;
    private int lastCheckedPos = -1;
    private String backupSellerId;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getActivity() != null) {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_backupseller, container, false);

        if (getActivity() != null) {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        //Set Screen Title
        try {
            if (bmodel != null && getArguments() != null && getArguments().getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_BACKUP_SELLER"));
            else
                setScreenTitle(getArguments().getString("screentitle"));
        } catch (Exception e) {

            setScreenTitle(getActivity().getResources().getString(R.string.backup_seller));
            Commons.printException(e);
        }
        DrawerLayout mDrawerLayout = view.findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                setScreenTitle(bmodel.mSelectedActivityName);
                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActivity().invalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        RecyclerView rc_users = view.findViewById(R.id.rc_users);
        rc_users.setLayoutManager(new LinearLayoutManager(getActivity()));
        rc_users.setAdapter(new RecyclerAdapter(bmodel.userMasterHelper.getBackupSellerList()));
        Button btn_submit = view.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (lastCheckedPos > -1) {
                    backupSellerId = bmodel.userMasterHelper.getBackupSellerList().get(lastCheckedPos).getUserid() + "";
                    new UploadBackupSeller().execute();
                } else
                    Toast.makeText(getActivity(), "Please select user", Toast.LENGTH_LONG).show();
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bmodel.getAppDataProvider().getUser().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            if (getActivity() != null)
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null) {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());
        }

        if (bmodel.getAppDataProvider().getUser().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view rootview
     */
    private void unbindDrawables(View view) {
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            if (getActivity() != null)
            getActivity().finish();
            return true;
        }

        return false;
    }

    @SuppressLint("HandlerLeak")
    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    alertDialog.dismiss();
                    showAlert(getResources().getString(
                            R.string.successfully_uploaded));
                    break;
                case 1:
                    alertDialog.dismiss();
                    if (getActivity() != null) {
                        bmodel = (BusinessModel) getActivity().getApplicationContext();
                        bmodel.showAlert(getResources().getString(
                                R.string.seller_already_assigned), 0);
                    }
                    break;
                case 2:
                    alertDialog.dismiss();
                    if (getActivity() != null) {
                        bmodel = (BusinessModel) getActivity().getApplicationContext();
                        bmodel.showAlert(getResources().getString(
                                R.string.backup_seller_already_assigned), 0);
                    }
                    break;
                case 3:
                    alertDialog.dismiss();
                    if (getActivity() != null) {
                        bmodel = (BusinessModel) getActivity().getApplicationContext();
                        bmodel.showAlert(getResources().getString(
                                R.string.user_already_assigned), 0);
                    }
                    break;
                case DataMembers.NOTIFY_NO_INTERNET:
                    if (getActivity() != null) {
                        bmodel = (BusinessModel) getActivity().getApplicationContext();
                        bmodel.showAlert(getResources().getString(
                                R.string.please_connect_to_internet), 0);
                    }
                    break;
                case DataMembers.NOTIFY_UPLOAD_ERROR:
                    alertDialog.dismiss();
                    if (getActivity() != null) {
                        bmodel = (BusinessModel) getActivity().getApplicationContext();
                        bmodel.showAlert(getResources().getString(
                                R.string.error_e10), 0);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    public Handler getHandler() {
        return handler;
    }

    class UploadBackupSeller extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder,
                    getResources().
                            getString(R.string.uploading_data));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {

            if (NetworkUtils.isNetworkConnected(getActivity())) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(getActivity());
                String rid = mUploadHelper.uploadBackupSeller(backupSellerId, getHandler());

                if ("0".equals(rid)) {
                    getHandler().sendEmptyMessage(0);
                    return false;
                } else if ("E29".equals(rid)) {
                    getHandler().sendEmptyMessage(1);
                    return false;
                } else if ("E30".equals(rid)) {
                    getHandler().sendEmptyMessage(2);
                    return false;

                } else if ("E28".equals(rid)) {
                    getHandler().sendEmptyMessage(3);
                    return false;
                } else {
                    getHandler().sendEmptyMessage(
                            DataMembers.NOTIFY_UPLOAD_ERROR);
                    return false;
                }
            } else {
                getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_NO_INTERNET);
                return false;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

        }

    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        ArrayList<UserMasterBO> data;

        RecyclerAdapter(ArrayList<UserMasterBO> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.list_item_backupseller, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.userMasterBO = data.get(position);
            holder.tv_username.setText(holder.userMasterBO.getUserName());
            holder.radioButton.setChecked(holder.userMasterBO.isBackup());
            holder.radioButton.setChecked(lastCheckedPos == position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_username;
            RadioButton radioButton;
            UserMasterBO userMasterBO;

            MyViewHolder(View itemView) {
                super(itemView);
                tv_username = itemView.findViewById(R.id.tv_user_name);
                radioButton = itemView.findViewById(R.id.rb_seller);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastCheckedPos = getAdapterPosition();
                        userMasterBO.setBackup(true);
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        startActivity(new Intent(getActivity(),
                                HomeScreenActivity.class));
                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

}
