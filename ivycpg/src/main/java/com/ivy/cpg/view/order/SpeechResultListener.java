package com.ivy.cpg.view.order;

public interface SpeechResultListener {
    void updateSpeechResult(String result);
    void updateSpeechPartialResult(String result);
    void dismissDialog();
}
