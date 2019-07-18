package com.ivy.cpg.view.survey;

import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.os.Build;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FileUtils;

import java.util.List;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder>
        {

    private List<String> list;
    private DragDropListener DragDropListener;
    private int flag = -1, maxPhoto = -1;
    private Activity activity;

    ListAdapter(Activity activity, List<String> list, DragDropListener DragDropListener, int flag, int maxPhoto) {
        this.activity = activity;
        this.list = list;
        this.DragDropListener = DragDropListener;
        this.flag = flag;
        this.maxPhoto = maxPhoto;
    }

    @Override
    public ListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = null;
        if (flag == 1) // Main Image Grid
        {
            view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.drag_drop_list_row, parent, false);
        } else if (flag == 2) // Thumbnail Image Grid
        {
            view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.drag_drop_thumbnail_row, parent, false);
        }

        return new ListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ListViewHolder holder, final int position) {
        setImageFromSrc(holder.imageView, list.get(position));
        holder.frameLayout.setTag(position);
        holder.frameLayout.setOnDragListener(new DragDropHelper(activity, DragDropListener));

        holder.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog(position);
            }
        });
        holder.frameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                RecyclerView imgRecyler = (RecyclerView) v.getRootView().findViewById(R.id.image_recyclerview); // Main Grid
                RecyclerView source = (RecyclerView) v.getParent(); // Source Grid
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                // Avoiding drag&drop from Top to Bottom Grid by the condition.
                if (source != imgRecyler ) {
                    int count = ((DragDropPictureActivity) activity).getArrayCount();
                    if (maxPhoto > count) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            v.startDragAndDrop(data, shadowBuilder, v, 0);
                        } else {
                            v.startDrag(data, shadowBuilder, v, 0);
                        }
                    } else {
                        Toast.makeText(activity, "Max. Photo reached", Toast.LENGTH_SHORT).show();
                    }
                }
                return false;
            }
        });

    }

    private void showAlertDialog(final int pos) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setMessage("Do you want to delete the picture?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                list.remove(pos);
                updateList(list);
                notifyDataSetChanged();
            }
        });
        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();

    }

    private void setImageFromSrc(ImageView imageView, String src) {
        String localPath = FileUtils.photoFolderPath + "/";
        if(src.contains("SVY_")) {
            int index = src.indexOf("SVY_");
            src = src.substring(index);
        }
        Glide.with(activity).load(localPath + src)
                .placeholder(R.drawable.ic_photo_camera_blue_24dp)
                .error(R.drawable.no_image_available)
                .dontAnimate()
                .dontTransform()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    List<String> getList() {
        return list;
    }

    void updateList(List<String> list) {
        this.list = list;
    }

    DragDropHelper getDragInstance() {
        if (DragDropListener != null) {
            return new DragDropHelper(activity, DragDropListener);
        } else {
            Log.e("ListAdapter", "DragDropListener wasn't initialized!");
            return null;
        }
    }

    class ListViewHolder extends RecyclerView.ViewHolder {
        TextView clearButton;
        ImageView imageView;
        FrameLayout frameLayout;

        ListViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.img_dragdrop);
            clearButton = (TextView) itemView.findViewById(R.id.txt_dragdropClear);
            frameLayout = (FrameLayout) itemView.findViewById(R.id.frame_layout_item);
        }
    }
}
