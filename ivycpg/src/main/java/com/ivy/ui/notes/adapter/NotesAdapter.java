package com.ivy.ui.notes.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.notes.NoteOnclickListener;
import com.ivy.ui.notes.model.NotesBo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    private Context mContext;
    private ArrayList<NotesBo> notesItemList;
    private boolean isFromHomeScreen;
    private NoteOnclickListener noteOnclickListener;

    public NotesAdapter(Context mContext, ArrayList<NotesBo> notesItemList, boolean isFromHomeScreen, NoteOnclickListener noteOnclickListener) {
        this.mContext = mContext;
        this.notesItemList = notesItemList;
        this.isFromHomeScreen = isFromHomeScreen;
        this.noteOnclickListener = noteOnclickListener;
    }

    @NonNull
    @Override
    public NotesAdapter.NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notes, parent, false);
        return new NotesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NotesAdapter.NotesViewHolder holder, int position) {

        NotesBo notObj = notesItemList.get(position);

        String userNameStr;
        String dateTimeStr;
        if (!notObj.getModifiedDate().isEmpty()
                && !notObj.getModifiedBy().isEmpty()) {
            userNameStr = notObj.getModifiedBy();
            dateTimeStr = notObj.getModifiedDate();
        } else {
            userNameStr = notObj.getCreatedBy();
            dateTimeStr = notObj.getCreatedDate() + " " + notObj.getTime();
        }

        holder.noteCreatedBy.setText(userNameStr);

        String retailerName = "@" + notObj.getRetailerName();
        holder.retailerNameTv.setText(retailerName);

        holder.noteTitleTV.setText(notObj.getNotesTitle());
        holder.noteDescTv.setText(notObj.getNotesDesc());

        holder.noteDateTimeTv.setText(DateTimeUtils.getMDHDateFormat(dateTimeStr));

        holder.listItemRL.setOnClickListener(v -> noteOnclickListener.onClickDetailView(notObj));

        holder.addDeleteBtn.setOnClickListener(v -> {

            PopupMenu popupMenu = new PopupMenu(mContext, holder.addDeleteBtn);
            popupMenu.inflate(R.menu.menu_edit_delete);
            popupMenu.setOnMenuItemClickListener(item -> {

                switch (item.getItemId()) {
                    case R.id.menu_edit_note:
                        noteOnclickListener.onClickEditNote(notObj);
                        return true;

                    case R.id.menu_delete_note:
                        noteOnclickListener.onClickDeleteNote(notObj);
                        notesItemList.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, notesItemList.size());
                        return true;

                    default:
                        return false;
                }
            });

            popupMenu.show();
        });


    }

    @Override
    public int getItemCount() {
        return notesItemList.size();
    }

    class NotesViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.list_item_note_title_tv)
        AppCompatTextView noteTitleTV;

        @BindView(R.id.list_item_note_desc_tv)
        AppCompatTextView noteDescTv;

        @BindView(R.id.list_item_note_created_by_tv)
        AppCompatTextView noteCreatedBy;

        @BindView(R.id.list_item_retailer_name_tv)
        AppCompatTextView retailerNameTv;

        @BindView(R.id.list_item_icon_more)
        AppCompatImageView addDeleteBtn;

        @BindView(R.id.list_item_note_date_tv)
        AppCompatTextView noteDateTimeTv;

        @BindView(R.id.row_item_rl)
        RelativeLayout listItemRL;


        NotesViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (!isFromHomeScreen)
                retailerNameTv.setVisibility(View.GONE);
        }
    }
}
