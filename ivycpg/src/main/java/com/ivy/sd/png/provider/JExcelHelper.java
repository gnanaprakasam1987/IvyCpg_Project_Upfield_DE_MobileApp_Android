package com.ivy.sd.png.provider;

import android.content.Context;

import com.ivy.sd.png.util.Commons;

import java.io.File;
import java.util.ArrayList;

import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public class JExcelHelper {

    private Context mContext;
    private static JExcelHelper instance = null;

    public JExcelHelper(Context context) {

        mContext = context;
    }

    public static JExcelHelper getInstance(Context context) {
        if (instance == null) {
            instance = new JExcelHelper(context);
        }
        return instance;
    }

    public void createExcel(String mFileName, ArrayList<JExcelHelper.ExcelBO> mExcelBOList) {
        try {
            File file = new File(mContext.getExternalFilesDir(null), mFileName);

            WritableWorkbook m_workbook = Workbook.createWorkbook(file);

            for(int i=0;i<mExcelBOList.size();i++){
                ExcelBO mExcelBO= mExcelBOList.get(i);

                WritableSheet sheet = m_workbook.createSheet(mExcelBO.getSheetName(), i);

                //Create Cell Font
                WritableFont cellFont = new WritableFont(WritableFont.ARIAL, 12);
                cellFont.setColour(Colour.BLACK);

                //Format the cell
                WritableCellFormat cellFormat = new WritableCellFormat(cellFont);
                cellFormat.setBackground(Colour.LIGHT_GREEN);
                cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

                int columnNameSize, columnValueSize, rowValueSize;
                columnNameSize = mExcelBO.getColumnNames().size();
                for (int j = 0; j < columnNameSize; j++) {
                    sheet.setColumnView(j, mExcelBO.getColumnNames().get(j).length() + 10);
                    sheet.addCell(new Label(j, 0, mExcelBO.getColumnNames().get(j), cellFormat));
                }


                cellFont = new WritableFont(WritableFont.ARIAL, 10);
                cellFont.setColour(Colour.BLACK);

                cellFormat = new WritableCellFormat(cellFont);
                cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

                columnValueSize = mExcelBO.getColumnValues().size();
                for (int k = 0; k < columnValueSize; k++) {
                    ArrayList<String> rowvalues = mExcelBO.getColumnValues().get(k);
                    rowValueSize = rowvalues.size();
                    for (int l = 0; l < rowValueSize; l++) {
                        sheet.addCell(new Label(l, (k + 1), rowvalues.get(l), cellFormat));
                    }
                }
            }

            m_workbook.write();

            m_workbook.close();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public class ExcelBO{
        String sheetName;
        ArrayList<String> columnNames;
        ArrayList<ArrayList<String>> columnValues;

        public String getSheetName() {
            return sheetName;
        }

        public void setSheetName(String sheetName) {
            this.sheetName = sheetName;
        }

        public ArrayList<String> getColumnNames() {
            return columnNames;
        }

        public void setColumnNames(ArrayList<String> columnNames) {
            this.columnNames = columnNames;
        }

        public ArrayList<ArrayList<String>> getColumnValues() {
            return columnValues;
        }

        public void setColumnValues(ArrayList<ArrayList<String>> columnValues) {
            this.columnValues = columnValues;
        }
    }

}
