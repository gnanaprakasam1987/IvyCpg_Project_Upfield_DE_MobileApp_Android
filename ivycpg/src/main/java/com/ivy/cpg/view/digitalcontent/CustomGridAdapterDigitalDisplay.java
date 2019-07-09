package com.ivy.cpg.view.digitalcontent;

import android.content.Context;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ImageBO;

import java.util.ArrayList;
import java.util.Collections;

class CustomGridAdapterDigitalDisplay extends RecyclerView.Adapter<CustomGridAdapterDigitalDisplay.ViewHolder> {
    private Context context;
    private ArrayList<ImageBO> items, mAllImagesList;
    private StoreWiseGallery ob;
    private TextView selected_folder;
    private RecyclerView mDetailGridView;

    public CustomGridAdapterDigitalDisplay(Context context, ArrayList<ImageBO> im, StoreWiseGallery ob2
            , TextView selected_folder, RecyclerView mDetailGridView,
                                           ArrayList<ImageBO> mAllImagesList) {

        this.context = context;
        this.items = im;
        ob = ob2;
        this.selected_folder = selected_folder;
        this.mDetailGridView = mDetailGridView;
        this.mAllImagesList = mAllImagesList;
        Collections.sort(mAllImagesList);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_store_wise_gallery_row, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bo = items.get(position);
        holder.imageView.setImageBitmap(holder.bo.getFilebitmap());
        holder.closePRODNAME.setText(holder.bo.getImageDirectory());
        holder.text_count.setText(holder.bo.getCount());

        showSelectedDirectory(items.get(0).getImageDirectory());

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectedDirectory(holder.bo.getImageDirectory());

            }
        });

    }

    private void showSelectedDirectory(String Directory) {
        selected_folder.setText(Directory);

        ArrayList<ImageBO> filteredList = new ArrayList<>();
        for (int i = 0; i < mAllImagesList.size(); i++) {
            if (mAllImagesList.get(i).getImageDirectory().equals(Directory)) {
                filteredList.add(mAllImagesList.get(i));
            }
        }

        mDetailGridView.setAdapter(new CustomGridAdapter(context, filteredList, ob));

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageBO bo;
        ImageView imageView;
        TextView closePRODNAME, text_count;
        CardView cardview;
        LinearLayout text_layout;

        public ViewHolder(View v) {
            super(v);
            imageView = (ImageView) v.
                    findViewById(R.id.icon);
            text_count = (TextView) v.findViewById(R.id.counttxtview);
            closePRODNAME = (TextView) v.findViewById(R.id.closePRODNAME);
            cardview = (CardView) v.findViewById(R.id.cardview);
            text_layout = (LinearLayout) v.findViewById(R.id.txtlayout);
        }


    }
}

