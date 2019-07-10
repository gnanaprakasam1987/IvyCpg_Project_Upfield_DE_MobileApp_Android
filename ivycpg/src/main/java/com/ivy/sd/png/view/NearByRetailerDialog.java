package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;

import androidx.core.view.MenuItemCompat;
import androidx.appcompat.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by rajkumar.s on 01-03-2016.
 */
public class NearByRetailerDialog extends Dialog implements SearchView.OnQueryTextListener {

    BusinessModel bmodel;
    ListView listView;
    Button mDoneBTN;
    Context ctx;
    androidx.appcompat.widget.Toolbar toolbar;
    Vector<RetailerMasterBO> retailersList;
    Vector<RetailerMasterBO> tempRetailer = new Vector<>();
    SearchView searchView;

    public NearByRetailerDialog(Context context, final BusinessModel bmodel, Vector<RetailerMasterBO> retailers, Vector<RetailerMasterBO> mSelectedRetailers) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nearby_retailer_dialog);
        this.bmodel = bmodel;
        ctx = context;
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_dialog);
        toolbar.getMenu().findItem(R.id.menu_close).setVisible(false);
        toolbar.getMenu().findItem(R.id.menu_done).setVisible(false);
        toolbar.getMenu().findItem(R.id.search).setVisible(true);
        searchView = (SearchView) MenuItemCompat.getActionView(toolbar.getMenu().findItem(R.id.search));
        setSearchTextColour(searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Retailer");
        searchView.setIconifiedByDefault(false);

        mDoneBTN = (Button) findViewById(R.id.btn_done);
        mDoneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vector<RetailerMasterBO> list = getSelectedRetailers();
                if (list.size() <= bmodel.configurationMasterHelper.VALUE_NEARBY_RETAILER_MAX) {
                    NearByRetailerInterface activity = (NearByRetailerInterface) ctx;
                    activity.updateNearByRetailer(list);
                    dismiss();
                } else {
                    Toast.makeText(ctx, "Only " + bmodel.configurationMasterHelper.VALUE_NEARBY_RETAILER_MAX + " retailer allowed.", Toast.LENGTH_LONG).show();
                }

            }
        });

        ImageView searchBtn = (ImageView) searchView
                .findViewById(R.id.search_button);
        searchBtn.setImageResource(R.drawable.icon_search);

        // style Clear Btn
        ImageView close_btn = (ImageView) searchView
                .findViewById(R.id.search_close_btn);
        close_btn.setImageResource(R.drawable.ic_clear_red);
        close_btn.setVisibility(View.GONE);

        listView = (ListView) findViewById(R.id.lv_retailers);
        initializeRetailers(retailers, mSelectedRetailers);
    }


    public NearByRetailerDialog(Context context, final int VALUE_NEARBY_RETAILER_MAX, Vector<RetailerMasterBO> retailers, Vector<RetailerMasterBO> mSelectedRetailers) {
        super(context);
        initDialog(context, VALUE_NEARBY_RETAILER_MAX);
        initializeRetailers(retailers, mSelectedRetailers);
    }

    public NearByRetailerDialog(Context context, final int VALUE_NEARBY_RETAILER_MAX, Vector<RetailerMasterBO> retailers, ArrayList<String> mSelectedRetailers) {
        super(context);
        initDialog(context, VALUE_NEARBY_RETAILER_MAX);
        initializeRetailers(retailers, mSelectedRetailers);
    }

    private void initializeRetailers(Vector<RetailerMasterBO> retailers, ArrayList<String> selectedRetailer) {
        if (selectedRetailer != null) {

            for (RetailerMasterBO bo : retailers) {
                for (String selectedBo : selectedRetailer) {
                    if (bo.getRetailerID().equals(selectedBo)) {
                        bo.setIsNearBy(true);
                    }

                }
            }
        }

        initAdapter(retailers);

    }

    private void initAdapter(Vector<RetailerMasterBO> retailers) {
        retailersList = retailers;
        MyAdapter adapter = new MyAdapter(retailersList);
        listView.setAdapter(adapter);
    }


    private void initializeRetailers(Vector<RetailerMasterBO> retailers, Vector<RetailerMasterBO> selectedRetailer) {
        if (selectedRetailer != null) {

            for (RetailerMasterBO bo : retailers) {
                for (RetailerMasterBO selectedBo : selectedRetailer) {
                    if (bo.getRetailerID().equals(selectedBo.getRetailerID())) {
                        bo.setIsNearBy(true);
                    }

                }
            }
        }

        initAdapter(retailers);

    }


    private void initDialog(Context context, int VALUE_NEARBY_RETAILER_MAX) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.nearby_retailer_dialog);
        ctx = context;
        toolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_dialog);
        toolbar.getMenu().findItem(R.id.menu_close).setVisible(false);
        toolbar.getMenu().findItem(R.id.menu_done).setVisible(false);
        toolbar.getMenu().findItem(R.id.search).setVisible(true);
        searchView = (SearchView) MenuItemCompat.getActionView(toolbar.getMenu().findItem(R.id.search));
        setSearchTextColour(searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Retailer");
        searchView.setIconifiedByDefault(false);

        mDoneBTN = (Button) findViewById(R.id.btn_done);
        mDoneBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Vector<RetailerMasterBO> list = getSelectedRetailers();
                if (list.size() <= VALUE_NEARBY_RETAILER_MAX) {
                    NearByRetailerInterface activity = (NearByRetailerInterface) ctx;
                    activity.updateNearByRetailer(list);
                    dismiss();
                } else {
                    Toast.makeText(ctx, "Only " + VALUE_NEARBY_RETAILER_MAX + " retailer allowed.", Toast.LENGTH_LONG).show();
                }

            }
        });

        ImageView searchBtn = (ImageView) searchView
                .findViewById(R.id.search_button);
        searchBtn.setImageResource(R.drawable.icon_search);

        // style Clear Btn
        ImageView close_btn = (ImageView) searchView
                .findViewById(R.id.search_close_btn);
        close_btn.setImageResource(R.drawable.ic_clear_red);
        close_btn.setVisibility(View.GONE);

        listView = (ListView) findViewById(R.id.lv_retailers);
    }


    @Override
    public boolean onQueryTextChange(String text) {
        updateRetailer(text);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String text) {

        if (text.isEmpty()) {
            MyAdapter adapter = new MyAdapter(retailersList);
            listView.setAdapter(adapter);
        } else if (text.length() >= 3) {
            tempRetailer.clear();
            for (int i = 0; i < retailersList.size(); i++) {
                if ((retailersList.get(i).getRetailerName()
                        .toLowerCase()).contains(text.toLowerCase())) {
                    tempRetailer.add(retailersList.get(i));
                }

            }
            MyAdapter adapter = new MyAdapter(tempRetailer);
            listView.setAdapter(adapter);

        }

        return false;
    }

    private void setSearchTextColour(SearchView searchView) {
        int searchPlateId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        LinearLayout searchPlate = (LinearLayout) searchView
                .findViewById(R.id.search_plate);
        searchPlate

                .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_light);

        // .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_dark);

    }


    private Vector<RetailerMasterBO> getSelectedRetailers() {
        Vector<RetailerMasterBO> mSelectedRetailers = new Vector<>();
        for (RetailerMasterBO bo : retailersList) {
            if (bo.isNearBy()) {
                mSelectedRetailers.add(bo);
            }
        }


        return mSelectedRetailers;
    }

    private class MyAdapter extends ArrayAdapter<RetailerMasterBO> {

        Vector<RetailerMasterBO> items;

        public MyAdapter(Vector<RetailerMasterBO> items) {
            super(ctx, R.layout.row_nearby_retailers, items);
            this.items = items;
        }

        public RetailerMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;


            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_nearby_retailers,
                        parent, false);
                holder = new ViewHolder();
                holder.retailerName = (TextView) row.findViewById(R.id.txt_retailerName);
                holder.chk = (CheckBox) row.findViewById(R.id.chk);
                holder.chk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            holder.retailer.setIsNearBy(true);
                        } else
                            holder.retailer.setIsNearBy(false);
                    }
                });


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.retailer = (RetailerMasterBO) items.get(position);
            holder.retailerName.setText(holder.retailer.getRetailerName());

            if (holder.retailer.isNearBy())
                holder.chk.setChecked(true);
            else
                holder.chk.setChecked(false);

            return row;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    class ViewHolder {
        TextView retailerName;
        CheckBox chk;
        RetailerMasterBO retailer;
    }

    public interface NearByRetailerInterface {
        void updateNearByRetailer(Vector<RetailerMasterBO> list);
    }

    private void updateRetailer(String text) {

        if (retailersList != null) {
            if (text.isEmpty()) {
                MyAdapter adapter = new MyAdapter(retailersList);
                listView.setAdapter(adapter);
            } else if (text.length() >= 3) {
                tempRetailer.clear();
                for (int i = 0; i < retailersList.size(); i++) {
                    if ((retailersList.get(i).getRetailerName()
                            .toLowerCase()).contains(text.toLowerCase())) {
                        tempRetailer.add(retailersList.get(i));
                    }

                }
                MyAdapter adapter = new MyAdapter(tempRetailer);
                listView.setAdapter(adapter);

            }
        } else {
            MyAdapter adapter = new MyAdapter(new Vector<RetailerMasterBO>());
            listView.setAdapter(adapter);
        }
    }
}
