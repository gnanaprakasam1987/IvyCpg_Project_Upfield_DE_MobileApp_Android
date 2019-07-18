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

public class ContactsTimeSlotAdapterEdit extends RecyclerView.Adapter<ContactsTimeSlotAdapterEdit.ViewHolder> {

    private LayoutInflater mInflater;
    private Context context;
    private ArrayList<RetailerContactAvailBo> retailerContactAvailBos = new ArrayList<>();

    private DeleteTimeSlotListener deleteTimeSlotListener;


    ContactsTimeSlotAdapterEdit(Context context,DeleteTimeSlotListener deleteTimeSlotListener) {
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        this.deleteTimeSlotListener = deleteTimeSlotListener;
    }

    public void listValues(ArrayList<RetailerContactAvailBo> retailerContactAvailBos){

        this.retailerContactAvailBos = retailerContactAvailBos;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.time_slot_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return retailerContactAvailBos.size();
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        String selectedTime = retailerContactAvailBos.get(position).getDay() + "    "
                + retailerContactAvailBos.get(position).getFrom() + " - "
                + retailerContactAvailBos.get(position).getTo();

        holder.timeSlot.setText(selectedTime);
        holder.deleteImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                deleteTimeSlotListener.deleteSlot(retailerContactAvailBos.get(position));

            }
        });

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

    public interface DeleteTimeSlotListener{
        void deleteSlot(RetailerContactAvailBo contactAvailBo);
    }
}
