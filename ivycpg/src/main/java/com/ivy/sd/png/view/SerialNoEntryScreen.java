package com.ivy.sd.png.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SerialNoBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.SerialNoInterface;
import com.ivy.sd.png.util.Commons;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Locale;


/**
 * Created by rajesh.k on 11-02-2016.
 */
public class SerialNoEntryScreen extends IvyBaseActivityNoActionBar implements SerialNoInterface {


    private BusinessModel bmodel;
    private SerialNoFragment mSerialNoFragment;
    static DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private String menuTitle;
    private FragmentTransaction transaction;
    private ArrayList<SerialNoBO> mSerialNoList;
    private ListView mSerialNoLV;

    private int mProductID;
    private SparseArray<ArrayList<SerialNoBO>> mSerialNoListByPid;
    private MyAdapter mAdapter;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    private EditText QUANTITY;
    private InputMethodManager inputManager;
    private TextView mTotalQtyTV, mScannedQtyTV, mProductNameTV;

    private String append = "";
    private String strBarCodeSearch = "0";
    private boolean isBarcode = false;
    private Holder mSelectedHolder;
    private Toolbar toolbar;


    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialno_entry);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

       /* try {
            FrameLayout bg = (FrameLayout) findViewById(R.id.left_drawer);
            LinearLayout rootBg = (LinearLayout) findViewById(R.id.root);
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("bg_menu");
                    }
                });
                for (File temp : files) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(temp
                            .getAbsolutePath());
                    Drawable bgrImage = new BitmapDrawable(this.getResources(), bitmapImage);
                    Drawable bgrImage1 = new BitmapDrawable(this.getResources(), bitmapImage);
                    int sdk = android.os.Build.VERSION.SDK_INT;
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        bg.setBackgroundDrawable(bgrImage);
                        rootBg.setBackgroundDrawable(bgrImage1);
                    } else {
                        bg.setBackground(bgrImage);
                        rootBg.setBackground(bgrImage1);
                    }
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
*/
        toolbar = (Toolbar) findViewById(R.id.toolbar);


        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setIcon(null);
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        /*
         * if (findViewById(R.id.fragment_container) != null) { if
		 * (savedInstanceState != null) { return; } // Create a new Fragment to
		 * be placed in the activity layout ReportMenuFragment
		 * reportMenuFragment = new ReportMenuFragment();
		 * reportMenuFragment.setArguments(getIntent().getExtras());
		 * getSupportFragmentManager().beginTransaction()
		 * .add(R.id.fragment_container, reportMenuFragment).commit(); }
		 */
        setScreenTitle("SerialNo Entry");
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        /** open drawer in swiping mode **/
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        mDrawerToggle = new ActionBarDrawerToggle(
                SerialNoEntryScreen.this, mDrawerLayout, R.string.ok,
                R.string.close) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }
        };

        setLanguage();
        mSerialNoFragment = (SerialNoFragment) getSupportFragmentManager()
                .findFragmentById(R.id.serialno_fragment);

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(GravityCompat.START);

