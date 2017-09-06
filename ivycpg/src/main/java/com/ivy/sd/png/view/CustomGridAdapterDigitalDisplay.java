package com.ivy.sd.png.view;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
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
    private ArrayList<ImageBO> items, allimages;
    private DigitalContentDisplayNew ob;
    private TextView selected_folder;
    private RecyclerView detailedgrid;

    public CustomGridAdapterDigitalDisplay(Context context, ArrayList<ImageBO> im, DigitalContentDisplayNew ob2
            , TextView selected_folder, RecyclerView detailedgrid,
                                           ArrayList<ImageBO> allimages) {

        this.context = context;
        this.items = im;
        ob = ob2;
        this.selected_folder = selected_folder;
        this.detailedgrid = detailedgrid;
        this.allimages = allimages;
        Collections.sort(allimages);
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_store_wise_gallery_row, parent, false);
        ViewHolder vh = new ViewHolder(v);

        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.bo = items.get(position);
        holder.imageView.setImageBitmap(holder.bo.getFilebitmap());
        holder.closePRODNAME.setText(holder.bo.getImageDirectory());
        holder.counttxtview.setText(holder.bo.getCount());

        showSelecetdDirectory(items.get(0).getImageDirectory());

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelecetdDirectory(holder.bo.getImageDirectory());

            }
        });

    }

    private void showSelecetdDirectory(String Directory) {
        selected_folder.setText(Directory);

        ArrayList<ImageBO> filteredList = new ArrayList<>();
        for (int i = 0; i < allimages.size(); i++) {
            if (allimages.get(i).getImageDirectory().equals(Directory)) {
                filteredList.add(allimages.get(i));
            }
        }

        detailedgrid.setAdapter(new CustomGridAdapter(context, filteredList, ob));

    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageBO bo;
        ImageView imageView;
        TextView closePRODNAME, counttxtview;
        CardView cardview;
        LinearLayout txtlayout;

        public ViewHolder(View v) {
            super(v);
            imageView = (ImageView) v.
                    findViewById(R.id.icon);
            counttxtview = (TextView) v.findViewById(R.id.counttxtview);
            closePRODNAME = (TextView) v.findViewById(R.id.closePRODNAME);
            cardview = (CardView) v.findViewById(R.id.cardview);
            txtlayout = (LinearLayout) v.findViewById(R.id.txtlayout);
        }


    }
}

