package com.ivy.sd.png.survey;

import android.app.Activity;
import android.content.ClipData;
import android.content.DialogInterface;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.util.List;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder>
        {

    private List<String> list;
    private DragDropListener DragDropListener;
    private int flag = -1, minPhoto = -1, maxPhoto = -1;
    private Activity activity;
    private boolean isLongPress=false,isSinglePress=false;

    ListAdapter(Activity activity, List<String> list, DragDropListener DragDropListener, int flag, int minPhoto, int maxPhoto) {
        this.activity = activity;
        this.list = list;
        this.DragDropListener = DragDropListener;
        this.flag = flag;
        this.minPhoto = minPhoto;
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
        //  Log.e("ListSize",list.size()+"");
        //  holder.text.setText(list.get(position));
        holder.frameLayout.setTag(position);
      //  holder.frameLayout.setOnTouchListener(this);
        holder.frameLayout.setOnDragListener(new DragDropHelper(activity, DragDropListener));

        holder.clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(activity, "Hi", Toast.LENGTH_SHORT).show();
                showAlertDialog(position);
            }
        });
        holder.frameLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                RecyclerView imgRecyler = (RecyclerView) v.getRootView().findViewById(R.id.image_recyclerview);
                RecyclerView thumbRecyler=(RecyclerView) v.getRootView().findViewById(R.id.thumnail_recyclerview);
                RecyclerView source = (RecyclerView) v.getParent();
                ClipData data = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
                if (source != imgRecyler ) {
                    int count = ((DragDropPictureActivity) activity).getArrayCount();
//                    Log.e("Count", maxPhoto + "\t" + count);
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
//        holder.frameLayout.setOnTouchListener(new View.OnTouchListener() {
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                RecyclerView imgRecyler = (RecyclerView) v.getRootView().findViewById(R.id.image_recyclerview);
//                RecyclerView thumbRecyler=(RecyclerView) v.getRootView().findViewById(R.id.thumnail_recyclerview);
//                RecyclerView source = (RecyclerView) v.getParent();
//
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        ClipData data = ClipData.newPlainText("", "");
//                        View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
//                        if (source != imgRecyler ) {
//                            int count = ((DragDropPictureActivity) activity).getArrayCount();
////                    Log.e("Count", maxPhoto + "\t" + count);
//                            if (maxPhoto > count) {
//                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                    v.startDragAndDrop(data, shadowBuilder, v, 0);
//                                } else {
//                                    v.startDrag(data, shadowBuilder, v, 0);
//                                }
//                            } else {
//                                Toast.makeText(activity, "Max. Photo reached", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                        return true;
//                }
//                return  gestureDetector.onTouchEvent(event);
//            }
//            private GestureDetector gestureDetector =new GestureDetector(activity, new GestureDetector.OnGestureListener() {
//                @Override
//                public boolean onDown(MotionEvent e) {
//                    return false;
//                }
//
//                @Override
//                public void onShowPress(MotionEvent e) {
//
//                }
//
//                @Override
//                public boolean onSingleTapUp(MotionEvent e) {
//                    return false;
//                }
//
//                @Override
//                public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//                    return false;
//                }
//
//                @Override
//                public void onLongPress(MotionEvent e) {
//
//                    Toast.makeText(activity, "onLongPress", Toast.LENGTH_SHORT).show();
//
//                }
//
//                @Override
//                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
//                    return false;
//                }
//            });
//        });

    }

    private void showAlertDialog(final int pos) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(activity);
        alertDialog.setMessage("Do you want to delete the picture?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                if(((DragDropPictureActivity) activity).removeFromDataList(list.get(pos))) {
//                    list.remove(pos);
//                    updateList(list);
//                    notifyDataSetChanged();
//                } else
//                {
//                    Toast.makeText(activity, "Unable to delete. Try again", Toast.LENGTH_SHORT).show();
//                }
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
        String localPath = HomeScreenFragment.folder
                .getPath() + "/";
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

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        RecyclerView imgRecyler = (RecyclerView) v.getRootView().findViewById(R.id.image_recyclerview);
//        RecyclerView thumbRecyler=(RecyclerView) v.getRootView().findViewById(R.id.thumnail_recyclerview);
//        RecyclerView source = (RecyclerView) v.getParent();
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                ClipData data = ClipData.newPlainText("", "");
//                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
//                if (source != imgRecyler && isLongPress) {
//                    int count = ((DragDropPictureActivity) activity).getArrayCount();
////                    Log.e("Count", maxPhoto + "\t" + count);
//                    if (maxPhoto > count) {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                            v.startDragAndDrop(data, shadowBuilder, v, 0);
//                        } else {
//                            v.startDrag(data, shadowBuilder, v, 0);
//                        }
//                        isLongPress=false;
//                    } else {
//                        Toast.makeText(activity, "Max. Photo reached", Toast.LENGTH_SHORT).show();
//                    }
//                }
//                return true;
//        }
//        return false;
//    }

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
            //   text=(TextView)itemView.findViewById(R.id.text);
            imageView = (ImageView) itemView.findViewById(R.id.img_dragdrop);
            clearButton = (TextView) itemView.findViewById(R.id.txt_dragdropClear);
            frameLayout = (FrameLayout) itemView.findViewById(R.id.frame_layout_item);
        }
    }
}
