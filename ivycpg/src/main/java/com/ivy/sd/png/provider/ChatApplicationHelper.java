package com.ivy.sd.png.provider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.widget.Toast;

import com.ivy.sd.png.util.Commons;

import java.util.List;

/**
 * Created by anbarasan.g on 16-10-2015.
 */
public class ChatApplicationHelper {

    private final String EMAIL = "Email";
    private final String PASS = "Pass";
    private final String NAME = "FullName";
    private final String APP_ID = "QB_APP_ID";
    private final String Auth_KEY = "QB_AUTH_KEY";
    private final String Auth_SECRET = "QB_AUTH_SECRET";

    private final Integer NAME_MIN_LENGTH = 3;
    private final Integer PASSWORD_MIN_LENGTH = 8;
    private final Integer INDEX = 0;
    private final Integer NAME_MAX_LENGTH = 50;
    private final Integer PASSWORD_MAX_LENGTH = 40;
    private final String PACKAGE = "com.ivymobility.chatservice";
    private final String PACKAGE_CLASS = "com.ivymobility.chatservice.ui.splash.SplashActivity";
    private static ChatApplicationHelper instance = null;
    private Context context;
    private final String EMAIL_PATTERN = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";


    private ChatApplicationHelper(Context context) {
        this.context = context;
    }

    public static ChatApplicationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ChatApplicationHelper(context);
        }
        return instance;
    }

    public void openChatApplication(String name, String email, String password, String appId, String authKey, String authSecret) {

        // Checking existing package from current android mobile
        boolean isExists = isPackageExisted(context, PACKAGE);

        if (isExists) {
            // navigation to(already downloaded) existing ivy chat application
            String emailName = email.split("@")[INDEX];
            if (name == null || name.length() < NAME_MIN_LENGTH) {
                alert("Name must be more than 2 characters");
                return;
            } else if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
                alert("Password must be more than 7 characters");
                return;
            } else if (email == null || emailName.length() < NAME_MIN_LENGTH) {
                alert("Your Email must have 3 characters in length");
                return;
            } else if (appId == null || appId.equals("")) {
                alert("Please enter a valid App ID");
                return;
            } else if (authKey == null || authKey.equals("")) {
                alert("Please enter a valid Auth Key");
                return;
            } else if (authSecret == null || authSecret.equals("")) {
                alert("Please enter a valid Auth Secret");
                return;
            } else if (!email.matches(EMAIL_PATTERN)) {
                alert("Please enter a valid email");
                return;
            } else if (name.length() > NAME_MAX_LENGTH) {
                alert("Name must not be more than 50 characters");
                return;
            } else if (password.length() > PASSWORD_MAX_LENGTH) {
                alert("Password must not be more than 40 characters");
                return;
            } else {
                moveToChatApp(context, name, email, password, appId, authKey, authSecret);
            }

        } else {
            // here its downloading form google play store
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + PACKAGE)));
            } catch (android.content.ActivityNotFoundException anfe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + PACKAGE)));
            }
        }
    }

    private void alert(String alert) {
        Toast.makeText(context, alert, Toast.LENGTH_LONG).show();
    }

    private void moveToChatApp(Context context, String name, String email, String password, String appId, String authKey, String authSecret) {
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            final ComponentName cn = new ComponentName(
                    PACKAGE,
                    PACKAGE_CLASS);

            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(EMAIL, email);
            intent.putExtra(PASS, password);
            intent.putExtra(NAME, name);
            intent.putExtra(APP_ID, appId);
            intent.putExtra(Auth_KEY, authKey);
            intent.putExtra(Auth_SECRET, authSecret);
            context.startActivity(intent);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean isPackageExisted(Context context, String targetPackage) {
        List<ApplicationInfo> packages;
        PackageManager pm = context.getPackageManager();
        packages = pm.getInstalledApplications(0);
        for (ApplicationInfo packageInfo : packages) {
            if (packageInfo.packageName.equals(targetPackage))
                return true;
        }
        return false;
    }
}
