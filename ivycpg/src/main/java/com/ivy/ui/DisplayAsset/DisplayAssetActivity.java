package com.ivy.ui.DisplayAsset;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompanyBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;

public class DisplayAssetActivity extends IvyBaseActivityNoActionBar implements DisplayAssetContractor.View {

    private ExpandableListView expandableListView;
    private DisplayAssetHelper displayAssetHelper;
    private Toolbar toolbar;
    private TextView label_company_name, textview_company_count, textview_other_count;
    private LinearLayout layout_status;
    private TextView textView_status;
    DisplayAssetPresenterImpl presenter;
    BusinessModel businessModel;
    Button button_save;
    int lastExpandedGroup = -1;
    private String expositionStatus = "";
    AppCompatRadioButton expoStatusYes;
    AppCompatRadioButton expoStatusNo;
    AppCompatRadioButton expoStatusNone;
    private final String yesStatus="YES";
    private final String noStatus="N0";
    private final String noneStatus="NOT APPLICABLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_asset);

        businessModel = (BusinessModel) getApplicationContext();
        businessModel.setContext(this);
        String menuName = "";
        if (getIntent().getExtras() != null)
            menuName = getIntent().getExtras().getString("menuName");

        toolbar = findViewById(R.id.toolbar);

        label_company_name = findViewById(R.id.label_company_name);
        textview_company_count = findViewById(R.id.textview_company_count);
        textview_other_count = findViewById(R.id.textview_other_count);

