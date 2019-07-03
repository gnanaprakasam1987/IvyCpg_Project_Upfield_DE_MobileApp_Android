package com.ivy.cpg.view.order.scheme;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

public class NextSlabSchemeDialog extends Dialog {

	Button btn_done;
	private BusinessModel bmodel;
	ListView schemePromoLv;
	SchemeAdapter schemeAdapter;

	protected NextSlabSchemeDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener, SchemeApply schemeApply) {
		super(context, cancelable, cancelListener);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_nextslab_scheme);
		schemePromoLv = (ListView) findViewById(R.id.schemePromoLv);

		getWindow().setLayout(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT);
		bmodel = (BusinessModel) schemeApply.getApplicationContext();
		bmodel.setContext(schemeApply);
		schemeAdapter = new SchemeAdapter(context);
		schemePromoLv.setAdapter(schemeAdapter);

		((Button) findViewById(R.id.btn_done))
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dismiss();
					}
				});

	}

	class SchemeAdapter extends ArrayAdapter {
		SchemeDetailsMasterHelper schemeHelper;
		public SchemeAdapter(Context context) {
			super(context, R.layout.dialog_nextslab_scheme_listview);
			schemeHelper=SchemeDetailsMasterHelper.getInstance(context);
		}

		@Override
		public int getCount() {
            return schemeHelper.getSchemePromotion()
                    .size();
		}

		@Override
		public Object getItem(int position) {
            return schemeHelper.getSchemePromotion().get(
                    position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
            SchemeBO schemeBO = schemeHelper
                    .getSchemePromotion().get(position);
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater.inflate(R.layout.dialog_nextslab_scheme_listview, parent,
						false);
				holder = new ViewHolder();
				holder.schemeNameTv = (TextView) row
						.findViewById(R.id.schemeNameTv);
				holder.buyQtyTv = (TextView) row.findViewById(R.id.buyQtyTv);
				holder.maxFreeQtyTv = (TextView) row
						.findViewById(R.id.maxFreeQtyTv);
				holder.maxPriceTv = (TextView) row
						.findViewById(R.id.maxPriceTv);
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			holder.schemeBO = schemeBO;
			holder.schemeNameTv.setText(schemeBO.getScheme());
			holder.buyQtyTv.setText(schemeBO.getSelectedQuantity() + ""); // Buy
																			// Qty
			holder.maxFreeQtyTv.setText(schemeBO.getMaximumQuantity() + "");
			holder.maxPriceTv.setText(schemeBO.getMaximumPrice() + "");
			return row;
		}
	}

	static class ViewHolder {
		TextView schemeNameTv, buyQtyTv, maxFreeQtyTv, maxPriceTv;
		SchemeBO schemeBO;
	}

}
