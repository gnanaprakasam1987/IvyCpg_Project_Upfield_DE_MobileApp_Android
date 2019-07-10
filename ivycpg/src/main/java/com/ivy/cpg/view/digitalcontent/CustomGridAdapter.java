package com.ivy.cpg.view.digitalcontent;

import android.content.Context;
import android.content.Intent;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ImageBO;
import com.ivy.sd.png.view.ViewImage;

import java.util.ArrayList;

public class CustomGridAdapter extends RecyclerView.Adapter<CustomGridAdapter.ViewHolder> {
	private Context context;
	private final ArrayList<ImageBO> items;
    private StoreWiseGallery ob;

    public CustomGridAdapter(Context context, ArrayList<ImageBO> im, StoreWiseGallery ob2) {

		this.context = context;
		this.items = im;
		ob=ob2;

	}



	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View v = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.grid_item, parent, false);

        return new ViewHolder(v);
    }

	@Override
	public void onBindViewHolder(final ViewHolder holder, final int position) {
		holder.bo = items.get(position);

		holder.imageView.setImageBitmap(holder.bo.getFilebitmap());
		holder.imageView.setOnClickListener(new OnClickListener() {


			public void onClick(View v) {
				Intent i = new Intent(context, ViewImage.class);
				i.putExtra("filepath", holder.bo.getImagepath());

				i.putExtra("filename", holder.bo.getImagename());

				i.putExtra("position", position);

                ob.startingActivity(i);

			}
		});
	}


	@Override
	public int getItemCount() {
		return items.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private ImageBO bo;
		private ImageView imageView;

		public ViewHolder(View v) {
			super(v);
			imageView = (ImageView) v
					.findViewById(R.id.grid_item_image);

		}


	}
}
