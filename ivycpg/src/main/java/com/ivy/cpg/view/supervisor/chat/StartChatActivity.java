package com.ivy.cpg.view.supervisor.chat;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.sd.png.asean.view.R;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.base.BaseNetworkAdapter;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.session.NetworkManager;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.AccountType;
import co.chatsdk.core.types.AuthKeys;
import co.chatsdk.ui.login.LoginActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class StartChatActivity extends co.chatsdk.ui.main.BaseActivity {

    // This is a list of extras that are passed to the login view
    protected HashMap<String, Object> extras = new HashMap<>();

    private String userChatId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_chat);

        setTaskDescription(getTaskDescriptionBitmap(), getTaskDescriptionLabel(), getTaskDescriptionColor());

        userChatId = getIntent().getExtras()!=null?getIntent().getExtras().getString("UUID"):"";

        if (ChatSDK.currentUser() == null) {
            //showProgressDialog("Connecting ...");
            customAuth();
        }
        else {

            if (userChatId.equals("")) {
                InterfaceManager.shared().a.startMainActivity(this);
                finish();
            }else
                startChatActivity();
        }

    }

    protected int getTaskDescriptionColor(){
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    protected String getTaskDescriptionLabel(){
        return (String) getTitle();
    }

    protected Bitmap getTaskDescriptionBitmap(){
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    }

    private void customAuth(){

        AccountDetails accountDetails = AccountDetails.username(SupervisorModuleConstants.FIREBASE_EMAIL, SupervisorModuleConstants.FIREBASE_PASSWORD);

        NetworkManager.shared().a.auth.authenticate(accountDetails).observeOn(AndroidSchedulers.mainThread())
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        dismissProgressDialog();
                    }
                })
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        if (userChatId.equals(""))
                            startMainActivity();
                        else
                            startChatActivity();
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable e) throws Exception {
                        toastErrorMessage(e, false);
                        ChatSDK.logError(e);

                        finish();
                    }
                });
    }

    private void startChatActivity(){
        Intent openChatIntent = new Intent(this, ChatSDK.ui().getChatActivity());
        openChatIntent.putExtra(InterfaceManager.THREAD_ENTITY_ID, userChatId);
        openChatIntent.setAction(userChatId);
        openChatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(openChatIntent);
        finish();
    }

    private void startMainActivity(){
        ChatSDK.ui().startMainActivity(StartChatActivity.this, extras);
        finish();
    }

    public void toastErrorMessage(Throwable error, boolean login){
        String errorMessage = "";

        if (StringUtils.isNotBlank(error.getMessage())) {
            errorMessage = error.getMessage();
        }
        else if (login) {
            errorMessage = getString(co.chatsdk.ui.R.string.login_activity_failed_to_login_toast);
        }
        else {
            errorMessage = getString(co.chatsdk.ui.R.string.login_activity_failed_to_register_toast);
        }

        showToast(errorMessage);
    }

    public static Map<String, Object> getMap(String[] keys,  Object...values){
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0 ; i < keys.length; i++){

            // More values then keys entered.
            if (i == values.length)
                break;

            map.put(keys[i], values[i]);
        }

        return map;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateExtras(intent.getExtras());
    }

    protected void updateExtras (Bundle bundle) {
        if (bundle != null) {
            for (String s : bundle.keySet()) {
                extras.put(s, bundle.get(s));
            }
        }
    }
}
