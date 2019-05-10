package com.ivy.cpg.view.roadactivity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdhocGallery extends IvyBaseActivityNoActionBar implements OnLongClickListener {

    private BusinessModel bmodel;

    private File deleteFilePath;
    private String deleteImageName = "";

    private List<File> tFileList;
    private List<Bitmap> tBitMapList;
    private List<String> tImageNameList;
    private HashMap<String, PhotoCaptureProductBO> adhocGalleryDetails;
    private RoadActivityHelper roadActivityHelper;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);

        roadActivityHelper = RoadActivityHelper.getInstance(getApplicationContext());
        adhocGalleryDetails = roadActivityHelper.loadAdhocPhotoCapturedDetails();

        if (adhocGalleryDetails.size() > 0) {
            loadGrid();
        }

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.gallery));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


    }

    private void loadGrid() {
        try {
            tFileList = new ArrayList<File>();
            tBitMapList = new ArrayList<Bitmap>();
            tImageNameList = new ArrayList<String>();

            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                            + DataMembers.photoFolderName + "/");
            File[] files = f.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    File file = files[i];

                    if (file.getName().startsWith("RA") && adhocGalleryDetails.containsKey(file.getName())) {

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inSampleSize = 4;

                        tFileList.add(file);
                        tImageNameList.add(file.getName());
                        tBitMapList.add(BitmapFactory.decodeFile(
                                files[i].getAbsolutePath(), options));
                    }

                }
            }

            GridView g = findViewById(R.id.PhoneImageGrid);
            g.setAdapter(new ImageAdapter());
        } catch (Exception e) {
            Commons.printException(e);
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return tBitMapList.size();
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
                convertView = mInflater.inflate(R.layout.adhocgalleryitem, null);
                holder.imageview = convertView
                        .findViewById(R.id.thumbImage);
                holder.check = findViewById(R.id.itemCheckBox);
                holder.retailerName = convertView
                        .findViewById(R.id.txt_retailerName);
                holder.imageCount = convertView
                        .findViewById(R.id.txt_imageCount);

                holder.imageview
                        .setOnLongClickListener(new OnLongClickListener() {

                            public boolean onLongClick(View v) {

                                deleteFilePath = holder.filePath;
                                deleteImageName = holder.ImageName;

                                if (!roadActivityHelper.getAdhocTransCount(deleteImageName)) {
                                    showDialog(0);
                                } else {
                                    Toast.makeText(getApplicationContext(), "There Should Be one Image Per Combination", Toast.LENGTH_LONG)
                                            .show();
                                }

                                return true;
                            }
                        });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.id = position;
            holder.imageview.setImageBitmap(tBitMapList.get(position));
            holder.filePath = tFileList.get(position);
            holder.ImageName = tImageNameList.get(position);
            holder.photoBO = adhocGalleryDetails.get(tImageNameList
                    .get(position));
            if (holder.photoBO != null) {

                holder.retailerName.setText(holder.photoBO.getRetailerName());
            }
            return convertView;
        }
    }

    class ViewHolder {
        PhotoCaptureProductBO photoBO;
        ImageView imageview;
        TextView retailerName, imageCount;
        Button check;
        File filePath;
        String ImageName;
        int id;
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(AdhocGallery.this)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_to_delete_the_image))
                        .setCancelable(false)
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        deleteFilePath.delete();
                                        bmodel.deleteAdhocImageDetailsFormTable(deleteImageName);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}