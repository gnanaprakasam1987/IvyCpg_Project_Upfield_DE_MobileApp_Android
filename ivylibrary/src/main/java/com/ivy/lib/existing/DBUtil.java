package com.ivy.lib.existing;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ivy.lib.BuildConfig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBUtil extends SQLiteOpenHelper {

    private final String DB_PATH;
    private final String DB_NAME;
    private SQLiteDatabase db;
    private final Context myContext;
    private net.sqlcipher.database.SQLiteDatabase db_encrypted = null;
    public static boolean isEncrypted;
    public static String DB_PASSWORD = "password";

    public DBUtil(Context ctx, String mDBName, String mDBPath) {
        super(ctx, mDBName, null, 2);
        this.myContext = ctx;
        this.DB_NAME = mDBName;
        this.DB_PATH = mDBPath;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        this.db = db;
    }

    public void insertSQL(String tbl, String columns, String content) {
        String sql = "insert into " + tbl + "(" + columns + ") values("
                + content + ")";
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getName(), sql);
        }

        if (!isEncrypted)
            db.execSQL(sql);
        else
            db_encrypted.execSQL(sql);
    }

    public void multiInsert(String sql) {
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getName(), sql);
        }

        if (!isEncrypted)
            db.execSQL(sql);
        else
            db_encrypted.execSQL(sql);
    }

    public void updateSQL(String sql) {
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getName(), sql);
        }

        if (!isEncrypted)
            db.execSQL(sql);
        else
            db_encrypted.execSQL(sql);

    }

    public void deleteSQL(String tbl, String where, boolean all) {
        String sql;
        if (all)
            sql = "delete from " + tbl;
        else
            sql = "delete from " + tbl + " where " + where;
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getName(), sql);
        }

        if (!isEncrypted)
            db.execSQL(sql);
        else
            db_encrypted.execSQL(sql);
    }

    public void executeQ(String sql) {
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getName(), sql);
        }

        if (!isEncrypted)
            db.execSQL(sql);
        else
            db_encrypted.execSQL(sql);

    }

    public Cursor selectSQL(String sql) {
        if (BuildConfig.DEBUG) {
            Log.d(this.getClass().getName(), sql);
        }

        if (!isEncrypted)
            return db.rawQuery(sql, null);
        else
            return db_encrypted.rawQuery(sql, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        // TODO Auto-generated method stub
    }

    public void closeDB() {
        if (!isEncrypted)
            this.db.close();
        else if (db_encrypted != null)
            db_encrypted.close();
    }

    public void createDataBase() {
        boolean dbExist = checkDataBase();
        if (!dbExist) {
            // By calling this method and empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            getWritableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Error copying database");
            }
        }
    }

    public boolean isDbNullOrClosed() {
        if (isEncrypted)
            return db_encrypted == null || !db_encrypted.isOpen();
        else
            return db == null || !db.isOpen();
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READONLY);
        } catch (SQLiteException e) {
            // database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null;
        // abbas
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {
        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);
        // Path to the just created empty db
        String outFileName = DB_PATH + DB_NAME;
        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);
        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }
        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();
        if (isEncrypted)
            encrypt(myContext, DB_NAME, DB_PASSWORD);
    }

    public void openDataBase() throws SQLException {
        // Open the database
        String myPath = DB_PATH + DB_NAME;
        if (!isEncrypted)
            db = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READWRITE);
        else {
            net.sqlcipher.database.SQLiteDatabase.loadLibs(myContext);
            File databaseFile = myContext.getDatabasePath(DB_NAME);
            db_encrypted =
                    net.sqlcipher.database.SQLiteDatabase.openDatabase(databaseFile.getAbsolutePath(),
                            DB_PASSWORD, null,
                            SQLiteDatabase.OPEN_READWRITE);
        }
    }

    public void encrypt(Context ctxt, String dbName,
                        String passphrase) throws IOException {
        // To load sqlite cipher libraries
        net.sqlcipher.database.SQLiteDatabase.loadLibs(ctxt);
        File originalFile = ctxt.getDatabasePath(dbName);
        if (originalFile.exists()) {
            // Temporary file to create encrypted database
            File newFile =
                    File.createTempFile("sqlcipherutils", "tmp",
                            ctxt.getCacheDir());
            //intialize sqlite cipher db
            db_encrypted =
                    net.sqlcipher.database.SQLiteDatabase.openDatabase(originalFile.getAbsolutePath(),
                            "", null,
                            SQLiteDatabase.OPEN_READWRITE);
            //copying sqlite db into sqlite cipher db
            db_encrypted.rawExecSQL(String.format("ATTACH DATABASE '%s' AS encrypted KEY '%s';",
                    newFile.getAbsolutePath(), passphrase));
            //export sqlite db data into sqlite cipher db
            db_encrypted.rawExecSQL("SELECT sqlcipher_export('encrypted')");
            //seperating sqlite cipher db
            db_encrypted.rawExecSQL("DETACH DATABASE encrypted;");

            int version = db_encrypted.getVersion();

            db_encrypted.close();

            db_encrypted =
                    net.sqlcipher.database.SQLiteDatabase.openDatabase(newFile.getAbsolutePath(),
                            passphrase, null,
                            SQLiteDatabase.OPEN_READWRITE);
            // set new version for sqlite cipher db
            db_encrypted.setVersion(version);
            db_encrypted.close();
            // removing sqlite db
            originalFile.delete();
            newFile.renameTo(originalFile);
        }
    }

}
