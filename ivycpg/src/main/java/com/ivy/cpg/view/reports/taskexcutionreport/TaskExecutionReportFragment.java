package com.ivy.cpg.view.reports.taskexcutionreport;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;

import java.util.ArrayList;
import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class TaskExecutionReportFragment extends IvyBaseFragment {
    // Global Variables
    private ListView lvwplist;
    private BusinessModel bmodel;
    private ArrayAdapter<BeatMasterBO> adapter;
    private Spinner spinnerRetailer;
    private Vector<ConfigureBO> menuDB = new Vector<ConfigureBO>();
    private TextView textview[] = null, t_rname;
    private ImageView imageView[] = null;
    LinearLayout.LayoutParams commonsparams, imgcommonsparams, texparams, imageparams, textlayoutparama;
    LinearLayout headerLayout;
    private boolean is7InchTablet;
    private TaskReportHelper taskReportHelper;
    private CompositeDisposable compositeDisposable;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        taskReportHelper =  TaskReportHelper.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_taskexecutionreport, container, false);

        sessionOut();
        t_rname = (TextView) view.findViewById(R.id.tv_rname);
        t_rname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        headerLayout = (LinearLayout) view.findViewById(R.id.ll_header);
        spinnerRetailer = (Spinner) view.findViewById(R.id.retailerSpinner);
        lvwplist = (ListView) view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);
        hideAndShow();
        downloadReportData();
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();


        adapter = new ArrayAdapter<BeatMasterBO>(getActivity(),
                android.R.layout.simple_spinner_item,
                ReportHelper.getInstance(getActivity()).getBeatinfo());

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRetailer.setAdapter(adapter);

        spinnerRetailer.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                BeatMasterBO ti = (BeatMasterBO) parent.getSelectedItem();
                // updateSbdSkuReportTable(ti.getBeatId());

            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    private void sessionOut() {
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    private ArrayList<TaskReportBo> taskretailerinfo = new ArrayList<>();

    private void downloadReportData() {
        /*final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());
        customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/
        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add((Disposable) Observable.zip(taskReportHelper.downloadTaskExecutionReport(),
                taskReportHelper.downloadNewActivityMenu()
                , taskReportHelper.downloadActMenus(), new Function3<ArrayList<TaskReportBo>, Vector<ConfigureBO>, Vector<ConfigureBO>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<TaskReportBo> taskReportList,
                                         Vector<ConfigureBO> storeCheckMenuList,
                                         Vector<ConfigureBO> storeActivityMenuList) throws Exception {

                        if (taskReportList.size() > 0) {
                            taskretailerinfo = taskReportList;
                            menuDB = storeCheckMenuList;
                            menuDB.addAll(storeActivityMenuList);

                            return true;
                        } else
                            return false;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isFlag) {
                        if (isFlag) {
                            //remove MENU_STORECHECK from menu db
                            Vector<ConfigureBO> tempMenus = new Vector<>();
                            for (ConfigureBO configureBO : menuDB) {
                                if (!configureBO.getConfigCode().equals("MENU_STORECHECK"))
                                    tempMenus.add(configureBO);
                            }
                            menuDB.clear();
                            menuDB.addAll(tempMenus);
                        } else {
                            Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                       // alertDialog.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_data), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        //alertDialog.dismiss();
                        updateSbdSkuReportTable(taskretailerinfo);
                    }
                })
        );

    }


    private LinearLayout getTextView(int mNumber, String mName) {
        textview[mNumber] = new TextView(getActivity());
        LinearLayout linearlayout = new LinearLayout(getActivity());

        textview[mNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.list_header_text_color));
        textview[mNumber].setGravity(Gravity.CENTER);
        textview[mNumber].setMaxLines(2);
        textview[mNumber].setTextSize(getResources().getDimension(R.dimen.font_small));

        textview[mNumber].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        if (Build.VERSION.SDK_INT < 23) {
            textview[mNumber].setTextAppearance(getActivity(), R.style.TextViewListTitle);
        } else {
            textview[mNumber].setTextAppearance(R.style.TextViewListTitle);
        }
        textview[mNumber].setText(mName);

        linearlayout.addView(textview[mNumber], textlayoutparama);

        return linearlayout;
    }

    private void hideAndShow() {
        textview = new TextView[100];
        imageView = new ImageView[100];

        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7" tablet
        is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(sizeLarge);

        //textview properties
        commonsparams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            commonsparams.setMarginStart(10);
        } else
            commonsparams.setMargins(10, 0, 0, 0);
        commonsparams.gravity = Gravity.CENTER;
        commonsparams.weight = 1;

        textlayoutparama = new LinearLayout.LayoutParams(
                100, ViewGroup.LayoutParams.WRAP_CONTENT);
        textlayoutparama.gravity = Gravity.CENTER_VERTICAL;

        //textview layout properties
        texparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        texparams.gravity = Gravity.CENTER;
        texparams.weight = 1;

        //imageview layout properties
        imgcommonsparams = new LinearLayout.LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            imgcommonsparams.setMarginStart(10);
        } else
            imgcommonsparams.setMargins(10, 0, 0, 0);
        imgcommonsparams.gravity = Gravity.CENTER;
        imgcommonsparams.weight = 1;

        //imageview properties
        imageparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            imageparams.setMarginStart(35);
        } else
            imageparams.setMargins(35, 0, 0, 0);
        int size = menuDB.size();

        final LinearLayout totalView = new LinearLayout(getActivity());
        for (int i = 0; i < size; i++) {
            String mName = menuDB.get(i).getMenuName();

            totalView.addView(getTextView(i, mName), commonsparams);

        }
        headerLayout.addView(totalView, texparams);

    }

    private void updateSbdSkuReportTable(ArrayList<TaskReportBo> items) {

        if (items == null || items.size() == 0) {
            bmodel.showAlert(getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        ArrayList<TaskReportBo> mylist = new ArrayList<TaskReportBo>();
        if (items == null || items.size() == 0) {
            mylist.clear();

        }
        for (int i = 0; i < taskReportHelper.getTaskretailerinfo().size(); ++i) {
            TaskReportBo ret = (TaskReportBo) items.get(i);
            mylist.add(ret);
        }

        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    private class MyAdapter extends ArrayAdapter<TaskReportBo> {
        private ArrayList<TaskReportBo> items;

        public MyAdapter(ArrayList<TaskReportBo> items) {
            super(getActivity(), R.layout.row_taskexecutionreport, items);
            this.items = items;

        }

        public TaskReportBo getItem(int position) {
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
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                row = inflater.inflate(R.layout.row_taskexecutionreport,
                        parent, false);

                holder = new ViewHolder();

                holder.retailerName = (TextView) row
                        .findViewById(R.id.row_retname);

                holder.ll_rowlayout = (LinearLayout) row
                        .findViewById(R.id.ll_rowlayout);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.outletreport = items.get(position);
            holder.retailerName.setText(holder.outletreport.getmRetailerName());
            holder.retailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


            holder.ll_rowlayout.removeAllViews();

            for (int i = 0; i < menuDB.size(); i++) {
                final LinearLayout imagetotalView = new LinearLayout(getActivity());
                if (holder.outletreport.getmMenuCodeMap() != null) {
                    if (holder.outletreport.getmMenuCodeMap().get(menuDB.get(i).getConfigCode()) != null) {
                        imagetotalView.addView(getImageView(true), imageparams);
                    } else {
                        imagetotalView.addView(getImageView(false), imageparams);
                    }
                } else {
                    imagetotalView.addView(getImageView(false), imageparams);
                }
                holder.ll_rowlayout.addView(imagetotalView, imgcommonsparams);
            }
            return row;
        }

        private ImageView getImageView(Boolean isDone) {

            ImageView imageView = new ImageView(getActivity());

            if (isDone) {
                imageView.setBackgroundResource(R.drawable.ok_tick);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            } else {
                imageView.setBackgroundResource(R.drawable.not_cross);
                imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }
            return imageView;
        }

    }

    class ViewHolder {
        private TaskReportBo outletreport;
        TextView retailerName;
        LinearLayout ll_rowlayout;
    }

}