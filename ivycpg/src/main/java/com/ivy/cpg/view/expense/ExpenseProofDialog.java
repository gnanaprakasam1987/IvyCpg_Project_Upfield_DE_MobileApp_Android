package com.ivy.cpg.view.expense;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class ExpenseProofDialog extends DialogFragment {

    private BusinessModel bmodel;
    Button button;
    GridView imgGrid;
    View v;
    String photoNamePath, refID;
    private File deleteFilePath;
    private String deleteImageName = "";
    private ExpenseSheetHelper expenseSheetHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        v = inflater.inflate(R.layout.fragment_exp_img_proof, container, false);

        refID = getArguments().getString("refId");
        Commons.print("refID, " + "" + refID);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        expenseSheetHelper = ExpenseSheetHelper.getInstance(getActivity());

        button = v.findViewById(R.id.closeBTN);
        imgGrid = v.findViewById(R.id.grid);


        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        photoNamePath = FileUtils.photoFolderPath + "/";

        imgGrid.setAdapter(new MyAdapter(expenseSheetHelper.getImagesList(refID)));

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


    }

    class MyAdapter extends ArrayAdapter<String> {

        private ArrayList<String> items;


        public MyAdapter(ArrayList<String> items) {
            super(getActivity(), R.layout.row_image_proof);
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
        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup viewGroup) {
            final ProofViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = inflater.inflate(
                        R.layout.row_image_proof, viewGroup, false);

                holder = new ProofViewHolder();
                holder.imageName = items.get(position);

                holder.imgProof = convertView.findViewById(R.id.iv_proof);
                holder.tvDelete = convertView.findViewById(R.id.tv_delete);

                File imgFile = new File(photoNamePath + "/" + holder.imageName);

                if (imgFile.exists()) {
                    holder.tvDelete.setVisibility(View.VISIBLE);
                    try {

                        holder.imgPath = imgFile.getAbsolutePath();
                        holder.imgProof.setImageBitmap(decodeFile(imgFile));

                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                } else {
                    holder.imgProof.setImageResource(R.drawable.no_image_available);
                    holder.tvDelete.setVisibility(View.GONE);
                }

                holder.tvDelete.setOnClickListener(new OnClickListener() {
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
        ImageView imgProof;
        TextView tvDelete;
        String imageName, imgPath;
    }

    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        int IMAGE_MAX_SIZE = 500;
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

    public void showAlert(String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        boolean isDelete = deleteFilePath.delete();
                        if (isDelete) {
                            expenseSheetHelper.deleteImageProof(deleteImageName);
                            if (expenseSheetHelper.getImagesList(refID).size() > 0)
                                imgGrid.setAdapter(new MyAdapter(expenseSheetHelper.getImagesList(refID)));
                            else
                                getDialog().dismiss();
                        }
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

}
