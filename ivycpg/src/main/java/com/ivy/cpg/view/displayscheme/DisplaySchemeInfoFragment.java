package com.ivy.cpg.view.displayscheme;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by Rajkumar on 3/1/18.
 * Display scheme Info
 */

public class DisplaySchemeInfoFragment extends IvyBaseFragment {

    BusinessModel businessModel;
    View rootView;
    String mSelectedSchemeId;
    RecyclerView listView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display_scheme_info, container,
                false);

        businessModel = (BusinessModel) getActivity().getApplicationContext();
        businessModel.setContext(getActivity());


        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            mSelectedSchemeId = extras.getString("schemeId");
        }

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews();
    }

    private void initializeViews() {

        try {
            if (getView() != null) {
                TextView label_display_period =  getView().findViewById(R.id.label_display_period);
                TextView label_booking_period =  getView().findViewById(R.id.label_booking_period);
                TextView label_qualifier =  getView().findViewById(R.id.label_qualifiers);

                label_display_period.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                label_booking_period.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                label_qualifier.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

                TextView textView_scheme_desc =  getView().findViewById(R.id.text_scheme_desc);
                textView_scheme_desc.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                TextView textView_display_period =  getView().findViewById(R.id.text_display_period);
                TextView textView_booking_period =  getView().findViewById(R.id.text_booking_period);
                TextView textView_qualifier =  getView().findViewById(R.id.text_qualifiers);
                textView_display_period.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                textView_booking_period.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                textView_qualifier.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

                SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext());
                for (SchemeBO schemeBO : schemeHelper.getDisplaySchemeMasterList()) {
                    if (schemeBO.getSchemeId().equals(mSelectedSchemeId)) {
                        textView_scheme_desc.setText(schemeBO.getScheme());
                        textView_display_period.setText(schemeBO.getDisplayPeriodStart() + " - " + schemeBO.getDisplayPeriodEnd());
                        textView_booking_period.setText(schemeBO.getBookingPeriodStart() + " - " + schemeBO.getBookingPeriodEnd());
                        textView_qualifier.setText(schemeBO.getQualifier());
                    }
                }

                getView().findViewById(R.id.view_dotted).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                getView().findViewById(R.id.view_dotted_line).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                TextView text_products_label =  getView().findViewById(R.id.text_product_title);
                text_products_label.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                listView =  getView().findViewById(R.id.list);
                listView.setHasFixedSize(true);
                final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                listView.setLayoutManager(mLayoutManager);

                ArrayList<String> mProductList = SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext()).downloadDisplaySchemeProducts(getActivity().getApplicationContext(), mSelectedSchemeId);

                RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(mProductList);
                listView.setAdapter(mAdapter);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<String> items;

        public RecyclerViewAdapter(ArrayList<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_display_scheme_info, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.text_product_name.setText(items.get(position));

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text_product_name;

            public ViewHolder(View v) {
                super(v);
                text_product_name =  v.findViewById(R.id.text_product_name);

                text_product_name.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

            }


        }
    }
}
