package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoyaltyBO;
import com.ivy.sd.png.bo.LoyaltyBenifitsBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;


public class LoyaltyPointsFragment extends IvyBaseFragment implements View.OnClickListener {

    private BusinessModel bmodel;
    private ArrayAdapter<LoyaltyBO> mloylatAdapter;
    private ArrayList<LoyaltyBenifitsBO> mylist;
    public LoyaltyBO ret;
    public static Vector<LoyaltyBO> loylatyitems;
    public GridLayoutManager gridlaymanager;
    private RecyclerViewAdapter mSchedule;
    public int mSelectedLoyaltyIndex;

    private InputMethodManager inputManager;
    private RecyclerView recyclerView;
    TextView selectedPointsTxt, givenPointsTxt;
    private int mSelectedLoyaltyID = 0;
    private int totlaSelectedPoints = 0;
    private int givenPoints = 0;
    private int bPoints = 0;
    private LinearLayout linearLayout;
    public int screenwidth = 0, screenheight = 0;
    private Button saveBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_loyalty_credit_points, container, false);

        linearLayout = (LinearLayout) rootView.findViewById(R.id.ll_snackbar);
        saveBtn = (Button) rootView.findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(this);

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        if (!bmodel.configurationMasterHelper.IS_LOYALTY_AUTO_PAYOUT) {
            ((RelativeLayout) getView().findViewById(R.id.footer)).setVisibility(View.VISIBLE);
        } else {
            ((RelativeLayout) getView().findViewById(R.id.footer)).setVisibility(View.GONE);
        }

        if (Build.VERSION.SDK_INT >= 13) {
            Point size = new Point();
            getActivity().getWindowManager().getDefaultDisplay().getSize(size);
            screenwidth = size.x;
            screenheight = size.y;
        } else {
            screenwidth = getActivity().getWindowManager().getDefaultDisplay().getWidth();
            screenheight = getActivity().getWindowManager().getDefaultDisplay().getHeight();
        }
        recyclerView = (RecyclerView) getView().findViewById(R.id.loyalty_recyclerview);
        recyclerView.setHasFixedSize(true);
        givenPointsTxt = (TextView) getView().findViewById(R.id.tos_amount);
        selectedPointsTxt = (TextView) getView().findViewById(R.id.totalProducts_redeem_points);

        givenPointsTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        selectedPointsTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) getView().findViewById(R.id.tv_selectedpts)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) getView().findViewById(R.id.tv_givenpts)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);

        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if (screenwidth > 600) {
            gridlaymanager = new GridLayoutManager(getActivity(), 3);
        } else {
            gridlaymanager = new GridLayoutManager(getActivity(), 2);
        }


        recyclerView.setLayoutManager(gridlaymanager);

        mloylatAdapter = new ArrayAdapter<LoyaltyBO>(getActivity(),
                android.R.layout.select_dialog_singlechoice);
        loylatyitems = bmodel.productHelper.getProductloyalties();

        for (LoyaltyBO temp : loylatyitems) {
            if (temp.getSelectedPoints() > 0) {
                temp.setGivenPoints(temp.getGivenPoints());
            }
            mloylatAdapter.add(temp);
        }
        updateloadLoyaltyBeniftsProducts(bmodel.productHelper.getProductloyalties().get(0).getLoyaltyId());
        updateTotalPoints(bmodel.productHelper.getProductloyalties().get(0).getLoyaltyId());
    }


    private void snackBarShow(String snacktext) {

        if (linearLayout != null) {
            linearLayout.removeAllViews();
        }
        Snackbar snackbar = Snackbar
                .make(linearLayout,
                        snacktext, Snackbar.LENGTH_LONG);
        TypedValue typedValue = new TypedValue();
        TypedArray a = getActivity().obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimary});
        a.recycle();
        View view = snackbar.getView();
        linearLayout.addView(view);

        view.setMinimumWidth(1500);
        view.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.colorRedripple_focused));//cardview_dark_background));
        TextView snackbarTV = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
        snackbarTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.common_signin_btn_dark_text_default));
        snackbar.show();


    }


    public void showLoyalties() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(mloylatAdapter, mSelectedLoyaltyIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLoyaltyIndex = item;
                        bmodel.productHelper.setmSelectedLoyaltyIndex(item);

                        updateloadLoyaltyBeniftsProducts(
                                bmodel.productHelper.getProductloyalties().get(item).getLoyaltyId());

                        updateTotalPoints(bmodel.productHelper.getProductloyalties().get(item).getLoyaltyId());

                        dialog.dismiss();


                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        if (vw == saveBtn) {
            onNextButtonClick();
        }

    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<LoyaltyBenifitsBO> items;

        public RecyclerViewAdapter(ArrayList<LoyaltyBenifitsBO> items) {

            this.items = items;
        }


        public LoyaltyBenifitsBO getItem(int position) {
            return items.get(position);
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int position) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_loyalty_view, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final LoyaltyBenifitsBO projectObj = items.get(position);
            if (screenheight <= 1000) {                                                 //for mobile device screen size
                holder.rwoLayout.getLayoutParams().height = 340;
                holder.btnLayout.getLayoutParams().height = 65;
            }

            holder.productDescriptionTxt.setText(projectObj.getBenifitDescription());
            holder.productCreditPointsTxt.setText("Points: " + projectObj.getBenifitPoints() + "");

            try {
                String[] splitImageName = projectObj.getImagePath().split("/");
                String imageName = "";
                if (splitImageName.length > 0)
                    imageName = splitImageName[splitImageName.length - 1];

                File file = new File(
                        getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + DataMembers.DIGITAL_CONTENT + "/"
                                + DataMembers.LOYALTY_POINTS + "/" + imageName);


                final Uri uri = Uri.fromFile(file);

                Glide.with(getActivity())
                        .load(uri)
                        .error(R.drawable.no_photo)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .fitCenter()
                        .listener(new RequestListener<Uri, GlideDrawable>() {
                            @Override
                            public boolean onException(Exception e, Uri model, Target<GlideDrawable> target, boolean isFirstResource) {

                                Commons.print("Glide failed loading image " + uri);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GlideDrawable resource, Uri model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(holder.productImage);
            }catch(Exception e){
                e.printStackTrace();
            }


            holder.qtyIncreaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (loylatyitems.get(mSelectedLoyaltyIndex).getSelectedPoints() < loylatyitems.get(mSelectedLoyaltyIndex).getGivenPoints()) {
                        if ((items.get(position).getBenifitPoints() + loylatyitems.get(mSelectedLoyaltyIndex).getSelectedPoints()) > loylatyitems.get(mSelectedLoyaltyIndex).getGivenPoints())
                            snackBarShow("Points Should Not be exceed");
                        else {
                            items.get(position).setBenifitQty((Integer.parseInt(holder.productQtyTxt.getText().toString()) + 1));
                            bPoints = items.get(position).getBenifitPoints();
                            totlaSelectedPoints += bPoints;
                            updatePoints(totlaSelectedPoints);
                            holder.productQtyTxt.setText(projectObj.getBenifitQty() + "");
                            //notifyItemChanged(position);

                        }
                    } else {
                        snackBarShow("You Reached Maximum Redeem Points");

                    }
                }
            });
            holder.qtyDecreaseBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (Integer.parseInt(holder.productQtyTxt.getText().toString()) != 0) {
                        items.get(position).setBenifitQty((Integer.parseInt(holder.productQtyTxt.getText().toString()) - 1));
                        bPoints = items.get(position).getBenifitPoints();
                        totlaSelectedPoints -= bPoints;
                        updatePoints(totlaSelectedPoints);
                        holder.productQtyTxt.setText(projectObj.getBenifitQty() + "");
                    } else {
                        snackBarShow("Not Applicable");
                    }


                }
            });
            holder.productQtyTxt.setText(projectObj.getBenifitQty() + "");
            holder.itemView.setTag(projectObj);


        }


        @Override
        public int getItemCount() {

            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView productImage;
            private TextView productDescriptionTxt, productCreditPointsTxt, productQtyTxt, qtyText;
            private Button qtyIncreaseBtn, qtyDecreaseBtn;
            private LinearLayout rwoLayout, btnLayout;
            private LoyaltyBenifitsBO benifitsBOObj;
            View row;


            public ViewHolder(View itemView) {
                super(itemView);
                rwoLayout = (LinearLayout) itemView.findViewById(R.id.row_linearlayout);
                btnLayout = (LinearLayout) itemView.findViewById(R.id.qty_layout);
                productImage = (ImageView) itemView.findViewById(R.id.product_images);
                productDescriptionTxt = (TextView) itemView.findViewById(R.id.loyalty_description);
                productQtyTxt = (TextView) itemView.findViewById(R.id.qty_data);
                productCreditPointsTxt = (TextView) itemView.findViewById(R.id.redeem_points);
                qtyIncreaseBtn = (Button) itemView.findViewById(R.id.qty_increase);
                qtyDecreaseBtn = (Button) itemView.findViewById(R.id.qty_decrease);

                productDescriptionTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                productDescriptionTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                if (!bmodel.configurationMasterHelper.IS_LOYALTY_AUTO_PAYOUT) {
                    qtyIncreaseBtn.setVisibility(View.VISIBLE);
                    qtyDecreaseBtn.setVisibility(View.VISIBLE);
                } else {
                    qtyDecreaseBtn.setVisibility(View.INVISIBLE);
                    qtyIncreaseBtn.setVisibility(View.INVISIBLE);
                }


            }
        }
    }

    private void updatePoints(int totlaRedeemPoints) {
        try {
            for (LoyaltyBO tpoints : loylatyitems) {
                if (mSelectedLoyaltyID == tpoints.getLoyaltyId()) {
                    tpoints.setSelectedPoints(totlaRedeemPoints);
                    tpoints.setBalancePoints(tpoints.getGivenPoints() - tpoints.getSelectedPoints());
                    totlaRedeemPoints = tpoints.getSelectedPoints();

                }
                selectedPointsTxt.setText("" + totlaRedeemPoints);

            }

        } catch (Exception e) {
            Commons.printException(e);
        }


    }

    public void updateTotalPoints(int mltyId) {
        mSelectedLoyaltyID = mltyId;
        try {
            if (loylatyitems == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            for (LoyaltyBO tpoints : loylatyitems) {

                if (mltyId == tpoints.getLoyaltyId()) {
                    totlaSelectedPoints = tpoints.getSelectedPoints();
                    givenPoints = tpoints.getGivenPoints();
                }
                givenPointsTxt.setText("" + givenPoints);
                selectedPointsTxt.setText("" + totlaSelectedPoints);


            }


        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void updateloadLoyaltyBeniftsProducts(int mltyId) {
        mSelectedLoyaltyID = mltyId;
        try {

            if (loylatyitems == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            mylist = new ArrayList<>();
            for (LoyaltyBO ret : loylatyitems) {
                if (mltyId == ret.getLoyaltyId()) {
                    mylist = ret.getLoyaltyTrackingList();
                }
            }

            mSchedule = new RecyclerViewAdapter(mylist);
            recyclerView.setAdapter(mSchedule);


        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void onNextButtonClick() {

        if (loylatyitems.get(mSelectedLoyaltyIndex).getSelectedPoints() > 0) {
            new SaveAsyncTask().execute();
        } else {
            mDialog1();
        }
    }

    public void mDialog1() {
        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                getActivity());
        alertDialogBuilder1
                .setIcon(null)
                .setCancelable(false)
                .setTitle(getResources().getString(R.string.no_items_added))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                            }
                        });

        AlertDialog alertDialog1 = alertDialogBuilder1.create();

        alertDialog1.show();
    }


    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_loyalty, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (!bmodel.configurationMasterHelper.IS_LOYALTY_AUTO_PAYOUT) {
            menu.findItem(R.id.menu_next).setVisible(true);
        } else {
            menu.findItem(R.id.menu_next).setVisible(false);
        }

        menu.findItem(R.id.menu_next).setVisible(false);

        menu.findItem(R.id.menu_loyalty_list).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(getActivity(), HomeScreenTwo.class));
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_loyalty_list) {
            showLoyalties();
            return true;
        } else if (i == R.id.menu_next) {
            onNextButtonClick();
        }

        return super.onOptionsItemSelected(item);
    }

    /*
    save the values Aysnc task through Background
     */

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... params) {

            try {

                bmodel.mLoyalityHelper.saveLoyaltyPoints(loylatyitems);
                bmodel.saveModuleCompletion("MENU_LOYALTY_POINTS");
                return Boolean.TRUE;

            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;

            }
        }

        @Override
        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.saving));
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
                        getResources().getString(R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();
                // If it Screen from Home screen Three, it redirect to that
                // activity, otherwise if it from Home screen two, it go back to
                // that activity after successfully save
                startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
//
            }
        }
    }
}
