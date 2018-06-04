package com.ivy.cpg.view.supervisor;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class OutletPagerDialogFragment extends DialogFragment {
    @Nullable

    private View rootView;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private CircleIndicator circleIndicator;
    private TextView tvStoreCount;
    private BusinessModel bmodel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getContext().getApplicationContext();

    }

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.outlet_pager_dialog_fragment, container, false);

        initViews(rootView);


        rootView.findViewById(R.id.close_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }

    private void initViews(View rootView){

        tvStoreCount = rootView.findViewById(R.id.tv_store_count);

        tvStoreCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        viewPager = rootView.findViewById(R.id.outlet_view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getContext(), new ArrayList<String>());
        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();

        circleIndicator = rootView.findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);

    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog().getWindow()!=null)
            getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

    }

    private class ViewPagerAdapter extends PagerAdapter {

        private Context mContext;
        private List<String> strings = new ArrayList<>();

        public ViewPagerAdapter(Context context, List<String> strings) {
            mContext = context;
            this.strings = strings;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.supervisor_outlet_dialog_layout, container, false);

            TextView tvStoreName,tvStoreAddress,tvVisitStatus,tvInTime,tvOutTime,tvDuration,tvOrderValue;

            tvStoreName = view.findViewById(R.id.tv_store_name);
            tvStoreAddress = view.findViewById(R.id.tv_address);
            tvVisitStatus = view.findViewById(R.id.tv_status_text);
            tvInTime = view.findViewById(R.id.tv_in_time_val);
            tvOutTime = view.findViewById(R.id.tv_out_time_val);
            tvDuration = view.findViewById(R.id.tv_duration_val);
            tvOrderValue = view.findViewById(R.id.tv_order_value);

            ((TextView)view.findViewById(R.id.tv_intime_txt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView)view.findViewById(R.id.tv_outtime_txt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView)view.findViewById(R.id.tv_duration_txt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            ((TextView)view.findViewById(R.id.tv_order_val_txt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            tvStoreName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tvStoreAddress.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            tvVisitStatus.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            tvInTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            tvOutTime.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            tvDuration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
            tvOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            container.addView(view);
            return view;
        }
    }

}
