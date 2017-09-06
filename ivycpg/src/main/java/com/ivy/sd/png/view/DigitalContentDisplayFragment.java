package com.ivy.sd.png.view;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ivy.countersales.CSHomeScreenFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DigitalContentBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import static java.lang.Math.pow;

public class DigitalContentDisplayFragment extends IvyBaseFragment implements BrandDialogInterface {

    private BusinessModel bmodel;

    private ArrayList<DigitalContentBO> mylist;
    public ArrayList<DigitalContentBO> filteredList;

    private String calledFrom = "", screenCode = "MENU_STK_ORD";

    private final String MENU_Init = "Initiative";

    private boolean isClicked = false;

    // Drawer Implementation
    private DrawerLayout mDrawerLayout;

    private static final String BRAND = "Brand";

    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();

    public int screenwidth = 0, screenheight = 0;

    public static final int reqWidth = 240,
            reqHeight = 240;

    private TabLayout tabLayout;

    private int isImg = 1, isAudio = 2, isVideo = 3, isXls = 4, isPDF = 5, isOthers = 6;
    private int mImgCount = 0, mAudioCount = 0, mVideoCount = 0, mXlsCount = 0, mPDFCount = 0, mOthersCount = 0;
    private TypedArray typearr;
    private View view;
    private FrameLayout drawer;
    private int mSelectedTab = 0;
    private ViewPager viewPager = null;
    private PagerAdapter adapter = null;
    String screentitle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_digital_content, container, false);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        screenwidth = displaymetrics.widthPixels;
        screenheight = displaymetrics.heightPixels;
        Bundle extras = getArguments();
        if (extras == null) {
            extras = getActivity().getIntent().getExtras();
        }

        if (extras != null) {
            screenCode = extras.getString("ScreenCode");
            calledFrom = extras.getString("FromInit");
            screenCode = screenCode != null ? screenCode : "";
            calledFrom = calledFrom != null ? calledFrom : "Digi";
        }

        if (calledFrom != null && calledFrom.equals("DigiCS")) {
            bmodel.productHelper.downloadProductFilter(CSHomeScreenFragment.MENU_DGT_CS);
        } else if (calledFrom != null && calledFrom.equalsIgnoreCase("MENU_DGT_SW")) {
            bmodel.productHelper.downloadProductFilter("MENU_DGT_SW");
        } else {
            bmodel.productHelper.downloadProductFilter("MENU_DGT");
        }

        drawer = (FrameLayout) view.findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (extras != null) {
            //Set Screen Title
            screentitle = extras.getString("screentitle");
        }

        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        // Set title to action bar
        if (screenCode.equals("MENU_STK_ORD")
                || screenCode.equals("MENU_ORDER") || calledFrom.equals("Digi"))
            bmodel.mSelectedActivityName = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_DGT");

        if (calledFrom.equals("DigiCS"))
            bmodel.mSelectedActivityName = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle(CSHomeScreenFragment.MENU_DGT_CS);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {

                if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    setScreenTitle(bmodel.mSelectedActivityName);
                    getActivity().supportInvalidateOptionsMenu();
                }
            }

            public void onDrawerOpened(View drawerView) {

                if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        };


        mDrawerLayout.addDrawerListener(mDrawerToggle);
        /*updatebrandtext(BRAND, -1);
        productFilterClickedFragment();*/
        new LoadAsyncTask(-1).execute();
        mDrawerLayout.closeDrawer(GravityCompat.END);
        LinearLayout footer = (LinearLayout) view.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);
        Button btnClose = (Button) view.findViewById(R.id.btn_close);
        if (bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            btnClose.setVisibility(View.VISIBLE);

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                            "", getActivity().getResources().getString(R.string.move_next_activity),
                            false, getActivity().getResources().getString(R.string.ok),
                            getActivity().getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                            Bundle extras = getActivity().getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", true);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
                            getActivity().finish();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    }).show();

                }
            });
        } else {
            btnClose.setVisibility(View.GONE);
        }
        Button btn_next = (Button) view.findViewById(R.id.btn_next);
        if (MENU_Init.equals(calledFrom)) {
            btn_next.setVisibility(View.VISIBLE);
            btn_next.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    click(2);
                }
            });
        } else {
            btn_next.setVisibility(View.GONE);
        }
        if (!bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY && !MENU_Init.equals(calledFrom)) {
            footer.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isClicked = false;
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            //Set Screen Title
            if (screentitle == null || screentitle.isEmpty())
                setScreenTitle(bmodel.getMenuName("MENU_DIGITIAL_SELLER"));
            else
                setScreenTitle(screentitle);
        }
        if (!bmodel.mSelectedActivityName.equalsIgnoreCase("MENU_DGT") && ((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            setScreenTitle(bmodel.mSelectedActivityName);
        } else if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            setScreenTitle(bmodel.labelsMasterHelper
                    .applyLabels((Object) "menu_dgt"));
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_only_next, menu);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

//        if (MENU_Init.equals(calledFrom)) {
//            menu.findItem(R.id.menu_next).setVisible(true);
//        } else {
        menu.findItem(R.id.menu_next).setVisible(false);//next button has been brought to bottom
//        }
        if (bmodel.productHelper.getRetailerModuleParentLeveBO() != null && bmodel.productHelper.getRetailerModuleParentLeveBO().size() > 0 || (bmodel.productHelper.getRetailerModuleChildLevelBO() != null && bmodel.productHelper.getRetailerModuleChildLevelBO().size() > 0)) {
            menu.findItem(R.id.menu_product_filter).setVisible(true);
        } else
            menu.findItem(R.id.menu_product_filter).setVisible(false);

        menu.findItem(R.id.menu_product_filter).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        } /*else {
            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
        }*/

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }
        if (calledFrom.equalsIgnoreCase("MENU_DGT_SW") || calledFrom.equalsIgnoreCase("DigiCS")) {
            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
        }

        if (drawerOpen) {
            menu.clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {

                if (screenCode.equals("MENU_DGT_SW")) {
                    Intent intent = new Intent(getActivity(),
                            HomeScreenActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                } else
                    click(1);
            }
            return true;
        } else if (i == R.id.menu_next) {
            click(2);
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;

        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Method used to call Five level filter.
     */

    private void FiveFilterFragment() {
        try {

            //QUANTITY = null;
            if (tabLayout != null) {
                mSelectedTab = tabLayout.getSelectedTabPosition();
            }
            Vector<String> vect = new Vector();
            for (String string : getResources().getStringArray(
                    R.array.productFilterArray)) {
                vect.add(string);
            }

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<Object>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    /**
     * productFilterClickedFragment for calling filter fragment
     */
    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment<?> frag = (FilterFragment<?>) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            bundle.putString("filterHeader", bmodel.productHelper
                    .getRetailerModuleChildLevelBO().get(0).getProductLevel());
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getRetailerModuleChildLevelBO());

            if (bmodel.productHelper.getRetailerModuleParentLeveBO() != null
                    && bmodel.productHelper.getRetailerModuleParentLeveBO().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getRetailerModuleParentLeveBO().get(0).getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
            }

            // set Fragment class Arguments
            FilterFragment<?> fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    //use palette to fectch suitable colors based on the images displayed
    /*
      * generate palette based on the bitmap
	  * get mostly used colors using getVibrantSwatch()
	  * set background of textview in accordance with the returned color
	  * set text colors in contrast to background.

	  * Note- palette may return null . So use default color in place of null value
	 */
    public void updatePalette(Bitmap bitmap, final DigitalContentBO ret) {
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {

                Palette.Swatch item = palette.getVibrantSwatch();//use default color if null value is returned
                if (item != null) {
                    ret.setTextbgcolor(adjustAlpha(item.getRgb(), 0.4f));
                } else {
                    ret.setTextbgcolor(1073741824);//decimal code of 40% of black
                }
                item = palette.getDarkVibrantSwatch();//use default color if null value is returned
                if (item != null) {
                    ret.setTextcolor(contrastcolor(item.getRgb()));
                } else {
                    ret.setTextcolor(Color.BLACK);
                }

            }

        });
    }

    //returns the suited color(black/white) for the background
    /*
      * returns white- if background is dark
	  * returns black- if background is light
	 */
    public int contrastcolor(int color) {
        float r = Color.red(color);
        float g = Color.green(color);
        float b = Color.blue(color);

        float gamma = 2.2f;
        float L = (float) (0.2126 * pow(r / 255, gamma)
                + 0.7152 * pow(g / 255, gamma)
                + 0.0722 * pow(b / 255, gamma));
        if (L > 0.5) {
            return Color.BLACK;
        } else {

            return Color.WHITE;
        }
    }

    /*create ARGB color
      * params
      * color - need to be adjusted
      * factor - factor of the color (from 0.0 to 1.0)
    */
    public int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    //decodes bitmap from resource with required height and width
    public static Bitmap decodeSampledBitmapFromResource(String res) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(res, options);
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(res, options);
    }


    /*
       reduces the image height and width if it is
       greater than the required height and width
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public void click(int action) {
        if (!isClicked) {
            isClicked = true;
            if (action == 1) {

                bmodel.setIsDigitalContent();
                bmodel.setDigitalContentInDB();
                bmodel.getRetailerMasterBO().setIsDigitalContent("Y");

                if (calledFrom.equals(MENU_Init)) {

                    if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(SDUtil
                                        .now(SDUtil.TIME));
                        Intent intent = new Intent(getActivity(),
                                InitiativeActivity.class);
                        intent.putExtra("ScreenCode", screenCode);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        getActivity().finish();
                    } else if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                        Intent init = new Intent(getActivity(),
                                OrderDiscount.class);
                        init.putExtra("ScreenCode", screenCode);
                        startActivity(init);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        getActivity().finish();
                    } else if (bmodel.configurationMasterHelper.IS_SCHEME_ON
                            && bmodel.configurationMasterHelper.IS_SCHEME_SHOW_SCREEN) {
                        Intent init = new Intent(getActivity(),
                                SchemeApply.class);
                        init.putExtra("ScreenCode", screenCode);
                        startActivity(init);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        getActivity().finish();
                    } else if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        Intent intent = new Intent(getActivity(),
                                CrownReturnActivity.class);
                        intent.putExtra("OrderFlag", "Nothing");
                        intent.putExtra("ScreenCode", screenCode);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                    } else {
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(SDUtil
                                        .now(SDUtil.TIME));
                        Intent intent;
                        if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                            intent = new Intent(getActivity(), CatalogOrder.class);
                        } else {
                            intent = new Intent(getActivity(), StockAndOrder.class);
                        }
                        intent.putExtra("OrderFlag", "Nothing");
                        intent.putExtra("ScreenCode", screenCode);
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                    }

                } else if ("Digi".equals(calledFrom)) {
                    bmodel.outletTimeStampHelper
                            .updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                    Intent intent = new Intent(getActivity(),
                            HomeScreenTwo.class);
                    startActivity(intent);
                    getActivity().finish();
                } else if (calledFrom.equals("DigiCS")) {
                    Intent intent = new Intent(getActivity(),
                            HomeScreenActivity.class).putExtra("menuCode", "MENU_COUNTER");
                    startActivity(intent);
                    getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                    getActivity().finish();
                }
            }

            if (action == 2) {
                bmodel.setIsDigitalContent();
                bmodel.setDigitalContentInDB();
                bmodel.getRetailerMasterBO().setIsDigitalContent("Y");
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                Intent i = new Intent(getActivity(),
                        OrderSummary.class);
                i.putExtra("ScreenCode", screenCode);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                getActivity().finish();
            }
        }
    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
    }

    @Override
    public void updatebrandtext(String filtertext, int pid) {
        // Close the drawer
        mDrawerLayout.closeDrawers();
        new LoadAsyncTask(pid).execute();


    }

    @Override
    public void updategeneraltext(String filtertext) {


    }

    @Override
    public void updateCancel() {
        // Close Drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        // TODO Auto-generated method stub
        Log.v("", "Data");

    }

    public HashMap<Integer, Integer> mSelectedIdByLevelId;

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        // TODO Auto-generated method stub
        Log.v("", "Data");
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();

        if (parentidList != null) {
            if (parentidList.size() > 0) {
                new LoadAsyncTask(parentidList).execute();
            } else {
                new LoadAsyncTask(-1).execute();
            }
        } else {
            new LoadAsyncTask(-1).execute();
        }


    }

    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;
        private int pid;
        Vector<LevelBO> parentidList;
        Vector<DigitalContentBO> items;
        int size;
        ArrayList<DigitalContentBO> imglist;

        private LoadAsyncTask(int pid) {
            super();
            this.pid = pid;
        }

        private LoadAsyncTask(Vector<LevelBO> parentidList) {
            super();
            this.parentidList = new Vector<>(parentidList);
        }

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                mylist = new ArrayList<>();
                imglist = new ArrayList<>();
                items = bmodel.planogramMasterHelper
                        .getDigitalMaster();
                if (items == null) {
                    getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            bmodel.showAlert(getResources().getString(R.string.no_data_exists),
                                    0);
                        }
                    });
                    return false;
                }
                size = items.size();

		/*
          add extension of image types in imglist
		  other types in mylist
		*/
                if (parentidList != null && parentidList.size() > 0) {
                    for (int k = 0; k < parentidList.size(); k++) {
                        pid = parentidList.get(k).getParentID();
                        loadList(pid);
                    }
                } else {
                    loadList(-1);
                }



       /*
         * loop through the imglist size
         * check the loopcount is less than mylist size
         * if yes add the imglist element to the loopcount position
           * increase loopcount to 5
         * if no add the imglist element at end of mylist.
        */
                int loopcount = 0;
                mImgCount = 0;
                for (int j = 0; j < imglist.size(); j++) {
                    int listsize = mylist.size();
                    if (loopcount < listsize) {
                        if (imglist.get(j).isLessimagewidth())//compares image width to screen width
                        {
                            mylist.add(imglist.get(j));
                        } else {
                            mylist.add(loopcount, imglist.get(j));
                            loopcount += 5;
                        }
                    } else {
                        mylist.add(imglist.get(j));
                    }
                }
                //for sorting types of files in group
                Collections.sort(mylist, DigitalContentBO.imgFileCompartor);
                bmodel.planogramMasterHelper.setFilteredDigitalMaster(mylist);


                if (mylist.size() > 0) {
                    for (int i = 0; i < mylist.size(); i++) {
                        if (mylist.get(i).getImgFlag() == isImg)
                            mImgCount++;
                        else if (mylist.get(i).getImgFlag() == isAudio)
                            mAudioCount++;
                        else if (mylist.get(i).getImgFlag() == isVideo)
                            mVideoCount++;
                        else if (mylist.get(i).getImgFlag() == isXls)
                            mXlsCount++;
                        else if (mylist.get(i).getImgFlag() == isPDF)
                            mPDFCount++;
                        else
                            mOthersCount++;
                    }
                }

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.loading),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();
            if (mylist.size() > 0) {

                if (tabLayout != null) {
                    tabLayout.removeAllTabs();
                }

                tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                viewPager = (ViewPager) view.findViewById(R.id.pager);
                adapter = new PagerAdapter
                        (getChildFragmentManager(), tabLayout.getTabCount());
                if (mImgCount > 0)
                    adapter.addFragment(new DigitalContentImagesFragement(), getResources().getString(R.string.tab_text_images) + ":" + mImgCount);
                if (mAudioCount > 0)
                    adapter.addFragment(new DigitalContentAudioFragement(), getResources().getString(R.string.tab_text_audio) + ":" + mAudioCount);
                if (mVideoCount > 0)
                    adapter.addFragment(new DigitalContentVideoFragement(), getResources().getString(R.string.tab_text_video) + ":" + mVideoCount);
                if (mXlsCount > 0)
                    adapter.addFragment(new DigitalContentXlsFragement(), getResources().getString(R.string.tab_text_xls) + ":" + mXlsCount);
                if (mPDFCount > 0)
                    adapter.addFragment(new DigitalContentPdfFragement(), getResources().getString(R.string.tab_text_pdf) + ":" + mPDFCount);
                if (mOthersCount > 0)
                    adapter.addFragment(new DigitalContentOthersFragement(), getResources().getString(R.string.tab_text_others) + ":" + mOthersCount);

            }

            if (viewPager != null && adapter != null) {
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setCurrentItem(mSelectedTab);
            }


        }

        private void loadList(int pid) {
            for (int i = 0; i < size; ++i) {
                DigitalContentBO ret = items.elementAt(i);
                if (ret.getProductID() == pid || pid == -1) {
                    if ((ret.getFileName().endsWith(".png") || ret.getFileName().endsWith(".jpeg")
                            || ret.getFileName().endsWith(".jpg") || ret.getFileName().endsWith(".JPG")
                            || ret.getFileName().endsWith(".PNG"))) {

                        ret.setTextbgcolor(1073741824); //decimal code of 40% of black
                        ret.setTextcolor(Color.BLACK);

                        ret.setImgFlag(isImg);
                        imglist.add(ret);

                    } else {
                        if (ret.getFileName().endsWith("xls")
                                || ret.getFileName().endsWith("xlsx")) {
                            ret.setTextbgcolor(1713404746); //decimal code of 40% of green
                            ret.setImgFlag(isXls);
                        } else {
                            ret.setTextbgcolor(1724731969); //decimal code of 40% of red
                            if (ret.getFileName().endsWith("pdf"))
                                ret.setImgFlag(isPDF);
                            else if (ret.getFileName().endsWith(".mp4")
                                    || ret.getFileName().endsWith("3gp"))
                                ret.setImgFlag(isVideo);
                            else if (ret.getFileName().endsWith(".mp3")
                                    || ret.getFileName().endsWith(".wma")
                                    || ret.getFileName().endsWith(".wav")
                                    || ret.getFileName().endsWith(".ogg"))
                                ret.setImgFlag(isAudio);
                            else
                                ret.setImgFlag(isOthers);
                        }
                        ret.setTextcolor(Color.WHITE);

                        mylist.add(ret);

                    }
                }
            }
        }
    }


    public class PagerAdapter extends FragmentPagerAdapter {//FragmentPagerAdapter {//FragmentStatePagerAdapter
        private int mNumOfTabs, index;
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }


    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

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
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

}
