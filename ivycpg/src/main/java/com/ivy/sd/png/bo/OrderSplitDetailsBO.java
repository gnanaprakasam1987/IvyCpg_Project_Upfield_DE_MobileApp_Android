/**
 * 
 */
package com.ivy.sd.png.bo;

/**
 * @author sivakumar.j
 *
 */
public class OrderSplitDetailsBO {
	
	private String orderID,retailerId,productID,productName,productShortName;
	private int pieceqty,caseQty;
	private boolean ticked=false;
	
	private int qty,uom_count,uom_id,ms_qty,retailer_id,is_free_product,d1,d2,d3,da,outer_qty,d_ouom_qty;
	private String rate,pCode,schId,mbarcode;
	public String getMbarcode() {
		return mbarcode;
	}

	public void setMbarcode(String mbarcode) {
		this.mbarcode = mbarcode;
	}

	private float total_amt;
	private double sd_per;
	private double sd_amt;
	private double sch_price;
	
	private int d_ouom_id=0;
	
	private int brand_id,category_id;
	private int so_piece,so_case;
	
	

	public OrderSplitDetailsBO()
	{
		orderID=retailerId=productID=productName=productShortName=rate=pCode=schId="";
		pieceqty=caseQty=qty=uom_count=uom_id=ms_qty=retailer_id=is_free_product=d1=d2=d3=da=outer_qty=0;
		setD_ouom_qty(0);
		sd_per=sd_amt=0;
		setSch_price(0);
		total_amt=0;
		setD_ouom_id(0);
		
		brand_id=category_id=-1;
	}
	
	/**
	 * @return the retailerId
	 */
	public String getRetailerId() {
		return retailerId;
	}
	
	/**
	 * @param retailerId the retailerId to set
	 */
	public void setRetailerId(String retailerId) {		
		this.retailerId =((retailerId==null)||(retailerId.length()<1))?(""):(retailerId);
	}

	/**
	 * @return the productID
	 */
	public String getProductID() {
		return productID;
	}

	/**
	 * @param productID the productID to set
	 */
	public void setProductID(String productID) {		
		this.productID=((productID==null)||(productID.length()<1))?(""):(productID);
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		
		this.productName=((productName==null)||(productName.length()<1))?(""):(productName);
	}

	/**
	 * @return the productShortName
	 */
	public String getProductShortName() {
		return productShortName;
	}

	/**
	 * @param productShortName the productShortName to set
	 */
	public void setProductShortName(String productName1) {		
		this.productShortName=((productName1==null)||(productName1.length()<1))?(""):(productName1);
	}

	/**
	 * @return the pieceqty
	 */
	public int getPieceqty() {
		return pieceqty;
	}

	/**
	 * @param pieceqty the pieceqty to set
	 */
	public void setPieceqty(int pieceqty) {
		this.pieceqty = pieceqty;
	}

	/**
	 * @return the caseQty
	 */
	public int getCaseQty() {
		return caseQty;
	}

	/**
	 * @param caseQty the caseQty to set
	 */
	public void setCaseQty(int caseQty) {
		this.caseQty = caseQty;
	}

	/**
	 * @return the orderID
	 */
	public String getOrderID() {
		return orderID;
	}

	/**
	 * @param orderID the orderID to set
	 */
	public void setOrderID(String orderID) {
		
		this.orderID =((orderID ==null)||(orderID .length()<1))?(""):(orderID );
	}

	/**
	 * @return the ticked
	 */
	public boolean isTicked() {
		return ticked;
	}

	/**
	 * @param ticked the ticked to set
	 */
	public void setTicked(boolean ticked) {
		this.ticked = ticked;
	}

	/**
	 * @return the qty
	 */
	public int getQty() {
		return qty;
	}

	/**
	 * @param qty the qty to set
	 */
	public void setQty(int qty) {
		this.qty = qty;
	}

	/**
	 * @return the uom_count
	 */
	public int getUom_count() {
		return uom_count;
	}

	/**
	 * @param uom_count the uom_count to set
	 */
	public void setUom_count(int uom_count) {
		this.uom_count = uom_count;
	}

	/**
	 * @return the uom_id
	 */
	public int getUom_id() {
		return uom_id;
	}

	/**
	 * @param uom_id the uom_id to set
	 */
	public void setUom_id(int uom_id) {
		this.uom_id = uom_id;
	}

	/**
	 * @return the ms_qty
	 */
	public int getMs_qty() {
		return ms_qty;
	}

	/**
	 * @param ms_qty the ms_qty to set
	 */
	public void setMs_qty(int ms_qty) {
		this.ms_qty = ms_qty;
	}

	/**
	 * @return the retailer_id
	 */
	public int getRetailer_id() {
		return retailer_id;
	}

