package com.ivy.cpg.view.displayscheme;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.order.scheme.SchemeBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FreeProductsDialogFragment;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by Rajkumar on 3/1/
 * Display scheme slabs
 */

public class DisplaySchemeSlabFragment extends IvyBaseFragment {

    View rootView;
    BusinessModel businessModel;
    RecyclerView recyclerView;
    String mSelectedSchemeId;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_display_scheme_slab, container,
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
                recyclerView =  getView().findViewById(R.id.list);
                recyclerView.setHasFixedSize(true);
                final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);

                ArrayList<SchemeBO> mSlabList = new ArrayList<>();
                for (SchemeBO bo : SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext()).getDisplaySchemeSlabs()) {
                    if (String.valueOf(bo.getParentId()).equals(mSelectedSchemeId)) {
                        mSlabList.add(bo);
                    }
                }
                RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(mSlabList);
                recyclerView.setAdapter(mAdapter);

                Button button_done =  getView().findViewById(R.id.done);
                button_done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        getActivity().finish();
                    }
                });
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

        private ArrayList<SchemeBO> items;

        public RecyclerViewAdapter(ArrayList<SchemeBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_display_scheme_slabs, parent, false);
            v.findViewById(R.id.view_dotted_line).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {
            final SchemeBO schemeBO = items.get(position);

            holder.text_slab_name.setText(schemeBO.getScheme());
            if (schemeBO.getGetType().equalsIgnoreCase("QTY")) {
                holder.imageView_free_products.setVisibility(View.VISIBLE);
                holder.text_value.setVisibility(View.GONE);
                holder.text_type.setText(getResources().getString(R.string.free_products));
            } else {
                holder.imageView_free_products.setVisibility(View.GONE);
                holder.text_value.setVisibility(View.VISIBLE);

                if (schemeBO.getGetType().equalsIgnoreCase("VALUE")) {
                    holder.text_type.setText(getResources().getString(R.string.amount));
                } else if (schemeBO.getGetType().equalsIgnoreCase("PER")) {
                    holder.text_type.setText(getResources().getString(R.string.percentage));
                } else if (schemeBO.getGetType().equalsIgnoreCase("EPER")) {
                    holder.text_type.setText(getResources().getString(R.string.percentage));
                } else if (schemeBO.getGetType().equalsIgnoreCase("PRICE")) {
                    holder.text_type.setText(getResources().getString(R.string.price));
                }
            }
            holder.text_value.setText(schemeBO.getDisplaySchemeValue());
            if (schemeBO.isSchemeSelected()) {
                holder.imageView_Available.setVisibility(View.VISIBLE);
            } else {
                holder.imageView_Available.setVisibility(View.GONE);
            }

            holder.imageView_free_products.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (schemeBO.getFreeProducts() != null && schemeBO.getFreeProducts().size() > 0) {
                        FragmentManager fm = getActivity().getSupportFragmentManager();
                        FreeProductsDialogFragment dialogFragment = new FreeProductsDialogFragment(schemeBO);
                        dialogFragment.setCancelable(false);
                        dialogFragment.show(fm, "free_products");
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
                    }

                }
            });

            holder.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (schemeBO.isSchemeSelected()) {
                        schemeBO.setSchemeSelected(false);
                        holder.imageView_Available.setVisibility(View.GONE);
                    } else {
                        schemeBO.setSchemeSelected(true);
                        holder.imageView_Available.setVisibility(View.VISIBLE);
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView text_slab_name, text_type, text_value;
            TextView label_type, label_value;
            ImageView imageView_free_products;
            CardView cardView;
            ImageView imageView_Available;

            public ViewHolder(View v) {
                super(v);
                text_slab_name =  v.findViewById(R.id.text_slab_name);
                text_type =  v.findViewById(R.id.text_type);
                text_value =  v.findViewById(R.id.text_value);
                label_type =  v.findViewById(R.id.label_type);
                label_value =  v.findViewById(R.id.label_value);
                imageView_free_products =  v.findViewById(R.id.imageView_free_products);
                imageView_Available =  v.findViewById(R.id.ivAvailable);

                cardView =  v.findViewById(R.id.card);

                text_slab_name.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                text_type.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                text_value.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

                label_type.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                label_value.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

            }


        }
    }
}
