package com.ivy.cpg.view.supervisor.chat;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.TypedValue;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import co.chatsdk.core.dao.DaoCore;
import co.chatsdk.core.dao.Keys;
import co.chatsdk.core.dao.User;
import co.chatsdk.core.session.ChatSDK;
import co.chatsdk.core.session.InterfaceManager;
import co.chatsdk.core.session.NetworkManager;
import co.chatsdk.core.session.StorageManager;
import co.chatsdk.core.types.AccountDetails;
import co.chatsdk.core.types.ConnectionType;
import co.chatsdk.firebase.FirebaseEventHandler;
import co.chatsdk.ui.utils.ToastHelper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

import static co.chatsdk.firebase.FirebasePaths.IndexPath;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_EMAIL;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.USERS;

public class StartChatActivity extends co.chatsdk.ui.main.BaseActivity {

    // This is a list of extras that are passed to the login view
    protected HashMap<String, Object> extras = new HashMap<>();

    private String userChatId = "",name="";

    private boolean isUserValidated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_chat);


        if (AppUtils.getSharedPreferences(this).getString(FIREBASE_EMAIL, "").equals("")) {
            Toast.makeText(this, "No Chat SDk Initialized", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userChatId = getIntent().getExtras() != null ? getIntent().getExtras().getString("UUID") : "";
        name = getIntent().getExtras() != null ? getIntent().getExtras().getString("name") : "";

        if (ChatSDK.shared().context == null){
            BusinessModel businessModel = (BusinessModel)getApplicationContext();
            businessModel.initializeChatSdk();
        }

        if (ChatSDK.currentUser() == null) {
            //showProgressDialog("Connecting ...");
            customAuth();
        }
        else {

            // To-do Need To Check authenticateWithUser in FirebaseAuthenticationHandler for further implementation

            FirebaseEventHandler.shared().currentUserOn(ChatSDK.currentUser().getEntityID());

            setUserName();

            if (userChatId.equals("")) {
                InterfaceManager.shared().a.startMainActivity(this);
                finish();
            }else {
                final User user = DaoCore.fetchEntityWithEntityID(User.class, userChatId);
                if (user != null)
                    user.setName(name);
                getmessageId(user);

//                startChatActivity();
            }
        }

    }

    private void setUserName() {
        try {
            BusinessModel businessModel = (BusinessModel) getApplicationContext();
            ChatSDK.currentUser().setName(businessModel.getAppDataProvider().getUser().getUserName());
            ChatSDK.currentUser().update();

            setUserInfo();
        }catch (Exception e){
            Commons.printException(e);
        }
    }

    protected int getTaskDescriptionColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = this.getTheme();
        theme.resolveAttribute(R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    protected String getTaskDescriptionLabel() {
        return (String) getTitle();
    }

    protected Bitmap getTaskDescriptionBitmap() {
        return BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
    }

    private void customAuth() {

        String email = AppUtils.getSharedPreferences(this).getString(FIREBASE_EMAIL, "");

        if (email.equals("")) {
            Toast.makeText(this, "Some Error Occured", Toast.LENGTH_SHORT).show();
            return;
        }

        AccountDetails accountDetails = AccountDetails.username(email, SupervisorModuleConstants.FIREBASE_PASSWORD);

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

                        setUserName();

                        if (userChatId.equals("")) {
                            InterfaceManager.shared().a.startMainActivity(StartChatActivity.this);
                            finish();
                        } else {

                            final User user = DaoCore.fetchEntityWithEntityID(User.class, userChatId);
                            if (user != null)
                                user.setName(name);
                            getmessageId(user);
                        }
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

    private void getUserInfo() {
        DatabaseReference mDatabase;

        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(AppUtils.getSharedPreferences(this).getString(FIREBASE_ROOT_PATH, ""))
                .child(USERS).child(userChatId).child("meta")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        User user = StorageManager.shared().fetchOrCreateEntityWithEntityID(User.class, userChatId);

                        HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

                        if (value != null) {

                            Commons.print("user in On Datachange= " + user);

                            user.setEntityID(userChatId);
                            user.setEmail(value.get(Keys.Email) != null ? value.get(Keys.Email).toString() : "");
                            user.setName(value.get(Keys.Name) != null ? value.get(Keys.Name).toString() : "");
                            user.setLocation(value.get(Keys.Location) != null ? value.get(Keys.Location).toString() : "");
                            user.setAvatarURL(value.get(Keys.AvatarURL) != null ? value.get(Keys.AvatarURL).toString() : "");

                            if (user.getName().equals(""))
                                user.setName(name);

                            ChatSDK.contact().addContact(user, ConnectionType.Contact);

                            getmessageId(user);

                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                        databaseError.toException();

                    }
                });

    }

    private void setUserInfo(){

        User user = ChatSDK.currentUser();

        Map<String,Object> mapVal = new HashMap<>();
        mapVal.put("name",user.getName());

        DatabaseReference mDatabase;
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.child(AppUtils.getSharedPreferences(this).getString(FIREBASE_ROOT_PATH, ""))
                .child(USERS).child(user.getEntityID()).child("meta")
                .updateChildren(mapVal);

        mDatabase.child(AppUtils.getSharedPreferences(this).getString(FIREBASE_ROOT_PATH, ""))
                .child(IndexPath).child(user.getEntityID())
                .updateChildren(mapVal);
    }

    private void startChatActivity() {
        Intent openChatIntent = new Intent(this, ChatSDK.ui().getChatActivity());
        openChatIntent.putExtra(InterfaceManager.THREAD_ENTITY_ID, extras);
        //openChatIntent.setAction(userChatId);
        openChatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(openChatIntent);
        finish();
    }

    private void startMainActivity() {
        ChatSDK.ui().startMainActivity(StartChatActivity.this, extras);
        finish();
    }

    private void getmessageId(User user) {

        ChatSDK.thread().createThread("", user, ChatSDK.currentUser())
                .observeOn(AndroidSchedulers.mainThread())
                .doFinally(() -> {
                    dismissProgressDialog();
                })
                .subscribe(thread -> {

                    Commons.print("Thread Created");

                    extras.put(InterfaceManager.THREAD_ENTITY_ID, thread.getEntityID());
                    startMainActivity();

                }, throwable -> {
                    ToastHelper.show(getApplicationContext(), throwable.getLocalizedMessage());

                    Commons.print("Thread not created, Error");

                    if (isUserValidated) {
                        InterfaceManager.shared().a.startMainActivity(this);
                        finish();
                    } else {
                        isUserValidated = true;
                        getUserInfo();
                    }
                });
    }

    public void toastErrorMessage(Throwable error, boolean login) {
        String errorMessage = "";

        if (StringUtils.isNotBlank(error.getMessage())) {
            errorMessage = error.getMessage();
        } else if (login) {
            errorMessage = getString(co.chatsdk.ui.R.string.login_activity_failed_to_login_toast);
        } else {
            errorMessage = getString(co.chatsdk.ui.R.string.login_activity_failed_to_register_toast);
        }

        showToast(errorMessage);
    }

    public static Map<String, Object> getMap(String[] keys, Object... values) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < keys.length; i++) {

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

    protected void updateExtras(Bundle bundle) {
        if (bundle != null) {
            for (String s : bundle.keySet()) {
                extras.put(s, bundle.get(s));
            }
        }
    }


}
