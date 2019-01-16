package com.ivy.cpg.view.order;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.RippleBackground;

@SuppressLint("ValidFragment")
public class SpeechToVoiceDialog extends DialogFragment {

    private TextView txtVoiceToSpeech;

    public SpeechToVoiceDialog(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = inflater.inflate(R.layout.spech_voice_dialog, container, false);

        txtVoiceToSpeech = view.findViewById(R.id.text_say_something);

        final RippleBackground rippleBackground = view.findViewById(R.id.content);
        rippleBackground.startRippleAnimation();

        try {

            if (getActivity() != null) {
                ((StockAndOrder) getActivity()).setFragment(new StockAndOrder.SpeechResultListener() {
                    @Override
                    public void updateSpeechResult(String result) {
                        txtVoiceToSpeech.setText(result);
                    }
                });
            }
        }catch(Exception e){
            Commons.printException(e);
        }

        return view;
    }



}