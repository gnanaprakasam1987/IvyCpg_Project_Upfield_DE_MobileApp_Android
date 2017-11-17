/**
 *
 */
package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.OrderSplitDetailsBO;
import com.ivy.sd.png.bo.OrderSplittingMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.OrderSplittingDetailsDialog.SaveClickListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * @author sivakumar.j
 */
public class OrderSplitDetailsScreenWithFilters extends IvyBaseActivityNoActionBar
        implements BrandDialogInterface, OnEditorActionListener {

    private BusinessModel bmodel;
    View parentView = null;
    ListView listView = null;
    ListView rListView = null;

    LoadOrderSplitDetails loadOrderSplitDetails = null;
    Button splitButton = null;
    // View splitButton=null;
    SplitClickLoad splitClickLoad = null;
    // RadioButton previousRadioButton=null;
    CheckBox previousCheckBox = null;
    boolean right_side_check_box_checked_status[];
    int previous_position = -1;
    private AlertDialog.Builder builder, builder1;
    private AlertDialog alertDialog, alertDialog1;

    //ProgressDialog progressDialogForMovingLeftToRightSide = null;
    DatePickerDialog datePickerDialogForDeliveryDate = null;
    OrderSplittingDetailsDialog orderSplittingDetailsDialog = null;

    TextView baseOrderNumberTextView = null;
    int transperant_color;
    int green_color;
    int yellow_color;
    private boolean isSplitSKuCountHit = false;

    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    // View Flipper
    private ViewFlipper viewFlipper;

    private InputMethodManager inputManager;
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();

    private Button addNewOrderSplittingMasterButton = null;
    private String selectSplitToContinuePrompt = null;
    private String descriptionLableStr = null;
    private TextView decriptionHeadingTextView = null;
    // private Button selectAllButton=null;
    private View selectAllButton = null;
    // private ProgressDialog progressDialogue;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        bmodel.orderSplitHelper.isOrderSplitDialogExecuted = false;
        bmodel.orderSplitHelper.isOrderSplitScreenExecuted = false;

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        green_color = getResources().getColor(R.color.order_split_green_color);
        transperant_color = getResources()
                .getColor(android.R.color.transparent);
        yellow_color = getResources().getColor(R.color.Yellow);

        bmodel.orderSplitHelper
                .setCurrentlySelectedOrderSplittingMasterBO(null);
        bmodel.orderSplitHelper.createNewTargetOrderSplittingMasterBOList();
        bmodel.orderSplitHelper
                .setCurrentlySelectedOrderSplittingMasterBO(null);
        right_side_check_box_checked_status = null;

        bmodel.orderSplitHelper
                .clearNeedToMoveLeftSideOrderSplittingDetailsBOList();

        setContentView(R.layout.activity_order_split_details_with_filter);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        descriptionLableStr = this.getResources().getString(
                R.string.description_label);
        decriptionHeadingTextView = (TextView) this
                .findViewById(R.id.description_heading);

        getSupportActionBar().setIcon(R.drawable.icon_stock);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        String title = this.getResources()
                .getString(R.string.order_split_title);

        getSupportActionBar().setTitle(title);
        title = null;

        // For filter start
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.order_split_title));
                supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.filter));
                supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        // For filter end

        listView = (ListView) this.findViewById(R.id.listView1);
        listView.setCacheColorHint(0);

        rListView = (ListView) this.findViewById(R.id.right_listView1);
        rListView.setCacheColorHint(0);

        baseOrderNumberTextView = (TextView) this
                .findViewById(R.id.base_order_number_text_veiw11);
        StringBuilder sb = new StringBuilder();
        sb.append(this.getResources().getString(
                R.string.base_order_number_label));
        sb.append(" : ");
        sb.append(bmodel.orderSplitHelper.getSelectedOrderSplitMasterBO()
                .getOrderID());

        baseOrderNumberTextView.setText(sb.toString());
        // baseOrderNumberTextView.setText(bmodel.orderSplitHelper.getSelectedOrderSplitMasterBO().getOrderID());
        sb = null;

        bmodel.orderSplitHelper.createNewOrderSplittingMasterBOList();
        loadOrderSplitDetails = new LoadOrderSplitDetails();
        loadOrderSplitDetails.execute();

        this.splitButton = (Button) this.findViewById(R.id.split_btn);
        // this.splitButton=(View)this.findViewById(R.id.split_btn);
        this.splitButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (bmodel.orderSplitHelper
                        .getCurrentlySelectedOrderSplittingMasterBO() == null) {
                    // Toast.makeText(OrderSplitDetailsScreenWithFilters.this,R.string.select_any_item_from_right_side_label
                    // , Toast.LENGTH_SHORT).show();
                    showAlertForSelectSplitToContinue();
                    return;
                }
                boolean isSelected = false;
                for (OrderSplitDetailsBO bo : bmodel.orderSplitHelper
                        .getTargetOrderSplitDetailsBOList()) {
                    if (bo.isTicked())
                        isSelected = true;
                }
                if (!isSelected) {
                    showAlertWithPositiveButton(getResources().getString(R.string.please_select_atleast_one_sku));
                    return;
                }
                if (bmodel.orderSplitHelper.isOrderSplitDialogExecuted)
                    bmodel.orderSplitHelper.isOrderSplitScreenExecuted = true;
                splitClickLoad = new SplitClickLoad();
                splitClickLoad.execute();
            }

        });

        addNewOrderSplittingMasterButton = (Button) this
                .findViewById(R.id.add_new_splitting_master_btn);
        addNewOrderSplittingMasterButton
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        if (bmodel.orderSplitHelper
                                .getTargetOrderSplittingMasterBOList().size() < 10) {
                            addNewOrderSplittingMasterInList();
                        } else {
                            Toast.makeText(
                                    OrderSplitDetailsScreenWithFilters.this,
                                    getResources()
                                            .getString(
                                                    R.string.maximum_order_split_count_is_10),
                                    Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        selectSplitToContinuePrompt = this.getResources().getString(
                R.string.please_select_a_split_to_continue_prompt);

        // this.selectAllButton=(Button)this.findViewById(R.id.select_all_button);
        this.selectAllButton = (View) this.findViewById(R.id.select_all_button);
        this.selectAllButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                bmodel.orderSplitHelper
                        .selectAllInTargetOrderSplitDetailsBOList();
                settingListViewAdapter();
            }

        });
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Commons.print("OnStart Called");
        productFilterClickedFragment();
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private EditText QUANTITY, mEdt_searchproductName;

    private void productFilterClickedFragment() {
        try {

            QUANTITY = null;
            // Vector vect = bmodel.brandMasterHelper.getBrandMaster();

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();

            bundle.putBoolean("hideBrandFilter", true);
            bundle.putString("filterName", BRAND);
            bundle.putBoolean("isFormBrand", true);
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getChildLevelBo());
            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    private void generalFilterClickedFragment() {
        try {

            QUANTITY = null;
            Vector<String> vect = new Vector();
            for (String string : getResources().getStringArray(
                    R.array.productFilterArray)) {
                vect.add(string);
            }

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", GENERAL);
            bundle.putBoolean("isFormBrand", false);
            // bundle.putStringArrayList("filterContent", new ArrayList<String>(
            // vect));

            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    class LoadOrderSplitDetails extends AsyncTask<Void, Void, Void> {

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(
                    OrderSplitDetailsScreenWithFilters.this, DataMembers.SD,
                    "Loading", true, false);*/
            builder1 = new AlertDialog.Builder(OrderSplitDetailsScreenWithFilters.this);

            bmodel.customProgressDialog(alertDialog1, builder1, OrderSplitDetailsScreenWithFilters.this, getResources().getString(R.string.loading));
            alertDialog1 = builder1.create();
            alertDialog1.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub

            // bmodel.orderSplitHelper.loadOrderSplitDetailsBOListFromDB(bmodel.orderSplitHelper.getSelectedRetailerId(),bmodel.orderSplitHelper.getSelectedOrderId());
            bmodel.orderSplitHelper
                    .loadOrderSplitDetailsWithBrandIdAndCategoryIdBOListFromDB(
                            bmodel.orderSplitHelper.getSelectedRetailerId(),
                            bmodel.orderSplitHelper.getSelectedOrderId());
            return null;
        }

        protected void onPostExecute(Void result) {
            // result is the value returned from doInBackground
           /* if (progressDialogue != null && progressDialogue.isShowing())
                progressDialogue.dismiss();*/
            if (alertDialog1 != null)
                alertDialog1.dismiss();
            settingListViewAdapter();

        }

    }

    @Override
    protected void onDestroy() {

        if (this.splitClickLoad != null) {
            Status splitStatus = this.splitClickLoad.getStatus();
            if (splitStatus == AsyncTask.Status.RUNNING) {
                this.splitClickLoad.cancel(true);
            }
        }
        this.splitClickLoad = null;

        if (this.loadOrderSplitDetails != null) {
            Status status = this.loadOrderSplitDetails.getStatus();
            if (status == AsyncTask.Status.RUNNING) {
                this.loadOrderSplitDetails.cancel(true);
            }
        }
        this.loadOrderSplitDetails = null;
        listView = null;

        this.selectSplitToContinuePrompt = null;
        this.decriptionHeadingTextView = this.baseOrderNumberTextView = null;
        this.descriptionLableStr = null;
        this.selectAllButton = null;

       /* if (progressDialogue != null) {
            if (progressDialogue.isShowing())
                progressDialogue.dismiss();
            progressDialogue = null;
        }*/
        if (alertDialog1 != null)
            alertDialog1.dismiss();

        super.onDestroy();
        // force the garbage collector to run
        System.gc();
    }

    class ViewHolder {
        private OrderSplitDetailsBO orderSplitDetailsBO;
        private int position;
        private TextView skuCodeTextView, descriptionTextView, ocTextView,
                opTextView;
        private CheckBox checkBox;

        private boolean is_selected = false;
        private LinearLayout container = null;

    }

    View previousConvertView = null;

    class MenuBaseAdapter extends BaseAdapter {

        List<OrderSplitDetailsBO> orderSplitDetailsBOList = null;

        public MenuBaseAdapter(List<OrderSplitDetailsBO> items) {
            orderSplitDetailsBOList = items;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return orderSplitDetailsBOList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.order_split_detail_list_item_menu, parent,
                        false);
                holder = new ViewHolder();

                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.list_item_check_box);
                holder.descriptionTextView = (TextView) convertView
                        .findViewById(R.id.list_item_description);

                holder.ocTextView = (TextView) convertView
                        .findViewById(R.id.list_item_oc);
                holder.opTextView = (TextView) convertView
                        .findViewById(R.id.list_item_op);
                holder.skuCodeTextView = (TextView) convertView
                        .findViewById(R.id.list_item_sku_code);

                holder.container = (LinearLayout) convertView
                        .findViewById(R.id.container);
                convertView.setTag(holder);

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        Commons.print(DataMembers.SD + ",left onClick ");
                        Commons.print(DataMembers.SD + ",b4 exe is selected ="
                                + holder.is_selected);
                        // if(holder.is_selected)
                        if (holder.orderSplitDetailsBO.isTicked()) {
                            // v.setBackgroundColor(transperant_color);
                            // holder.container.setBackgroundColor(transperant_color);
                            //
                            // holder.container.setBackground(null);
                            holder.container.setBackgroundResource(0);

                            holder.is_selected = false;
                            holder.orderSplitDetailsBO.setTicked(false);
                        } else {
                            // v.setBackgroundColor(green_color);
                            holder.container.setBackgroundColor(green_color);
                            holder.is_selected = true;
                            holder.orderSplitDetailsBO.setTicked(true);
                        }

                        Commons.print(DataMembers.SD + ",after exe is selected ="
                                + holder.is_selected);
                    }

                });

            } else {
                holder = (ViewHolder) convertView.getTag();

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        Commons.print(DataMembers.SD + ",left onClick ");
                        Commons.print(DataMembers.SD + ",b4 exe is selected ="
                                + holder.is_selected);

                        // if(holder.is_selected)
                        if (holder.orderSplitDetailsBO.isTicked()) {
                            // v.setBackgroundColor(transperant_color);
                            // holder.container.setBackgroundColor(transperant_color);
                            // holder.container.setBackground(null);
                            holder.container.setBackgroundResource(0);
                            holder.is_selected = false;
                            holder.orderSplitDetailsBO.setTicked(false);
                        } else {
                            // v.setBackgroundColor(green_color);
                            holder.container.setBackgroundColor(green_color);
                            holder.is_selected = true;
                            holder.orderSplitDetailsBO.setTicked(true);
                        }
                    }

                });
            }

            holder.position = position;
            holder.orderSplitDetailsBO = orderSplitDetailsBOList
                    .get(holder.position);

            holder.descriptionTextView.setText(holder.orderSplitDetailsBO
                    .getProductName());

            holder.ocTextView.setText(holder.orderSplitDetailsBO.getCaseQty()
                    + "");
            holder.opTextView.setText(holder.orderSplitDetailsBO.getPieceqty()
                    + "");
            holder.skuCodeTextView.setText(holder.orderSplitDetailsBO
                    .getMbarcode());

            holder.checkBox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            // TODO Auto-generated method stub
                            Commons.print(DataMembers.SD + ",check box is " + isChecked);

                            holder.orderSplitDetailsBO.setTicked(isChecked);

                        }
                    });

            if (holder.orderSplitDetailsBO.isTicked()) {
                holder.container.setBackgroundColor(green_color);
                holder.is_selected = true;
                // holder.orderSplitDetailsBO.setTicked(true);
            } else {
                holder.container.setBackgroundResource(0);
                holder.is_selected = false;
                // holder.orderSplitDetailsBO.setTicked(false);
            }
            return convertView;
        }

    }

    public void settingListViewAdapter() {
        try {
            if (listView == null) {
                listView = (ListView) this.findViewById(R.id.listView1);
                listView.setCacheColorHint(0);
            }
            this.listView.setAdapter(new MenuBaseAdapter(bmodel.orderSplitHelper
                    .getTargetOrderSplitDetailsBOList()));

            StringBuilder sbuilder = new StringBuilder();
            sbuilder.append(this.descriptionLableStr == null ? "" : this.descriptionLableStr);
            sbuilder.append("(");
            sbuilder.append(this.listView.getCount());
            sbuilder.append("/");
            sbuilder.append(bmodel.orderSplitHelper.getSrcOrderSplitDetailsBOList() == null ? "0" : bmodel.orderSplitHelper.getSrcOrderSplitDetailsBOList()
                    .size());
            sbuilder.append(")");

            this.decriptionHeadingTextView.setText(sbuilder.toString());
            sbuilder = null;
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void settingListViewAdapterForRightSide() {
        this.right_side_check_box_checked_status = new boolean[bmodel.orderSplitHelper
                .getTargetOrderSplittingMasterBOList().size()];
        this.previous_position = -1;
        this.previousCheckBox = null;
        bmodel.orderSplitHelper
                .setCurrentlySelectedOrderSplittingMasterBO(null);

        previousConvertView = null;
        this.rListView.setAdapter(new MenuBaseAdapterForRightSide(
                bmodel.orderSplitHelper.getTargetOrderSplittingMasterBOList()));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i = item.getItemId();
        if (i == android.R.id.home) {// this.goBackToOrderSplitMasterScreen();
            backButtonClickedProcess();

            return true;
        } else if (i == R.id.menu_save) {
            saveButtonClickedProcess();
            return true;
        } else if (i == R.id.menu_spl_filter) {// generalFilterClicked();

            generalFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_product_filter) {// brandFilterClicked();

            productFilterClickedFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class ViewHolderRightSide {
        private OrderSplittingMasterBO orderSplittingMasterBO;
        private int position;
        private TextView orderSplitTextView, nskuTextView;
        private CheckBox checkBox;
        // private Button editButton=null;
        private ImageView editButton = null;
        private boolean is_selected = false;
    }

    class MenuBaseAdapterForRightSide extends BaseAdapter {

        List<OrderSplittingMasterBO> targetOrderSplittingMasterBOList = null;

        public MenuBaseAdapterForRightSide(List<OrderSplittingMasterBO> ob) {
            targetOrderSplittingMasterBOList = ob;
            updateSplitSkuCount(ob);
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return targetOrderSplittingMasterBOList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return arg0;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            final ViewHolderRightSide holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.order_split_detail_list_item_menu_right_side,
                        parent, false);
                holder = new ViewHolderRightSide();

				/*
                 * holder.checkBox=(CheckBox)convertView.findViewById(R.id.
				 * list_item_check_box);
				 * holder.descriptionTextView=(TextView)convertView
				 * .findViewById(R.id.list_item_description);
				 * 
				 * holder.ocTextView=(TextView)convertView.findViewById(R.id.
				 * list_item_oc);
				 * holder.opTextView=(TextView)convertView.findViewById
				 * (R.id.list_item_op);
				 * holder.skuCodeTextView=(TextView)convertView
				 * .findViewById(R.id.list_item_sku_code);
				 */

                holder.nskuTextView = (TextView) convertView
                        .findViewById(R.id.list_item_right_side_n_sku);
                holder.orderSplitTextView = (TextView) convertView
                        .findViewById(R.id.list_item_right_side_order_split);

                holder.checkBox = (CheckBox) convertView
                        .findViewById(R.id.list_item_radio_btn);
                holder.editButton = (ImageView) convertView
                        .findViewById(R.id.list_item_right_side_edit_btn);
                convertView.setTag(holder);

                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Commons.print(DataMembers.SD + ",BEFOR CHANGE HOLDER SELECTED ="
                                + holder.is_selected);

                        holder.is_selected = (holder.is_selected) ? (false)
                                : (true);

                        Commons.print(DataMembers.SD + ",after CHANGE HOLDER SELECTED ="
                                + holder.is_selected);

                        View currentView = v;

                        CheckBox current = holder.checkBox;

                        if (previousConvertView != null) {
                            if (previousConvertView == currentView) {
                                holder.orderSplittingMasterBO
                                        .setChecked(holder.is_selected);

                                if (holder.is_selected) {
                                    bmodel.orderSplitHelper
                                            .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                    right_side_check_box_checked_status[holder.position] = true;

                                    currentView
                                            .setBackgroundColor(yellow_color);

                                } else {
                                    bmodel.orderSplitHelper
                                            .setCurrentlySelectedOrderSplittingMasterBO(null);
                                    right_side_check_box_checked_status[holder.position] = false;

                                    // currentView.setBackgroundColor(transperant_color);
                                    currentView.setBackgroundResource(0);
                                }

                                previousConvertView = currentView;
                                return;

                            } else {
                                right_side_check_box_checked_status[previous_position] = false;
                                // previousCheckBox.setChecked(false);
                                previousConvertView.setBackgroundResource(0);

                                bmodel.orderSplitHelper
                                        .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                right_side_check_box_checked_status[holder.position] = true;

                                previous_position = holder.position;
                                currentView.setBackgroundColor(yellow_color);

                                previousConvertView = currentView;
                                return;
                            }
                        }

                        holder.orderSplittingMasterBO
                                .setChecked(holder.is_selected);

                        // bmodel.orderSplitHelper.setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                        if (holder.is_selected) {
                            bmodel.orderSplitHelper
                                    .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                            right_side_check_box_checked_status[holder.position] = true;
                            currentView.setBackgroundColor(yellow_color);
                        } else {
                            bmodel.orderSplitHelper
                                    .setCurrentlySelectedOrderSplittingMasterBO(null);
                            right_side_check_box_checked_status[holder.position] = false;
                            currentView.setBackgroundResource(0);
                        }
                        previous_position = holder.position;
                        previousCheckBox = current;

                        previousConvertView = currentView;
                    }

                });
            } else {
                holder = (ViewHolderRightSide) convertView.getTag();

                convertView.setOnClickListener(new OnClickListener() {

                                                   @Override
                                                   public void onClick(View v) {
                                                       // TODO Auto-generated method stub
                                                       Commons.print(DataMembers.SD + ",BEFOR CHANGE HOLDER SELECTED ="
                                                               + holder.is_selected);

                                                       holder.is_selected = (holder.is_selected) ? (false)
                                                               : (true);

                                                       Commons.print(DataMembers.SD + ",after CHANGE HOLDER SELECTED ="
                                                               + holder.is_selected);

                                                       View currentView = v;

                                                       CheckBox current = holder.checkBox;

                                                       if (previousConvertView != null) {
                                                           if (previousConvertView == currentView) {
                                                               holder.orderSplittingMasterBO
                                                                       .setChecked(holder.is_selected);

                                                               if (holder.is_selected) {
                                                                   bmodel.orderSplitHelper
                                                                           .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                                                   right_side_check_box_checked_status[holder.position] = true;

                                                                   currentView
                                                                           .setBackgroundColor(yellow_color);

                                                               } else {
                                                                   bmodel.orderSplitHelper
                                                                           .setCurrentlySelectedOrderSplittingMasterBO(null);
                                                                   right_side_check_box_checked_status[holder.position] = false;

                                                                   // currentView.setBackgroundColor(transperant_color);
                                                                   currentView.setBackgroundResource(0);
                                                               }

                                                               previousConvertView = currentView;
                                                               return;

                                                           } else {
                                                               right_side_check_box_checked_status[previous_position] = false;
                                                               // previousCheckBox.setChecked(false);
                                                               previousConvertView.setBackgroundResource(0);

                                                               bmodel.orderSplitHelper
                                                                       .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                                               right_side_check_box_checked_status[holder.position] = true;

                                                               previous_position = holder.position;
                                                               currentView.setBackgroundColor(yellow_color);

                                                               previousConvertView = currentView;
                                                               return;
                                                           }
                                                       }

                                                       holder.orderSplittingMasterBO
                                                               .setChecked(holder.is_selected);

                                                       // bmodel.orderSplitHelper.setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                                       if (holder.is_selected) {
                                                           bmodel.orderSplitHelper
                                                                   .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                                           right_side_check_box_checked_status[holder.position] = true;
                                                           currentView.setBackgroundColor(yellow_color);
                                                       } else {
                                                           bmodel.orderSplitHelper
                                                                   .setCurrentlySelectedOrderSplittingMasterBO(null);
                                                           right_side_check_box_checked_status[holder.position] = false;
                                                           currentView.setBackgroundResource(0);
                                                       }
                                                       previous_position = holder.position;
                                                       previousCheckBox = current;

                                                       previousConvertView = currentView;
                                                   }

                                               }

                );
            }

            holder.position = position;
            holder.orderSplittingMasterBO = targetOrderSplittingMasterBOList
                    .get(holder.position);

            holder.nskuTextView.setText(holder.orderSplittingMasterBO
                    .getTotal_child_count() + "");
            holder.orderSplitTextView.setText("Split "
                    + holder.orderSplittingMasterBO.getSplitting_index());

            holder.editButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    bmodel.orderSplitHelper
                            .setCurrentlySelectedOrderSplittingMasterBOForEdit(holder.orderSplittingMasterBO);

                    // OrderSplittingDetailsDialog
                    // orderSplittingDetailsDialog=new
                    // OrderSplittingDetailsDialog(OrderSplitDetailsScreen.this);
                    orderSplittingDetailsDialog = new OrderSplittingDetailsDialog(
                            green_color,
                            OrderSplitDetailsScreenWithFilters.this);
                    // orderSplittingDetailsDialog.setGreen_color(green_color);
                    bmodel.orderSplitHelper
                            .reSetCurrentlySelectedOrderSplittingMasterBOForEditChildFlag();

                    orderSplittingDetailsDialog
                            .setSaveClickInterface(new SaveClickListener() {

                                @Override
                                public void saveProcess() {
                                    isSplitSKuCountHit = false;
                                    // TODO Auto-generated method stub
                                    Commons.print(DataMembers.SD + ",save proced");

                                    bmodel.orderSplitHelper
                                            .generateCurrently_selected_orderSplittingDetails_count_for_re_add();
                                    if (bmodel.orderSplitHelper
                                            .getCurrently_selected_orderSplittingDetails_count_for_re_add() > 0) {
                                        bmodel.orderSplitHelper
                                                .createNewNeedToMoveLeftSideOrderSplittingDetailsBOList();
                                        if (bmodel.orderSplitHelper
                                                .getCurrentlySelectedOrderSplittingMasterBOForEdit()
                                                .getOrderSplittingDetailsBOList()
                                                .size() > 0) {
                                            // Moving to right to left

                                            bmodel.orderSplitHelper
                                                    .reAddToTheTargetOrderSplitDetailsBOListAndDeleteFromRightSide();
                                        }
                                    }
                                }

                                @Override
                                public void showProgressDialog(String message) {
                                    // TODO Auto-generated method stub
                                    showProgressDialogForMovingLeftToRightSide(message);
                                }

                                @Override
                                public void dismissProgressDialog() {
                                    // TODO Auto-generated method stub
                                    isSplitSKuCountHit = false;
                                    dismissProgressDialogForMovingLeftToRightSide();
                                }

                                @Override
                                public void resettingListViews() {
                                    // TODO Auto-generated method stub
                                    reSetTheListViewsAdapter();
                                }

                                @Override
                                public void showDatePickerDialogForSelectDate() {
                                    // TODO Auto-generated method stub

                                    showDatePickerDialogForDeliverDate();
                                }

                                @Override
                                public void dismissDatePickerDialogForSelectDate() {
                                    // TODO Auto-generated method stub
                                    dismissDatePickerDialogForDeliverDate();
                                }

                            });

                    // orderSplittingDetailsDialog.getWindow().setLayout(LayoutParams.FILL_PARENT,
                    // LayoutParams.FILL_PARENT);

                    WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                    lp.copyFrom(orderSplittingDetailsDialog.getWindow()
                            .getAttributes());
                    lp.width = WindowManager.LayoutParams.MATCH_PARENT;
                    lp.height = WindowManager.LayoutParams.MATCH_PARENT;

                    orderSplittingDetailsDialog.show();
                    orderSplittingDetailsDialog.getWindow().setAttributes(lp);
                }

            });
            holder.checkBox.setClickable(true);
            holder.checkBox
                    .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                        @Override
                        public void onCheckedChanged(CompoundButton buttonView,
                                                     boolean isChecked) {
                            // TODO Auto-generated method stub

                            CheckBox current = (CheckBox) buttonView;
                            Commons.print(DataMembers.SD + ",check box is " + isChecked);

                            if (previousCheckBox != null) {
                                if (previousCheckBox == current) {
                                    holder.orderSplittingMasterBO
                                            .setChecked(isChecked);

                                    if (isChecked) {
                                        bmodel.orderSplitHelper
                                                .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                        right_side_check_box_checked_status[holder.position] = true;
                                    } else {
                                        bmodel.orderSplitHelper
                                                .setCurrentlySelectedOrderSplittingMasterBO(null);
                                        right_side_check_box_checked_status[holder.position] = false;
                                    }
                                    return;

                                } else {
                                    right_side_check_box_checked_status[previous_position] = false;
                                    previousCheckBox.setChecked(false);

                                    bmodel.orderSplitHelper
                                            .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                    right_side_check_box_checked_status[holder.position] = true;

                                    previous_position = holder.position;
                                    return;
                                }
                            }
                            holder.orderSplittingMasterBO.setChecked(isChecked);

                            // bmodel.orderSplitHelper.setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                            if (isChecked) {
                                bmodel.orderSplitHelper
                                        .setCurrentlySelectedOrderSplittingMasterBO(holder.orderSplittingMasterBO);
                                right_side_check_box_checked_status[holder.position] = true;
                            } else {
                                bmodel.orderSplitHelper
                                        .setCurrentlySelectedOrderSplittingMasterBO(null);
                                right_side_check_box_checked_status[holder.position] = false;
                            }
                            previous_position = holder.position;
                            previousCheckBox = current;
                        }
                    });
            return convertView;
        }

    }

    public void updateSplitSkuCount(List<OrderSplittingMasterBO> ob) {

        for (OrderSplittingMasterBO bo : ob) {
            if (bo.getTotal_child_count() > 0)
                isSplitSKuCountHit = true;
        }

    }

    class SplitClickLoad extends AsyncTask<Void, Void, Void> {
        //  private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
          /*  progressDialogue = ProgressDialog.show(
                    OrderSplitDetailsScreenWithFilters.this, DataMembers.SD,
                    "Loading..", true, false);*/
            builder = new AlertDialog.Builder(OrderSplitDetailsScreenWithFilters.this);

            bmodel.customProgressDialog(alertDialog, builder, OrderSplitDetailsScreenWithFilters.this, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub

            updateCurrentlyTickedOrderSplitDetailsBOListAndMove();

            bmodel.orderSplitHelper
                    .generateTargetOrderSplitDetailsBOListForBrandId();
            return null;
        }

        protected void onPostExecute(Void result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            //  progressDialogue.dismiss();
            settingListViewAdapter();
            settingListViewAdapterForRightSide();
            // settingListViewAdapter();

        }
    }

    public void updateCurrentlyTickedOrderSplitDetailsBOListAndMove() {
        /*
         * b4 implementing filter start
		 * bmodel.orderSplitHelper.createNewCurrentlyTickedOrderSplitDetailsBOList
		 * ();
		 * 
		 * List<OrderSplitDetailsBO>
		 * targetOrderSplitDetailsBOList=bmodel.orderSplitHelper
		 * .getTargetOrderSplitDetailsBOList(); int
		 * size_of_targetOrderSplitDetailsBOList
		 * =targetOrderSplitDetailsBOList.size();
		 * if(size_of_targetOrderSplitDetailsBOList<=0) return;
		 * 
		 * for(OrderSplitDetailsBO ob1:targetOrderSplitDetailsBOList) {
		 * if(ob1.isTicked()) {
		 * bmodel.orderSplitHelper.appendCurrentlyTickedOrderSplitDetailsBOList
		 * (ob1); } }
		 * 
		 * bmodel.orderSplitHelper.moveOrderSplitDetailsToOrderSplittingMaster();
		 * b4 implementing filter end
		 */

        // After implementing filter start
        bmodel.orderSplitHelper
                .createNewCurrentlyTickedOrderSplitDetailsBOList();

        List<OrderSplitDetailsBO> sourceOrderSplitDetailsBOList = bmodel.orderSplitHelper
                .getSrcOrderSplitDetailsBOList();
        int size_of_sourceOrderSplitDetailsBOList = sourceOrderSplitDetailsBOList
                .size();
        if (size_of_sourceOrderSplitDetailsBOList <= 0)
            return;

        for (OrderSplitDetailsBO ob1 : sourceOrderSplitDetailsBOList) {
            if (ob1.isTicked()) {
                bmodel.orderSplitHelper
                        .appendCurrentlyTickedOrderSplitDetailsBOList(ob1);
            }
        }

        bmodel.orderSplitHelper.moveOrderSplitDetailsToOrderSplittingMaster();
        // After implementing filter end
    }

    public void reSetTheListViewsAdapter() {
        bmodel.orderSplitHelper
                .generateTargetOrderSplitDetailsBOListForBrandId();

        settingListViewAdapter();
        settingListViewAdapterForRightSide();
    }

    public void showProgressDialogForMovingLeftToRightSide(String message) {
        /*if (this.progressDialogForMovingLeftToRightSide == null)
            progressDialogForMovingLeftToRightSide = ProgressDialog.show(
					OrderSplitDetailsScreenWithFilters.this, DataMembers.SD,
					message, true, false);
		else
			progressDialogForMovingLeftToRightSide.setMessage(message);*/
        if (alertDialog == null) {
            builder = new AlertDialog.Builder(OrderSplitDetailsScreenWithFilters.this);

            bmodel.customProgressDialog(alertDialog, builder, OrderSplitDetailsScreenWithFilters.this, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }
      /*  if (progressDialogForMovingLeftToRightSide.isShowing() == false)
            progressDialogForMovingLeftToRightSide.show();*/
    }

    public void dismissProgressDialogForMovingLeftToRightSide() {
        /*if ((this.progressDialogForMovingLeftToRightSide != null)
                && (this.progressDialogForMovingLeftToRightSide.isShowing()))
			this.progressDialogForMovingLeftToRightSide.dismiss();*/
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    public void saveButtonClickedProcess() {
        // int left_side_count=this.listView.getCount();

        int left_side_count = bmodel.orderSplitHelper
                .getSrcOrderSplitDetailsBOList().size();
        int right_side_count = this.rListView.getCount();


        if (!isSplitSKuCountHit)
            showAlertForUnnableToSave(getResources().getString(R.string.you_are_trying_to_split_zero_sku));
        else if (!bmodel.orderSplitHelper.isOrderSplitDialogExecuted && bmodel.orderSplitHelper.isAllOrderTickAvailableSplit())
            showAlertForUnnableToSave(getResources().getString(R.string.unable_to_save_you_unticked_the_split));
        else if ((left_side_count < 1) && (right_side_count > 0)) {
            SaveSplittedOrder saveSplittedOrder = new SaveSplittedOrder();
            saveSplittedOrder.execute();// */
        } else {
            showAlertForUnnableToSave("Unable to save : Complete the split.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_order_split_details_screen, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void showAlertForUnnableToSave(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                OrderSplitDetailsScreenWithFilters.this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    public void showAlertForOrderNotSaved(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                OrderSplitDetailsScreenWithFilters.this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goBackToOrderSplitMasterScreen();
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    @SuppressLint("NewApi")
    public void showDatePickerDialogForDeliverDate() {
        if (this.datePickerDialogForDeliveryDate == null) {
            Calendar cal = Calendar.getInstance();
            int day = cal.get(Calendar.DATE);
            int month = cal.get(Calendar.MONTH);
            int year = cal.get(Calendar.YEAR);

            // bmodel.setSelectedDateFromDatePickerDialog(day+"/"+month+"/"+year);
            bmodel.setSelectedDateFromDatePickerDialog(DateUtil
                    .convertDateObjectToRequestedFormat(cal.getTime(),
                            bmodel.configurationMasterHelper.outDateFormat));
            datePickerDialogForDeliveryDate = new DatePickerDialog(this,
                    new DatePickerDialog.OnDateSetListener() {

                        @Override
                        public void onDateSet(DatePicker view, int year1,
                                              int month1, int day1) {
                            // TODO Auto-generated method stub
                            Calendar selectedDate = new GregorianCalendar(
                                    year1, month1, day1);
                            bmodel.setSelectedDateFromDatePickerDialog(DateUtil.convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(),
                                    bmodel.configurationMasterHelper.outDateFormat));// day1+"/"+(month1+1)+"/"+year1);

                            if (orderSplittingDetailsDialog != null)
                                orderSplittingDetailsDialog.setDeliverDate();
                        }

                    }, month, day, year);

            try {
                datePickerDialogForDeliveryDate.getDatePicker().setMinDate(
                        cal.getTimeInMillis());
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        this.datePickerDialogForDeliveryDate.setCancelable(true);
        if (this.datePickerDialogForDeliveryDate.isShowing() == false)
            this.datePickerDialogForDeliveryDate.show();
    }

    public void dismissDatePickerDialogForDeliverDate() {
        if ((this.datePickerDialogForDeliveryDate != null)
                && (this.datePickerDialogForDeliveryDate.isShowing())) {
            this.datePickerDialogForDeliveryDate.dismiss();
        }
    }

    class SaveSplittedOrder extends AsyncTask<Void, Void, Void> {
        //    private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(
                    OrderSplitDetailsScreenWithFilters.this, DataMembers.SD,
                    "Saving..", true, false);*/
            builder = new AlertDialog.Builder(OrderSplitDetailsScreenWithFilters.this);

            bmodel.customProgressDialog(alertDialog, builder, OrderSplitDetailsScreenWithFilters.this, "Saving..");
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // TODO Auto-generated method stub

            bmodel.orderSplitHelper.saveOrder(bmodel.orderSplitHelper
                    .getSelectedRetailerId());
            return null;
        }

        protected void onPostExecute(Void result) {
            // result is the value returned from doInBackground
            //   progressDialogue.dismiss();
            alertDialog.dismiss();
            goBackToOrderSplitMasterScreen();
            return;
            // settingListViewAdapter();

        }

    }

    public void goBackToOrderSplitMasterScreen() {
        bmodel.orderSplitHelper.clearTargetOrderSplittingMasterBOList();
        bmodel.orderSplitHelper.clearOrderSplittingMasterBOList();
        bmodel.orderSplitHelper.setLast_split_master_index(0);

        bmodel.orderSplitHelper
                .setCurrentlySelectedOrderSplittingMasterBOForEdit(null);
        bmodel.orderSplitHelper
                .clearNeedToMoveLeftSideOrderSplittingDetailsBOList();
        bmodel.orderSplitHelper
                .setCurrently_selected_orderSplittingDetails_count_for_re_add(-1);

        Intent i = new Intent(OrderSplitDetailsScreenWithFilters.this,
                OrderSplitMasterScreen.class);
        finish();
        startActivity(i);
    }

    public void backButtonClickedProcess() {
        int left_side_count = this.listView.getCount();
        int right_side_count = this.rListView.getCount();

        // Log.d(DataMembers.SD,
        // "left side count="+left_side_count+", rside count="+right_side_count);

        if (right_side_count > 0) {
            showAlertForOrderNotSaved(this.getResources().getString(
                    R.string.split_not_saved_label));
        } else {
            goBackToOrderSplitMasterScreen();
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        // TODO Auto-generated method stub

        // bmodel.orderSplitHelper.setCurrently_selected_brand_id_from_filter(id);
        bmodel.orderSplitHelper
                .setCurrently_selected_category_id_from_filter(id);

        // bmodel.orderSplitHelper.generateTargetOrderSplitDetailsBOListForBrandId(id);

        bmodel.orderSplitHelper.resetAllSrcOrderSplitDetailsBOList();
        bmodel.orderSplitHelper
                .generateTargetOrderSplitDetailsBOListForCategoryId(id);
        this.settingListViewAdapter();
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateCancel() {
        // TODO Auto-generated method stub
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {
        // TODO Auto-generated method stub

    }

    public void addNewOrderSplittingMasterInList() {
        bmodel.orderSplitHelper.addNewOrderSplittingMasterInList();
        this.settingListViewAdapterForRightSide();
    }

    public void showAlertForSelectSplitToContinue() {
        showAlertWithPositiveButton(this.selectSplitToContinuePrompt);
    }

    public void showAlertWithPositiveButton(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                OrderSplitDetailsScreenWithFilters.this);
        builder.setCancelable(false);
        builder.setMessage(message);
        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

    }
}
