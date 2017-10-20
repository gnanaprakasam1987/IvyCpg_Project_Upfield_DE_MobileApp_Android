package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajkumar.s on 11/30/2016.
 */

public class SupplierSelectionDialog extends DialogFragment {

    ExpandableListView lstSupplier;
    BusinessModel bmodel;
    ArrayList<SupplierMasterBO> mSupplierList,primarySupplier,secondarySupplier;
    Context mContext;
    ArrayList<String> lst_group;
    ArrayList<List<SupplierMasterBO>> lst_child;
    public  SupplierSelectionDialog(){

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.supplier_selection_dialog, container,
                false);
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            getDialog().setTitle(getResources().getString(R.string.select_supplier));
            lstSupplier = (ExpandableListView) getView().findViewById(R.id.lst_supplier);

            mSupplierList = bmodel.downloadSupplierDetails();

            primarySupplier = new ArrayList<>();
            secondarySupplier = new ArrayList<>();
            for (int i = 0; i < mSupplierList.size(); i++) {

                if (mSupplierList.get(i).getIsPrimary() == 1) {
                    primarySupplier.add(mSupplierList.get(i));

                } else {
                    secondarySupplier.add(mSupplierList.get(i));

                }
            }

            lst_group = new ArrayList<>();
            lst_child = new ArrayList<>();
            if (primarySupplier.size() > 0) {
                lst_group.add(getResources().getString(R.string.primary_supplier));
                lst_child.add(primarySupplier);
            }
            if (secondarySupplier.size() > 0) {
                lst_group.add(getResources().getString(R.string.secondary_supplier));
                lst_child.add(secondarySupplier);
            }


            Myadapter adapter = new Myadapter();
            lstSupplier.setAdapter(adapter);

            for (int i = 0; i < lst_group.size(); i++)
                lstSupplier.expandGroup(i);
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }


    private class Myadapter extends BaseExpandableListAdapter {
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return lst_child.get(groupPosition).get(childPosition);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }
        @Override
        public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            final ChildViewHolder childHolder;
            List<SupplierMasterBO> list=lst_child.get(groupPosition);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_supplier_selection,
                        parent, false);
                childHolder = new ChildViewHolder();
                childHolder.tv_supplier=(TextView)row.findViewById(R.id.tv_supplier) ;
                childHolder.chk=(CheckBox) row.findViewById(R.id.chk_selected) ;
                childHolder.chk.setClickable(false);
                childHolder.tv_supplier.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        bmodel.getRetailerMasterBO().setSupplierBO(childHolder.childList.get(childHolder.childPosition));
                        bmodel.getRetailerMasterBO().setDistributorId(childHolder.childList.get(childHolder.childPosition).getSupplierID());
                        bmodel.getRetailerMasterBO().setDistParentId(childHolder.childList.get(childHolder.childPosition).getDistParentID());
                        bmodel.updateRetailerWiseSupplierType(childHolder.childList.get(childHolder.childPosition)
                                .getSupplierID());
                        getDialog().dismiss();
                    }
                });


                row.setTag(childHolder);
            } else {
                childHolder = (ChildViewHolder) row.getTag();
            }
            childHolder.childPosition=childPosition;
            childHolder.childList=list;
            childHolder.tv_supplier.setText(childHolder.childList.get(childHolder.childPosition).getSupplierName());

              if(bmodel.getRetailerMasterBO().getDistributorId()==childHolder.childList.get(childHolder.childPosition).getSupplierID()) {
                  childHolder.chk.setVisibility(View.VISIBLE);
                  childHolder.chk.setChecked(true);
              }
            else{
                  childHolder.chk.setVisibility(View.GONE);
              }


            return row;
        }

        @Override
        public View getGroupView(int position, boolean b, View view, ViewGroup viewGroup) {

            GroupViewHolder groupHolder;
            View row = view;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_supplier_selection_header,
                        viewGroup, false);
                groupHolder = new GroupViewHolder();
                groupHolder.tv_header=(TextView)row.findViewById(R.id.tv_supplier) ;
               // groupHolder.tv_header.setGravity(Gravity.LEFT);
                groupHolder.tv_header.setTextColor(getResources().getColor(R.color.Black));
                groupHolder.tv_header.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_large));


                row.setTag(groupHolder);
            } else {
                groupHolder = (GroupViewHolder) row.getTag();
            }
                //lstSupplier.expandGroup(position);
                groupHolder.tv_header.setText(lst_group.get(position));


            return row;
        }
        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        @Override
        public int getGroupCount() {
            return lst_group.size();
        }

        @Override
        public long getGroupId(int i) {
            return 0;
        }

        @Override
        public Object getGroup(int i) {
            return lst_group.get(i);
        }

        @Override
        public int getChildrenCount(int i) {
            return lst_child.get(i).size();
        }
    }

    class GroupViewHolder {
        TextView tv_header;

    }

    class ChildViewHolder {
        TextView tv_supplier;
       List<SupplierMasterBO> childList;
        CheckBox chk;
        int childPosition;

    }

}
