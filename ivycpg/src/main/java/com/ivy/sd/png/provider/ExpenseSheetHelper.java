package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ExpenseSheetBO;
import com.ivy.sd.png.bo.ExpensesBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ExpenseSheetHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static ExpenseSheetHelper instance = null;
    private ArrayList<ExpenseSheetBO> currentMonthExpense;
    private ArrayList<SpinnerBO> expnenses;
    public ArrayList<ExpenseSheetBO> getCurrentMonthExpense() {
        return currentMonthExpense;
    }

    public ArrayList<SpinnerBO> getExpnenses() {
        return expnenses;
    }

    protected ExpenseSheetHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static ExpenseSheetHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ExpenseSheetHelper(context);
        }
        return instance;
    }

    public void loadExpenseData() {
        String CODE_EXPENSE_LIST_TYPE = "EXPENSE_TYPE";
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;


            currentMonthExpense = new ArrayList<>();
            sql = "SELECT userId,typeId,typeName,date,amount FROM ExpenseMTDDetail "
                            + "WHERE Date >= " + QT(getMonthFirstDateString()) + " AND Date <= " + QT(getYesterdayDateString()) + " AND "
                            + "userID = " + QT("" + bmodel.userMasterHelper.getUserMasterBO().getUserid()) + " "
                            + "ORDER BY Date ASC";
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    ExpenseSheetBO currentExps = new ExpenseSheetBO();
                    currentExps.setUserId(c.getInt(0));
                    currentExps.setTypeId(c.getInt(1));
                    currentExps.setTypeName(c.getString(2));
                    currentExps.setDate(c.getString(3));
                    currentExps.setAmount(c.getString(4));

                    currentMonthExpense.add(currentExps);
                }
                c.close();
            }

            expnenses = new ArrayList<>();
            sql = "select listID,listname from StandardListMaster "
                            + "where listType = " + "'" + CODE_EXPENSE_LIST_TYPE + "'";
            c = db.selectSQL(sql);
            SpinnerBO spinnerBO;
            if (c != null) {
                while (c.moveToNext()) {
                    spinnerBO = new SpinnerBO(c.getInt(0), c.getString(1));
                    expnenses.add(spinnerBO);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }


    public String QT(String data) {
        return "'" + data + "'";
    }

    private String getYesterdayDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -1);
        return dateFormat.format(cal.getTime());
    }

    private String getMonthFirstDateString() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();   // this takes current date
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return dateFormat.format(cal.getTime());
    }

    public String checkExpenseHeader(String Date) {
        String tid = "";
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select Tid from ExpenseHeader "
                            + "where userid = " + QT("" + bmodel.userMasterHelper.getUserMasterBO().getUserid())
                            + " AND date = " + QT(Date)
                            + " AND Upload = " + QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    tid = c.getString(0);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return tid;
    }

    public int checkExpenseHeader() {
        int count = 0;
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select Tid from ExpenseHeader "
                            + "where userid = " + QT("" + bmodel.userMasterHelper.getUserMasterBO().getUserid())
                            + " AND Upload = " + QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                count = c.getCount();
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return count;
    }

    public double getExpenseTotal(String Tid, String date) {
        double total = 0;
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select TotalAmount from ExpenseHeader "
                            + "where userid = " + QT("" + bmodel.userMasterHelper.getUserMasterBO().getUserid())
                            + " AND date = " + QT(date)
                            + " AND Tid = " + QT(Tid)
                            + " AND Upload = " + QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    total = c.getDouble(0);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return total;
    }

    public ArrayList<ExpensesBO> getSelectedExpenses(String Tid) {
        ArrayList<ExpensesBO> expenseList = new ArrayList<>();

        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select ED.TypeID,ED.Tid,ED.amount,ED.Refid,SM.listname from ExpenseDetail ED "
                            + "inner join StandardListMaster SM on SM.listID = ED.TypeID "
                            + "where ED.Tid = " + QT(Tid)
                            + " AND ED.Upload = "+ QT("N");
            c = db.selectSQL(sql);

            ExpensesBO expensesBO;
            if (c != null) {
                while (c.moveToNext()) {
                    expensesBO = new ExpensesBO();
                    expensesBO.setTypeId(c.getInt(0));
                    expensesBO.setTid(c.getString(1));
                    expensesBO.setAmount(c.getString(2));
                    expensesBO.setRefId(c.getString(3));
                    expensesBO.setTypeName(c.getString(4));
                    expensesBO.setImageList(getImagesList(expensesBO.getRefId()));
                    expenseList.add(expensesBO);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return expenseList;
    }

    public ArrayList<ExpensesBO> getAllExpenses() {
        ArrayList<ExpensesBO> expenseList = new ArrayList<>();

        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select ED.TypeID,ED.Tid,ED.amount,ED.Refid,SM.listname,EH.date from ExpenseDetail ED "
                            + "inner join StandardListMaster SM on SM.listID = ED.TypeID "
                            + "inner join ExpenseHeader EH on EH.Tid =  ED.Tid "
                            + "where ED.Upload = "+ QT("N");
            c = db.selectSQL(sql);

            ExpensesBO expensesBO;
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    expensesBO = new ExpensesBO();
                    expensesBO.setTypeId(c.getInt(0));
                    expensesBO.setTid(c.getString(1));
                    expensesBO.setAmount(c.getString(2));
                    expensesBO.setRefId(c.getString(3));
                    expensesBO.setTypeName(c.getString(4));
                    expensesBO.setDate(c.getString(5));
                    expensesBO.setImageList(getImagesList(expensesBO.getRefId()));
                    expenseList.add(expensesBO);
                }
            }

            c.close();

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return expenseList;
    }

    public ArrayList<String> getImagesList(String RefID) {
        ArrayList<String> imageList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;

            sql = "select imagename from ExpenseImageDetails "
                            + "where Refid = " + QT(RefID)
                            + "AND Upload = " + QT("N");
            c = db.selectSQL(sql);

            if (c != null) {
                while (c.moveToNext()) {
                    String[] imjObj = c.getString(0).split("/");
                    imageList.add(imjObj[3]);
                }
                c.close();
            }

            db.closeDB();

        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return imageList;
    }

    public void updateHeaderInsert(String Tid, double totalAmount, String amountValue, int expType, ArrayList<String> imageList, String refID) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String sql;

            String expDetailCol = "Tid,typeID,amount,Refid";
            String expImgDetailCol = "Tid,Refid,imagename";
            String values;

            String mtbl_expensedetail = "ExpenseDetail";
            String mtbl_expenseimgdetail = "ExpenseImageDetails";


            //Updating toatal value in Expense Header
            sql = "UPDATE ExpenseHeader SET TotalAmount = " + totalAmount
                    + " WHERE Tid = " + Tid;

            db.updateSQL(sql);

            //insert in Expense details

            values = Tid + ","
                    + expType + ","
                    + QT(amountValue) + ","
                    + QT(refID);

            db.insertSQL(mtbl_expensedetail, expDetailCol, values);

            //insert in imageDetail table
            for (String image : imageList) {

                values = Tid + ","
                        + QT(refID) + ","
                        + QT(image);

                db.insertSQL(mtbl_expenseimgdetail, expImgDetailCol, values);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
            db.closeDB();
        }
    }

    public void saveAllData(ExpensesBO expensesBO, String date) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String expHeaderCol = "Tid,userid,date,TotalAmount,utcDate,Upload";
            String expDetailCol = "Tid,typeID,amount,Refid";
            String expImgDetailCol = "Tid,Refid,imagename";
            String values;

            String mtbl_expenseheader = "ExpenseHeader";
            String mtbl_expensedetail = "ExpenseDetail";
            String mtbl_expenseimgdetail = "ExpenseImageDetails";


            //insert value in Expense Header
            values = expensesBO.getTid() + ","
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + ","
                    + QT(date) + ","
                    + expensesBO.getAmount() + ","
                    + DatabaseUtils.sqlEscapeString(Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")) + ","
                    + QT("N");

            db.insertSQL(mtbl_expenseheader, expHeaderCol, values);

            //insert in Expense details

            values = expensesBO.getTid() + ","
                    + expensesBO.getTypeId() + ","
                    + QT(expensesBO.getAmount()) + ","
                    + QT(expensesBO.getRefId());

            db.insertSQL(mtbl_expensedetail, expDetailCol, values);


            for (String image : expensesBO.getImageList()) {

                values = expensesBO.getTid() + ","
                        + QT(expensesBO.getRefId()) + ","
                        + QT(image);

                db.insertSQL(mtbl_expenseimgdetail, expImgDetailCol, values);
            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
            db.closeDB();
        }
    }

    public void deleteImageProof(String ImageName) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            //For adding server ref path to image name
            String path = "Expense/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/";

            db.deleteSQL(DataMembers.tbl_expenseimagedetails, "imagename="
                    + QT(path + ImageName), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }


    //delete a row of expense with the images
    public void deleteExpense(String refID, String Tid, String date, String amount) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String sql;
            db.deleteSQL(DataMembers.tbl_expensedetails, "Refid="
                    + QT(refID), false); // QT(ImageName));
            db.deleteSQL(DataMembers.tbl_expenseimagedetails, "Refid="
                    + QT(refID), false); // QT(ImageName));

            double update_total = getExpenseTotal(Tid, date) - Double.parseDouble(amount);

            //Updating toatal value in Expense Header
            sql = "UPDATE ExpenseHeader SET TotalAmount = " + update_total
                    + " WHERE Tid = " + Tid
                    + " AND date = " + QT(date);

            db.updateSQL(sql);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }

    // Delete header if all transcation got deleted
    public void deleteHeader(String Tid) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_expenseheader, "Tid ="
                    + QT(Tid), false);


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }


}
