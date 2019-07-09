package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FileUtils;

import java.io.File;

public class ReasonPhotoDialog extends DialogFragment {

    private BusinessModel bmodel;
    Button btnCancel, btnSave;
    Spinner ms_reason;
    ImageView ivReason;
    View v;
    private String mModuleName;
    private NonproductivereasonBO reasonObj;

    private String mImageName, mImagePath, mSelectedReasonId;
    private boolean isPhotoTaken = false;
    ArrayAdapter<ReasonMaster> reasonAdapter;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
            getDialog().setCancelable(false);
        }

        this.setCancelable(false);

        v = inflater.inflate(R.layout.fragment_reason_with_image, container, false);

        mModuleName = getArguments().getString("modulename");
        Commons.print("modulename, " + "" + mModuleName);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        btnCancel = v.findViewById(R.id.closeBTN);
        btnSave = v.findViewById(R.id.saveBTN);
        ms_reason = v.findViewById(R.id.sp_reason);
        ivReason = v.findViewById(R.id.reason_image_view);


        return v;
    }


    public boolean isShowing() {
        return getDialog() != null;
    }


    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }

        int dialogHeight = (int) getActivity().getResources().getDimension(R.dimen.dialog_height); // specify a value here

        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, dialogHeight);

        reasonObj = bmodel.reasonHelper.getReasonsWithPhoto();
        mImageName = reasonObj.getImageName();
        mImagePath = reasonObj.getImagePath();
        reasonAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        reasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        reasonAdapter.add(new ReasonMaster("0", getActivity().getResources()
                .getString(R.string.select_reason)));
        for (ReasonMaster temp : bmodel.reasonHelper.getNonVisitReasonMaster()) {
            reasonAdapter.add(temp);
        }

        ms_reason.setAdapter(reasonAdapter);

        if (reasonObj != null) {
            if (reasonObj.getReasonid() != null) {

                ms_reason.setSelection(getReasonIndex(reasonObj.getReasonid()));
            }
            if (reasonObj.getImageName() != null
                    && !reasonObj.getImageName().isEmpty()) {
                setImage(reasonObj.getImageName());
                isPhotoTaken = true;
            } else if (reasonObj.getImageName().isEmpty())
                setImage(reasonObj.getImageName());
        }

        if (mSelectedReasonId != null)

        {
            ms_reason.setSelection(getReasonIndex(mSelectedReasonId));
        }

        btnCancel.setOnClickListener(new

                                             OnClickListener() {

                                                 @Override
                                                 public void onClick(View v) {
                                                     if (isPhotoTaken) {
                                                         String fnameStarts = "NP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                                                 + "_" + bmodel.retailerMasterBO.getRetailerID()
                                                                 + "_" + mModuleName
                                                                 + "_" + Commons.now(Commons.DATE);

                                                         boolean nFilesThere = bmodel
                                                                 .checkForNFilesInFolder(
                                                                         FileUtils.photoFolderPath, 1,
                                                                         fnameStarts);

                                                         if (nFilesThere) {
                                                             bmodel.deleteFiles(FileUtils.photoFolderPath,
                                                                     fnameStarts);
                                                         }

                                                     }
                                                     getDialog().dismiss();

                                                 }
                                             });

        btnSave.setOnClickListener(new

                                           OnClickListener() {
                                               @Override
                                               public void onClick(View view) {
                                                   if (mSelectedReasonId.equals("0"))
                                                       Toast.makeText(getActivity(), getResources().getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                                                   else if (bmodel.configurationMasterHelper.IS_CHECK_PHOTO_MANDATORY
                                                           && !isPhotoTaken)
                                                       Toast.makeText(getActivity(), getResources().getString(R.string.photo_mandatory), Toast.LENGTH_SHORT).show();
                                                   else {
                                                       reasonObj = new NonproductivereasonBO();
                                                       reasonObj.setReasonid(mSelectedReasonId);
                                                       reasonObj.setRetailerid(bmodel.retailerMasterBO.getRetailerID());
                                                       reasonObj.setImageName(mImageName);
                                                       reasonObj.setImagePath(mImagePath);
                                                       reasonObj.setModuleCode(mModuleName);
                                                       bmodel.reasonHelper.saveNpReasons(reasonObj);
                                                       getDialog().dismiss();
                                                   }
                                               }
                                           });


        ivReason.setOnClickListener(new

                                            OnClickListener() {
                                                @Override
                                                public void onClick(View view) {
                                                    if (bmodel.isExternalStorageAvailable()) {

                                                        mImageName = "NP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                                                + "_" + bmodel.retailerMasterBO.getRetailerID()
                                                                + "_" + mModuleName
                                                                + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";

                                                        mImagePath = "NonProductive/"
                                                                + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                                                                .replace("/", "") + "/"
                                                                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mImageName;

                                                        String fnameStarts = "NP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                                                + "_" + bmodel.retailerMasterBO.getRetailerID()
                                                                + "_" + mModuleName
                                                                + "_" + Commons.now(Commons.DATE);

                                                        boolean nFilesThere = bmodel
                                                                .checkForNFilesInFolder(
                                                                        FileUtils.photoFolderPath, 1,
                                                                        fnameStarts);
                                                        if (nFilesThere) {
                                                            showFileDeleteAlert(fnameStarts);
                                                        } else {
                                                            Intent intent = new Intent(getActivity(),
                                                                    CameraActivity.class);
                                                            intent.putExtra(CameraActivity.QUALITY, 40);
                                                            String path = FileUtils.photoFolderPath + "/"
                                                                    + mImageName;
                                                            intent.putExtra(CameraActivity.PATH, path);
                                                            startActivityForResult(intent,
                                                                    bmodel.CAMERA_REQUEST_CODE);
                                                        }

                                                    }

                                                }
                                            });

        ms_reason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()

        {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ReasonMaster reasonMaster = (ReasonMaster) ms_reason.getSelectedItem();
                mSelectedReasonId = reasonMaster.getReasonID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


    }

    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + " " + 1 + " "
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        isPhotoTaken = false;
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == bmodel.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Sucessfully Captured.");
                isPhotoTaken = true;
                bmodel.reasonHelper.saveImage(mImageName, mImagePath);

            } else {
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Canceled");
                isPhotoTaken = false;
                reasonObj.setImageName("");
                reasonObj.setImagePath("");
            }
        } else {
            Commons.print(bmodel.mSelectedActivityName
                    + "Camers Activity : Canceled");
            isPhotoTaken = false;
            reasonObj.setImageName("");
            reasonObj.setImagePath("");
        }
    }


    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    private void setImage(String imagepath) {
        File imgFile = new File(FileUtils.photoFolderPath + "/" + imagepath);
        Glide.with(getActivity())
                .load(imgFile)
                .error(ContextCompat.getDrawable(getActivity(), R.drawable.no_image_available))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivReason);
    }


    private int getReasonIndex(String reasonId) {

        int len = reasonAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = reasonAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }


}
