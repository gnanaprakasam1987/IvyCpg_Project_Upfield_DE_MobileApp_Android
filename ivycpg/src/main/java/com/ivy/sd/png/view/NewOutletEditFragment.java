package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by hanifa.m on 5/5/2017.
 */

public class NewOutletEditFragment extends IvyBaseFragment {
    RecyclerView recyclerView;
    private BusinessModel bmodel;
    LinearLayoutManager linearLayoutManager;
    GridLayoutManager gridLayoutManager;
    Bundle bundle;
    private DisplayMetrics displaymetrics;
    private NewOutletBO selected_sortlist;
    private String selectedRetId;
    private ArrayList<NewOutletBO> sortListNew = new ArrayList<>();
    private RecyclerViewAdapter recycleradapter;
    private int lastCheckedPosition = -1;
    TextView toolBarTitle;
    private boolean isLeave_today, isremove = false;
    public ArrayList<ChannelBO> mChannelList;
    public static boolean fromHomeScreen = false;
    private TextView textDetails;
    private Button okBtn, cancelBtn;
    private boolean isdialog = false;
    private AlertDialog alertDialog;
    private View view;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

view=inflater.inflate(R.layout.activity_new_profile_edit,container,false);

        recyclerView = (RecyclerView)view.findViewById(R.id.rcv_new_retailers);


        if(((AppCompatActivity) getActivity()).getSupportActionBar() !=null){
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
        }

        if (bmodel.mAttendanceHelper.checkLeaveAttendance())
            isLeave_today = true;

        displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(sizeLarge);

        setScreenTitle(getArguments().getString("screentitle"));
        if (is7InchTablet)
            gridLayoutManager = new GridLayoutManager(getActivity(), 2);
        else
            gridLayoutManager = new GridLayoutManager(getActivity(), 1);

        recyclerView.setLayoutManager(gridLayoutManager);

        recycleradapter = new RecyclerViewAdapter(bmodel.newOutletHelper.getNewRetailers());
        recyclerView.setAdapter(recycleradapter);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<NewOutletBO> items;


