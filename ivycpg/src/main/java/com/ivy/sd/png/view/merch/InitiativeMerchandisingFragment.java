package com.ivy.sd.png.view.merch;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SBDMerchandisingBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class InitiativeMerchandisingFragment extends IvyBaseFragment {
    private TypedArray typearr;
    private LinearLayout main;
    private BusinessModel bmodel;
    private Vector<SBDMerchandisingBO> sbdMerchandisingVector;
    private String[] brandIds;
    private CheckBox checkBox[];
    View view;
    boolean bool = false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_merchandising, container, false);

        main = (LinearLayout) view.findViewById(R.id.sbdmerchandisingLayout);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.title).getTag()) != null)
                ((TextView) view.findViewById(R.id.title))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.title)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            // return;
        }

        sbdMerchandisingVector = bmodel.sbdMerchandisingHelper.downloadSBDMerchandising("MERCH_INIT");
        brandIds = bmodel.sbdMerchandisingHelper.getDistinctBrandIdOfSBDMerchandising("MERCH_INIT");
        checkBox = new CheckBox[sbdMerchandisingVector.size()];

        if (brandIds != null)
            constructScreen();

        return view;
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        boolean bool = false;

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().getParent().finish();
        }

        main.removeAllViews();

		/*
         * sbdMerchandisingVector =
		 * bmodel.sbdMerchandisingHelper.downloadSBDMerchandising("MERCH_INIT");
		 * brandIds =
		 * bmodel.sbdMerchandisingHelper.getDistinctBrandIdOfSBDMerchandising
		 * ("MERCH_INIT");
		 */
        checkBox = new CheckBox[sbdMerchandisingVector.size()];
        if (brandIds != null) {
            constructScreen();
        }
    }

    private void constructScreen() {

        int count = 0;
        int siz = sbdMerchandisingVector.size();
//
        for (int i = 0; i < brandIds.length; i++) {

            TextView tv = new TextView(getActivity());
//			tv.setText("-");
            tv.setTextColor(Color.BLACK);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.font_medium));
            tv.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            main.addView(tv);

            for (int j = 0; j < siz; j++) {
                SBDMerchandisingBO sbd = sbdMerchandisingVector.get(j);
                if (brandIds[i].equals(sbd.getBrandid() + "")) {
                    tv.setText(sbd.getBrandName());
                    checkBox[count] = new CheckBox(getActivity());
                    checkBox[count].setText(sbd.getValueText());
                    checkBox[count].setTag(sbd);
                    checkBox[count].setChecked(sbd.isDone());
                    checkBox[count].setTextColor(Color.BLACK);
                    main.addView(checkBox[count]);

                    checkBox[count].setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {

                            CheckBox cbox = (CheckBox) v;
                            if (cbox.isChecked()) {
                                bmodel.sbdMerchandisingHelper.setSBDStatus(
                                        (SBDMerchandisingBO) cbox.getTag(),
                                        true);
                            } else {
                                bmodel.sbdMerchandisingHelper.setSBDStatus(
                                        (SBDMerchandisingBO) cbox.getTag(),
                                        false);
                            }

                        }
                    });

                    count++;
                }
            }
        }
    }

    public void onSave() {
        if (!bool) {
            bool = true;
            try {
                bmodel.sbdMerchandisingHelper.update_Is_Init_Merchandising_Done_Flag();
                bmodel.sbdMerchandisingHelper.setIsInitMerchandisingDone("Y");
                bmodel.getRetailerMasterBO().setIsInitMerchandisingDone("Y");

		/*for (int i = 0; i < checkBox.length; i++) {

			if (checkBox[i].isChecked()) { // bool = true;
				bmodel.sbdMerchandisingHelper.setSBDStatus(
						(SBDMerchandisingBO) checkBox[i].getTag(), true);
			} else { // bool = false;
				bmodel.sbdMerchandisingHelper.setSBDStatus(
						(SBDMerchandisingBO) checkBox[i].getTag(), false);
			}
		}*/

		/*
		 * if (checkBox.length == 0) {
		 * 
		 * finish(); BusinessModel.loadActivity(MerchPricing.this,
		 * DataMembers.actHomeScreenTwo); return; }
		 */
		/*
		 * else if (!bool) { Toast.makeText( this, getResources().getString(
		 * R.string.please_check_any_items_to_save), Toast.LENGTH_SHORT).show();
		 * 
		 * return; }
		 */

                if (checkBox.length > 0) {
                    new SaveTask().execute();
                } else {
                    getActivity().finish();
                    BusinessModel.loadActivity(getActivity(), DataMembers.actHomeScreenTwo);
                }
            } catch (Exception e) {
                // TODO: handle exception
                Commons.printException(e);
                bool = false;
            }
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(

                                getResources().getString(R.string.doyouwantgoback))

                        .setPositiveButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        getActivity().finish();
                                        BusinessModel.loadActivity(getActivity(),
                                                DataMembers.actHomeScreenTwo);

                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

									/* User clicked Cancel so do some stuff */
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
        }
        return null;

    }

    class SaveTask extends AsyncTask<Integer, Integer, Boolean> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.sbdMerchandisingHelper
                        .saveSBDMerchandising("MERCH_INIT");
                bmodel.sbdMerchandisingHelper.updateSBDInitMerchandisingAchieved();
            } catch (Exception e) {
                Commons.printException(e);
                bool = false;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD,
                    getResources()
                            .getString(R.string.saving_init_merchandising),
                    true, false);*/
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving_init_merchandising));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            alertDialog.dismiss();
         //   progressDialogue.dismiss();
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.sbdMerchandisingHelper.clearMerchandising();
            bmodel.showAlert(getResources().getString(R.string.saved_successfully), -27);
            bool = false;
			/*getActivity().finish();
			BusinessModel.loadActivity(getActivity(),
					DataMembers.actHomeScreenTwo);*/
        }
    }



}
