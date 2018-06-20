package com.ivy.sd.png.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.FontUtils;

/**
 * Created by mayuri.v on 4/27/2017.
 */
public class CommonDialog extends Dialog {

    public positiveOnClickListener posClickListener;
    public negativeOnClickListener negClickListener;
    private String title;
    private String msg;
    private String posBtnTxt;
    private String negBtnTxt;
    private Context context;
    private Context bContext;
    protected BusinessModel bmodel;
    private boolean imgDisplay;
    private boolean isMoveNext, isDynamicImageAvail = false;
    private String imageSrc;


    public CommonDialog(Context bContext, Context context, String title, String msg,
                        boolean imgDisplay, String posBtnTxt, positiveOnClickListener posClickListener) {
        super(context);
        this.context = context;
        this.bContext = bContext;
        bmodel = (BusinessModel) bContext;
        this.posClickListener = posClickListener;
        this.title = title;
        this.msg = msg;
        this.imgDisplay = imgDisplay;
        this.posBtnTxt = posBtnTxt;
        this.negBtnTxt = null;

    }

    public CommonDialog(Context bContext, Context context, String title, String msg,
                        boolean imgDisplay, String posBtnTxt, String negBtnTxt,
                        positiveOnClickListener posClickListener, negativeOnClickListener negClickListener) {
        super(context);
        this.context = context;
        this.bContext = bContext;
        bmodel = (BusinessModel) bContext;
        this.posClickListener = posClickListener;
        this.title = title;
        this.msg = msg;
        this.imgDisplay = imgDisplay;
        this.posBtnTxt = posBtnTxt;
        this.negBtnTxt = negBtnTxt;
        this.negClickListener = negClickListener;
    }

    public CommonDialog(Context bContext, Context context, String title, String msg,
                        boolean imgDisplay, String posBtnTxt, String negBtnTxt, boolean isMoveNext,
                        positiveOnClickListener posClickListener, negativeOnClickListener negClickListener) {
        super(context);
        this.context = context;
        this.bContext = bContext;
        bmodel = (BusinessModel) bContext;
        this.posClickListener = posClickListener;
        this.title = title;
        this.msg = msg;
        this.imgDisplay = imgDisplay;
        this.posBtnTxt = posBtnTxt;
        this.negBtnTxt = negBtnTxt;
        this.negClickListener = negClickListener;
        this.isMoveNext = isMoveNext;
    }

    //Dialog with Dynamic Image
    public CommonDialog(Context bContext, Context context, String title, String msg,
                        boolean imgDisplay, String posBtnTxt, String negBtnTxt, boolean isMoveNext, String ImageSrc,
                        positiveOnClickListener posClickListener, negativeOnClickListener negClickListener) {
        super(context);
        this.context = context;
        this.bContext = bContext;
        bmodel = (BusinessModel) bContext;
        this.posClickListener = posClickListener;
        this.title = title;
        this.msg = msg;
        this.imgDisplay = imgDisplay;
        this.posBtnTxt = posBtnTxt;
        this.negBtnTxt = negBtnTxt;
        this.negClickListener = negClickListener;
        this.isMoveNext = isMoveNext;
        this.imageSrc = ImageSrc;
        if (ImageSrc != null && (!ImageSrc.equalsIgnoreCase("")))
            isDynamicImageAvail = true;
    }


    // This is my interface //
    public interface positiveOnClickListener {
        void onPositiveButtonClick();
    }

    public interface negativeOnClickListener {
        void onNegativeButtonClick();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = null;
        if (isDynamicImageAvail)
            view = View.inflate(context, R.layout.common_dialog_layout_with_image, null);
        else
            view = View.inflate(context, R.layout.common_dialog_layout, null);

        setContentView(view);
        setCancelable(false);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        TextView txtTitle = (TextView) view.findViewById(R.id.title);
        if (title != null) {
            if (!title.isEmpty() && !title.equals("")) {
                txtTitle.setVisibility(View.VISIBLE);
                txtTitle.setText(title);
                txtTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, context));
            }
        }

        ImageView dialog_img = (ImageView) view.findViewById(R.id.dialog_img);

        TextView msg_text = (TextView) view.findViewById(R.id.msg_text);
        msg_text.setText(msg);
        msg_text.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, context));

        if (imgDisplay) {
            dialog_img.setVisibility(View.VISIBLE);
            msg_text.setGravity(Gravity.CENTER_HORIZONTAL);
        }
        if (isDynamicImageAvail) {
            //Setting the image size - Width & Height dynamically based on the screen size.
            // Width - Match Parent
            // Height - Screen height divide by 2
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;
            int height = displayMetrics.heightPixels;
            Log.e("Widthheight", width + " " + height);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (height / 2.4));
            dialog_img.setLayoutParams(layoutParams);
            msg_text.setGravity(Gravity.CENTER_VERTICAL);
            Glide.with(context).load(imageSrc)
                    .centerCrop()
                    .placeholder(R.drawable.downloadsuccess)
                    .error(R.drawable.no_image_available)
                    .override(300, 300)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(dialog_img);
            if (txtTitle.getVisibility() == View.GONE)
                msg_text.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, context));
        }


        Button mDoneBTN = (Button) view.findViewById(R.id.btn_done);
        mDoneBTN.setText(posBtnTxt);
        mDoneBTN.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //callback method
                posClickListener.onPositiveButtonClick();
                dismiss();
            }
        });

        Button mCancelBTN = (Button) view.findViewById(R.id.btn_cancel);
        if (negBtnTxt != null) {
            mCancelBTN.setText(negBtnTxt);
            mCancelBTN.setVisibility(View.VISIBLE);
        }

        mCancelBTN.setOnClickListener(new android.view.View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //callback method
                negClickListener.onNegativeButtonClick();
                dismiss();

            }
        });

        mDoneBTN.setTypeface(FontUtils.getFontBalooHai(context, FontUtils.FontType.REGULAR));
        mCancelBTN.setTypeface(FontUtils.getFontBalooHai(context, FontUtils.FontType.REGULAR));

        if (isMoveNext) {
            TextView text_move_next = (TextView) findViewById(R.id.text_move_next);
            text_move_next.setVisibility(View.VISIBLE);
            text_move_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    negClickListener.onNegativeButtonClick();
                    dismiss();
                }
            });
        }


    }

}
