package com.ivy.cpg.view.survey;

import android.app.Activity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.DragEvent;
import android.view.View;

import com.ivy.sd.png.asean.view.R;

import java.util.List;

class DragDropHelper implements View.OnDragListener {

    private boolean isDropped = false;
    private DragDropListener listener;
    private Activity activity;

    DragDropHelper(Activity activity, DragDropListener listener) {
        this.listener = listener;
        this.activity = activity;
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DROP:
                isDropped = true;
                int positionTarget = -1;

                View viewSource = (View) event.getLocalState();
                int viewId = v.getId();
                final int flItem = R.id.frame_layout_item;
                final int rvTop = R.id.image_recyclerview;
                final int rvBottom = R.id.thumnail_recyclerview;

                switch (viewId) {
                    case flItem:
                    case rvTop:
                    case rvBottom:

                        RecyclerView target = null;
                        switch (viewId) {
                            case rvTop:
                                target = (RecyclerView) v.getRootView().findViewById(rvTop);
                                break;
                            case rvBottom:
                                target = (RecyclerView) v.getRootView().findViewById(rvBottom);
                                break;
                            default:
                                target = (RecyclerView) v.getParent();
                                positionTarget = (int) v.getTag();
                        }

                        if (viewSource != null) {
                            RecyclerView source = (RecyclerView) viewSource.getParent();
                            ListAdapter adapterSource = (ListAdapter) source.getAdapter();
                            int positionSource = (int) viewSource.getTag();
                            int sourceId = source.getId();

                            String list = adapterSource.getList().get(positionSource);
                            List<String> listSource = adapterSource.getList();
                            if(sourceId!=rvBottom ) {
                                listSource.remove(positionSource);
                                adapterSource.updateList(listSource);
                                adapterSource.notifyDataSetChanged();
                            }
                            else if(target==source )
                            {
                                listSource.remove(positionSource);
                                adapterSource.updateList(listSource);
                                adapterSource.notifyDataSetChanged();
                            }

                            ListAdapter adapterTarget = (ListAdapter) target.getAdapter();
                            List<String> customListTarget = adapterTarget.getList();
                            if (positionTarget >= 0) {
                                customListTarget.add(positionTarget, list);
                            } else {
                                customListTarget.add(list);
                            }
                            adapterTarget.updateList(customListTarget);
                            adapterTarget.notifyDataSetChanged();

                            if (sourceId == rvBottom && adapterSource.getItemCount() < 1) {
                                listener.setEmptyListBottom(true);
                            }
                            if (sourceId == rvTop && adapterSource.getItemCount() < 1) {
                                listener.setEmptyListTop(true);
                            }
                        }
                        break;
                }
                break;
        }

        if (!isDropped && event.getLocalState() != null) {
            ((View) event.getLocalState()).setVisibility(View.VISIBLE);
        }
        return true;
    }
}