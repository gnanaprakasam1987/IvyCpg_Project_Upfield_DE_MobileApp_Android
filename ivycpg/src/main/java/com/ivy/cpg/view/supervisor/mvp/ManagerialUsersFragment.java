package com.ivy.cpg.view.supervisor.mvp;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.customviews.MultiLevelRecyclerView;
import com.ivy.cpg.view.supervisor.mvp.models.ManagerialBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramkumard on 27/2/19.
 * To display List of User's and their Child User's
 */

public class ManagerialUsersFragment extends Fragment {

    private View view;
    private MultiLevelRecyclerView recyclerView;
    private BusinessModel bmodel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_managerial_users, container, false);
        recyclerView = view.findViewById(R.id.user_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        prepareData();
        recyclerView.removeItemClickListeners();
        recyclerView.setToggleItemOnClick(false);

        recyclerView.setAccordion(false);

        return view;
    }

    private void prepareData() {
        if (bmodel.userMasterHelper.getUserHierarchyList() != null && !bmodel.userMasterHelper.getUserHierarchyList().isEmpty())
        recyclerView.setAdapter(new MyAdapter(getActivity(), bmodel.userMasterHelper.getUserHierarchyList(), recyclerView));
    }

    public class MyAdapter extends MultiLevelAdapter {

        private Holder mViewHolder;
        private Context mContext;
        private List<ManagerialBO> mListItems = new ArrayList<>();
        private MultiLevelRecyclerView mMultiLevelRecyclerView;

        MyAdapter(Context mContext, List<ManagerialBO> mListItems, MultiLevelRecyclerView mMultiLevelRecyclerView) {
            super(mListItems);
            this.mListItems = mListItems;
            this.mContext = mContext;
            this.mMultiLevelRecyclerView = mMultiLevelRecyclerView;
            if (mListItems != null && mListItems.size() > 0 && bmodel.userMasterHelper.getmPrevSelectedItem() == null) {
                bmodel.userMasterHelper.setmPrevSelectedItem(mListItems.get(0));
            }

        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.managerial_users_list_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            mViewHolder = (Holder) holder;
            mViewHolder.mItem = mListItems.get(position);

            mViewHolder.tv_user.setText(mViewHolder.mItem.getUserLevel());

            if (mViewHolder.mItem.hasChildren() && mViewHolder.mItem.getChildren().size() > 0) {
                mViewHolder.img_dropdown.setVisibility(View.VISIBLE);
            } else {
                mViewHolder.img_dropdown.setVisibility(View.GONE);
            }

            mViewHolder.img_dropdown.animate().rotation(mViewHolder.mItem.isExpanded() ? 90 : 0).start();

            if (mViewHolder.mItem.isSelected())
                mViewHolder.img_selected.setVisibility(View.VISIBLE);
            else
                mViewHolder.img_selected.setVisibility(View.GONE);

            float density = mContext.getResources().getDisplayMetrics().density;
            ((ViewGroup.MarginLayoutParams) mViewHolder.parentLayout.getLayoutParams()).leftMargin = (int) (((getItemViewType(position) == 0 ? 0.5 : getItemViewType(position)) * 20) * density + 0.5f);
        }

        private class Holder extends RecyclerView.ViewHolder {

            TextView tv_user;
            ImageView img_dropdown;
            ImageView img_selected;
            RelativeLayout parentLayout;
            ManagerialBO mItem;

            Holder(View itemView) {
                super(itemView);
                tv_user = itemView.findViewById(R.id.tv_user);
                img_dropdown = itemView.findViewById(R.id.img_dropdown);
                img_selected = itemView.findViewById(R.id.img_selected);
                parentLayout = itemView.findViewById(R.id.parent_layout);

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mMultiLevelRecyclerView.toggleItemsGroup(getAdapterPosition());
                        ManagerialBO managerialBO = mListItems.get(getAdapterPosition());

                        if (managerialBO.isChild() && !managerialBO.hasChildren()
                                && managerialBO.getChildren() == null) {

                            if (!managerialBO.isSelected()) {
                                img_selected.setVisibility(View.VISIBLE);
                                managerialBO.setSelected(true);
                                updateParentViews(managerialBO);
                                bmodel.userMasterHelper.setmPrevSelectedItem(managerialBO);
                            }
                            notifyDataSetChanged();
                        } else if (!managerialBO.isChild() && managerialBO.hasChildren()
                                && managerialBO.getChildren() != null) {

                            img_dropdown.animate().rotation(managerialBO.isExpanded() ? 90 : 0).start();
                        }
                    }
                });

            }
        }

        private void updateParentViews(ManagerialBO managerialBO) {

            ManagerialBO prevBO = bmodel.userMasterHelper.getmPrevSelectedItem();
            while (prevBO != null && prevBO.getLevel() >= 1 && !managerialBO.equals(prevBO)
                    && mMultiLevelRecyclerView.getParentOfItem(prevBO) != null) {
                prevBO.setSelected(false);
                mMultiLevelRecyclerView.getParentOfItem(prevBO).setSelected(false);
                prevBO = mMultiLevelRecyclerView.getParentOfItem(prevBO);
            }

            while (prevBO != null && managerialBO.getLevel() >= 1 && !managerialBO.equals(prevBO)
                    && mMultiLevelRecyclerView.getParentOfItem(managerialBO) != null) {
                mMultiLevelRecyclerView.getParentOfItem(managerialBO).setSelected(true);
                managerialBO = mMultiLevelRecyclerView.getParentOfItem(managerialBO);
            }

        }

    }

}
