package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;


public class NewOutletEditFragment extends IvyBaseFragment implements ChannelSelectionDialog.ChannelSelectionListener
        , NewOutletEditSortDialog.ShortSelectionListener {
    RecyclerView recyclerView;
    private BusinessModel bmodel;
    GridLayoutManager gridLayoutManager;
    Bundle bundle;
    private DisplayMetrics displaymetrics;
    private String selectedRetId;
    private ArrayList<NewOutletBO> retailerEditList;
    private RecyclerViewAdapter recycleradapter;
    private boolean isLeave_today;
    public ArrayList<ChannelBO> mChannelList;
    public static boolean fromHomeScreen = false;
    private TextView textDetails;
    private Button okBtn, cancelBtn;
    private AlertDialog alertDialog;
    private View view;
    private static final String MENU_NEW_RETAILER = "MENU_NEWRET_EDT";
    private ChannelSelectionDialog dialogFragment;
    NewOutletEditSortDialog sortDialog;
    private int lastCheckedPosition = -1;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_new_profile_edit, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        //bmodel.setContext(getActivity());


        recyclerView = (RecyclerView) view.findViewById(R.id.rcv_new_retailers);


        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
        }

        /*if (bmodel.mAttendanceHelper.checkLeaveAttendance(getActivity()))
            isLeave_today = true;*/

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

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);
        retailerEditList = new ArrayList<>();
        retailerEditList = bmodel.newOutletHelper.getNewRetailers();
        if (retailerEditList.size() > 0) {
            recycleradapter = new RecyclerViewAdapter(retailerEditList);
            recyclerView.setAdapter(recycleradapter);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    @Override
    public void loadNewOutLet(int position, String menuName) {
        ChannelBO channelBO = mChannelList.get(position);
        bmodel.newOutletHelper.setmSelectedChannelid(channelBO.getChannelId());
        bmodel.newOutletHelper.setmSelectedChannelname(channelBO.getChannelName());
        bmodel.newOutletHelper.loadNewOutletConfiguration(channelBO.getChannelId());
        bmodel.newOutletHelper.loadRetailerType();
        bmodel.newOutletHelper.downloadLinkRetailer();
        new LoadNewOutLet().execute("0");
        dialogFragment.dismiss();
    }

    @Override
    public void updateShortList(ArrayList<NewOutletBO> retailerEditList, int lastCheckedPos) {
        lastCheckedPosition = lastCheckedPos;
        recycleradapter = new RecyclerViewAdapter(retailerEditList);
        recyclerView.setAdapter(recycleradapter);
        recycleradapter.notifyDataSetChanged();
        sortDialog.dismiss();
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
                    selectedRetId = retailer.getRetailerId();
                    if (selectedRetId.length() > 0)
                        new LoadNewOutLet().execute("1");
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
                    i.putExtra("isNewRetailerEdit", true);
                    startActivity(i);
                    //getActivity().finish();
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
                        okBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                        cancelBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
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
            else {
                holder.viewDivider.setVisibility(View.GONE);
                holder.ib_edit.setVisibility(View.GONE);
            }

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView retailer_name, retailer_address;
            ImageButton ib_edit, ib_delete;
            View viewDivider;
            LinearLayout ll_retailer;

            public ViewHolder(View v) {
                super(v);
                retailer_name = v.findViewById(R.id.tv_retailername);
                retailer_address =  v.findViewById(R.id.tv_retaileraddress);
                ib_edit =  v.findViewById(R.id.iv_edit);
                ib_delete =  v.findViewById(R.id.delete);
                ll_retailer =  v.findViewById(R.id.ll_retailer_name);
                viewDivider =  v.findViewById(R.id.view_divider);

                retailer_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                retailer_address.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

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

            customProgressDialog(builder, getResources().getString(R.string.deleting));
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

                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.deleted_sucessfully),
                        Toast.LENGTH_SHORT).show();

            }
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_new_retailer, menu);
        menu.findItem(R.id.menu_add).setVisible(true);
        menu.findItem(R.id.menu_sort).setVisible(true);
        menu.findItem(R.id.menu_capture).setVisible(false);
        menu.findItem(R.id.menu_survey).setVisible(false);

        if (retailerEditList.size() == 0)
            menu.findItem(R.id.menu_sort).setEnabled(false);


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

            if ((DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                            .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
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
                Toast.makeText(getActivity(), bmodel.synchronizationHelper.dataMissedTable + " " + getResources().getString(R.string.data_not_mapped) + " " +
                                getResources().getString(R.string.please_redownload),
                        Toast.LENGTH_SHORT).show();
            } else if (bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
//                bmodel.mSelectedActivityName = menuItem.getMenuName();
                mChannelList = bmodel.newOutletHelper.getChannelList();
                if (mChannelList != null && mChannelList.size() > 0) {
                    dialogFragment = new ChannelSelectionDialog(getActivity(), mChannelList, bmodel.newOutletHelper.getLevelame());
                    dialogFragment.setChannelSelectionListener(this);
                    dialogFragment.show();
                } else {
                    Toast.makeText(getActivity(), "Channel Not Mapped ", Toast.LENGTH_SHORT).show();
                }


            } else {
                new LoadNewOutLet().execute("0");
            }
        } else if (i == R.id.menu_sort) {
            if (retailerEditList.size() > 0) {
                sortDialog = new NewOutletEditSortDialog(getActivity(),  retailerEditList, lastCheckedPosition);
                sortDialog.setChannelSelectionListener(this);
                sortDialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = sortDialog.getWindow();
                lp.copyFrom(window.getAttributes());
                lp.width = displaymetrics.widthPixels - 100;
                lp.height = (int) (displaymetrics.heightPixels / 2.5);//WindowManager.LayoutParams.WRAP_CONTENT;
                window.setAttributes(lp);
            }
        }


        return super.onOptionsItemSelected(item);
    }

    private AlertDialog.Builder builder;

    class LoadNewOutLet extends AsyncTask<String, Void, Boolean> {
        private String mParam;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(String... params) {
            mParam = params[0];

            try {
                if (!bmodel.configurationMasterHelper.IS_CHANNEL_SELECTION_NEW_RETAILER) {
                    bmodel.newOutletHelper.loadNewOutletConfiguration(0);
                    bmodel.newOutletHelper.downloadLinkRetailer();
                }

                if (bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_ORDER || bmodel.configurationMasterHelper.SHOW_NEW_OUTLET_OPPR) {
                    GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_NEW_RETAILER);
                    if (genericObjectPair != null) {
                        bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                        bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                    }
                    bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_NEW_RETAILER));
                    bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                            bmodel.productHelper.getFilterProductLevels(),true));


                    if (mParam.equals("1"))
                        bmodel.productHelper.updateOutletOrderedProducts(selectedRetId);
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }


        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (alertDialog != null)
                alertDialog.dismiss();

            if (result) {
                Intent i = new Intent(getActivity(), NewOutlet.class);
                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                if (mParam.equals("1")) {
                    i.putExtra("screenMode", 2);
                    i.putExtra("retailerId", selectedRetId);
                } else if (mParam.equals("0")) {
                    i.putExtra("screenMode", 4);
                    fromHomeScreen = true;
                }
                i.putExtra("isNewRetailerEdit", true);
                startActivity(i);
                // getActivity().finish();

            } else {
                bmodel = (BusinessModel) getActivity().getApplicationContext();
                bmodel.showAlert(
                        "Error: "
                                + getResources().getString(
                                R.string.new_store_infn_not_saved), 0);
            }

        }

    }
}
