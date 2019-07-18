package com.ivy.cpg.view.survey;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;

import java.io.File;

import static com.ivy.utils.FileUtils.decodeFile;

public class CaptureSignatureDialog extends Dialog {

    private LinearLayout mContent;
    private String imageName;
    private String photoPath;
    private SignatureView mSignature;
    private Bitmap mBitmap;
    private View mView;

    private PositiveClickListener posClickListener;
    private NegativeOnClickListener negClickListener;
    private String title;
    private String msg;
    private String posBtnTxt;
    private String negBtnTxt;
    private Context context;
    private String capturedImageName;

    public CaptureSignatureDialog(Context context, String title, String msg,
                                  String posBtnTxt, PositiveClickListener posClickListener, String negBtnTxt, NegativeOnClickListener negClickListener, String imageName,
                                  String capturedImageName, String photoPath) {
        super(context);
        this.context = context;
        this.title = title;
        this.msg = msg;
        this.posBtnTxt = posBtnTxt;
        this.posClickListener = posClickListener;
        this.negBtnTxt = negBtnTxt;
        this.negClickListener = negClickListener;
        this.imageName = imageName;
        this.capturedImageName = capturedImageName;
        this.photoPath = photoPath;
        prepareDirectory();

    }

    public interface PositiveClickListener {
        void onPositiveButtonClick(boolean isSignCaptured);
    }

    public interface NegativeOnClickListener {
        void onNegativeButtonClick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);


        setContentView(R.layout.dialog_capture_signature);
        setCancelable(false);

        TextView txtTitle = findViewById(R.id.tv_title);
        if (StringUtils.isNullOrEmpty(title))
            txtTitle.setText(title);

        TextView msg_text = findViewById(R.id.tv_msg);
        if (StringUtils.isNullOrEmpty(title))
            msg_text.setText(msg);

        Button mDoneBTN = findViewById(R.id.btn_done);
        mDoneBTN.setText(posBtnTxt);
        mDoneBTN.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                if (FileUtils.isExternalStorageAvailable(10)) {
                    if (SignatureView.signCaptured) {
                        mView.setDrawingCacheEnabled(true);
                        if (mBitmap == null) {
                            mBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);
                        }
                        new SaveSignature().execute("");
                    } else {
                        Toast.makeText(context, context.getResources().getString(R.string.sign_mandatory), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.unable_to_access_the_sdcard), Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        Button mCancelBTN = findViewById(R.id.btn_cancel);
        if (!StringUtils.isNullOrEmpty(negBtnTxt)) {
            mCancelBTN.setText(negBtnTxt);
            mCancelBTN.setVisibility(View.VISIBLE);
        }

        mCancelBTN.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //callback method
                if (negClickListener != null)
                    negClickListener.onNegativeButtonClick();
                dismiss();

            }
        });


        mContent = findViewById(R.id.ll_sign);
        ImageView mClear = findViewById(R.id.btn_clear);
        mSignature = new SignatureView(context, null, mContent, photoPath, imageName);
        mContent.addView(mSignature, ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        mClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSignature.clear();
                mContent.setBackground(null);
                mContent.setBackground(context.getResources().getDrawable(R.drawable.button_box_corner_grey));
            }
        });

        if (!StringUtils.isNullOrEmpty(capturedImageName))
            setSignImage();
        mView = mContent;
    }

    private void setSignImage() {
        String[] splitPath = capturedImageName.split("/");
        capturedImageName = splitPath[splitPath.length - 1];
        File imgFile = new File(photoPath + capturedImageName);
        if (imgFile.exists()) {
            try {
                Bitmap myBitmap = decodeFile(imgFile);
                mContent.setBackground(new BitmapDrawable(myBitmap));
                SignatureView.signCaptured = true;
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    private void prepareDirectory() {
        try {
            File tempdir = new File(photoPath);
            if (!tempdir.exists())
                tempdir.mkdirs();
        } catch (Exception e) {
            Commons.printException(e);
            Toast.makeText(context, context.getResources().getString(R.string.could_not_init_file_system), Toast.LENGTH_LONG).show();
        }
    }

    private class SaveSignature extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            //callback method
            if (posClickListener != null)
                posClickListener.onPositiveButtonClick(result);
            dismiss();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(photoPath, 1, capturedImageName);

            if (mIsFileAvailable)
                FileUtils.deleteFiles(photoPath, capturedImageName);

            return saveSign();
        }
    }

    private boolean saveSign() {
        mView.setDrawingCacheEnabled(true);
        return mSignature.save(mView);
    }

}