	/**
	 * @param retailer_id the retailer_id to set
	 */
	public void setRetailer_id(int retailer_id) {
		this.retailer_id = retailer_id;
	}

	/**
	 * @return the is_free_product
	 */
	public int getIs_free_product() {
		return is_free_product;
	}

	/**
	 * @param is_free_product the is_free_product to set
	 */
	public void setIs_free_product(int is_free_product) {
		this.is_free_product = is_free_product;
	}

	/**
	 * @return the d1
	 */
	public int getD1() {
		return d1;
	}

	/**
	 * @param d1 the d1 to set
	 */
	public void setD1(int d1) {
		this.d1 = d1;
	}

	/**
	 * @return the d2
	 */
	public int getD2() {
		return d2;
	}

	/**
	 * @param d2 the d2 to set
	 */
	public void setD2(int d2) {
		this.d2 = d2;
	}

	/**
	 * @return the d3
	 */
	public int getD3() {
		return d3;
	}

	/**
	 * @param d3 the d3 to set
	 */
	public void setD3(int d3) {
		this.d3 = d3;
	}

	/**
	 * @return the da
	 */
	public int getDa() {
		return da;
	}

	/**
	 * @param da the da to set
	 */
	public void setDa(int da) {
		this.da = da;
	}

	/**
	 * @return the outer_qty
	 */
	public int getOuter_qty() {
		return outer_qty;
	}

	/**
	 * @param outer_qty the outer_qty to set
	 */
	public void setOuter_qty(int outer_qty) {
		this.outer_qty = outer_qty;
	}

	/**
	 * @return the d_ouom_qty
	 */
	public int getD_ouom_qty() {
		return d_ouom_qty;
	}

	/**
	 * @param d_ouom_qty the d_ouom_qty to set
	 */
	public void setD_ouom_qty(int d_ouom_qty) {
		this.d_ouom_qty = d_ouom_qty;
	}

	/**
	 * @return the rate
	 */
	public String getRate() {
		return rate;
	}

	/**
	 * @param rate the rate to set
	 */
	public void setRate(String rate) {
		
		this.rate =((rate ==null)||(rate.length()<1))?(""):(rate);
	}

	/**
	 * @return the pCode
	 */
	public String getpCode() {
		return pCode;
	}

	/**
	 * @param pCode the pCode to set
	 */
	public void setpCode(String pCode) {
		
		this.pCode =((pCode ==null)||(pCode.length()<1))?(""):(pCode);
	}

	/**
	 * @return the schId
	 */
	public String getSchId() {
		return schId;
	}

	/**
	 * @param schId the schId to set
	 */
	public void setSchId(String schId) {
		
		this.schId =((schId ==null)||(schId.length()<1))?(""):(schId);
	}

	/**
	 * @return the total_amt
	 */
	public float getTotal_amt() {
		return total_amt;
	}

	/**
	 * @param total_amt the total_amt to set
	 */
	public void setTotal_amt(float total_amt) {
		this.total_amt = total_amt;
	}

	/**
	 * @return the sd_per
	 */
	public double getSd_per() {
		return sd_per;
	}

	/**
	 * @param sd_per the sd_per to set
	 */
	public void setSd_per(double sd_per) {
		this.sd_per = sd_per;
	}

	/**
	 * @return the sd_amt
	 */
	public double getSd_amt() {
		return sd_amt;
	}

	/**
	 * @param sd_amt the sd_amt to set
	 */
	public void setSd_amt(double sd_amt) {
		this.sd_amt = sd_amt;
	}

	/**
	 * @return the sch_price
	 */
	public double getSch_price() {
		return sch_price;
	}

	/**
	 * @param sch_price the sch_price to set
	 */
	public void setSch_price(double sch_price) {
		this.sch_price = sch_price;
	}

	/**
	 * @return the d_ouom_id
	 */
	public int getD_ouom_id() {
		return d_ouom_id;
	}

	/**
	 * @param d_ouom_id the d_ouom_id to set
	 */
	public void setD_ouom_id(int d_ouom_id) {
		this.d_ouom_id = d_ouom_id;
	}

	/**
	 * @return the branch_id
	 */
	public int getBrandId() {
		return brand_id;
	}

	/**
	 * @param branch_id the branch_id to set
	 */
	public void setBrandId(int branch_id) {
		this.brand_id = branch_id;
	}

	/**
	 * @return the category_id
	 */
	public int getCategoryId() {
		return category_id;
	}

	/**
	 * @param category_id the category_id to set
	 */
	public void setCategoryId(int category_id) {
		this.category_id = category_id;
	}
	
	public int getSo_piece() {
		return so_piece;
	}

	public void setSo_piece(int so_piece) {
		this.so_piece = so_piece;
	}

	public int getSo_case() {
		return so_case;
	}

	public void setSo_case(int so_case) {
		this.so_case = so_case;
	}

}
