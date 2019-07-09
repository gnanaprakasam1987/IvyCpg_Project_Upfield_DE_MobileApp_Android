package com.ivy.cpg.view.serializedAsset;

import android.app.DatePickerDialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class SerialNoChangeAdapter extends RecyclerView.Adapter<SerialNoChangeAdapter.ViewHolder> {
    private ArrayList<SerializedAssetBO> assetBOArrayList;
    private BarCodeChangeListener barCodeChangeListener;
    private String serialNoLabel;
    private Context mContext;
    private AppCompatButton dateBtn;
    private String outPutDateFormat;
    private SerializedAssetHelper serializedAssetHelper;
    private InputMethodManager inputManager;
    private BusinessModel mBModel;

    public SerialNoChangeAdapter(Context mContext, ArrayList<SerializedAssetBO> assetBOArrayList, BarCodeChangeListener barCodeChangeListener, String outPutDateFormat, SerializedAssetHelper serializedAssetHelper, BusinessModel mBModel) {
        this.mContext = mContext;
        this.assetBOArrayList = assetBOArrayList;
        this.barCodeChangeListener = barCodeChangeListener;
        this.outPutDateFormat = outPutDateFormat;
        this.serializedAssetHelper = serializedAssetHelper;
        this.mBModel = mBModel;
        serialNoLabel = mContext.getString(R.string.serial_no) + ": ";
        inputManager = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    @NonNull
    @Override
    public SerialNoChangeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_serial_no_change, parent, false);

        return new SerialNoChangeAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SerialNoChangeAdapter.ViewHolder holder, int position) {

        SerializedAssetBO assetBo = assetBOArrayList.get(holder.getAdapterPosition());


        holder.assetNameTv.setText(assetBo.getAssetName());

        String serialNoStr = serialNoLabel + assetBo.getSerialNo();
        holder.oldSerialNoTv.setText(serialNoStr);

        holder.newSerialNoEdTxt.setText(assetBo.getNewSerialNo());

        holder.rentalPriceEdTxt.setText(String.valueOf(assetBo.getRentalPrice()));
        holder.effToDateBtn.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(assetBo.getEffectiveToDate(), outPutDateFormat));

        holder.newSerialNoEdTxt.setOnTouchListener((v, event) -> {
            holder.newSerialNoEdTxt.onTouchEvent(event);
            holder.newSerialNoEdTxt.requestFocus();
            return true;
        });

        holder.newSerialNoEdTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                String newSerialNo = s.toString();

                if (newSerialNo.length() > 1)
                    if (((SerialNoChangeActivity) mContext).isUniqueSerialNo(newSerialNo)) {
                        assetBOArrayList.get(holder.getAdapterPosition()).setNewSerialNo(newSerialNo);
                    } else {
                        newSerialNo = newSerialNo.length() > 1 ? newSerialNo.substring(0,
                                newSerialNo.length() - 1) : "0";
                        holder.newSerialNoEdTxt.setText(newSerialNo);
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.serial_no) + " "
                                + mContext.getResources().getString(R.string.already_exist), Toast.LENGTH_LONG).show();
                    }
            }
        });


        holder.rentalPriceEdTxt.setOnTouchListener((v, event) -> {
            holder.rentalPriceEdTxt.onTouchEvent(event);
            holder.rentalPriceEdTxt.requestFocus();
            return true;
        });

        holder.rentalPriceEdTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                double price = SDUtil.convertToDouble(s.toString());
                assetBo.setRentalPrice(price);
            }
        });

        holder.serialNoCapture.setOnClickListener(v -> {
            inputManager.hideSoftInputFromWindow(((SerialNoChangeActivity) mContext).getCurrentFocus().getWindowToken(), 0);
            barCodeChangeListener.barCodeScan(holder.getAdapterPosition());
        });

        holder.effToDateBtn.setOnClickListener(v -> {
            inputManager.hideSoftInputFromWindow(((SerialNoChangeActivity) mContext).getCurrentFocus().getWindowToken(), 0);
            dateBtn = holder.effToDateBtn;
            dateBtn.setTag(assetBo);
            Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            MyDatePickerDialog myDatePickerDialog = new MyDatePickerDialog(mContext, R.style.DatePickerDialogStyle,
                    mDateSetListener, year, month, day);

            myDatePickerDialog.setPermanentTitle(mContext.getString(R.string.choose_date));
            myDatePickerDialog.show();
        });


    }


    @Override
    public int getItemCount() {
        return assetBOArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView assetNameTv;
        private AppCompatTextView oldSerialNoTv;
        private AppCompatEditText newSerialNoEdTxt;
        private AppCompatImageView serialNoCapture;
        private AppCompatEditText rentalPriceEdTxt;
        private AppCompatButton effToDateBtn;

        public ViewHolder(View itemView) {
            super(itemView);

            assetNameTv = itemView.findViewById(R.id.asset_name);
            oldSerialNoTv = itemView.findViewById(R.id.old_serial_no_tv);
            newSerialNoEdTxt = itemView.findViewById(R.id.new_serial_no_edt);
            serialNoCapture = itemView.findViewById(R.id.barcode_scan_img);
            rentalPriceEdTxt = itemView.findViewById(R.id.edit_rent_price);
            effToDateBtn = itemView.findViewById(R.id.eff_to_date_button);

            if (!serializedAssetHelper.SHOW_ASSET_RENTAL_PRICE) {
                itemView.findViewById(R.id.rent_price_label).setVisibility(View.GONE);
                rentalPriceEdTxt.setVisibility(View.GONE);
            } else {
                if (mBModel.labelsMasterHelper.applyLabels(itemView.findViewById(R.id.rent_price_label).getTag()) != null)
                    ((AppCompatTextView) itemView.findViewById(R.id.rent_price_label))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(itemView.findViewById(R.id.rent_price_label).getTag()));
            }

            if (!serializedAssetHelper.SHOW_ASSET_EFFECTIVE_DATE) {
                itemView.findViewById(R.id.eff_to_date_label).setVisibility(View.GONE);
                effToDateBtn.setVisibility(View.GONE);
            } else {
                if (mBModel.labelsMasterHelper.applyLabels(itemView.findViewById(R.id.eff_to_date_label).getTag()) != null)
                    ((AppCompatTextView) itemView.findViewById(R.id.eff_to_date_label))
                            .setText(mBModel.labelsMasterHelper
                                    .applyLabels(itemView.findViewById(R.id.eff_to_date_label).getTag()));
            }

            if (!serializedAssetHelper.SHOW_SERIAL_NO_IN_UPDATE_REQUEST) {
                newSerialNoEdTxt.setVisibility(View.GONE);
                serialNoCapture.setVisibility(View.GONE);
            }

        }
    }


    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);

            Calendar currentCal = Calendar.getInstance();
            SerializedAssetBO bo = (SerializedAssetBO) dateBtn.getTag();
            if (currentCal.after(selectedDate)) {
                Toast.makeText(mContext,
                        R.string.select_future_date,
                        Toast.LENGTH_SHORT).show();
                bo.setEffectiveToDate("");
                dateBtn.setText("");
            } else {

                bo.setEffectiveToDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }
        }
    };
}