        public RecyclerViewAdapter(ArrayList<NewOutletBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_new_profile_edit, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final NewOutletBO retailer = items.get(position);

            holder.retailer_name.setText(retailer.getOutletName());
            holder.retailer_address.setText(retailer.getAddress3());
            holder.ib_edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bmodel.newOutletHelper.loadNewOutletConfiguration(0);
                    Intent i = new Intent(getActivity(), NewOutlet.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("screenMode", 2);
                    i.putExtra("retailerId", retailer.getRetailerId());
                    startActivity(i);
                    getActivity().finish();
                }
            });
            holder.ll_retailer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bmodel.newOutletHelper.loadNewOutletConfiguration(0);
                    Intent i = new Intent(getActivity(), NewOutlet.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra("screenMode", 1);
                    i.putExtra("retailerId", retailer.getRetailerId());
                    startActivity(i);
                    getActivity().finish();
                }
            });

            holder.ib_delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    selectedRetId = retailer.getRetailerId();
                    if (!selectedRetId.isEmpty()) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        View alertLayout = inflater.inflate(R.layout.custom_dialog_newone, null);
                        builder.setView(alertLayout);
                        builder.setCancelable(false);
                        textDetails = (TextView) alertLayout.findViewById(R.id.header_txt);
                        okBtn = (Button) alertLayout.findViewById(R.id.ok_btn);
                        cancelBtn = (Button) alertLayout.findViewById(R.id.cancel_btn);
                        textDetails.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                        textDetails.setText("Do u want to Delete this  " + retailer.getOutletName() + " .....?");
                        okBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new DeleteAsyncTask().execute();
                                alertDialog.dismiss();
                                items.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, items.size());

                            }
                        });

                        cancelBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog = builder.create();
                        alertDialog.show();

                    }
                }
            });


            if (bmodel.configurationMasterHelper.IS_NEW_RETAILER_EDIT)
                holder.ib_edit.setVisibility(View.VISIBLE);
            else
                holder.ib_edit.setVisibility(View.GONE);

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView retailer_name, retailer_address;
            ImageButton ib_edit, ib_delete;
            LinearLayout ll_retailer;

            public ViewHolder(View v) {
                super(v);
                retailer_name = (TextView) v.findViewById(R.id.tv_retailername);
                retailer_address = (TextView) v.findViewById(R.id.tv_retaileraddress);
                ib_edit = (ImageButton) v.findViewById(R.id.iv_edit);
                ib_delete = (ImageButton) v.findViewById(R.id.delete);
                ll_retailer = (LinearLayout) v.findViewById(R.id.ll_retailer_name);

                retailer_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                retailer_address.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            }


        }
    }



    public class NewOutletEditSortDialog extends Dialog {
        private BusinessModel bmodel;
        private Context context;
        private String listLoad;
        private TextView headerText;
        private boolean isdialog = false;

        protected NewOutletEditSortDialog(final Context context, final String listLoad) {
            super(context);

            this.context = context;
            this.listLoad = listLoad;
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            bmodel = (BusinessModel) context.getApplicationContext();
            setContentView(R.layout.custom_dialog_newoutlet_edit);
            int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7inch" tablet
            if (sizeLarge == 3)
                isdialog = true;

            if (isdialog)
                getWindow().setLayout(1000, 600);
            else if (!isdialog)
                getWindow().setLayout(1000, 760);
            this.setCancelable(false);


            ArrayList<String> listName = new ArrayList<>();
            RecyclerView reason_recycler = (RecyclerView) findViewById(R.id.reason_recycler);
            reason_recycler.setLayoutManager(new LinearLayoutManager(context));
            ArrayList<NewOutletBO> sortList = new ArrayList<>();
            NewOutletBO outletBO;
            listName.add("A - Z");
            listName.add("Z - A");
//            listName.add("New Retailers");
//            listName.add("Last Modified");

            for (int i = 0; i < listName.size(); i++) {
                outletBO = new NewOutletBO();
                outletBO.setSrotText(listName.get(i));
                sortList.add(outletBO);
            }

            reason_recycler.setAdapter(new SortListAdapter(sortList));
        }


        class SortListAdapter extends RecyclerView.Adapter<SortListAdapter.ViewHolder> {
            private ArrayList<NewOutletBO> items;

            public SortListAdapter(ArrayList<NewOutletBO> items) {
                this.items = items;
            }


            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.common_reason_popup_recycler_items, parent, false);
                return new ViewHolder(v);
            }

            @Override
            public void onBindViewHolder(final ViewHolder holder, final int position) {
                holder.sortonObj = items.get(position);
                holder.sortlist_radio_btn.setText(holder.sortonObj.getSrotText());
                if (holder.sortonObj.isCheckedList())
                    holder.sortlist_radio_btn.setChecked(true);
                else
                    holder.sortlist_radio_btn.setChecked(false);

                holder.sortlist_radio_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastCheckedPosition = getItemViewType(position);
                        selected_sortlist = holder.sortonObj;

                        if (holder.sortlist_radio_btn.isChecked()) {
                            holder.sortonObj.setCheckedList(true);
                        } else {
                            holder.sortonObj.setCheckedList(false);
                        }

                        sortListNew = bmodel.newOutletHelper.getNewRetailers();
                        if (selected_sortlist.getSrotText().equals("A - Z")) {
                            Collections.sort(sortListNew, new Comparator<NewOutletBO>() {
                                @Override
                                public int compare(NewOutletBO fstr, NewOutletBO sstr) {
                                    return fstr.getOutletName().compareToIgnoreCase(sstr.getOutletName());
                                }
                            });
                            recycleradapter = new RecyclerViewAdapter(sortListNew);
                            recyclerView.setAdapter(recycleradapter);
                            recycleradapter.notifyDataSetChanged();
                            dismiss();
                        } else if (selected_sortlist.getSrotText().equals("Z - A")) {
                            Collections.sort(sortListNew, new Comparator<NewOutletBO>() {
                                @Override
                                public int compare(NewOutletBO fstr, NewOutletBO sstr) {
                                    return sstr.getOutletName().compareToIgnoreCase(fstr.getOutletName());
                                }
                            });
                            recycleradapter = new RecyclerViewAdapter(sortListNew);
                            recyclerView.setAdapter(recycleradapter);
                            recycleradapter.notifyDataSetChanged();
                            dismiss();
                        }

                        notifyDataSetChanged();
                    }
                });
                holder.sortlist_radio_btn.setChecked(position == lastCheckedPosition);
            }

            @Override
            public int getItemViewType(int position) {
                return position;
            }

            @Override
            public int getItemCount() {
                return items.size();
            }

            public class ViewHolder extends RecyclerView.ViewHolder {
                AppCompatRadioButton sortlist_radio_btn;
                NewOutletBO sortonObj;

                public ViewHolder(View itemView) {
                    super(itemView);

                    sortlist_radio_btn = (AppCompatRadioButton) itemView.findViewById(R.id.reason_radio_btn);
                    sortlist_radio_btn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                }
            }
        }
    }


