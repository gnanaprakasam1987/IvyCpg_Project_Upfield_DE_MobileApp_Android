package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class NewRetailerHelper {

	private Context context;
	private Vector<NewOutletBO> outletMaster;
	public int mSelectedImagetID = 1;
	NewOutletBO imageType;

	public NewOutletBO getImageType() {
		return imageType;
	}

	public void setImageType(NewOutletBO imageType) {
		this.imageType = imageType;
	}

	/**
	 * This String used to store captured image's image Name
	 * 
	 */
	public String mSelectedImageName = "";

	Vector<ConfigureBO> profileConfig = null;

	private static NewRetailerHelper instance = null;
	BusinessModel bmodel;
	Vector<NewOutletBO> imageTypeList = new Vector<NewOutletBO>();

	protected NewRetailerHelper(Context context) {
		this.context = context;
		bmodel = (BusinessModel) context;
		// bmodel.setContext(context);
	}

	public Vector<NewOutletBO> getImageTypeList() {
		return imageTypeList;
	}

	public void setImageTypeList(Vector<NewOutletBO> imageTypeList) {
		this.imageTypeList = imageTypeList;
	}

	public static NewRetailerHelper getInstance(Context context) {
		if (instance == null) {
			instance = new NewRetailerHelper(context);
		}
		return instance;
	}

	public String QT(String data) // Quote
	{
		return "'" + data + "'";
	}

	/**
	 * Download configuration fields for new retailer
	 * 
	 * @return
	 */
	public void loadProfileConfiguration() {
		ConfigureBO ConfigureBO;
   
		try {
			profileConfig = new Vector<ConfigureBO>();
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.createDataBase();
			db.openDataBase();
			Cursor c = null;
			// select HHTCode, flag , MName , MNumber , Rfield1 , RField2 from
			// HhtMenuMaster where MenuType= 'NEW_OUTLET' order by RField
			c = db.selectSQL("select HHTCode, flag , MName , MNumber ,  RField from HhtMenuMaster where MenuType= 'MENU_NEW_RET' ");
			if (c != null) {
				while (c.moveToNext()) {
					ConfigureBO = new ConfigureBO();
					ConfigureBO.setConfigCode(c.getString(0));
					ConfigureBO.setFlag(c.getInt(1));
					ConfigureBO.setMenuName(c.getString(2));
					ConfigureBO.setMenuNumber((c.getString(3)));
					ConfigureBO.setMandatory((c.getInt(4)));
					profileConfig.add(ConfigureBO);
				}
			}
			c.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}

	}

	public Vector<ConfigureBO> getProfileConfiguraion() {
		return profileConfig;

	}

	public boolean hasMandatoryField() {
		boolean check = false;
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.createDataBase();
			db.openDataBase();

			int counts = 0;

			Cursor c = db
					.selectSQL("select COUNT(RField) from HhtMenuMaster  where RField =1 AND MenuType='MENU_NEW_RET' ");
			c.moveToFirst();
			counts = c.getInt(0);
			Commons.print("Count of RField : ,"+ counts+"");
			c.close();
			db.close();

			if (counts > 0)
				check = true;
			else
				check = false;

		} catch (Exception e) {
			Commons.printException(e);
		}
		return check;

	}

	public void loadImageType() {
		imageTypeList.clear();
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.openDataBase();
			Cursor c = db
					.selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_IMAGE_TYPE'");
			if (c.getCount() > 0) {
				setImageTypeList(new Vector<NewOutletBO>());
				while (c.moveToNext()) {
					imageType = new NewOutletBO();
					imageType.setListId(c.getInt(0));
					imageType.setListName(c.getString(2));
					getImageTypeList().add(imageType);
				}
				c.close();
			}
			db.closeDB();
		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
		}
	}



}
