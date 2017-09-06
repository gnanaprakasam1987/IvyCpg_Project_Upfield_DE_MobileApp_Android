package com.ivyretail.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PromotionTrackingActivity extends IvyBaseActivityNoActionBar implements
		BrandDialogInterface {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_promotiontracking);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			setSupportActionBar(toolbar);
			if(getSupportActionBar() != null)
				getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
		}
		BusinessModel bmodel = (BusinessModel) this.getApplicationContext();
		bmodel.setContext(this);
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
	public void updatebrandtext(String filtertext, int id) {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updatebrandtext(filtertext, id);
	}

	@Override
	public void updategeneraltext(String filtertext) {
	}

	@Override
	public void updateCancel() {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updateCancel();
	}

	@Override
	public void loadStartVisit() {
	}

	@Override
	public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
	}

	@Override
	public void updateMultiSelectionBrand(List<String> filtername,
			List<Integer> filterid) {
	}

	@Override
	public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updatefromFiveLevelFilter(parentidList);

	}
	@Override
	public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId,ArrayList<Integer> mAttributeProducts,String filtertext) {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.updatefromFiveLevelFilter(parentidList,mSelectedIdByLevelId,mAttributeProducts,filtertext);
	}

	public void numberPressed(View vw) {
		android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
		PromotionTrackingFragment asf = (PromotionTrackingFragment) fm
				.findFragmentById(R.id.promotion_tracking_fragment);
		asf.numberPressed(vw);
	}
}