package com.ivy.cpg.view.supplierselect;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

public class SupplierSelectionActivity extends IvyBaseActivityNoActionBar {

    private ExpandableListView lstSupplier;
    private BusinessModel bmodel;
    private ArrayList<SupplierMasterBO> mSupplierList = new ArrayList<>();
    private ArrayList<String> lst_group;
    private ArrayList<List<SupplierMasterBO>> lst_child;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.supplier_selection_dialog);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null ) {

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayShowTitleEnabled(false);
//            // Used to on / off the back arrow icon
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            setScreenTitle(getResources().getString(R.string.select_supplier));
        }

        bmodel = (BusinessModel) getApplicationContext();

        mSupplierList = getIntent().getParcelableArrayListExtra("SupplierList");

        lstSupplier = findViewById(R.id.lst_supplier);
        //Prepare Data

        try {
            if (!bmodel.configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE
                    && mSupplierList.size() > 0
                    && mSupplierList.get(0).getIsPrimary() == 1) {

                ArrayList<SupplierMasterBO> primarySupplier = new ArrayList<>();
                ArrayList<SupplierMasterBO> secondarySupplier = new ArrayList<>();

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
                    lst_group.add(getString(R.string.primary_supplier));
                    lst_child.add(primarySupplier);
                }
                if (secondarySupplier.size() > 0) {
                    lst_group.add(getString(R.string.secondary_supplier));
                    lst_child.add(secondarySupplier);
                }

                refreshView();

                for (int i = 0; i < lst_group.size(); i++)
                    lstSupplier.expandGroup(i);

            } else {
                lstSupplier.setGroupIndicator(null);
                refreshView();
            }


        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private void refreshView() {
        Myadapter adapter = new Myadapter();
        lstSupplier.setAdapter(adapter);
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
            List<SupplierMasterBO> list = lst_child.get(groupPosition);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_supplier_selection,
                        parent, false);
                childHolder = new ChildViewHolder();
                childHolder.tv_supplier = row.findViewById(R.id.tv_supplier);
                childHolder.chk = row.findViewById(R.id.chk_selected);
                childHolder.chk.setClickable(false);

                childHolder.tv_supplier.setTypeface(FontUtils.getFontRoboto(SupplierSelectionActivity.this, FontUtils.FontType.MEDIUM));

                childHolder.tv_supplier.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {


                        bmodel.getRetailerMasterBO().setSupplierBO(childHolder.childList.get(childHolder.childPosition));
                        bmodel.getRetailerMasterBO().setDistributorId(childHolder.childList.get(childHolder.childPosition).getSupplierID());
                        bmodel.getRetailerMasterBO().setDistParentId(childHolder.childList.get(childHolder.childPosition).getDistParentID());
                        bmodel.getRetailerMasterBO().setSupplierTaxLocId(childHolder.childList.get(childHolder.childPosition).getSupplierTaxLocId());
                        bmodel.updateRetailerWiseSupplierType(childHolder.childList.get(childHolder.childPosition).getSupplierID());

                        setResult(1,getIntent().putExtra("SupplierName",childHolder.childList.get(childHolder.childPosition).getSupplierName()));
                        finish();
                    }
                });


                row.setTag(childHolder);
            } else {
                childHolder = (ChildViewHolder) row.getTag();
            }
            childHolder.childPosition = childPosition;
            childHolder.childList = list;
            childHolder.tv_supplier.setText(childHolder.childList.get(childHolder.childPosition).getSupplierName());

            if (bmodel.getRetailerMasterBO().getDistributorId() == childHolder.childList.get(childHolder.childPosition).getSupplierID()) {
                childHolder.chk.setChecked(true);
            } else {
                childHolder.chk.setChecked(false);
            }


            return row;
        }

        @Override
        public View getGroupView(final int position, boolean b, View view, ViewGroup viewGroup) {

            final GroupViewHolder groupHolder;

            View row = view;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_supplier_selection_header,
                        viewGroup, false);
                groupHolder = new GroupViewHolder();
                groupHolder.tv_header = row.findViewById(R.id.tv_header);
                groupHolder.tv_supplier = row.findViewById(R.id.tv_supplier);
                groupHolder.chk = row.findViewById(R.id.chk_selected);
                groupHolder.chk.setClickable(false);

                if (!bmodel.configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE
                        && mSupplierList.size() > 0
                        && mSupplierList.get(0).getIsPrimary() == 1) {
                    groupHolder.tv_supplier.setVisibility(View.GONE);
                    groupHolder.chk.setVisibility(View.GONE);

                } else {
                    groupHolder.tv_header.setVisibility(View.GONE);
                    groupHolder.chk.setVisibility(View.VISIBLE);
                }

                groupHolder.tv_supplier.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bmodel.getRetailerMasterBO().setSupplierBO(mSupplierList.get(position));
                        bmodel.getRetailerMasterBO().setDistributorId(mSupplierList.get(position).getSupplierID());
                        bmodel.getRetailerMasterBO().setDistParentId(mSupplierList.get(position).getDistParentID());
                        bmodel.getRetailerMasterBO().setSupplierTaxLocId(mSupplierList.get(position).getSupplierTaxLocId());
                        bmodel.updateRetailerWiseSupplierType(mSupplierList.get(position)
                                .getSupplierID());

                        setResult(1,getIntent().putExtra("SupplierName",mSupplierList.get(position).getSupplierName()));
                        finish();
                    }
                });
                row.setTag(groupHolder);
            } else {
                groupHolder = (GroupViewHolder) row.getTag();

            }

            if (!bmodel.configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE
                    && mSupplierList.size() > 0
                    && mSupplierList.get(0).getIsPrimary() == 1) {
                groupHolder.tv_header.setText(lst_group.get(position));
            } else {

                groupHolder.tv_supplier.setText(mSupplierList.get(position).getSupplierName());

                if (bmodel.getRetailerMasterBO().getDistributorId() == mSupplierList.get(position).getSupplierID()) {
                    groupHolder.chk.setChecked(true);
                } else {
                    groupHolder.chk.setChecked(false);
                }
            }

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
            return lst_group == null ? mSupplierList.size() : lst_group.size();
        }

        @Override
        public long getGroupId(int i) {
            return 0;
        }

        @Override
        public Object getGroup(int i) {
            return lst_group == null ? mSupplierList.get(i) : lst_group.get(i);
        }

        @Override
        public int getChildrenCount(int i) {
            return lst_child.get(i).size();
        }
    }

    class GroupViewHolder {
        TextView tv_header, tv_supplier;
        CheckBox chk;
    }

    class ChildViewHolder {
        TextView tv_supplier;
        List<SupplierMasterBO> childList;
        CheckBox chk;
        int childPosition;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