//        mSerialNoFragment.onReportListener(this);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerLayout.openDrawer(GravityCompat.START);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mTotalQtyTV = (TextView) findViewById(R.id.tv_total_qty);
        mScannedQtyTV = (TextView) findViewById(R.id.tv_total_scanned);
        mProductNameTV = (TextView) findViewById(R.id.tv_product_name);
        mSerialNoLV = (ListView) findViewById(R.id.lv_serialno_enty);
        mSerialNoListByPid = OrderHelper.getInstance(SerialNoEntryScreen.this).getSerialNoListByProductId();


    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }


    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
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
                    // TODO: handle exception
                }
            }
        }
    }


    @Override
    public void updateSerialNo(int productid) {
        QUANTITY = null;

        if (mSerialNoListByPid == null) {
            mSerialNoListByPid = new SparseArray<>();
            mSerialNoListByPid.put(productid, new ArrayList<SerialNoBO>());
        }
        if (mProductID != 0) {
            if (mSerialNoListByPid != null) {
                mSerialNoListByPid.put(mProductID, mSerialNoList);

            }
        }


        mProductID = productid;
        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(productid + "");
        if (productBO != null) {
            mProductNameTV.setText(productBO.getProductShortName());
            final int totalQty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());
            mTotalQtyTV.setText(totalQty + "");

        }

        if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
            mDrawerLayout.closeDrawers();

        mSerialNoList = mSerialNoListByPid.get(productid);
        if (mSerialNoList == null || mSerialNoList.size() == 0) {
            mSerialNoList = new ArrayList<SerialNoBO>();
            SerialNoBO serialNoBO = new SerialNoBO();
            serialNoBO.setFromNo(0 + "");
            serialNoBO.setToNo(0 + "");
            serialNoBO.setScannedQty(0);
            mSerialNoList.add(serialNoBO);
        }
        OrderHelper.getInstance(SerialNoEntryScreen.this).setSerialNoListByProductId(mSerialNoListByPid);


        mAdapter = new MyAdapter();
        mSerialNoLV.setAdapter(mAdapter);

    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mSerialNoList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                holder = new Holder();
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater
                        .inflate(R.layout.list_edit_serialno, parent, false);
                holder.fromET = (EditText) convertView.findViewById(R.id.et_from);
                holder.toET = (EditText) convertView.findViewById(R.id.et_to);
                holder.totalScannedTV = (TextView) convertView.findViewById(R.id.tv_scanned);


                holder.fromET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String fromQty = s.toString();
                        holder.serialNoBo.setFromNo(fromQty);

                        if (isBarcode) {
                            holder.toET.setText(fromQty);
                        }


                    }
                });
                holder.fromET.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.fromET;
                        mSelectedHolder = holder;
                        int inType = holder.fromET.getInputType();
                        holder.fromET.setInputType(InputType.TYPE_NULL);
                        holder.fromET.onTouchEvent(event);
                        holder.fromET.setInputType(inType);
                        holder.fromET.selectAll();
                        holder.fromET.requestFocus();

                        return true;
                    }
                });
                holder.toET.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        String toQty = s.toString();
                        holder.serialNoBo.setToNo(toQty);
                        if (!toQty.equals("null") && !toQty.equals("0")) {

                            try {
                                int scannedQty = 0;
                                BigInteger toSerialNo = new BigInteger(toQty);
//                                        int fromSerialNo=Integer.parseInt(holder.serialNoBo.getToNo());
                                int res = toSerialNo.compareTo(new BigInteger(0 + ""));
                                if (res != 0) {
                                    scannedQty = (toSerialNo.subtract(new BigInteger(holder.serialNoBo.getFromNo()))).intValue();
                                    scannedQty = scannedQty + 1;

                                    if (scannedQty <= 0) {
                                        scannedQty = 0;
                                    }
                                }

                                holder.serialNoBo.setScannedQty(scannedQty);
                                holder.totalScannedTV.setText(scannedQty + "");

                            } catch (NumberFormatException e) {
                                holder.serialNoBo.setScannedQty(1);
                                holder.totalScannedTV.setText(1 + "");
                            }


                        } else {
                            holder.totalScannedTV.setText(0 + "");
                            holder.serialNoBo.setScannedQty(0);
                        }
                        holder.serialNoBo.setToNo(toQty);

                      /*  if (s != null && !s.equals("0")  && !s.equals("null")) {

                        } else {




                        }*/
                        updateTotalScannedQty();


                    }
                });
                holder.toET.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.toET;

                        int inType = holder.toET.getInputType();
                        holder.toET.setInputType(InputType.TYPE_NULL);
                        holder.toET.onTouchEvent(event);
                        holder.toET.setInputType(inType);
                        holder.toET.selectAll();
                        holder.toET.requestFocus();

                        return true;
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.serialNoBo = mSerialNoList.get(position);

            try {
                holder.fromQty = new BigInteger(holder.serialNoBo.getFromNo());
                holder.toQty = new BigInteger(holder.serialNoBo.getToNo());
            } catch (NumberFormatException e) {
                Commons.print(e.getMessage());
            }


            holder.fromET.setText(holder.serialNoBo.getFromNo() + "");
            holder.toET.setText(holder.serialNoBo.getToNo() + "");

            holder.totalScannedTV.setText(holder.serialNoBo.getScannedQty() + "");


            return convertView;
        }
    }

    class Holder {
        SerialNoBO serialNoBo;
        EditText fromET;
        EditText toET;
        TextView totalScannedTV;
        BigInteger fromQty = null;
        BigInteger toQty = null;

    }

    private void setLanguage() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        onConfigurationChanged(conf);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_serial_no, menu);

        // Find the share item
        // MenuItem shareItem = menu.findItem(R.id.menu_share);

        // Need to use MenuItemCompat to retrieve the Action Provider
        // mActionProvider = (ShareActionProvider) MenuItemCompat
        // .getActionProvider(shareItem);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_barcode).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_plus).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_back).setVisible(!drawerOpen);


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
                mDrawerLayout.closeDrawers();

            supportInvalidateOptionsMenu();
            mSerialNoFragment.updateOrderList();
            return true;
        }

        int i = item.getItemId();
        if (i == android.R.id.home) {
//            supportInvalidateOptionsMenu();
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();

            return true;
        } else if (i == R.id.menu_next) {

            mSerialNoListByPid.put(mProductID, mSerialNoList);
            OrderHelper.getInstance(SerialNoEntryScreen.this).setSerialNoListByProductId(mSerialNoListByPid);
            if (!OrderHelper.getInstance(this).isAllScanned()) {
                Toast.makeText(this, getResources().getString(R.string.mismatch_scanned_products), Toast.LENGTH_SHORT).show();
                return true;
            } else if (OrderHelper.getInstance(SerialNoEntryScreen.this).isDuplicateSerialNo()) {
                Toast.makeText(this, getResources().getString(R.string.duplicate_serialno), Toast.LENGTH_SHORT).show();
                return true;
            } else {
                new SaveSerialNo().execute("");

            }

        } else if (i == R.id.menu_barcode) {

            if (QUANTITY == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.please_select_item), 0);
                return true;
            }
            //isLoaded = false;
            checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                new IntentIntegrator(this).setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (i == R.id.menu_plus) {

            if (mSerialNoList != null) {
                SerialNoBO serialNoBO = new SerialNoBO();
                serialNoBO.setFromNo(0 + "");
                serialNoBO.setToNo(0 + "");
                serialNoBO.setScannedQty(0);
                mSerialNoList.add(serialNoBO);
            }
            mSerialNoLV.setAdapter(mAdapter);
            return true;

        } else if (i == R.id.menu_back) {
            finish();
        } else if (i == R.id.menu_move) {
            moveFromLeftToRight();
        }
        return super.onOptionsItemSelected(item);
    }

    class SaveSerialNo extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(SerialNoEntryScreen.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(String... params) {

            OrderHelper.getInstance(SerialNoEntryScreen.this).saveSerialNoTemp(SerialNoEntryScreen.this);
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            finish();
        }
    }

    private void updateTotalScannedQty() {
        int totalScannedQty = 0;
        for (SerialNoBO serialNoBO : mSerialNoList) {
            totalScannedQty = totalScannedQty + serialNoBO.getScannedQty();


        }

        ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(mProductID + "");
        if (productBO != null)
            productBO.setTotalScannedQty(totalScannedQty);


        mScannedQtyTV.setText(productBO.getTotalScannedQty() + "");


    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        int val = 0;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {

            int id = vw.getId();
            if (id == R.id.calcdel) {
                try {
                    BigInteger s = new BigInteger(QUANTITY.getText()
                            .toString());
                    BigInteger s1 = new BigInteger("10");
                    s = s.divide(s1);
                    QUANTITY.setText(s + "");
                } catch (NumberFormatException e) {
                    Commons.print(e.getMessage());
                    QUANTITY.setText("");
                }

            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt((String) append);
            }


        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                isBarcode = true;
                strBarCodeSearch = result.getContents();
                QUANTITY.setText(strBarCodeSearch);
                isBarcode = false;
            }
        }
    }

    private void moveFromLeftToRight() {
        if (mSelectedHolder != null) {
            mSelectedHolder.toET.setText(mSelectedHolder.fromET.getText().toString());

        } else {
            Toast.makeText(this, getResources().getString(R.string.please_select_item), Toast.LENGTH_SHORT).show();
        }
    }
}
