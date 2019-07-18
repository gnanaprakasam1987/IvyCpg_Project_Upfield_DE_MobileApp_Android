package com.ivy.cpg.view.van.stockproposal;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.Vector;

public class StockProposalModuleHelper {

	private Context context;
	private BusinessModel bmodel;
	private static StockProposalModuleHelper instance = null;

	protected StockProposalModuleHelper(Context context) {
		this.context = context;
		this.bmodel = (BusinessModel) context.getApplicationContext();
	}

	public static StockProposalModuleHelper getInstance(Context context) {
		if (instance == null) {
			instance = new StockProposalModuleHelper(context);
		}
		return instance;
	}

	public void saveStockProposal(Vector<LoadManagementBO> stockProposalMaster) {
		try {

			String invid = bmodel.userMasterHelper.getUserMasterBO()
					.getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.createDataBase();
			db.openDataBase();
			LoadManagementBO product;
			int siz = stockProposalMaster.size();
			for (int i = 0; i < siz; ++i) {
				product = (LoadManagementBO) stockProposalMaster.elementAt(i);
				if (product.getStkprocaseqty() > 0
						|| product.getStkpropcsqty() > 0
						|| product.getStkproouterqty() > 0) {
					Cursor stockcursor = db
							.selectSQL("select uid from StockProposalMaster where pid="
									+ product.getProductid()
									+ " and upload='Y'");
					if (stockcursor.getCount() > 0) {
						stockcursor.moveToNext();
						invid = StringUtils.getStringQueryParam(stockcursor.getString(0));
					}
					stockcursor.close();
				}
			}
			db.executeQ("delete from StockProposalMaster");
			// db.executeQ("update StockProposalMaster set editqty= 0");
			String columns = "pid,caseQty,outerQty,uid,pcsQty,qty,duomid,duomQty,dOuomQty,dOuomid,date";
			for (int i = 0; i < siz; ++i) {
				product = (LoadManagementBO) stockProposalMaster.elementAt(i);
				product.setStkprototalQty((product.getStkprocaseqty() * product
						.getCaseSize())
						+ product.getStkpropcsqty()
						+ product.getStkproouterqty() * product.getOuterSize());

				Commons.print("uid=" + invid);
				if (product.getStkproouterqty() > 0
						|| product.getStkprocaseqty() > 0
						|| product.getStkpropcsqty() > 0) {
					String values = product.getProductid() + ","
							+ product.getStkprocaseqty() + ","
							+ product.getStkproouterqty() + "," + invid + ","
							+ product.getStkpropcsqty() + ","
							+ product.getStkprototalQty() + ","
							+ product.getdUomid() + "," + product.getCaseSize()
							+ "," + product.getOuterSize() + ","
							+ product.getdOuonid() + ","
							+ StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

					String sql = "insert into " + "StockProposalMaster" + "("
							+ columns + ") values(" + values + ")";
					db.executeQ(sql);
				}
			}

			db.closeDB();
		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
		}
	}



	public void loadSBDData() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.createDataBase();
			db.openDataBase();

			Vector<Integer> sbdDist = new Vector<Integer>();
			// Order Header
			String sql = "select distinct(productid) from SbdDistributionMaster ";
			Cursor c = db.selectSQL(sql);

			if (c != null) {
				while (c.moveToNext()) {
					sbdDist.add(c.getInt(0));
				}
			}
			c.close();

			Vector<Integer> sbdDistAcheived = new Vector<Integer>();
			// Order Header
			String sql1 = "select productid from SbdDistributionMaster where grpName in (select gname from SbdDistributionAchievedMaster  )";
			Cursor c1 = db.selectSQL(sql1);

			if (c1 != null) {
				while (c1.moveToNext()) {
					sbdDistAcheived.add(c1.getInt(0));
				}
			}
			c1.close();

			db.closeDB();

			for (int i = 0; i < bmodel.productHelper.getLoadMgmtProducts().size(); i++) {
				LoadManagementBO p = (LoadManagementBO) bmodel.productHelper
						.getLoadMgmtProducts().get(i);
				if (sbdDist.contains(Integer.valueOf(p.getProductid()))) {
					p.setSBDProduct(true);
					bmodel.productHelper.getLoadMgmtProducts().setElementAt(p, i);
				}
				if (sbdDistAcheived.contains(Integer.valueOf(p.getProductid()))) {
					p.setSBDAcheived(true);
					bmodel.productHelper.getLoadMgmtProducts().setElementAt(p, i);
				}

			}
		} catch (Exception e) {
			Commons.printException(e);
		}

	}

	public void loadInitiative() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.openDataBase();
			Vector<Integer> init = new Vector<Integer>();
			Cursor c1 = db
					.selectSQL("select distinct(I.productInitId) from InitiativeDetailMaster I inner join productmaster b on b.pid=I.productInitId");
			if (c1 != null) {
				while (c1.moveToNext()) {

					init.add(c1.getInt(0));
				}
			}

			c1.close();

			for (int i = 0; i < bmodel.productHelper.getLoadMgmtProducts().size(); i++) {
				LoadManagementBO p = (LoadManagementBO) bmodel.productHelper
						.getLoadMgmtProducts().get(i);
				Integer prodId = new Integer(p.getProductid());
				if (init.contains(prodId)) {
					p.setIsInitiativeProduct(1);
					bmodel.productHelper.getLoadMgmtProducts().setElementAt(p, i);
				}
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	public void loadPurchased() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.createDataBase();
			db.openDataBase();
			SparseArray<Integer> hashMap = new SparseArray<Integer>();

			String sql = "select distinct(pid),flag from RtrWiseProductWisePurchased";

			Cursor c = db.selectSQL(sql);
			if (c != null) {
				while (c.moveToNext()) {
					hashMap.put(c.getInt(0), c.getInt(1));
				}
			}
			c.close();
			db.closeDB();
			for (LoadManagementBO p : bmodel.productHelper.getLoadMgmtProducts()) {
				Integer value = hashMap.get(p.getProductid());
				if (value != null) {
					p.setIsPurchased(value);
				}
			}
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	public boolean isMustStockFilled(Vector<LoadManagementBO> stockPropVector) {
		LoadManagementBO stock;
		int siz = stockPropVector.size();
		for (int i = 0; i < siz; i++) {
			stock = (LoadManagementBO) stockPropVector.get(i);
			if (stock.getIsMust() == 1 && stock.getStkprocaseqty() == 0
					&& stock.getStkpropcsqty() == 0
					&& stock.getStkproouterqty() == 0) {
				return false;
			}
		}
		return true;
	}
}