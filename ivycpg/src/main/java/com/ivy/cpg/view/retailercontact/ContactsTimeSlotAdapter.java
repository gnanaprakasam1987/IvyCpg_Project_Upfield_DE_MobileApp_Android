package com.ivy.cpg.view.retailercontact;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;

public class ContactsTimeSlotAdapter extends RecyclerView.Adapter<ContactsTimeSlotAdapter.ViewHolder> {

    private LayoutInflater mInflater;
    private Context context;
    private ArrayList<RetailerContactAvailBo> availBos = new ArrayList<>();

    ContactsTimeSlotAdapter(Context context,ArrayList<RetailerContactAvailBo> availBos) {
        this.mInflater = LayoutInflater.from(context);
        this.context = context;
        this.availBos = availBos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.time_slot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return availBos.size();
    }


    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String selectedTime = availBos.get(position).getDay() + "    "
                + availBos.get(position).getFrom() + " - "
                + availBos.get(position).getTo();

        holder.timeSlot.setText(selectedTime);
        holder.deleteImg.setVisibility(View.GONE);

    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView timeSlot;
        private ImageView deleteImg;

        public ViewHolder(View itemView) {
            super(itemView);

            timeSlot = itemView.findViewById(R.id.time_slot);
            deleteImg = itemView.findViewById(R.id.delete_img);
        }
    }
}