package com.ivy.cpg.view.promotion;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.DataPickerDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class PromotionTrackingActivity extends IvyBaseActivityNoActionBar implements
		BrandDialogInterface, DataPickerDialogFragment.UpdateDateInterface,FiveLevelFilterCallBack {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promotiontracking);

		Toolbar toolbar =  findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			if(getSupportActionBar() != null)
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		BusinessModel bmodel = (BusinessModel) this.getApplicationContext();
		//bmodel.setContext(this);
		overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(this,
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			finish();
		}
	}

	@Override
	public void onBackPressed() {
	}

	protected void onDestroy() {
		super.onDestroy();
		unbindDrawables(findViewById(R.id.root));
	}

	private void unbindDrawables(View view) {
		if(view!=null) {
			if (view.getBackground() != null) {
				view.getBackground().setCallback(null);
			}
			if (view instanceof ViewGroup) {
				for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
					unbindDrawables(((ViewGroup) view).getChildAt(i));
				}
				try {
					((ViewGroup) view).removeAllViews();
				} catch (Exception e) {
					Commons.printException(""+e);
				}
			}
		}
	}

	@Override
	public void updateBrandText(String mFilterText, int id) {
		FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updateBrandText(mFilterText, id);
	}

	@Override
	public void updateGeneralText(String mFilterText) {
	}

	@Override
	public void updateCancel() {
		FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updateCancel();
	}

	@Override
	public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
		FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
	}

	public void numberPressed(View vw) {
		FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.numberPressed(vw);
	}

	@Override
	public void updateDate(Date date, String tag) {
		PromotionTrackingFragment checkModeFragment = (PromotionTrackingFragment) getSupportFragmentManager().findFragmentByTag("promotiontracking");
		if (checkModeFragment != null)
			checkModeFragment.updateDate(date, tag);
	}
}