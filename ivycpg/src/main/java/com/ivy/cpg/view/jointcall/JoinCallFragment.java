package com.ivy.cpg.view.jointcall;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

import static com.ivy.lib.Utils.QT;

public class JoinCallFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private ArrayList<UserMasterBO> mjoinCallUserList;
    private UserMasterBO mSelectedUserBO;
    private RecyclerView recyclerViewJonitcall;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_join_call_main, container, false);

        recyclerViewJonitcall = view.findViewById(R.id.jointcall_recycview);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setElevation(0);
            actionBar.setDisplayShowTitleEnabled(false);
        }

        setScreenTitle(bmodel.configurationMasterHelper.getJointCallTitle());

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    private void updateList() {
        bmodel.userMasterHelper.downloadJoinCallusers();
        bmodel.userMasterHelper.downloadDistributionDetails();
        mjoinCallUserList = bmodel.userMasterHelper.getUserMasterBO()
                .getJoinCallUserList();

        if (mjoinCallUserList != null) {
            loadJoincalldata();
        }
    }

    /**
     * Method used to call Joint call RecyclerView.
     */
    private void loadJoincalldata() {
        JointCallRecyclerAdapter adapter = new JointCallRecyclerAdapter();
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewJonitcall.setLayoutManager(mLayoutManager);
        recyclerViewJonitcall.setItemAnimator(new DefaultItemAnimator());
        recyclerViewJonitcall.setAdapter(adapter);
    }

    /**
     * Custom Recyclerview adapter for Jointcall.
     */
    public class JointCallRecyclerAdapter extends RecyclerView.Adapter<JointCallRecyclerAdapter.MyViewHolder> {


        public class MyViewHolder extends RecyclerView.ViewHolder {
            private UserMasterBO userBO;
            private TextView nameTV;
            private ImageView inandoutBTN;
            private TextView userType;
            private LinearLayout llCircle, parentLayout, childLayout;
            private ImageView dashedLine;

            public MyViewHolder(View view) {
                super(view);

                nameTV = view
                        .findViewById(R.id.tv_join_user_name);
                inandoutBTN = view
                        .findViewById(R.id.btn_inandout);
                userType = view
                        .findViewById(R.id.tv_join_user_type);
                llCircle = view
                        .findViewById(R.id.icon_ll);
                parentLayout = view
                        .findViewById(R.id.parentLayout);
                childLayout = view
                        .findViewById(R.id.childLayout);
                dashedLine = view
                        .findViewById(R.id.dashedline);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_join_call, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final JointCallRecyclerAdapter.MyViewHolder holder, int position) {

            //Custom font set for Textviews.
            holder.userType.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            holder.nameTV.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

            //Set values from UsermasterBo list
            holder.userBO = mjoinCallUserList.get(position);
            holder.nameTV.setText(holder.userBO.getUserName());
            holder.userType.setText(holder.userBO.getUserType());

            // Login and Logout icon changes dynamically.
            if (holder.userBO.getIsJointCall() == 1) {
                holder.llCircle.setBackgroundResource(R.drawable.activity_icon_bg_completed);
                holder.inandoutBTN.setImageResource(R.drawable.ic_vector_logout);
                holder.inandoutBTN.setColorFilter(Color.argb(255, 255, 255, 255));

            } else {
                holder.llCircle.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.inandoutBTN.setImageResource(R.drawable.activity_icon_next);
                holder.inandoutBTN.setColorFilter(Color.argb(0, 0, 0, 0));
            }

            if (position != 0) {
                if (mjoinCallUserList.get(position - 1).getUserType().equalsIgnoreCase(holder.userBO.getUserType())) {
                    holder.userType.setVisibility(View.GONE);
                    holder.dashedLine.setVisibility(View.GONE);
                    if (position % 2 == 0) {
                        holder.childLayout.setBackgroundColor(getResources().getColor(R.color.list_even_item_bg));
                    } else {
                        holder.childLayout.setBackgroundColor(getResources().getColor(R.color.list_odd_item_bg));
                    }
                } else {
                    holder.childLayout.setBackgroundColor(getResources().getColor(R.color.white));
                    holder.userType.setVisibility(View.VISIBLE);
                    holder.dashedLine.setVisibility(View.VISIBLE);
                }
            } else {//postion  0 for first joint call user
                holder.childLayout.setBackgroundColor(getResources().getColor(R.color.white));
                holder.userType.setVisibility(View.VISIBLE);
                holder.dashedLine.setVisibility(View.VISIBLE);

            }

            //Login and Logout functionality click listener.
            holder.parentLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSelectedUserBO = holder.userBO;
                    if (holder.userBO.getIsJointCall() == 1) {
                        showDialogLogout();
                    } else {
                        if (isAlreadyLoginJointUser()
                                || bmodel.configurationMasterHelper.IS_MULTIPLE_JOINCALL) {
                            FragmentManager fm = getActivity().getSupportFragmentManager();
                            JointCallFragmentDialog dialog = new JointCallFragmentDialog(
                                    holder.userBO,
                                    new JoinDialogInterface() {

                                        @Override
                                        public void updateJoinList() {
                                            loadJoincalldata();
                                            updateJoinDetails(1);
                                        }

                                        @Override
                                        public void insertJointCallDetails(String remarks) {
                                            insertJoinCallDetails(remarks);
                                        }


                                    });

                            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                            dialog.show(fm, "Join Call Fragment");
                            dialog.setCancelable(false);
                        } else {
                            Toast.makeText(getActivity(),
                                    getResources()
                                            .getString(
                                                    R.string.already_another_user_login),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mjoinCallUserList.size();
        }

        private boolean isAlreadyLoginJointUser() {
            for (UserMasterBO userBo : mjoinCallUserList) {
                if (userBo.getIsJointCall() == 1) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Method used to logout joint call user.
     */
    private void showDialogLogout() {

        final Dialog dialog = new Dialog(getActivity());
        final ViewGroup nullParent = null;
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_jointcall_logout, nullParent);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        Button cancelBtn = dialog.findViewById(R.id.btn_cancel);
        Button yesBtn = dialog.findViewById(R.id.btn_yes);
        yesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedUserBO.setIsJointCall(0);
                loadJoincalldata();
                updateJoinDetails(0);
                if (bmodel.configurationMasterHelper.IS_SHOW_JOINT_CALL_REMARKS)
                    updateJoinCallDetails();
                dialog.dismiss();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if ("JOINT_CALL".equals(getActivity().getIntent().getStringExtra("From"))) {
                    getActivity().finish();
                } else {
                    Intent i = new Intent(getActivity(), HomeScreenActivity.class);
                    startActivity(i);
                    getActivity().finish();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Method used to update Joint call login status.
     *
     * @param value to update in usermaster table
     */
    private void updateJoinDetails(int value) {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.updateSQL("update usermaster set isJointCall=" + value
                    + " where userid=" + mSelectedUserBO.getUserid());
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void insertJoinCallDetails(String remarks) {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();


            ArrayList<UserMasterBO> joinCallUserList = bmodel.userMasterHelper
                    .getUserMasterBO().getJoinCallUserList();
            if (joinCallUserList != null) {
                String columns = "Uid,UserId,JointCallUserId,TimeIn,TimeOut,Remarks,DateTime";
                for (UserMasterBO userMasterBO : joinCallUserList) {
                    if (userMasterBO.getIsJointCall() == 1) {
                        String date = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                        String time = DateTimeUtils.now(DateTimeUtils.TIME);

                        String uId = DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

                        String values = uId + "," + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "," + userMasterBO.getUserid() + "," +
                                QT(date + " " + time) + "," + QT(date + " " + time) + "," + QT(remarks) + "," + QT(DateTimeUtils.now(DateTimeUtils.DATE_TIME));

                        db.insertSQL("JointCallDetail", columns, values);
                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void updateJoinCallDetails() {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String date = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            String time = DateTimeUtils.now(DateTimeUtils.TIME);
            db.updateSQL("update JointCallDetail set upload='N',TimeOut=" + QT(date + " " + time)
                    + " where TimeIn=TimeOut and JointCallUserId=" + mSelectedUserBO.getUserid());
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