        layout_status = findViewById(R.id.layout_status);
        textView_status = findViewById(R.id.textview_status);
        button_save = findViewById(R.id.btn_save);
        expoStatusYes = findViewById(R.id.expo_status_yes);
        expoStatusNo = findViewById(R.id.expo_status_no);
        expoStatusNone = findViewById(R.id.expo_status_none);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                setScreenTitle(menuName);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
        }

        displayAssetHelper = DisplayAssetHelper.getInstance(this);
        presenter = new DisplayAssetPresenterImpl(displayAssetHelper);
        presenter.setView(this);

        expandableListView = findViewById(R.id.listview_assets);
        expandableListView.setAdapter(new MyAdapter(displayAssetHelper.getDisplayAssetList()));
        presenter.refreshStatus();
        expositionStatus = presenter.getLastExposStatus(this);
        int size = displayAssetHelper.getDisplayAssetList().size();
        if (size > 0) {
            expandableListView.expandGroup(0);
        }
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int groupPosition) {
                if (lastExpandedGroup != -1
                        && groupPosition != lastExpandedGroup) {
                    expandableListView.collapseGroup(lastExpandedGroup);
                }
                lastExpandedGroup = groupPosition;
            }
        });
        if (!expositionStatus.isEmpty())
            updateExpositionStatusInEditMode();

        ((RadioGroup) findViewById(R.id.rg_expo_status)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.expo_status_yes:
                        expoStatusNo.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_yes_grey));
                        expoStatusNone.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_yes_grey));

                        expoStatusYes.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.green_productivity));
                        expositionStatus = yesStatus;
                        break;
                    case R.id.expo_status_no:
                        expoStatusYes.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_yes_grey));
                        expoStatusNone.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_yes_grey));

                        expoStatusNo.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_no_red));
                        expositionStatus = noStatus;
                        break;
                    case R.id.expo_status_none:
                        expoStatusYes.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_yes_grey));
                        expoStatusNo.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_yes_grey));

                        expoStatusNone.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.dark_red));
                        expositionStatus = noneStatus;
                        break;
                }
            }
        });
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (presenter.saveDisplayAssets(DisplayAssetActivity.this, expositionStatus)) {
                    Toast.makeText(DisplayAssetActivity.this, getResources().getString(R.string.saved_successfully), Toast.LENGTH_LONG).show();

                } else {
                    Toast.makeText(DisplayAssetActivity.this, getResources().getString(R.string.error_in_saving), Toast.LENGTH_LONG).show();
                }
                startActivity(new Intent(DisplayAssetActivity.this,
                        HomeScreenTwo.class));
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        });
    }


    private void updateExpositionStatusInEditMode() {
        switch (expositionStatus) {
            case yesStatus:
                expoStatusYes.setChecked(true);
                expoStatusYes.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.green_productivity));
                break;
            case noStatus:
                expoStatusNo.setChecked(true);
                expoStatusNo.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.plano_no_red));
                break;
            case noneStatus:
                expoStatusNone.setChecked(true);
                expoStatusNone.setTextColor(ContextCompat.getColor(DisplayAssetActivity.this, R.color.dark_red));
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            Intent intent = new Intent(this, HomeScreenTwo.class);
            if (getIntent().getBooleanExtra("PreVisit", false))
                intent.putExtra("PreVisit", true);

            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }

        return super.onOptionsItemSelected(item);
    }

    class MyAdapter extends BaseExpandableListAdapter {

        ArrayList<AssetTrackingBO> assetList;

        public MyAdapter(ArrayList<AssetTrackingBO> assetList) {
            this.assetList = assetList;
        }

        @Override
        public Object getChild(int arg0, int arg1) {

            ArrayList<CompanyBO> companyList = assetList.get(arg0).getCompanyList();

            return companyList.get(arg1);
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;


            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_display_asset_child,
                        parent, false);
                holder = new ViewHolder();
                holder.textView_companyName = row.findViewById(R.id.textview_company_name);
                holder.imageView_minus = row.findViewById(R.id.imiage_minus);
                holder.imageView_plus = row.findViewById(R.id.image_plus);
                holder.editText_quantity = row.findViewById(R.id.image_quantity);


                holder.imageView_minus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        int value = Integer.parseInt(holder.editText_quantity.getText().toString());
                        if (value > 0) {
                            value -= 1;
                        } else
                            value = 0;

                        CompanyBO companyBO = (CompanyBO) getChild(groupPosition, childPosition);
                        companyBO.setQuantity(value);

                        holder.editText_quantity.setText(String.valueOf(value));

                        presenter.refreshStatus();
                    }
                });
                holder.imageView_plus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int value = Integer.parseInt(holder.editText_quantity.getText().toString());
                        value += 1;
                        if (value < 0) {
                            value = 0;
                        }

                        CompanyBO companyBO = (CompanyBO) getChild(groupPosition, childPosition);
                        companyBO.setQuantity(value);

                        holder.editText_quantity.setText(String.valueOf(value));

                        presenter.refreshStatus();
                    }
                });

               /* holder.editText_quantity.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                        try {
                            int qty = Integer.valueOf(editable.toString());
                            CompanyBO companyBO=(CompanyBO)getChild(groupPosition,childPosition);
                            companyBO.setQuantity(qty);
                        }
                        catch (Exception ex){
                            Commons.printException(ex);
                        }

                        presenter.refreshStatus();
                    }
                });*/


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            CompanyBO companyBO = (CompanyBO) getChild(groupPosition, childPosition);
            holder.textView_companyName.setText(companyBO.getCompetitorName());
            holder.editText_quantity.setText(String.valueOf(companyBO.getQuantity()));

            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {


            return assetList.get(groupPosition).getCompanyList().size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public int getGroupCount() {

            return assetList.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_simple_textview,
                        parent, false);
                holder = new ViewHolder();
                holder.textView_assetName = row.findViewById(R.id.texview_asset_name);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.textView_assetName.setText(assetList.get(groupPosition).getAssetName());

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

    }

    class ViewHolder {
        TextView textView_assetName, textView_companyName;
        Button imageView_minus;
        Button imageView_plus;
        EditText editText_quantity;

    }

    @Override
    public void updateStatus(String companyName, double ownCompanyWeightage, double otherCompanyMaxWeightage, int flag) {

        if (!companyName.equals(""))
            label_company_name.setText(companyName);
        else label_company_name.setText("Own");
        textview_company_count.setText(String.valueOf(ownCompanyWeightage));
        textview_other_count.setText(String.valueOf(otherCompanyMaxWeightage));

        layout_status.setVisibility(View.VISIBLE);
        if (flag == 1) {
            layout_status.setBackground(getResources().getDrawable(R.color.green_productivity));
            textView_status.setText(getResources().getString(R.string.advantage));
        } else if (flag == 2) {
            layout_status.setBackground(getResources().getDrawable(R.color.colorPrimaryOrange));
            textView_status.setText(getResources().getString(R.string.equal));
        } else if (flag == 3) {
            layout_status.setBackground(getResources().getDrawable(R.color.colorPrimaryRed));
            textView_status.setText(getResources().getString(R.string.dis_advantage));
        } else {
            layout_status.setVisibility(View.GONE);
        }

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            expandableListView.setIndicatorBounds(expandableListView.getRight() - 100, expandableListView.getWidth());
        } else {
            expandableListView.setIndicatorBoundsRelative(expandableListView.getRight() - 100, expandableListView.getWidth());
        }

    }
}