/*
    delete the values Aysnc task through Background
     */

    class DeleteAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... params) {

            try {

                bmodel.newOutletHelper.deleteRetailerEdit(selectedRetId);
                return Boolean.TRUE;

            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;

            }
        }

        @Override
        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.deleting));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            //progressDialogue.dismiss();
            if (result == Boolean.TRUE) {

                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.deleted_sucessfully),
                        Toast.LENGTH_SHORT).show();

            }
        }
    }

    @SuppressLint("ValidFragment")
    class ChannelEditSelectionDialogFragment extends DialogFragment {
        private String mTitle = "";
        private String mMenuName = "";

        private TextView mTitleTV;
        private Button mOkBtn, mDismisBtn;
        private ListView mChannelLV;


        public ChannelEditSelectionDialogFragment() {
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");
            mMenuName = getArguments().getString("screentitle");


        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            getDialog().setTitle(mTitle);
            mTitleTV = (TextView) getView().findViewById(R.id.title);
            mTitleTV.setVisibility(View.GONE);
            mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
            mOkBtn.setVisibility(View.GONE);
            mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
            mChannelLV = (ListView) getView().findViewById(R.id.lv_colletion_print);

            ArrayAdapter<ChannelBO> adapter = new ArrayAdapter<ChannelBO>(getActivity(), android.R.layout.simple_list_item_single_choice, mChannelList);
            mChannelLV.setAdapter(adapter);
            mChannelLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    ChannelBO channelBO = mChannelList.get(position);
                    bmodel.newOutletHelper.setmSelectedChannelid(channelBO.getChannelId());
                    bmodel.newOutletHelper.setmSelectedChannelname(channelBO.getChannelName());
                    bmodel.newOutletHelper.loadNewOutletConfiguration(channelBO.getChannelId());
                    bmodel.newOutletHelper.loadRetailerType();
                    bmodel.newOutletHelper.downloadLinkRetailer();
                }
            });
            mOkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            mDismisBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getDialog().dismiss();


                }
            });


        }


    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_new_retailer, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_add).setVisible(true);
        menu.findItem(R.id.menu_sort).setVisible(true);
        menu.findItem(R.id.menu_capture).setVisible(false);
        menu.findItem(R.id.menu_survey).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_add) {

            if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL),
                    "yyyy/MM/dd") != 0)
                    && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.next_day_coverage),
                        Toast.LENGTH_SHORT).show();

            } else if (bmodel.synchronizationHelper.isDayClosed()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.day_closed),
                        Toast.LENGTH_SHORT).show();
            } else if (isLeave_today) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.leaveToday),
                        Toast.LENGTH_SHORT).show();
            } else if (!bmodel.synchronizationHelper.isDataAvailable()) {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else if (bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
//                bmodel.mSelectedActivityName = menuItem.getMenuName();
                mChannelList = bmodel.newOutletHelper.getChannelList();
                if (mChannelList != null && mChannelList.size() > 0) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    ChannelEditSelectionDialogFragment dialogFragment = new ChannelEditSelectionDialogFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("title", bmodel.newOutletHelper.getLevelame());
                    bundle.putString("screentitle", "New OutLet");
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(fm, "Sample Fragment");
                    dialogFragment.setCancelable(false);
                } else {
                    Toast.makeText(getActivity(), "Channel Not Mapped ", Toast.LENGTH_SHORT).show();
                }


            } else {
                if (!bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                    bmodel.newOutletHelper.loadNewOutletConfiguration(0);
                    bmodel.newOutletHelper.downloadLinkRetailer();
                }
                Intent j = new Intent(getActivity(), NewOutlet.class);
                j.putExtra("screenMode", 0);
                fromHomeScreen = true;
                j.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(j);
                getActivity().finish();
            }
        } else if (i == R.id.menu_sort) {
            NewOutletEditSortDialog sortDialog = new NewOutletEditSortDialog(getActivity(), "nonVisit");
            sortDialog.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = sortDialog.getWindow();
            lp.copyFrom(window.getAttributes());
            lp.width = displaymetrics.widthPixels - 100;
            lp.height = (int) (displaymetrics.heightPixels / 2.5);//WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
        }
        
        
        return super.onOptionsItemSelected(item);
    }
}
