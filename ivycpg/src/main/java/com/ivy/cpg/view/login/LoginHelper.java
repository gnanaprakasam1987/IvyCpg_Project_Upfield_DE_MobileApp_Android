package com.ivy.cpg.view.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_EMAIL;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_PASSWORD;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_ROOT_PATH;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.USERS;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.USER_INFO;

/**
 * Created by subramanian.r on 11-11-2015.
 */
public class LoginHelper {

    private final BusinessModel businessModel;
    private static LoginHelper instance = null;

    private static final String CODE_IS_PWD_ENCRYPTED = "ISPWDENC";
    public boolean IS_PASSWORD_ENCRYPTED;
    public boolean SHOW_FORGET_PASSWORD;
    public boolean SHOW_CHANGE_PASSWORD;
    private static final String CODE_CHANGE_PASSWORD = "PWD01";
    private static final String CODE_FORGET_PWD = "PWD02";

    private final String SENDER_ID = "534457766184";
    //    private GoogleCloudMessaging gcm;
    private static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private static final String PROPERTY_IS_REG_ID_NEW = "registration_id_upload";

    private static final String CODE_PWD_LOCK = "FUN46";
    private static final String CODE_MAXIMUM_ATTEMPT_COUNT = "Max_Login_Attempt_count";
    public boolean IS_PASSWORD_LOCK;
    public int MAXIMUM_ATTEMPT_COUNT = 0;
    private String termsContent = "";
    private boolean isTermsAccepted = false;

    private LoginHelper(Context context) {
        this.businessModel = (BusinessModel) context.getApplicationContext();
    }

