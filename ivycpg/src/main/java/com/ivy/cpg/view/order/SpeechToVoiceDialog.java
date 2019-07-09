package com.ivy.cpg.view.order;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.RippleBackground;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

@SuppressLint("ValidFragment")
public class SpeechToVoiceDialog extends DialogFragment implements RecognitionListener {

    private TextView txtVoiceToSpeech;

    private SpeechRecognizer speechRecognizer;

    private RippleBackground rippleBackground;

    private Context context;

    private SpeechResultListener speechResultListener;

    public SpeechToVoiceDialog(SpeechResultListener listener){
        speechResultListener=listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        View view = inflater.inflate(R.layout.spech_voice_dialog, container, false);

        txtVoiceToSpeech = view.findViewById(R.id.text_say_something);
        rippleBackground = view.findViewById(R.id.content);

        context = getActivity();


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(this);

        promptSpeechInput();

        return view;
    }

    /**
     * Showing google speech input dialog
     * */
    private void promptSpeechInput() {

        InputMethodManager imm = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null && ((Activity)context).getCurrentFocus() != null)
            imm.hideSoftInputFromWindow(Objects.requireNonNull(((Activity) context).getCurrentFocus()).getWindowToken(), 0);

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {

            if (SpeechRecognizer.isRecognitionAvailable(getActivity())) {
                speechRecognizer.startListening(intent);

                rippleBackground.startRippleAnimation();

            }
            else {
                Toast.makeText(getActivity(),
                        getString(R.string.speech_not_supported),
                        Toast.LENGTH_SHORT).show();
                speechResultListener.dismissDialog();
            }

            //startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (Exception a) {
            Commons.printException(a);
            speechResultListener.dismissDialog();
        }
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {
        speechResultListener.dismissDialog();
    }

    @Override
    public void onError(int error) {
        speechResultListener.dismissDialog();
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> speechResult = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (speechResult != null && speechResult.size() > 0) {
            txtVoiceToSpeech.setText(speechResult.get(0));
            speechResultListener.updateSpeechResult(speechResult.get(0));
        }else
            speechResultListener.dismissDialog();

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
        ArrayList<String> speechResult = partialResults
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (speechResult != null && speechResult.size() > 0) {
            txtVoiceToSpeech.setText(speechResult.get(0));
            speechResultListener.updateSpeechPartialResult(speechResult.get(0));
        }
    }

    @Override
    public void onEvent(int eventType, Bundle params) {

        ArrayList<String> speechResult = params
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        if (speechResult != null && speechResult.size() > 0) {
            speechResultListener.updateSpeechResult(speechResult.get(0));

            Toast.makeText(getActivity(), "onEvent speechResult " + speechResult, Toast.LENGTH_SHORT).show();
        }
    }



}