package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

    private BusinessModel mBModel;
    protected RecyclerView recyclerView;
    protected TextView toolBarTitle;

    private int width, height;
    public static boolean isPhotoDelete = false;

    protected  ArrayList<String> prodList;
    protected HashMap<String, ArrayList<String>> mImageListByProductName;
    protected HashMap<String, ArrayList<String>> mPhotoTypeListByProductName;
    protected HashMap<String, ArrayList<String>> mLocationListByProductName;
    protected GalRecyclerAdapter galRecyclerAdapter;
    protected ArrayList<String> imgPathShare=new ArrayList<>();
    protected ArrayList<String> imgPathDelete=new ArrayList<>();
    protected ArrayList<File> imgFileDelete=new ArrayList<>();
    protected HashMap<String, String> mInStoreLocationNameById = new HashMap<>();


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gall);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        mBModel.loadPhotoCapturedDetailsSelectedRetailer();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if (toolbar != null) {
            toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
            toolBarTitle.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            toolBarTitle.setText(getResources().getString(R.string.my_gallery));
        }

        recyclerView = (RecyclerView) findViewById(R.id.gal_recycler_view);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(false);
            recyclerView.setItemViewCacheSize(200);
            recyclerView.setDrawingCacheEnabled(true);
            recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        }
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        loadGrid();
        // To get the device screen width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }
        else if(i==R.id.menu_gallery_share)
        {
            if(imgPathShare.size()>0)
                shareTheImages(imgPathShare);
            else
                showAlertDialog();
        }
        else if(i==R.id.menu_gallery_delete)
        {
            if(imgPathDelete.size()>0 && imgFileDelete.size()>0)
                showDeleteAlertDialog(imgPathDelete, imgFileDelete);
            else
                showAlertDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    //Update the screen title based on the selection of images
    @SuppressLint("SetTextI18n")
    protected void setSelectionScreenTitle(int count)
    {
        if(count>0) {
            toolBarTitle.setText(count + " selected");
        }
        else {
            toolBarTitle.setText("My Gallery");
        }
        invalidateOptionsMenu();
    }

    protected void shareTheImages(ArrayList<String> imagePathArray) {

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Pictures");
        intent.setType("image/*");
        ArrayList<Uri> files = new ArrayList<>();
        /* List of the files you want to send */
        for(String path : imagePathArray) {
            File file = new File(path);
            Uri uri = Uri.fromFile(file);
            files.add(uri);
        }
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        startActivity(Intent.createChooser(intent, "Share Image"));
        //startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_gallery, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(imgPathShare.size()==0 && imgPathDelete.size()==0 && imgFileDelete.size()==0)
        {
            menu.removeItem(R.id.menu_gallery_share);
            menu.removeItem(R.id.menu_gallery_delete);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void loadGrid() {
        try {
            prodList = new ArrayList<>();
            imgPathShare=new ArrayList<>();
            imgPathDelete=new ArrayList<>();
            imgFileDelete=new ArrayList<>();
            mImageListByProductName = new HashMap<>();
            mPhotoTypeListByProductName = new HashMap<>();
            mLocationListByProductName = new HashMap<>();

            for (StandardListBO temp : mBModel.productHelper
                    .getInStoreLocation()) {
                mInStoreLocationNameById.put(temp.getListID(), temp.getListName());

            }

            for (PhotoTypeMasterBO photoTypeBo : PhotoCaptureHelper.getInstance(getApplicationContext()).getPhotoTypeMaster()) {
                ArrayList<PhotoCaptureProductBO> tempPhotoBo = photoTypeBo
                        .getPhotoCaptureProductList();

                for (PhotoCaptureProductBO phcapture : tempPhotoBo) {
                    for (LocationBO lbo : phcapture.getInStoreLocations()) {
                        if (lbo.getImagepath() != null && !lbo.getImagepath().isEmpty()) {
                            System.out.println(photoTypeBo.getPhotoTypeDesc() + ":" + lbo.getProductName() + " : " + lbo.getLocationId() + " : " + lbo.getLotcode() + " : " + lbo.getImagepath());
                            if (prodList.size() > 0) {
                                if (!prodList.contains(lbo.getProductName())) {
                                    prodList.add(lbo.getProductName());
                                }
                            } else {
                                prodList.add(lbo.getProductName());
                            }

                            if (!mImageListByProductName.containsKey(lbo.getProductName())) {
                                ArrayList<String> imglist = new ArrayList<>();
                                imglist.add(lbo.getImageName());
                                mImageListByProductName.put(lbo.getProductName(), imglist);

                                ArrayList<String> typelist = new ArrayList<>();
                                typelist.add(photoTypeBo.getPhotoTypeDesc());
                                mPhotoTypeListByProductName.put(lbo.getProductName(), typelist);

                                ArrayList<String> loclist = new ArrayList<>();
                                loclist.add(mInStoreLocationNameById.get(String.valueOf(lbo.getLocationId())));
                                mLocationListByProductName.put(lbo.getProductName(), loclist);

                            } else {
                                mImageListByProductName.get(lbo.getProductName()).add(lbo.getImageName());
                                mPhotoTypeListByProductName.get(lbo.getProductName()).add(photoTypeBo.getPhotoTypeDesc());
                                mLocationListByProductName.get(lbo.getProductName()).add(mInStoreLocationNameById.get(String.valueOf(lbo.getLocationId())));

                            }
                        }
                    }
                }
            }
            galRecyclerAdapter = new GalRecyclerAdapter();
            recyclerView.setAdapter(galRecyclerAdapter);
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
        ImageView imageview, prod_img;
        RelativeLayout RLCheckBg;
        CheckBox CBSelect;
        Button check;
        int id;
        TextView  type_loc_txt, loc_txt;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onLongClick(View v) {

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
     * @param view Root view
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
                e.printStackTrace();
            }
        }
    }

    private class GalRecyclerAdapter extends RecyclerView.Adapter<GalRecyclerAdapter.MyViewHolder> {

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView ProdName;
            private MyGridView PhoneCaptureGrid;

            private MyViewHolder(View view) {
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

            holder.ProdName.setText(prodList.get(position));
            holder.PhoneCaptureGrid.setAdapter(new ProdGridAdapter(mImageListByProductName.get(prodList.get(position)), mPhotoTypeListByProductName.get(prodList.get(position)), mLocationListByProductName.get(prodList.get(position))));
            holder.ProdName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }

        @Override
        public int getItemCount() {
            return prodList.size();
        }


    }

    protected void showAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this)
                .setTitle("Please Select the Images and Try again!!!")
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(prodList.size()>0)
                        {
                            galRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setCancelable(false);
        mBModel.applyAlertDialogTheme(builder);
    }

    /* showDeleteAlertDialog( path of the image, image file) */
    private void showDeleteAlertDialog(final ArrayList<String> imagePathArray, final ArrayList<File> imageFileArray)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(Gallery.this)
                .setTitle(
                        getResources().getString(
                                R.string.do_you_want_to_delete_the_image))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                for(int i=0;i<imagePathArray.size();i++) {
                                    for (PhotoTypeMasterBO tempTypeBO : PhotoCaptureHelper.getInstance(getApplicationContext()).getPhotoTypeMaster()) {
                                        ArrayList<PhotoCaptureProductBO> tempCaptureBO = tempTypeBO
                                                .getPhotoCaptureProductList();
                                        for (PhotoCaptureProductBO photo : tempCaptureBO) {
                                            for (LocationBO lbo : photo.getInStoreLocations()) {
                                                if (lbo.getImageName() != null && !lbo.getImageName().isEmpty()) {

                                                    if (lbo.getImageName().equalsIgnoreCase(imagePathArray.get(i))) {
                                                        lbo.setImageName("");
                                                        lbo.setImagepath("");
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    if (imageFileArray.get(i).delete()) {
                                        mBModel.deleteImageDetailsFormTable(imagePathArray.get(i));
                                        mBModel.photocount--;
                                    }
                                }
                                loadGrid();
                                isPhotoDelete = true;
                            }
                        })
                .setNegativeButton(
                        getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
        mBModel.applyAlertDialogTheme(builder);
    }

    private class ProdGridAdapter extends BaseAdapter {
        private LayoutInflater mInflater;
        ArrayList<String> imgArrList;
        ArrayList<String> typeArrList;
        ArrayList<String> locArrList;
        BitmapFactory.Options options = new BitmapFactory.Options();

        ProdGridAdapter(ArrayList<String> imgArrList, ArrayList<String> typeArrList, ArrayList<String> locArrList) {
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

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.prod_grid_item_new, parent, false);
                holder.prod_img = (ImageView) convertView
                        .findViewById(R.id.prod_img);

                holder.type_loc_txt = (TextView) convertView
                        .findViewById(R.id.type_loc_txt);
                holder.loc_txt = (TextView) convertView
                        .findViewById(R.id.loc_txt);
                holder.RLCheckBg=(RelativeLayout)convertView.findViewById(R.id.layout_share_select);
                holder.CBSelect=(CheckBox)convertView.findViewById(R.id.check_share_select);

                holder.type_loc_txt.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.loc_txt.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

                holder.RLCheckBg.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(holder.CBSelect.isChecked())
                            holder.CBSelect.setChecked(false);
                        else
                            holder.CBSelect.setChecked(true);
                    }
                });

                holder.CBSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String imagePath=getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                                + DataMembers.photoFolderName + "/" +imgArrList.get(position);
                        if(isChecked)
                        {
                            holder.RLCheckBg.setBackgroundColor(ResourcesCompat.getColor(getResources(),R.color.shelfs_bg_semi_transperant,null));
                            imgPathShare.add(imagePath);
                            imgPathDelete.add(imgArrList.get(position));
                            imgFileDelete.add(new File(imagePath));
                            setSelectionScreenTitle(imgPathShare.size());
                        }
                        else
                        {
                            holder.RLCheckBg.setBackgroundResource(0);
                            if(imgPathShare.contains(imagePath))
                            {
                                int index=imgPathShare.indexOf(imagePath);
                                imgPathShare.remove(index);
                                imgPathDelete.remove(index);
                                imgFileDelete.remove(index);
                                setSelectionScreenTitle(imgPathShare.size());
                            }
                        }
                    }
                });
                setSelectionScreenTitle(imgPathShare.size());
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.type_loc_txt.setText(typeArrList.get(position));
            holder.loc_txt.setText(locArrList.get(position));

            Glide.with(Gallery.this).load(
                    Gallery.this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                            + DataMembers.photoFolderName + "/" + imgArrList.get(position))
                    .centerCrop()
                    .error(R.drawable.no_image_available)
                    .override(width/2, height/4)
                    .into(holder.prod_img);

            return convertView;
        }
    }

}