    public static LoginHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoginHelper(context);
        }
        return instance;
    }

    public void clearInstance() {
        instance = null;
    }

    public void loadPasswordConfiguration(Context mContext) {
        DBUtil db;
        db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb;
            sb = new StringBuffer();
            sb.append("select flag from hhtmodulemaster where hhtcode =");
            sb.append(businessModel.QT(CODE_PWD_LOCK) + " and ForSwitchSeller = 0");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    int value = c.getInt(0);
                    if (value == 1) {
                        sb = new StringBuffer();
                        sb.append("select RField from hhtmodulemaster where hhtcode =");
                        sb.append(businessModel.QT(CODE_MAXIMUM_ATTEMPT_COUNT));
                        sb.append(" and Flag=1 and ForSwitchSeller = 0");
                        c = db.selectSQL(sb.toString());
                        if (c.getCount() > 0) {
                            if (c.moveToNext()) {

                                MAXIMUM_ATTEMPT_COUNT = c.getInt(0);

                            }
                        }
                        if (MAXIMUM_ATTEMPT_COUNT > 0) {
                            int listId = businessModel.configurationMasterHelper.getActivtyType("RESET_PWD");
                            if (listId != 0)
                                IS_PASSWORD_LOCK = true;
                        }
                    }
                }
            }


            SHOW_CHANGE_PASSWORD = false;
            SHOW_FORGET_PASSWORD = false;

            sb = new StringBuffer();
            sb.append("SELECT hhtcode FROM hhtmodulemaster WHERE (hhtcode = ");
            sb.append(businessModel.QT(CODE_CHANGE_PASSWORD));
            sb.append(" OR hhtcode = ");
            sb.append(businessModel.QT(CODE_FORGET_PWD));
            sb.append(") AND Flag = 1 and ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if (c.getString(0).equalsIgnoreCase(CODE_CHANGE_PASSWORD)) {
                        SHOW_CHANGE_PASSWORD = true;
                    } else if (c.getString(0).equalsIgnoreCase(CODE_FORGET_PWD)) {
                        SHOW_FORGET_PASSWORD = true;
                    }
                }
            }

            IS_PASSWORD_ENCRYPTED = false;

            sb = new StringBuffer();
            sb.append("SELECT hhtcode FROM hhtmodulemaster WHERE hhtcode = ");
            sb.append(businessModel.QT(CODE_IS_PWD_ENCRYPTED));
            sb.append(" AND Flag = 1 and ForSwitchSeller = 0");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    IS_PASSWORD_ENCRYPTED = true;
                }
            }

        } catch (Exception e) {
            db.closeDB();
            Commons.printException(e);
        }
    }

    public String getSupportNo(Context mContext) {
        DBUtil db = new DBUtil(mContext.getApplicationContext(), DataMembers.DB_NAME
        );
        String support_no = "";

        try {

            db.openDataBase();
            String sb = "select listname from standardlistmaster " +
                    "where listtype= 'HELPLINE_TYPE' and ListCode = 'PHONE'";
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    support_no = c.getString(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.print(e.getMessage());
            if (db != null) {
                db.closeDB();
            }
        }
        return support_no;
    }

    public void onFCMRegistration(final Context mContext) {
        final SharedPreferences prefs = AppUtils.getSharedPreferences(mContext);
        boolean registrationId = prefs.getBoolean(PROPERTY_IS_REG_ID_NEW, false);

        if (checkPlayServices(mContext.getApplicationContext())) {

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Commons.printException(task.getException());
                                return;
                            }

                            final String fcmToken = task.getResult().getToken();

                            //Send FCM Token to aws server if change in Fcm Reg id
                            if (!registrationId)
                                registerInBackground(mContext, fcmToken);


                            if (isRealTimeConfigAvail(mContext) || isSupervisorMenuAvail(mContext)) {
                                final String domainName = getDomainName(mContext);
                                final String loginName = businessModel.userMasterHelper.getUserMasterBO().getLoginName();

                                final String email = loginName + "@" + domainName + ".com";

                                if (FirebaseAuth.getInstance().getCurrentUser() == null && !email.equals("")) {

                                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, FIREBASE_PASSWORD)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task1) {
                                                    // Get new Instance ID token

                                                    if (!task1.isSuccessful()) {
                                                        if (task1.getException() instanceof FirebaseAuthUserCollisionException) {
                                                            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, FIREBASE_PASSWORD)
                                                                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<AuthResult> task1) {

                                                                            if (isSupervisorMenuAvail(mContext))
                                                                                updateTokenInFirebase(mContext, fcmToken, domainName);

                                                                            storeFireBaseCredentials(mContext,
                                                                                    email,
                                                                                    domainName);

                                                                            if (businessModel.configurationMasterHelper.IS_FIREBASE_CHAT_ENABLED)
                                                                                businessModel.initializeChatSdk();

                                                                        }
                                                                    });
                                                        }
                                                    } else {

                                                        if (isSupervisorMenuAvail(mContext))
                                                            updateTokenInFirebase(mContext, fcmToken, domainName);

                                                        storeFireBaseCredentials(mContext,
                                                                email,
                                                                domainName);

                                                        if (businessModel.configurationMasterHelper.IS_FIREBASE_CHAT_ENABLED)
                                                            businessModel.initializeChatSdk();

                                                    }

                                                }
                                            });
                                }

                            }
                        }
                    });


            //Subscribe Topic Name
            final String topicName = getFCMTopicName(mContext);
            if (topicName != null && !topicName.equals("")) {

                String[] topicNameArr = topicName.split(",");

                for (String topic : topicNameArr) {

                    if (validateTopicName(topic)) {
                        FirebaseMessaging.getInstance().subscribeToTopic(topic);
                    }
                }
            }

        } else {
            Commons.printInformation("No valid Google Play Services APK found.");
        }
    }

    private boolean isRealTimeConfigAvail(Context context) {
        boolean isRealTimeConfigAvail = false;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor moduleCursor = db.selectSQL("select hhtcode from HhtModuleMaster where hhtCode = 'REALTIME01' OR hhtCode = 'CHAT02' OR hhtCode = 'UPLOADATTENDANCE'");
            if (moduleCursor != null && moduleCursor.getCount() > 0) {
                isRealTimeConfigAvail = true;
                moduleCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return isRealTimeConfigAvail;

    }

    private boolean isSupervisorMenuAvail(Context context) {
        boolean isSupervisorMenuAvail = false;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor menuCursor = db.selectSQL("select hhtcode from HhtMenuMaster where hhtcode = 'MENU_SUPERVISOR_ACTIVITY'");
            if (menuCursor != null && menuCursor.getCount() > 0) {
                isSupervisorMenuAvail = true;
                menuCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return isSupervisorMenuAvail;

    }

    //Validating Topic Name to match the regex
    private boolean validateTopicName(final String name) {

        Pattern pattern = Pattern.compile("[a-zA-Z0-9-_.~%]{1,900}");

        Matcher matcher = pattern.matcher(name);
        return matcher.matches();

    }


    //Get Domain Name to make base node in Firebase Database
    private String getDomainName(Context context) {
        String rootPath = "";

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select DomainName from AppVariables");
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    rootPath = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return rootPath == null ? "" : rootPath;
    }


    //Get Fcm Topic Names to Subscribe
    private String getFCMTopicName(Context context) {
        StringBuilder topicName = new StringBuilder();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select ListCode from StandardListMaster where ListType='PUSH_TOPICS'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {

                    topicName.append(",").append(c.getString(0));
                }

            }
            c.close();

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }


        return topicName.length() > 0 ? topicName.substring(1) : "";
    }


    /**
     * Stores the FIREBASE_ROOT_PATH and the FIREBASE_EMAIL in the application's
     * {@code SharedPreferences}.
     *
     * @param context    application's context.
     * @param domainName FIREBASE_ROOT_PATH
     */
    private void storeFireBaseCredentials(Context context, String email, String domainName) {

        final SharedPreferences prefs = AppUtils.getSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Commons.printInformation("Saving Domain on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(FIREBASE_ROOT_PATH, domainName);
        editor.putString(FIREBASE_EMAIL, email);
        editor.apply();
    }

    //Set User Fcm Registration Id in Firestore Database Node
    private void updateTokenInFirebase(Context context, String token, String rootPath) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("token", token);

        int positionId = getUserPositionId(context);

        if (positionId == 0)
            return;

        db.collection(rootPath)
                .document(USERS)
                .collection(USER_INFO)
                .document(positionId + "")
                .set(userInfo);
    }


    //Get User Position Id to save Users Fcm Reg id (Notification Purpose)
    private int getUserPositionId(Context context) {
        int posId = 0;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select UserPositionId from usermaster where userid =" + StringUtils.QT(businessModel.userMasterHelper.getUserMasterBO().getUserid() + ""));
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    posId = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return posId;

    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new Resources.NotFoundException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If it
     * doesn't, display a dialog that allows users to download the APK from the
     * Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(Context mContext) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(mContext);
        /*int resultCode = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(mContext);*/
        if (resultCode != ConnectionResult.SUCCESS) {
            if (!googleApiAvailability.isUserResolvableError(resultCode)) {
                Commons.printInformation("This device is not supported.");
            }
            return false;
        }
        return true;
    }

    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and the app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground(final Context mContext, final String token) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg;
                try {
//                    if (gcm == null)
//                        gcm = GoogleCloudMessaging.getInstance(mContext);
//
//                    businessModel.regid = gcm.register(SENDER_ID);

                    businessModel.regid = token;
                    msg = "Device registered, registration ID=" + businessModel.regid;
                    if (BuildConfig.FLAVOR.equalsIgnoreCase("aws"))
                        businessModel.synchronizationHelper.updateAuthenticateToken(false);
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    Commons.printException(ex);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {
                if (businessModel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    storeRegistrationId(mContext, businessModel.regid);
                } else {
                    String errorMsg = businessModel.synchronizationHelper.getErrormessageByErrorCode().get(businessModel.synchronizationHelper.getAuthErroCode());
                    if (errorMsg != null) {
                        Toast.makeText(mContext, errorMsg, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }.execute(null, null, null);
    }

    /**
     * Stores the registration ID and the app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId   registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = AppUtils.getSharedPreferences(context);
        int appVersion = getAppVersion(context);
        Commons.printInformation("Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.putBoolean(PROPERTY_IS_REG_ID_NEW, true);
        editor.apply();
    }

    public boolean isPasswordReset(Context mContext) {
        boolean isReset = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select IsResetPassword from usermaster where loginid ='" + businessModel.userNameTemp + "' COLLATE NOCASE");
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    isReset = c.getInt(0) > 0;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return isReset;
    }

    public void deleteUserMaster(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_userMaster, null, true);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * This method will restore the database saved in External storage into
     * application.
     *
     * @return true - successful and false - failed
     */
    public boolean reStoreDB(Context mContext) {
        try {
            File sd = Environment.getExternalStorageDirectory();
            if (sd.canRead()) {
                File currentDB = new File(mContext.getDatabasePath(DataMembers.DB_NAME).getPath());
                File backupDB = new File(
                        mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/pandg/" + DataMembers.DB_NAME);

                if (backupDB.exists()) {
                    FileChannel src = new FileInputStream(backupDB)
                            .getChannel();
                    FileChannel dst = new FileOutputStream(currentDB)
                            .getChannel();
                    dst.transferFrom(src, 0, src.size());
                    src.close();
                    dst.close();
                    return true;
                }
            }
        } catch (Exception e) {
            Commons.printException("Synchronisation," + e + "");
        }

        return false;
    }

    /**
     * deleteAllValues will be called before updating the apk. This method will
     * delete the database completely and also update AutoUpdate shared
     * preference.
     */
    public void deleteAllValues(Context mContext) {

        try {
            mContext.deleteDatabase(DataMembers.DB_NAME);
            businessModel.synchronizationHelper.deleteDBFromSD();
            SharedPreferences pref = mContext.getSharedPreferences("autoupdate",
                    MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = pref.edit();
            prefsEditor.putString("URL", "");
            prefsEditor.putString("isUpdateExist", "False");
            prefsEditor.apply();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public String getPasswordCreatedDate(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        String date = "";
        try {
            db.createDataBase();
            db.openDataBase();
            String query = "select PasswordCreatedDate from AppVariables";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    date = c.getString(0);


                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return date;
    }

    public String getPasswordExpiryDate(String createdDate) {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        try {
            today.setTime(formatter.parse(createdDate));
            today.add(Calendar.DATE, businessModel.configurationMasterHelper.PSWD_EXPIRY);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return formatter.format(today.getTime());

    }

    /**
     * To download Terms and Conditions from AppVariables Table
     */
    public void downloadTermsAndConditions(Context context) {

        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("select TermsContent,isTermsAccepted from AppVariables");

            if (c != null) {
                while (c.moveToNext()) {
                    termsContent = c.getString(0);
                    isTermsAccepted = c.getInt(1) == 1;
                    setTermsAccepted(isTermsAccepted);
                    setTermsContent(termsContent);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String getTermsContent() {
        return termsContent;
    }

    private void setTermsContent(String termsContent) {
        this.termsContent = termsContent;
    }

    public boolean isTermsAccepted() {
        return isTermsAccepted;
    }

    private void setTermsAccepted(boolean termsAccepted) {
        isTermsAccepted = termsAccepted;
    }

    public int getNearByDueDataTaskAvail(Context mContext) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        int taskCount = 0;
        try {
            db.openDataBase();
            String maxDueDate = DateTimeUtils.getRequestedDateByGetType(businessModel.configurationMasterHelper.IS_TASK_DUDE_DATE_COUNT, Calendar.DATE);
            String query = "select distinct A.taskid" +
                    " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid " +
                    " left join DatewisePlan DWP on DWP.Date = B.DueDate" +
                    " and DWP.EntityId = A.retailerID and DWP.Status!='D' and DWP.EntityType = 'RETAILER'" +
                    " where B.DueDate<=" + StringUtils.QT(maxDueDate) + " and DWP.Date IS NULL and (B.Status!='D' OR B.Status IS NULL)" +
                    " and A.retailerId!=0 and A.TaskId not in (Select taskid from TaskHistory where RetailerId = A.retailerId)";

            Cursor c = db.selectSQL(query);

            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    taskCount = c.getCount();
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
        return taskCount;
    }
}
