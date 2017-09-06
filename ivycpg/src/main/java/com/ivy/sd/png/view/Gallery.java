package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.PhotoCaptureProductBO;
import com.ivy.sd.png.bo.PhotoTypeMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MyGridView;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.PhotoCaptureHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class Gallery extends IvyBaseActivityNoActionBar implements OnLongClickListener {

    private BusinessModel bmodel;

    private File deleteFilePath;
    private String deleteImageName = "";


    private boolean isFromHome = false, isfromstorecheck;
    private boolean bool = false;

    RecyclerView recyclerView;
    private Toolbar toolbar;
    TextView toolBarTitle;
    HashMap<String, String> stdlistMap = new HashMap<>();
    private String fromScreen;
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gall);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (extras.containsKey("IsFromHome")) {
                isFromHome = extras.getBoolean("IsFromHome");

                bmodel.loadPhotoCapturedDetails();
            }
            if (extras.containsKey("IsFromStoreCheck")) {
                isfromstorecheck = extras.getBoolean("IsFromStoreCheck");
                bmodel.loadPhotoCapturedDetails();
            }
        }

        fromScreen = getIntent().getStringExtra("from") != null ? getIntent().getStringExtra("from") : "";
        bmodel.loadPhotoCapturedDetailsSelectedRetailer();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        toolBarTitle.setText("My Gallery");

        recyclerView = (RecyclerView) findViewById(R.id.gal_recycler_view);


        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        loadGrid();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    ArrayList<String> prodlist;

    HashMap<String, ArrayList<String>> imghashMap;
    HashMap<String, ArrayList<String>> typehashMap;
    HashMap<String, ArrayList<String>> lochashMap;

    private void loadGrid() {
        try {
            prodlist = new ArrayList<>();
            imghashMap = new HashMap<>();
            typehashMap = new HashMap<>();
            lochashMap = new HashMap<>();


            for (StandardListBO temp : bmodel.productHelper
                    .getInStoreLocation()) {
                stdlistMap.put(temp.getListID(), temp.getListName());

            }

            for (PhotoTypeMasterBO photoTypeBo : PhotoCaptureHelper.getInstance(getApplicationContext()).getPhotoTypeMaster()) {
                ArrayList<PhotoCaptureProductBO> tempPhotoBo = photoTypeBo
                        .getPhotoCaptureProductList();

                for (PhotoCaptureProductBO phcapture : tempPhotoBo) {

                    for (LocationBO lbo : phcapture.getInStoreLocations()) {


                        if (lbo.getImagepath() != null && !lbo.getImagepath().toString().isEmpty()) {


                            System.out.println(photoTypeBo.getPhotoTypeDesc() + ":" + lbo.getProductName() + " : " + lbo.getLocationId() + " : " + lbo.getLotcode() + " : " + lbo.getImagepath());


                            if (prodlist.size() > 0) {
                                if (prodlist.contains(lbo.getProductName())) {
                                    //no action
                                } else {

                                    prodlist.add(lbo.getProductName());
                                }
                            } else {

                                prodlist.add(lbo.getProductName());
                            }


                            if (!imghashMap.containsKey(lbo.getProductName())) {
                                ArrayList<String> imglist = new ArrayList<String>();
                                imglist.add(lbo.getImageName());
                                imghashMap.put(lbo.getProductName(), imglist);

                                ArrayList<String> typelist = new ArrayList<String>();
                                typelist.add(photoTypeBo.getPhotoTypeDesc());
                                typehashMap.put(lbo.getProductName(), typelist);

                                ArrayList<String> loclist = new ArrayList<String>();

                                loclist.add(stdlistMap.get(String.valueOf(lbo.getLocationId())));
                                lochashMap.put(lbo.getProductName(), loclist);


                            } else {
                                imghashMap.get(lbo.getProductName()).add(lbo.getImageName());
                                typehashMap.get(lbo.getProductName()).add(photoTypeBo.getPhotoTypeDesc());
                                lochashMap.get(lbo.getProductName()).add(stdlistMap.get(String.valueOf(lbo.getLocationId())));

                            }


                        }
                    }
                }
            }

            recyclerView.setAdapter(new GalRecyclerAdapter());
        } catch (Exception e) {
            Commons.printException(e);
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

    class ViewHolder {
        PhotoCaptureProductBO photoBO;
        ImageView imageview, prod_img;
        TextView retailerName, imageCount;
        Button check;
        File filePath;
        String ImageName;
        int id;
        LinearLayout ly;
        TextView prod_photo_txt, type_loc_txt, loc_txt;
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_to_delete_the_image))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        for (PhotoTypeMasterBO tempTypeBO : PhotoCaptureHelper.getInstance(getApplicationContext()).getPhotoTypeMaster()) {
                                            ArrayList<PhotoCaptureProductBO> tempCaptureBO = tempTypeBO
                                                    .getPhotoCaptureProductList();
                                            for (PhotoCaptureProductBO photo : tempCaptureBO) {
                                                for (LocationBO lbo : photo.getInStoreLocations()) {
                                                    if (lbo.getImageName() != null && !lbo.getImageName().isEmpty()) {

                                                        if (lbo.getImageName().equalsIgnoreCase(deleteImageName)) {
                                                            lbo.setImageName("");
                                                            lbo.setImagepath("");
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        deleteFilePath.delete();
                                        bmodel.deleteImageDetailsFormTable(deleteImageName);
                                        bmodel.photocount--;
                                        loadGrid();

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
        }
        return null;
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        // super.onBackPressed();

        // finish();
        // bmodel.loadActivity(gall.this, DataMembers.actPhotocapture);

    }

    @Override
    public boolean onLongClick(View v) {
        // TODO Auto-generated method stub
        return false;
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
                // TODO: handle exception
            }
        }
    }

    public class GalRecyclerAdapter extends RecyclerView.Adapter<GalRecyclerAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView ProdName;
            MyGridView PhoneCaptureGrid;

            public MyViewHolder(View view) {
                super(view);
                ProdName = (TextView) view.findViewById(R.id.ProdName);
                PhoneCaptureGrid = (MyGridView) view.findViewById(R.id.PhoneCaptureGrid);

            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.gal_rec_item_lay, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            holder.ProdName.setText(prodlist.get(position));
            holder.PhoneCaptureGrid.setAdapter(new ProdGridAdapter(imghashMap.get(prodlist.get(position)), typehashMap.get(prodlist.get(position)), lochashMap.get(prodlist.get(position))));
            holder.ProdName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        }

        @Override
        public int getItemCount() {
            return prodlist.size();
        }


    }


    public class ProdGridAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        ArrayList<String> imgArrList;
        ArrayList<String> typeArrList;
        ArrayList<String> locArrList;
        BitmapFactory.Options options = new BitmapFactory.Options();

        public ProdGridAdapter(ArrayList<String> imgArrList, ArrayList<String> typeArrList, ArrayList<String> locArrList) {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.imgArrList = imgArrList;
            this.typeArrList = typeArrList;
            this.locArrList = locArrList;
            options.inSampleSize = 4;
        }

        public int getCount() {
            return imgArrList.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();

                convertView = mInflater.inflate(R.layout.prod_grid_item, null);
                holder.prod_img = (ImageView) convertView
                        .findViewById(R.id.prod_img);

                holder.type_loc_txt = (TextView) convertView
                        .findViewById(R.id.type_loc_txt);
                holder.loc_txt = (TextView) convertView
                        .findViewById(R.id.loc_txt);
                if ("photo_cap".equals(fromScreen))
                    holder.loc_txt.setVisibility(View.GONE);

                holder.type_loc_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.loc_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));


                holder.prod_img
                        .setOnLongClickListener(new OnLongClickListener() {

                            public boolean onLongClick(View v) {
                                // TODO Auto-generated method stub


                                System.out.println("v tag==" + v.getTag());
                                deleteFilePath = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                                        + DataMembers.photoFolderName + "/" + v.getTag().toString());
                                deleteImageName = v.getTag().toString();
                                showDialog(0);

                                return true;
                            }
                        });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.type_loc_txt.setText(typeArrList.get(position));
            holder.loc_txt.setText(locArrList.get(position));


            holder.prod_img.setImageBitmap(BitmapFactory.decodeFile(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                            + DataMembers.photoFolderName + "/" + imgArrList.get(position), options));
            holder.prod_img.setTag(imgArrList.get(position));

            return convertView;
        }
    }

}