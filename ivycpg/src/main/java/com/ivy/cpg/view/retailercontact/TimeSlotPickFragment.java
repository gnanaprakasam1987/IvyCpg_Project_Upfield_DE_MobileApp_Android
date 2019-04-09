package com.ivy.cpg.view.retailercontact;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ivy.cpg.view.retailercontact.customview.WheelDayPicker;
import com.ivy.cpg.view.retailercontact.customview.WheelHourPicker;
import com.ivy.cpg.view.retailercontact.customview.WheelPicker;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class TimeSlotPickFragment extends DialogFragment {

    @NonNull
    private WheelDayPicker daysPickerFrom;
    @NonNull
    private WheelHourPicker hoursPickerFrom;
    @NonNull
    private WheelHourPicker hoursPickerTo;

    private List<WheelPicker> pickers = new ArrayList<>();

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_slot_pick, container, false);

        daysPickerFrom = view.findViewById(R.id.dayPicker);
        hoursPickerFrom = view.findViewById(R.id.hoursPicker);

        hoursPickerTo = view.findViewById(R.id.hoursPicker_to);

        pickers.addAll(Arrays.asList(
                daysPickerFrom,
                hoursPickerFrom,
                hoursPickerTo
        ));

        daysPickerFrom
                .setOnDaySelectedListener(new WheelDayPicker.OnDaySelectedListener() {
                    @Override
                    public void onDaySelected(WheelDayPicker picker, int position, String name, Date date) {
                    }
                });


        hoursPickerFrom
                .setOnFinishedLoopListener(new WheelHourPicker.FinishedLoopListener() {
                    @Override
                    public void onFinishedLoop(WheelHourPicker picker) {
                        daysPickerFrom.scrollTo(daysPickerFrom.getCurrentItemPosition() + 1);
                    }
                })
                .setHourChangedListener(new WheelHourPicker.OnHourChangedListener() {
                    @Override
                    public void onHourChanged(WheelHourPicker picker, int hour) {
                    }
                });

        hoursPickerTo
                .setOnFinishedLoopListener(new WheelHourPicker.FinishedLoopListener() {
                    @Override
                    public void onFinishedLoop(WheelHourPicker picker) {
                        hoursPickerTo.scrollTo(hoursPickerTo.getCurrentItemPosition() + 1);
                    }
                })
                .setHourChangedListener(new WheelHourPicker.OnHourChangedListener() {
                    @Override
                    public void onHourChanged(WheelHourPicker picker, int hour) {
                    }
                });

        hoursPickerFrom.setCyclic(true);
        hoursPickerTo.setCyclic(true);
        daysPickerFrom.setCyclic(true);

        hoursPickerFrom.setCurved(true);
        hoursPickerTo.setCurved(true);
        daysPickerFrom.setCurved(true);

        view.findViewById(R.id.okay_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (hoursPickerTo.getCurrentHour() == hoursPickerFrom.getCurrentHour() ||
                        hoursPickerTo.getCurrentHour() < hoursPickerFrom.getCurrentHour()) {
                    Toast.makeText(context, context.getString(R.string.from_time_should_be_lesser), Toast.LENGTH_SHORT).show();
                }else {

                    RetailerContactAvailBo retailerContactAvailBo = new RetailerContactAvailBo();
                    retailerContactAvailBo.setDay(daysPickerFrom.getCurrentDay());
                    retailerContactAvailBo.setFrom(hoursPickerFrom.getSelectedTime());
                    retailerContactAvailBo.setTo(hoursPickerTo.getSelectedTime());

                    EventBus.getDefault().post(retailerContactAvailBo);
                }
            }
        });

        return view;
    }


}
