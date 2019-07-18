package com.ivy.cpg.view.promotion;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.cpg.view.digitalcontent.PlayVideoActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class PromotionAttachmentDialog extends DialogFragment implements OnClickListener {

    private BusinessModel bmodel;
    @SuppressLint("ValidFragment")
    private final String mModuleName;
    private ArrayList<PromotionAttachmentBO> attchmentList;
    private RecyclerView attachmentRecyclerView;

    public PromotionAttachmentDialog(String mModuleName, ArrayList<PromotionAttachmentBO> attchmentList) {
        super();
        this.attchmentList = attchmentList;
        this.mModuleName = mModuleName;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow()
                    .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        View view = inflater.inflate(R.layout.promotionattachment_fragment_dialog, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.titleBar).getTag()) != null)
                ((TextView) view.findViewById(R.id.titleBar))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.titleBar)
                                        .getTag()));

        } catch (Exception e) {
            Commons.printException(e);
        }
        attachmentRecyclerView = view.findViewById(R.id.attachmentRecyclerView);

        attachmentRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        attachmentRecyclerView.setItemAnimator(new DefaultItemAnimator());
        attachmentRecyclerView.setHasFixedSize(false);
        attachmentRecyclerView.setNestedScrollingEnabled(false);
        attachmentRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));

        AttachmentAdapter adapter = new AttachmentAdapter(getContext(), attchmentList);
        attachmentRecyclerView.setAdapter(adapter);

        Button cancel = view.findViewById(R.id.btn_cancel);
        cancel.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_cancel) {
            dismiss();
        }
    }

    public class AttachmentAdapter extends RecyclerView.Adapter<AttachmentAdapter.ViewHolder> {
        private Context mContext;
        private ArrayList<PromotionAttachmentBO> attachmentList;

        public AttachmentAdapter(Context mContext, ArrayList<PromotionAttachmentBO> attachmentList) {
            this.mContext = mContext;
            this.attachmentList = attachmentList;
        }

        @NonNull
        @Override
        public AttachmentAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.promotionattachment_icon_view, parent, false);
            return new AttachmentAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull AttachmentAdapter.ViewHolder holder, int position) {
            PromotionAttachmentBO attachmentBO = attachmentList.get(position);
            holder.iconText.setText(attachmentBO.getFileName());

            Uri path;
            if (Build.VERSION.SDK_INT >= 24) {
                path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(
                        getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                + bmodel.getAppDataProvider().getUser().getUserid()
                                + DataMembers.DIGITAL_CONTENT + "/"
                                + DataMembers.PROMOTION + "/" + attachmentBO.getFileName()));
            } else {
                path = Uri.fromFile(new File(
                        getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                + bmodel.getAppDataProvider().getUser().getUserid()
                                + DataMembers.DIGITAL_CONTENT + "/"
                                + DataMembers.PROMOTION + "/" + attachmentBO.getFileName()));
            }
            Glide.with(getActivity())
                    .load(path)
                    .error(ContextCompat.getDrawable(getActivity(), R.drawable.no_image_available))
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(holder.iconImage);

            holder.iconImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (attachmentBO.getFileName().toLowerCase().endsWith(".pdf"))
                        openFile(attachmentBO.getFileName(), "application/pdf", getResources()
                                .getString(R.string.no_application_available_to_view_pdf));
                    else if (attachmentBO.getFileName().toLowerCase().endsWith(".jpg") || attachmentBO.getFileName().toLowerCase().endsWith(".png"))
                        openFile(attachmentBO.getFileName(), "image/*", getResources()
                                .getString(R.string.no_application_available_to_view_video));
                    else if (attachmentBO.getFileName().toLowerCase().endsWith(".mp4") || attachmentBO.getFileName().toLowerCase().endsWith(".mov"))
                        openFile(attachmentBO.getFileName(), "", getResources()
                                .getString(R.string.no_application_available_to_view_video));
                    else
                        Toast.makeText(getContext(), "No Apps to open this File", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public int getItemCount() {
            return attachmentList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView iconText;
            private ImageView iconImage;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconText = itemView.findViewById(R.id.homeicontext);
                iconImage = itemView.findViewById(R.id.homeicon);
            }
        }
    }

    private void openFile(String name, String mime, String errorMsg) {
        Uri path;
        String filePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + bmodel.getAppDataProvider().getUser().getUserid() + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.PROMOTION + "/" + name;
        File file = new File(filePath);

        if (file.exists()) {
            Intent intent;
            if (name.toLowerCase().endsWith(".mp4") || name.toLowerCase().endsWith(".mov")) {
                String mimeType = getMimeType(filePath);
                if (mimeType == null || !mimeType.contains("video")) {
                    Toast.makeText(getActivity(), getResources().getString(R.string.file_format_not_support),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                intent = new Intent(getActivity(), PlayVideoActivity.class);
                path = (Build.VERSION.SDK_INT >= 24) ? FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider", file) : Uri.fromFile(file);

                intent.putExtra("DigiContentId", "");
                intent.putExtra("PId", "");
                intent.putExtra("videoPath", path.toString());
            } else {
                intent = new Intent(Intent.ACTION_VIEW);
                path = (Build.VERSION.SDK_INT >= 24) ? FileProvider.getUriForFile(getActivity(),
                        BuildConfig.APPLICATION_ID + ".provider", file) : Uri.fromFile(file);
                intent.setDataAndType(path, mime);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        getActivity(),
                        errorMsg,
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.file_not_found),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String fileUrl) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String type = fileNameMap.getContentTypeFor(fileUrl);

        Commons.print("Mime Type: " + type + " URL:" + fileUrl);

        return type;
    }
}
