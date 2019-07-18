package com.ivy.cpg.view.asset;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PosmGallery extends IvyBaseActivityNoActionBar {

    BusinessModel bmodel;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.grid)
    GridView imgGrid;

    private Unbinder unbinder;

    private String listId, photoNamePath, imageName, serialNo;
    private int assetId, productID;

    private ArrayList<AssetTrackingBO> mAssetTrackingList;
    private AssetTrackingHelper assetTrackingHelper;
    private static final int CAMERA_REQUEST_CODE = 1;

    private int IMAGE_MAX_SIZE = 500;
    private File deleteFilePath;
    private String deleteImageName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posm_gallery);
        unbinder = ButterKnife.bind(this);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        listId = getIntent().getStringExtra("listId");
        assetId = getIntent().getIntExtra("assetId", 0);
        serialNo = getIntent().getStringExtra("serialNo");
        productID = getIntent().getIntExtra("productID", 0);

        Commons.print("listId, " + "" + listId);
        Commons.print("assetId, " + "" + assetId);

        assetTrackingHelper = AssetTrackingHelper.getInstance(this);

        photoNamePath = FileUtils.photoFolderPath + "/";


        loadData();

    }

    private void loadData() {
        mAssetTrackingList = new ArrayList<>();
        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
            if (standardListBO.getListID().equals(listId)) {
                mAssetTrackingList = standardListBO.getAssetTrackingList();
                break;
            }
        }

        for (AssetTrackingBO assetTrackingBO : mAssetTrackingList) {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetTrackingBO.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;

            if (assetTrackingBO.getAssetID() == assetId && assetTrackingBO.getSerialNo().equals(serialNo)
                    && assetTrackingBO.getProductId() == productID && assetTrackingBO.getImageList().size() > 0) {
                setScreenTitle(assetTrackingBO.getAssetName() + " " + getResources().getString(R.string.tab_text_images));
                imgGrid.setAdapter(new MyAdapter(assetTrackingBO.getImageList()));
                break;
            }
        }
    }

    @OnClick(R.id.fab_photo)
    public void btnPhotoClick(View view) {
        int count = 0;
        for (AssetTrackingBO assetTrackingBO : mAssetTrackingList) {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetTrackingBO.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            if (assetTrackingBO.getAssetID() == assetId && assetTrackingBO.getSerialNo().equals(serialNo)
                    && assetTrackingBO.getProductId() == productID) {
                count = assetTrackingBO.getImageList().size();
                break;
            }
        }

        if (assetTrackingHelper.POSM_PHOTO_COUNT == count)
            Toast.makeText(this, getResources().getString(R.string.you_have_already_taken_maximun_images), Toast.LENGTH_SHORT)
                    .show();
        else
            captureCustom();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    class MyAdapter extends ArrayAdapter<String> {

        private ArrayList<String> items;


        public MyAdapter(ArrayList<String> items) {
            super(PosmGallery.this, R.layout.row_posm_gallery);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public String getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            final ProofViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(PosmGallery.this);
                convertView = inflater.inflate(
                        R.layout.row_posm_gallery, null);

                holder = new ProofViewHolder(convertView);

                try {
                    String[] imjObj = items.get(position).split("/");
                    holder.imageName = imjObj[3];

                } catch (Exception e) {
                    Commons.printException(e);
                    holder.imageName = "";
                }

                File imgFile = new File(photoNamePath + "/" + holder.imageName);

                if (imgFile.exists()) {
                    holder.llDelete.setVisibility(View.VISIBLE);
                    try {

                        holder.imgPath = imgFile.getAbsolutePath();
                        holder.imgProof.setImageBitmap(decodeFile(imgFile));

                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else {
                    holder.imgProof.setImageResource(R.drawable.no_image_available);
                    holder.llDelete.setVisibility(View.GONE);
                }

                holder.llDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        deleteFilePath = new File(holder.imgPath);
                        deleteImageName = holder.imageName;

                        showAlert(getResources().getString(
                                R.string.do_you_want_to_delete_the_image));
                    }
                });

            }


            return convertView;
        }
    }


    static class ProofViewHolder {
        @BindView(R.id.iv_proof)
        ImageView imgProof;
        @BindView(R.id.lldelete)
        LinearLayout llDelete;

        String imageName, imgPath;

        ProofViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }

    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.ceil(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return b;
    }

    public void showAlert(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        deleteFilePath.delete();
                        String path = "Asset/"
                                + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                                .replace("/", "") + "/"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + deleteImageName;
                        assetTrackingHelper.deleteImageProof(PosmGallery.this, path);
                        for (AssetTrackingBO assetTrackingBO : mAssetTrackingList) {
                            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetTrackingBO.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (assetTrackingBO.getAssetID() == assetId && assetTrackingBO.getSerialNo().equals(serialNo)
                                    && assetTrackingBO.getProductId() == productID) {
                                for (int i = 0; i < assetTrackingBO.getImageList().size(); i++) {
                                    if (assetTrackingBO.getImageList().get(i).equals(path)) {
                                        assetTrackingBO.getImageList().remove(i);
                                        break;
                                    }
                                }
                           /* if (assetTrackingBO.getImageList().size() > 0) {
                                imgGrid.setAdapter(new MyAdapter(assetTrackingBO.getImageList()));
                                break;
                            } else
                                finish();*/
                                finish();
                            }
                        }

                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    private void captureCustom() {
        try {
            String photoPath = getExternalFilesDir(
                    Environment.DIRECTORY_PICTURES)
                    + "/" + DataMembers.photoFolderName + "/";

            imageName = "AT_"
                    + bmodel.getRetailerMasterBO()
                    .getRetailerID() + "_" + listId + "_"
                    + assetId + "_"
                    + Commons.now(Commons.DATE_TIME)
                    + "_img.jpg";
            Intent intent = new Intent(this,
                    CameraActivity.class);
            intent.putExtra(CameraActivity.QUALITY, 40);
            String path = photoPath + "/" + imageName;
            intent.putExtra(CameraActivity.PATH, path);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);

        } catch (Exception e) {
            Commons.printException(e.toString());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print("PosmGallery" + "," +
                        "Camera Activity : Successfully Captured.");

                onSaveImageName();

            } else {
                Commons.print("PosmGallery" + "," + "Camera Activity : Canceled");
            }
        }

    }

    private void onSaveImageName() {
        String imagePath = "Asset/"
                + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;

        for (AssetTrackingBO assetBO : mAssetTrackingList) {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            if (assetId == assetBO.getAssetID() && serialNo.equals(assetBO.getSerialNo())
                    && productID == assetBO.getProductId()) {
                ArrayList<String> imageList = assetBO.getImageList();
                imageList.add(imagePath);
                assetBO.setImageList(imageList);
                imgGrid.setAdapter(new MyAdapter(assetBO.getImageList()));
                break;
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
