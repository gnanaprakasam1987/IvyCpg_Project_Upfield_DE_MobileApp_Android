package com.ivy.sd.png.util;

import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.model.ApplicationConfigs;

import java.util.HashMap;

public class DataMembers {

    public static final String DB_NAME = ApplicationConfigs.DB_NAME;

    public static final int IVY_SERVER_ERROR = 2002;
    public static final int IVY_APP_INTERNAL_EXCEPTION = 2001;

    // Image Download URL
    public static String IMG_DOWN_URL;

    //For Microsoft Azure Cloud Storage
    public static String AZURE_TYPE = "Azure";
    public static String AZURE_CONTAINER = "devappfiles";
    public static String AZURE_CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=devappwestindiageo;AccountKey=whdcWLAMnbipdHseTeb6ifOWczAjOcoUjT0wXgnIm+F6cJYGIyumNsapX4IH2vBBMeh9/qVfxpxbAZGjlv1XLw==;EndpointSuffix=core.windows.net";
    public static String AZURE_SAS = "whdcWLAMnbipdHseTeb6ifOWczAjOcoUjT0wXgnIm+F6cJYGIyumNsapX4IH2vBBMeh9/qVfxpxbAZGjlv1XLw==";

    public static String AZURE_ENDPOINT = "core.windows.net";
    public static String AZURE_ACCOUNT_NAME = "devappwestindiageo";
    public static String AZURE_KEY = "whdcWLAMnbipdHseTeb6ifOWczAjOcoUjT0wXgnIm+F6cJYGIyumNsapX4IH2vBBMeh9/qVfxpxbAZGjlv1XLw==";
    public static String AZURE_BASE_URL = "https://devappwestindiageo.blob.core.windows.net";
    public static String AZURE_ROOT_DIRECTORY = "IvyDistributor";


    // For Cloud Image Upload Starts
    public static String S3_BUCKET_REGION = "s3-ap-southeast-1.amazonaws.com";// "Singapore";//
    public static String S3_BUCKET = "ivydevbkt/ivy_distributor";
    public static String S3_ROOT_DIRECTORY = "IvyDistributor";

    // Sync
    public static final String SFDC_URLDOWNLOAD_MASTER_URL = "/services/apexrest/ivybase/v2/URLDownload/Masters/";
    public static String SERVER_URL = BuildConfig.BASE_URL;
    //public static String SERVER_URL = BuildConfig.BASE_URL;
    //public static String SERVER_URL = "http://india-dev.ivycpg.com/ivycpg_jnjindiagt_webApi01/api";
    //public static String SERVER_URL = "http://india-dev.ivycpg.com/ivycpg_jnjindiagt_webApi01/api";
    //public static String SERVER_URL = "http://me-dev.ivycpg.com/IvyCPG_jnj_dubai_Webapi/api";
    //public static String SERVER_URL = "https://qa-product.ivycpg.com/webapi/api";
    //public static final String AUTHENTICATE = "/usermaster/AuthenticateUser";
    public static final String AUTHENTICATE = "/usermaster/SecureAuthenticateUser";
    public static final String CHANGE_PWD = "/ChangePassword/Validate";

    public static String fileName = "sd_png_asean_android.apk";

    public static final int MESSAGE_UNZIPPED = 10113;

    public static final String DIGITAL_CONTENT = "TRAN";
    public static final String APP_DIGITAL_CONTENT = "APP";
    public static final String PLANOGRAM = "PL";
    public static final String DIGITALCONTENT = "DC";
    public static final String MVP = "MVP";
    public static final String LOYALTY_POINTS = "LLTY";
    public static final String PRINT = "PRINT";
    public static final String PRINTFILE = "PRINTFILE";
    public static final String PROFILE = "PRO";
    public static final String CATALOG = "CAT";
    public static final String USER = "USER";
    public static final String TASK_DIGITAL_CONTENT = "TDC";
    public static final String SERIALIZED_ASSET_DIG_CONTENT = "SADC";

    public static final int NOTIFY_WEB_UPLOAD_SUCCESS = 32;
    public static final int NOTIFY_WEB_UPLOAD_ERROR = 33;
    public static final int ATTENDANCE_UPLOAD = 101;
    // print file
    public static final String IVYDIST_PATH = "IvyDist";
    public static final String PRINT_FILE_PATH = "PrintFile";
    public static final String PRINT_FILE_START = "PF_INV";

    public static String COMP_PLEVELNAME = "";

    // Activity List
    public static final String actLoginScreen = "LoginScreen";
    public static final String actHomeScreen = "HomeScreenActivity";
    public static final String actOrderAndStock = "StockAndOrder";
    public static final String actOrderSummary = "OrderSummary";
    public static final String actPlanning = "Planning";
    public static final String actPhotocapture = "PhotoCapture";
    public static final String actHomeScreenTwo = "HomeScreenTwo";
    //public static final String actSynchronization = "Synchronization";
    public static final String actNewRetailer = "NewRetailer";
    public static final String actDigitalContent = "DigitalContent";
    public static final String actCollection = "CollectionScreen";
    public static final String actactivationscreen = "ScreenActivationActivity";
    public static final String actTargetPlan = "TargetPlan";
    public static final String actclosingstock = "ClosingStock";

    //FitScore List
    public static final String FIT_STOCK = "FIT_STOCK";
    public static final String FIT_PRICE = "FIT_PRICE";
    public static final String FIT_PROMO = "FIT_PROMO";
    public static final String FIT_ASSET = "FIT_ASSET";
    public static final String FIT_POSM = "FIT_POSM";
    public static final String FIT_SERIALIZED_ASSET = "FIT_SERIALIZED_ASSET";

    public static final String MENU_STOCK = "MENU_STOCK";
    public static final String MENU_PRICE = "MENU_PRICE";
    public static final String MENU_PROMO = "MENU_PROMO";
    public static final String MENU_ASSET = "MENU_ASSET";
    public static final String MENU_POSM = "MENU_POSM";

    public static final String MODULE_STOCK = "Stock Check";
    public static final String MODULE_PRICE = "Price Check";
    public static final String MODULE_ASSET = "Asset Tracking";
    public static final String MODULE_POSM = "POSM Tracking";
    public static final String MODULE_PROMO = "Promotion Tracking";

    // store application's image Folder name in sdcard
    public static final String photoFolderName = "IvyDist";

    //To store Activation Key from shared preference
    public static String ACTIVATION_KEY = "";

    public static String uidSOS = "";
    public static String uidSOD = "";
    public static String backDate = "";
    public static final int LOCAL_LOGIN = 1;
    public static final int SYNCUPLOAD = 5;
    public static final int SYNCUPLOADRETAILERWISE = 217;
    public static final int UPLOAD_FILE_IN_AMAZON = 5959;
    public static final int NOTIFY_FILE_UPLOADED__COMPLETED_IN_AMAZON = 595959;
    public static final int NOTIFY_FILE_UPLOADED_FAILED_IN_AMAZON = 696969;
    public static final int SYNCSIHUPLOAD = -30;
    public static final int NOTIFY_SIH_UPLOADED = -31;
    public static final int NOTIFY_SIH_UPLOAD_ERROR = -32;

    public static final int SYNCSTKAPPLYUPLOAD = -33;
    public static final int NOTIFY_STOCKAPLY_UPLOADED = -34;
    public static final int NOTIFY_STOCKAPLY_UPLOAD_ERROR = -35;

    public static final int NOTIFY_LP_UPLOADED = -38;
    public static final int NOTIFY_LP_UPLOAD_ERROR = -39;
    public static final int SYNCLYTYPTUPLOAD = -40;

    public static final int SYNC_REALLOC_UPLOAD = -36;
    public static final int SYNC_EXPORT = 218;
    public static final int AMAZONIMAGE_UPLOAD = 501;
    //public static final int SYNCUPLOAD_IMAGE = 5555;
    public static final int AZURE_IMAGE_UPLOAD = 999;
    public static final int NOTIFY_UPLOADED_IMAGE = 55551;
    public static final int NOTIFY_UPLOAD_ERROR_IMAGE = 55552;
    public static final int SAVECOLLECTION = 66;
    public static final int SAVEORDERANDSTOCK = 1001;
    public static final int SAVEORDERPARTIALLY = 1000;
    public static final int DELETE_ORDER = 1002;
    public static final int DELETE_STOCK_AND_ORDER = 1003;
    public static final int SAVENEWOUTLET = 123;
    public static final int NOTIFY_UPLOAD_CLOSINGSTOCK = 1007;
    public static final int SAVEINVOICE = 1008;
    public static final int SAVESUBDORDER = 1013;
    public static final int NOTIFY_USEREXIST = 0;
    public static final int NOTIFY_NOT_USEREXIST = 1;
    public static final int NOTIFY_UPDATE = 2;

    public static final int NOTIFY_UPLOADED = 5;
    public static final int NOTIFY_UPLOAD_ERROR = 6;
    public static final int NOTIFY_CLOSE_HOME = 8;
    public static final int NOTIFY_NO_INTERNET = 9;
    public static final int NOTIFY_SALES_RETURN_SAVED = 20;
    public static final int NOTIFY_AUTOUPDATE_FOUND = 55;
    public static final int NOTIFY_CONNECTION_PROBLEM = 56;
    public static final int NOTIFY_NEW_OUTLET_SAVED = 201;
    public static final int NOTIFY_NEW_PHOTO_SAVED = 203;
    public static final int NOTIFY_ACTIVATION_TO_LOGIN = 62;
    // Used in OrderSummary
    public static final int NOTIFY_ORDER_SAVED = -6;
    public static final int NOTIFY_INVOICE_SAVED = -7;
    public static final int NOTIFY_ORDER_DELETED = -8;
    public static final int NOTIFY_DATABASE_NOT_SAVED = -9;
    public static final int NOTIFY_ORDER_NOT_SAVED = -11;
    public static final int NOTIFY_INVOICE_NOT_SAVED = -12;

    public static final int NOTIFY_EXPORT_SUCCESS = 69;
    public static final int NOTIFY_EXPORT_FAILURE = 70;

    public static final int NOTIFY_TOKENT_AUTHENTICATION_FAIL = 74;
    public static final int NOTIFY_URL_NOT_CONFIGURED = 1050;
    public static final int DISTSAVEORDERANDSTOCK = 1010;
    public static final int DIST_DELETE_ORDER = 1011;
    public static final int DIST_DELETE_STOCK_ORDER = 1012;

    // Delivery order for realtime sync
    public static final int SYNC_ORDER_DELIVERY_STATUS_UPLOAD = -47;
    public static final int NOTIFY_ORDER_DELIVERY_STATUS_UPLOADED = -48;
    public static final int NOTIFY_ORDER_DELIVERY_STATUS_UPLOAD_ERROR = -49;

    //Pick List
    public static final int SYNCPICKLISTUPLOAD = -50;
    public static final int NOTIFY_PICKLIST_UPLOADED = -51;
    public static final int NOTIFY_PICKLIST_UPLOAD_ERROR = -52;

    public static final int SYNC_TRIP = -80;
    public static final int NOTIFY_TRIP_UPLOADED = -81;
    public static final int NOTIFY_TRIP_UPLOAD_ERROR = -82;

    // ***********
    public static final int PRINT_COUNT = 5;
    public static final int PRINT_TEXT_SIZE = 1;
    public static final int NOTIFY_PRINT = -10;
    public static final int NEWOUTLET_UPLOAD = -20;
    public static final int RETAILER_DOWNLOAD_FAILED = -22;
    public static final int NOTIFY_CALL_ANALYSIS_TIMER = 200;
    public static final String SD = "IvyCPG";

    public static boolean invoicereportspinner = true;

    public static final String tbl_Payment = "Payment";
    public static final String tbl_SalesReturnHeader = "SalesReturnHeader";
    public static final String tbl_SalesReturnDetails = "SalesReturnDetails";
    public static final String tbl_vanload = "VanLoad";
    private static final String tbl_odameter = "Odameter";
    private static final String tbl_CollectionDocument = "CollectionDocument";

    public static final String tbl_retailerscoreheader = "RetailerScoreHeader";
    public static final String tbl_retailerscoredetail = "RetailerScoreDetails";

    public static final String tbl_closingstockheader = "ClosingStockHeader";
    public static final String tbl_closingstockdetail = "ClosingStockDetail";
    public static final String tbl_SbdMerchandisingDetail = "SbdMerchandisingDetail";
    public static final String tbl_SbdMerchandisingHeader = "SbdMerchandisingHeader";
    public static final String tbl_LastVisitStock = "LastVisitStock";
    public static final String tbl_LastVisitStock_History = "LastVisitStock_History";

    public static final String tbl_AnswerHeader = "AnswerHeader";
    private static final String tbl_AnswerDetail = "AnswerDetail";
    private static final String tbl_AnswerImageDetail = "AnswerImageDetail";
    public static final String tbl_OutletTimestamp = "OutletTimestamp";
    public static final String tbl_OutletTimestamp_images = "OutletTimestampImages";
    public static final String tbl_PhotoCapture = "PhotoCapture";
    private static final String tbl_NewOutletImage = "NewOutletImage";

    private static final String tbl_leaveapprovaldetails = "LeaveApprovalDetails";
    private static final String tbl_leaveapprovaldetails_cols = "RefId,Status,ApprovedDate,uid";

    public static final String tbl_expensedetails = "ExpenseDetail";
    private static final String tbl_expensedetails_cols = "Tid,typeID,amount,Refid";
    public static final String tbl_expenseheader = "ExpenseHeader";
    private static final String tbl_expenseheader_cols = "Tid,userid,date,TotalAmount,utcDate";
    public static final String tbl_expenseimagedetails = "ExpenseImageDetails";
    private static final String tbl_expenseimagedetails_cols = "Tid,Refid,imagename";

    public static final String tbl_retailercontractrenewal = "RetailerContractRenewalDetails";
    private static final String tbl_retailercontractrenewal_cols = "RetailerId,ContractId,Tid,startdate,enddate,utcDate,templateid,description,typelovid";

    private static final String tbl_newretailersurveyresultheader = "NewRetailerSurveyResultHeader";
    private static final String tbl_newretailersurveyresultheader_cols = "uid,surveyid,retailerid";
    private static final String tbl_newretailersurveyresultdetail = "NewRetailerSurveyResultDetail";
    private static final String tbl_newretailersurveyresultdetail_cols = "uid,qid,qtype,answerid,answer,score,noreply";
    private static final String tbl_retailerpriorityproducts = "RetailerPriorityProducts";
    private static final String tbl_retailerpriorityproducts_cols = "RetailerId,ProductId,LevelId";

    public static final String CR1 = "\n";
    // Used to communicate state changes in the ApkDownloaderThread
    public static final int MESSAGE_DOWNLOAD_STARTED = 1000;
    public static final int MESSAGE_APK_DOWNLOAD_COMPLETE = 1001;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 1002;
    public static final int MESSAGE_DOWNLOAD_CANCELED = 1003;
    public static final int MESSAGE_CONNECTING_STARTED = 1004;
    public static final int MESSAGE_ENCOUNTERED_ERROR_APK = 1005;
    public static final int THIRD_PARTY_INSTALLATION_ERROR = 1006;
    public static final int SDCARD_NOT_AVAILABLE = 1007;
    public static final int MESSAGE_DOWNLOAD_COMPLETE_DC = 1008;
    public static final int MESSAGE_ENCOUNTERED_ERROR_DC = 1009;

    public static final String tbl_beatMaster = "BeatMaster";
    public static final String tbl_beatMaster_cols = "BeatID,BeatDescription,today,BeatCode";


    public static final String tbl_userMaster = "UserMaster";
    public static final String tbl_userMaster_cols = "distributorid,distributorTinNumber,distributorName,branchid,vanid,userid,username,Password,loginid,distContactNo,OrganisationId,downloaddate,UserCode,custommsg,accountno,credit_limit,admincno,isJointCall,vanno,SchemeFactor,upliftFactor,usertype,ProfileImagePath,BackupUserId,UserPositionId,UserLevelId";

    public static final String tbl_DTPMaster = "DTPMaster";
    public static final String tbl_retailerMaster = "RetailerMaster";

    public static final String tbl_AssetService = "AssetServiceRequest";
    private static final String tbl_AssetService_cols = "Uid,date,AssetId,serialNum,reasonid,retailerid";

    public static final String tbl_AssetAddDelete = "AssetAddDelete";
    private static final String tbl_AssetAddDelete_cols = "uid,retailerid,AssetId,serialNum,installdate,creationdate,flag,mappingid,productid,TypeLovId,reasonid,remarks,toRetailerId";

    public static final String tbl_SerializedAssetHeader = "SerializedAssetHeader";
    public static final String tbl_SerializedAssetHeader_cols = "uid,DateTime,RetailerId,remarks,VisitId";

    public static final String tbl_SerializedAssetDetail = "SerializedAssetDetail";
    public static final String tbl_SerializedAssetDetail_cols = "uid,AssetID,isAvailable,ReasonID,SerialNumber,conditionId,NFCNumber,installdate,lastServicedate,isAuditDone";

    public static final String tbl_SerializedAssetImageDetail = "SerializedAssetImageDetails";
    public static final String tbl_SerializedAssetImageDetail_cols = "uid,AssetID,ImageName,serialNumber";

    public static final String tbl_SerializedAssetServiceRequest = "SerializedAssetServiceRequest";
    public static final String tbl_SerializedAssetServiceRequest_cols = "Uid,date,AssetId,serialNumber,reasonid,retailerid,serviceProviderId,IssueDescription,ImagePath,Status,ExpectedResolutionDate";

    public static final String tbl_SerializedAssetTransfer = "SerializedAssetTransfer";
    private static final String tbl_SerializedAssetTransfer_cols = "uid,AssetId,serialNumber,NFCNumber,installDate,creationdate,RequestType,reasonid,remark,retailerId,Transfer_To,Transfer_Type,AllocationRefId,rentPrice,toDate,VisitId,DeliveryDate,Qty";

    public static final String tbl_SerializedAssetTransferImages = "SerializedAssetTransferImages";
    private static final String tbl_SerializedAssetTransferImg__cols = "uid,ImageName";

    public static final String tbl_SerializedAssetUpdate = "SerializedAssetUpdate";
    private static final String tbl_SerializedAssetUpdate_cols = "Uid,AssetId,serialNumber,AllocationRefId,NewSerialNumber,Date,RetailerId,rentPrice,toDate,VisitId";

    public static final String tbl_SerializedAssetApproval = "SerializedAssetApproval";
    private static final String tbl_SerializedAssetApproval_cols = "AssetId,RetailerId,RequestId,RequestedDate,ApprovalStatus,ApprovalDate,SerialNumber,Type";

    public static final String tbl_HhtModuleMaster = "HhtModuleMaster";
    public static final String tbl_HhtMenuMaster = "HhtMenuMaster";

    private static final String tbl_TaskMaster = "TaskMaster";
    private static final String tbl_TaskMaster_cols = "taskid,taskdesc,taskcode,TaskOwner,Date,DueDate,CategoryId,EndDate,Status";

    private static final String tbl_TaskImageDetails = "TaskImageDetails";
    private static final String tbl_TaskImageDetails_cols = "TaskId,TaskImageId,TaskImageName,Status";

    public static final String tbl_TaskConfigurationMaster = "TaskConfigurationMaster";
    private static final String tbl_TaskConfigurationMaster_cols = "taskid,retailerid,uid,date,usercreated,userid";

    public static final String tbl_InvoiceMaster = "InvoiceMaster";
    public static final String tbl_InvoiceDetails = "InvoiceDetails";

    private static final String tbl_StockProposalMaster = "StockProposalMaster";
    private static final String tbl_StockProposalMaster_cols = "uid,pid,qty,pcsQty,caseQty,outerQty,duomid,duomQty,dOuomQty,dOuomid,date";

    public static final String tbl_StandardListMaster = "StandardListMaster";

    public static final String tbl_DailyTargetPlanned = "DailyTargetPlanned";
    private static final String tbl_DailyTargetPlanned_cols = "TargetID,RetailerID,TargetValue,Date,IsGoldenStore";

    public static final String tbl_DenominationDetails = "DenominationDetails";
    private static final String tbl_DenominationDetails_cols = "uid,value,count,lineAmount,isCoin";

    public static final String tbl_DenominationHeader = "DenominationHeader";
    private static final String tbl_DenominationHeader_cols = "uid,date,amount";

    public static final String tbl_orderHeader = "OrderHeader";
    public static final String tbl_orderDetails = "OrderDetail";
    public static final String tbl_OrderFreeIssues = "OrderFreeIssues";
    public static final String tbl_InvoiceFreeIssues = "InvoiceFreeIssues";
    public static final String tbl_orderReturnDetails = "OrderReturnDetail";
    private static final String tbl_closingStockHeader = "ClosingStockHeader";
    private static final String tbl_closingStockDetails = "ClosingStockDetail";
    private static final String tbl_InvoiceHeaderUpload = "InvoiceMaster";
    private static final String tbl_InvoiceDetailsUpload = "InvoiceDetails";
    private static final String tbl_OutletTimestampupload = "OutletTimestamp";
    private static final String tbl_OutletTimestampImagesupload = "OutletTimestampImages";
    private static final String tbl_retailerMasterupload = "RetailerMaster";
    private static final String tbl_retailerAddress = "RetailerAddress";
    private static final String tbl_retailerContact = "RetailerContact";
    private static final String tbl_retailerAttribute = "RetailerAttribute";
    private static final String tbl_retailerEditAttribute = "RetailerEditAttribute";
    public static final String tbl_orderHeaderRequest = "OrderHeaderRequest";
    public static final String tbl_orderDetailRequest = "OrderDetailRequest";
    public static final String tbl_retailerPotential = "RetailerPotential";
    public static final String tbl_contactAvailability = "ContactAvailability";

    private static final String tbl_deviateReasontableupload = "deviateReasontable";
    private static final String tbl_SbdMerchandisingHeaderupload = "SbdMerchandisingHeader";
    private static final String tbl_SbdMerchandisingDetailupload = "SbdMerchandisingDetail";
    private static final String tbl_Photocaptureupload = "Photocapture";
    public static final String tbl_DayClose = "DayClose";
    private static final String tbl_NonProductiveTable = "Nonproductivereasonmaster";
    private static final String tbl_NonProductiveModuleTable = "NonProductiveModules";
    public static final String tbl_AssetHeader = "AssetHeader";
    public static final String tbl_AssetDetail = "AssetDetail";
    public static final String tbl_AssetImgInfo = "AssetImageDetails";
    public static final String tbl_NearExpiryHeader = "NearExpiry_Tracking_Header";
    private static final String tbl_NearExpiryHeader_cols = "Tid,RetailerId,Uid,Date,TimeZone,RefId,ridSF,VisitId";
    private static final String tbl_NearExpiryDetail = "NearExpiry_Tracking_Detail";
    private static final String tbl_NearExpiryDetail_cols = "Tid,PId,LocId,ExpDate,UOMId,UOMQty,isAuditDone,IsOwn";
    public static final String tbl_PlanogramDetail = "PlanogramDetails";
    public static final String tbl_PlanogramHeader = "PlanogramHeader";
    private static final String tbl_PlanogramDetail_cols = "TiD,MappingId,PId,ImageName,ImagePath,Adherence,ReasonID,LocID,CounterId,isAuditDone,ComplianceStatus,CompliancePercentage";
    private static final String tbl_PlanogramHeader_cols = "TiD,RetailerId,Date,Timezone,RefId,Type,CounterId,DistributorID,ridSF,VisitId";
    public static final String tbl_CompetitorHeader = "CompetitorHeader";
    public static final String tbl_CompetitorDetails = "CompetitorDetails";
    private static final String tbl_CompetitorHeader_cols = "Tid,RetailerId,CompetitorID,Feedback,ImageName,Date,Pid,Remark,CounterId,distributorid,ridSF,VisitId";
    private static final String tbl_CompetitorDetails_cols = "Tid,TrackingListID,tcompetitorid,pid,FromDate,ToDate,Feedback,ImageName,qty,reasonID,RField1";
    private static final String tbl_invoicetaxDetails = "InvoiceTaxDetails";
    private static final String tbl_invoicetaxDetails_cols = "invoiceid,pid,taxRate,taxType,taxValue,IsFreeProduct,applyLevelId";
    private static final String tbl_ordertaxDetails = "OrderTaxDetails";
    private static final String tbl_ordertaxDetails_cols = "orderid,pid,taxRate,taxType,taxValue,IsFreeProduct,groupid,applyLevelId";
    private static final String tbl_invoice_return_detail = "InvoiceReturnDetail";
    private static final String tbl_invoice_return_detail_cols = "InvoiceID,Pid,UomID,TypeID,Qty,Price,LineValue,LiableQty,ReturnQty";

    // For Asean IS Upload Tables
    private static final String tbl_PromotionHeader = "PromotionHeader";
    private static final String tbl_PromotionDetail = "PromotionDetail";

    private static final String tbl_MonthlyPlanHeaderMaster = "MonthlyPlanHeaderMaster";
    private static final String tbl_MonthlyPlanDetail = "MonthlyPlanDetail";

    private static final String tbl_SOD_Tracking_Detail = "SOD_Tracking_Detail";
    private static final String tbl_SOD_Tracking_Header = "SOD_Tracking_Header";

    private static final String tbl_SOD_Asset_Tracking_Detail = "SOD_Asset_Tracking_Detail";
    private static final String tbl_SOD_Asset_Tracking_Header = "SOD_Asset_Tracking_Header";
    private static final String tbl_SOD_Assets_Detail = "SOD_Assets_Detail";

    private static final String tbl_SOSKU_Tracking_Detail = "SOSKU_Tracking_Detail";
    private static final String tbl_SOSKU_Tracking_Header = "SOSKU_Tracking_Header";

    private static final String tbl_SOS_Tracking_Detail = "SOS_Tracking_Detail";
    private static final String tbl_SOS_Tracking_Header = "SOS_Tracking_Header";
    private static final String tbl_SOS_Tracking_Parent_Detail = "SOS_Tracking_Parent_Detail";
    public static final String tbl_SOS__Block_Tracking_Detail = "SOS_Tracking_Block_Detail";

    private static final String tbl_delivery_header = "DeliveryHeader";
    private static final String tbl_delivery_header_cols = "OrderId,Status,RetailerId,DeliveryDate,ReasonId";

    private static final String tbl_delivery_detail = "DeliveryDetail";
    private static final String tbl_delivery_detail_cols = "OrderId,PId,OrderedQty,DeliveredQty,UOMId,Price";

    private static final String tbl_orderHeader_cols = "OrderID,RetailerID,RouteId,OrderValue,LinesPerCall,OrderDate,DeliveryDate,IsToday,po,remark,discount,is_splitted_order,is_processed,latitude,longitude,ReturnValue,CrownCount,IndicativeOrderID,IFlag,sid,stype,timestampid,JFlag,totalTaxValue,invoicestatus,imagename,SalesType,totalweight,isApproval,PrintFilePath,RField1,RField2,ordertime,SParentID,RemarksType,RField3,orderImagePath,ParentHierarchy,AddressId,ridSF,VisitId,LevelCode";
    private static final String tbl_orderDetails_cols = "OrderID,ProductID,Qty,uomid,Rate,uomcount,msqqty,pieceqty,caseqty,d1,d2,d3,DA,outerQty,dOuomQty,dOuomid,soPiece,soCase,OrderType,casePrice,outerPrice,pcsUOMId,totalamount,batchid,PriceOffId,PriceOffValue,isFreeProduct,weight,ReasonId,HsnCode,RField1,NetAmount,UpsellingQty,ASRP";
    private static final String tbl_orderReturnDetails_cols = "OrderID,Pid,UomID,TypeID,Qty,Price,LineValue,LiableQty,ReturnQty";
    private static final String tbl_closingStockHeader_cols = "StockID,Date,RetailerID,remark,latitude,longitude,DistributorID,Weightage,Score,AvailabilityShare,ridSF,VisitId";

    public static final String tbl_OrderFreeIssues_cols = "Uid,productId,uomId,qty,conversionQty,reasonId,price,taxPrice,totalValue,batchId";
    public static final String tbl_InvoiceFreeIssues_cols = "Uid,productId,uomId,qty,conversionQty,reasonId,price,taxPrice,totalValue,batchId";

    private static final String tbl_closingStockDetails_cols = "StockID,ProductID,Shelfpqty,whpqty,uomid,uomqty,msqqty,Shelfcqty,whcqty,whoqty,shelfoqty,ouomid,ouomqty,LocId,isDistributed,isListed,ReasonID,Facing,IsOwn,PcsUOMId,Rfield1,Rfield2,Rfield3,Score,isAvailable,isAuditDone";
    private static final String tbl_InvoiceHeaderUpload_cols = "InvoiceNo,RetailerId,InvoiceDate,InvoiceAmount,Discount,InvNetAmount,OrderId,remark,ImageName,latitude,longitude,return_amt,imgName,LinesPerCall,totalweight,SalesType,print_count,sid,stype,SchemeAmount,TaxAmount,creditPeriod,PrintFilePath,SParentID,timestampid,AddressId,ridSF,VisitId";
    private static final String tbl_InvoiceDetailsUpload_cols = "InvoiceId,ProductId,Qty,Rate,UomId,uomCount,pcsQty,caseQty,d1,d2,d3,DA,outerQty,dOuomQty,dOuomid,batchid,casePrice,outerPrice,pcsUOMId,OrderType,totalamount,PriceOffId,PriceOffValue,isFreeProduct,weight,hasSerial,TaxAmount,SchemeAmount,DiscountAmount,NetAmount,HsnCode,RField1";
    private static final String tbl_PaymentUpload_cols = "uid,BillNumber,Amount,CashMode,ChequeNumber,ChequeDate,BankID,BranchCode,RetailerID,BeatID,Date,remark,payType,ImageName,GroupId,StatusLovId,totaldiscount,DistributorID,receiptno,refid,RefNo,PrintFilePath,DistParentID,BankName,BranchName,ridSF,VisitId";
    private static final String tbl_CollectionDocument_cols = "uid,BillNumber,ContactName,ContactNumber,RetailerID,DocRefNo,ReasonID,Remarks,SignaturePath";
    private static final String tbl_OutletTimestampupload_cols = "VisitID,BeatID,VisitDate,RetailerID,TimeIn,TimeOut,latitude,longitude,JFlag,gpsAccuracy,gpsDistance,gpsCompliance,Sequence,Feedback,DistributorID,Battery,LocationProvider,IsLocationEnabled,IsDeviated,outLatitude,outLongitude,OrderValue,lpc,RetailerName,ridSF,tripUid";
    private static final String tbl_OutletTimestampImageupload_cols = "uid,imageName";
    private static final String tbl_retailerMasterupload_cols = "RetailerID,RetailerName,SubChannelid,Beatid,VisitDays,LocationId,creditlimit,RPTypeId,tinnumber,RField3,distributorId,taxtypeid,contractstatuslovid,classid,AccountId,VatNo,creditPeriod,ProfileImagePath,inSEZ,GSTNumber,RField5,RField6,TinExpDate,pan_number,food_licence_number,food_licence_exp_date,DLNo,DLNoExpDate,RField4,RField7,userid";
    private static final String tbl_deviateReasontableupload_cols = "uid,retailerid,date,reasonid,DistributorID,remarks,ridSF";
    private static final String tbl_SbdMerchandisingHeaderupload_cols = "uid,date,RetailerID,beatid";
    private static final String tbl_SbdMerchandisingDetailupload_cols = "uid,sbdid,brandid,visibilityListid,value,TypeListId,isHit";
    private static final String tbl_Photocaptureupload_cols = "uid,date,RetailerID,imagepath,phototypeid,pid,fromdate,todate,LocId,sku_name,abv,lot_code,seq_num,DistributorID,feedback,ridSF,VisitId";
    public static final String tbl_DayClose_cols = "status,TimeOut";

    private static final String tbl_NonProductiveTable_cols = "UID,retailerid,RouteID,Date,ReasonID,ReasonTypes,DistributorID,ImagePath,remarks,ridSF";
    private static final String tbl_NonProductiveModuleTable_cols = "Tid,RetailerID,ModuleCode,ReasonID,ImagePath";
    private static final String tbl_SalesReturnHeader_cols = "uid,date,RetailerID,ReturnValue,Lpc,remark,latitude,longitude,credit_flag,unload,IsCreditNoteApplicable,ReplacedValue,Distributorid,DistParentID,SignaturePath,imgName,RefModule,RefModuleTId,IFlag,invoiceid,isCancel,CollectStatus,UserID,ridSF,VisitId";
    private static final String tbl_SalesReturnDetails_cols = "uid,outerQty,dOuomQty,dOuomid,Cqty,duomQty,duomid,Pqty,batchid,Condition,mfgdate,expdate,oldmrp,ProductID,invoiceno,srpedited,totalQty,reason_type,LotNumber,piece_uomid,HsnCode";
    private static final String tbl_AnswerHeader_cols = "surveyid,retailerid,uid,date,ModuleID,SupervisiorId,Remark,achScore,tgtScore,AchBonusPoint,MaxBonusPoint,type,counterid,refid,DistributorID,userid,ridSF,VisitId";
    private static final String tbl_AnswerDetail_cols = "answerid,qid,answer,qtype,uid,score,isExcluded";
    private static final String tbl_AnswerImageDetail_cols = "qid,uid,imgName";

    private static final String tbl_vanload_cols = "pid,uid,qty,date,outerQty,caseQty,pcsQty,duomQty,duomid,dOuomQty,dOuomId,BatchId,batchno,SubDepotId";
    private static final String tbl_odameter_cols = "uid,start,end,starttime,endtime,startlatitude,startlongitude,endlatitude,endlongitude,date,tripUid";
    private static final String tbl_AssetHeader_Cols = "uid,Date,RetailerId,remark,TypeLovId,tgtTotal,achTotal,Weightage,Score,distributorid,ridSF,VisitId,refid";
    private static final String tbl_AssetDetail_Cols = "uid,AssetID,AvailQty,ImageName,ReasonID,SerialNumber,Mappingid,Productid,installdate,servicedate,conditionId,CompQty,locid,PosmGroupLovId,isExecuted,Score,TgtLocId,isAuditDone";
    private static final String tbl_AssetImgInfo_Cols = "uid,AssetID,ImageName,Mappingid,PId,locid";

    private static final String tbl_stock_apply = "StockApply";
    private static final String tbl_stock_apply_cols = "uid,date,Status";
    public static final String tbl_vanunload_details = "VanUnloadDetails";
    private static final String tbl_vanunload_details_cols = "uid,pid,batchid,caseqty,pcsqty,outerqty,duomqty,douomqty,dUomId,dOuomid,date,type,SubDepotId,TypeID,LineValue,Price,RefId,LiableQty,isFree";
    public static final String tbl_SIH = "StockInHandMaster";
    private static final String tbl_SIH_cols = "pid,batchid,qty,adjusted_qty";
    public static final String tbl_ExcessStockInHand = "ExcessStockInHand";
    private static final String tbl_ExcessStockInHand_cols = "pid,qty";
    public static final String tbl_NonSalableSIHMaster = "NonSalableSIHMaster";
    private static final String tbl_NonSalableSIHMaster_cols = "pid,qty,reasonid";
    private static final String tbl_NEWOUTLETIMAGE_cols = "RetailerID,ListId,ImageName";
    public static final String tbl_outlet_time_stamp_detail = "OutletTimeStampDetail";
    public static final String tbl_outlet_time_stamp_detail_cols = "UID,ModuleCode,TimeIn,TimeOut,RetailerID";

    public static final String tbl_Free_SIH = "FreeStockInHandMaster";
    private static final String tbl_Free_SIH_cols = "pid,batchid,qty";

    // For Asean IS Upload Table Columns
    private static final String tbl_splitted_order = "SplittedOrder";
    private static final String tbl_splitted_order_cols = "RetailerID,OrderID";

    private static final String tbl_PromotionHeader_cols = "Uid,RetailerId,Date,Remark,distributorid,Weightage,Score,ridSF,VisitId";
    private static final String tbl_PromotionDetail_cols = "Uid,PromotionID,BrandID,IsExecuted,ImageName,reasonid,flag,MappingId,locid,ExecRatingLovId,PromoQty,HasAnnouncer,Score,fromDate,toDate,remarks";

    private static final String tbl_MonthlyPlanHeaderMaster_cols = "RetailerID,Base,Promotion,Initiative,TargetMonth,Others,IsLock";
    private static final String tbl_MonthlyPlanDetail_cols = "RetailerId,InitId,Target";

    private static final String tbl_SOD_Tracking_Detail_cols = "Uid,pid,Norm,Actual,Required,Gap,ParentTotal,ReasonId,ImageName,RetailerId,IsOwn,Parentid,MappingId,LocId,isAuditDone";
    private static final String tbl_SOD_Tracking_Header_cols = "Uid,RetailerId,Date,Remark,ridSF,VisitId,refID";

    private static final String tbl_SOD_Asset_Tracking_Detail_cols = "Uid,pid,Norm,Actual,Required,Gap,ParentTotal,ReasonId,ImageName,RetailerId,IsOwn,Parentid,MappingId,LocId,isAuditDone";
    private static final String tbl_SOD_Asset_Tracking_Header_cols = "Uid,RetailerId,Date,Remark";
    private static final String tbl_SOD_Assets_Detail_cols = "Uid,AssetID,Actual,ReasonID,LocationID,Retailerid,ProductId,isPromo,isDisplay";

    private static final String tbl_SOSKU_Tracking_Header_cols = "Uid,RetailerId,Date,Remark,ridSF,VisitId";
    private static final String tbl_SOSKU_Tracking_Detail_cols = "Uid,pid,Norm,Actual,Required,Gap,ParentTotal,ReasonId,ImageName,RetailerId,IsOwn,Parentid,MappingId";

    private static final String tbl_SOS_Tracking_Detail_cols = "Uid,pid,Norm,Actual,Required,Gap,ParentTotal,ReasonId,ImageName,RetailerId,IsOwn,Parentid,MappingId,locid,remarks,Flex1,isAuditDone";
    private static final String tbl_SOS_Tracking_Header_cols = "Uid,RetailerId,Date,Remark,ridSF,VisitId,refID";
    private static final String tbl_SOS_Tracking_Parent_Detail_cols = "Uid,pid,blockcount,shelfcount,shelflength,extrashelf,total,locid";
    private static final String tbl_SOS_Block_Tracking_Detail_cols = "Uid,pid,ChildPid,SubCellId,CellId,locid";

    public static final String tbl_credit_note = "CreditNote";
    public static final String tbl_credit_note_cols = "id,refno,amount,retailerid,date,creditnotetype,isused,Appliedamount,Actualamount";

    private static final String tbl_location_tracking = "LocationTracking";
    private static final String tbl_location_tracking_cols = "Tid,Date,Latitude,Longtitude,Accuracy,Activity,Battery,LocationProvider,IsLocationEnabled";

    public static final String tbl_PriceHeader = "PriceCheckHeader";
    private static final String tbl_PriceHeader_cols = "Tid,RetailerId,Date,TimeZone,distributorid,Weightage,Score,ridSF,VisitId";

    private static final String tbl_PriceDetail = "PriceCheckDetail";
    private static final String tbl_PriceDetail_cols = "Tid,PId,Changed,Price,Compliance,ReasonId,Own,UomID,Mrp,mop,price_change_reasonid,Score,inStoreLocId,hasPriceTag";

    private static final String tbl_attendancedetail = "AttendanceDetail";
    private static final String tbl_attendancedetail_cols = "Tid,DateIn,Atd_ID,ReasonID,FromDate,ToDate,Timezone,Status,Remarks,Session,jointUserId,LeaveType_LovId,TotalDays,timeSpent,userid";

    private static final String tbl_leavedetail = "LeaveDetail";
    private static final String tbl_leavedetail_cols = "Tid,StartDate,EndDate,FrequencyType,ApplyDays";

    private static final String tbl_outletjoincall = "OutletJoinCall";
    private static final String tbl_outletjoincall_cols = "timestampid,supid";

    public static final String tbl_EmptyReconciliationHeader = "EmptyReconciliationHeader";
    private static final String tbl_EmptyReconciliationHeader_cols = "Tid,Value,Date,TimeZone";

    public static final String tbl_EmptyReconciliationDetail = "EmptyReconciliationDetail";
    private static final String tbl_EmptyReconciliationDetail_cols = "Tid,PId,Qty,UomId,UomCount,Price,LineValue";

    public static final String tbl_SubDepotSettlement = "SubDepotSettlement";
    private static final String tbl_SubDepotSettlement_cols = "Uid,PaymentType,Amount,SubDepotId,ReFid,Date";

    public static final String tbl_scheme_details = "SchemeDetail";
    private static final String tbl_SchemeDetail_cols = "OrderID,InvoiceID,SchemeID,ProductID,SchemeType,Value,parentid,Amount";

    public static final String tbl_SchemeFreeProductDetail = "SchemeFreeProductDetail";
    private static final String tbl_SchemeFreeProductDetail_cols = "OrderID,InvoiceID,SchemeID,FreeProductID,FreeQty,UomID,UomCount,BatchId,parentid,Price,TaxAmount,HsnCode";

    public static final String tbl_TransactionSequence = "TransactionSequence";
    private static final String tbl_TransactionSequence_cols = "TypeID,SeqNo";

    public static final String tbl_InvoiceDiscountDetail = "InvoiceDiscountDetail";
    private static final String tbl_InvoiceDiscountDetail_cols = "OrderId,InvoiceId,Pid,Typeid,Value,Percentage,ApplyLevelid,DiscountId,IsCompanyGiven";

    public static final String tbl_TripMaster = "TripMaster";
    public static final String tbl_TripMaster_cols = "uid,userId,startDate,endDate,status,startlatitude,startlongitude,endlatitude,endlongitude";

    public static final String tbl_OrderDiscountDetail = "OrderDiscountDetail";
    private static final String tbl_OrderDiscountDetail_cols = "OrderId,Pid,Typeid,Value,Percentage,ApplyLevelid,DiscountId,IsCompanyGiven";


    public static final String tbl_RoadActivityHeader = "RoadActivityTransaction";
    private static final String tbl_RoadActivityHeader_cols = "Uid,TypeId,PId,LocationId,Remarks";

    public static final String tbl_RoadActivityDetail = "RoadActivityTransactionDetail";
    private static final String tbl_RoadActivityDetail_cols = "Uid,ImgName";

    public static final String tbl_RetailerEditHeader = "RetailerEditHeader";
    public static final String tbl_RetailerEditDetail = "RetailerEditDetail";
    public static final String tbl_RetailerContactEdit = "RetailerContactEdit";
    public static final String tbl_ContactAvailabilityEdit = "ContactAvailabilityEdit";

    public static final String tbl_ContactAvailability_Cols = "CPId,CPAId,Day,StartTime,EndTime";
    public static final String tbl_ContactAvailabilityEdit_Cols = "CPId,CPAId,Day,StartTime,EndTime,Status,Tid";
    private static final String tbl_RetailerEditHeader_cols = "Tid,RetailerId,Date";
    private static final String tbl_RetailerEditDetail_cols = "Tid,code,Value,RefId";
    private static final String tbl_RetailerContactEdit_cols = "Contact_Title,Contact_Title_LovId,ContactName,ContactName_LName,ContactNumber,Email,IsPrimary,Status,CPId,RetailerId,Tid,salutationLovId,IsEmailNotificationReq";


    private static final String tbl_RetailerVerification = "RetailerVerification";
    private static final String tbl_RetailerVerification_cols = "RefId,IsValidated";
    public static final String tbl_password_rules = "PasswordPolicyRules";
    private static final String tbl_activity_jointcall = "ActivityJointCall";
    private static final String tbl_activity_jointcall_cols = "menucode,uid,supervisorid";

    public static final String tbl_DistributorMaster = "DistributorMaster";
    public static final String tbl_IncentiveDashboard = "IncentiveDashboard";


    public static final String tbl_distributor_closingstock_header = "DistStockCheckHeader";
    public static final String tbl_distributor_closingstock_detail = "DistStockCheckDetails";
    private static final String tbl_distributor_closingstock_header_cols = "UId,DistId,Date,DownloadedDate";
    private static final String tbl_distributor_closingstock_detail_cols = "UId,PId,BatchId,WarehouseId,UomId,UomCount,Qty";

    public static final String tbl_DistInvoiceDetails = "DistInvoiceDetails";
    private static final String tbl_DistInvoiceDetails_cols = "InvoiceId,StatusLovId";

    public static final String tbl_DistTimeStampHeader = "DistTimeStampHeader";
    private static final String tbl_DistTimeStampHeader_cols = "UId,DistId,Date,TimeIn,TimeOut,Latitude,Longitude,DownloadedDate";

    public static final String tbl_DistTimeStampDetails = "DistTimeStampDetails";
    private static final String tbl_DistTimeStampDetails_cols = "UId,MenuCode,TimeIn,TimeOut,Date";

    public static final String tbl_distributor_order_header = "DistOrderHeader";
    public static final String tbl_distributor_order_detail = "DistOrderDetails";
    private static final String tbl_distributor_order_header_cols = "UId,DistId,Date,TotalValue,LPC,DownloadedDate,DeliveryDate";
    private static final String tbl_distributor_order_detail_cols = "UId,PId,Qty,Price,BatchId,UomId,UomCount,LineValue";
    private static final String tbl_TaskExecutionDetails = "TaskExecutionDetails";
    private static final String getTbl_TaskExecutionDetails_cols = "TaskId,RetailerId,Date,UId,ridSF";
    private static final String tbl_SOD_Tracking_Block_Detail = "SOD_Tracking_Block_Detail";
    private static final String tbl_SOD_Tracking_Block_Detail_cols = "uid,PId,CellId,SubCellId,ChildPId,LocId";
    private static final String tbl_SOD_Tracking_Parent_Detail = "SOD_Tracking_Parent_Detail";
    private static final String tbl_SOD_Tracking_Parent_Detail_cols = "uid,PId,BlockCount,ShelfCount,ShelfLength,ExtraShelf,total,LocId";

    private static final String tbl_UserFeedBack = "UserFeedBack";
    private static final String tbl_UserFeedBack_cols = "UId,DateTime,TypeLovId,Feedback,Rating";

    public static final String tbl_van_delivery_header = "VanDeliveryHeader";
    private static final String tbl_van_delivery_header_cols = "Uid,RetailerID,InvoicedDate,DeliveryDate,status,ReasonId,Remarks,Proofpicture,latitude,longtitude,utcdate,invoiceid,contactName,contactNo,SignaturePath,PickListId";
    public static final String tbl_van_delivery_detail = "VanDeliveryDetail";
    private static final String tbl_van_delivery_detail_cols = "Uid,Pid,Uomid,Batchid,invoiceqty,Deliveredqty,Returnqty";

    private static final String tbl_InvoiceSerialNO = "InvoiceSerialNumbers";
    private static final String tbl_InvoiceSerialNO_cols = "orderid,invoiceid,pid,serialNumber,uomid,Retailerid";

    public static final String tbl_nearbyRetailer = "NearByRetailers";
    private static final String tbl_nearbyRetailer_cols = "rid,nearbyrid";
    public static final String tbl_nearbyEditRequest = "RrtNearByEditRequest";
    private static final String tbl_nearbyEditRequest_cols = "tid,rid,nearbyrid,status";
    public static final String tbl_RetailerEditPriorityProducts = "RetailerEditPriorityProducts";
    private static final String tbl_RetailerEditPriorityProducts_cols = "tid,retailerId,productId,levelid,status";

    public static final String tbl_SalesReturnReplacementDetails = "SalesReturnReplacementDetails";
    private static final String tbl_SalesReturnReplacementDetails_cols = "uid,returnpid,batchid,uomid,uomCount,returnQty,pid,price,value,qty";

    public static final String tbl_SalesReturn_tax_Details = "SalesReturnTaxDetails";
    private static final String tb_SalesReturnTaxDetails_cols = "uid,Retailerid,pid,taxRate,taxType,applyLevelId,taxValue";

    private static final String tbl_AttendanceTimeDetails = "AttendanceTimeDetails";
    private static final String tbl_AttendanceTimeDetails_cols = "uid,date,intime,outtime,remarks,reasonid,userid,latitude,longitude,counterid";
    public static final String tbl_PaymentDiscount_Detail = "PaymentDiscountDetail";
    private static final String tbl_PaymentDiscountDetail_Cols = "uid,discountperc,discountvalue";
    public static final String tbl_ReallocationHeader = "ReallocationHeader";
    public static final String tbl_ReallocationHeader_Cols = "tid,AllocatedBy,AllocatedTo,Date,utcdate";
    public static final String tbl_Reallocationdetail = "Reallocationdetail";
    public static final String tbl_Reallocationdetail_Cols = "tid,retailerid,fromuserid,touserid";
    private static final String tbl_RetailerEntryDetails = "RetailerEntryDetails";
    private static final String tbl_RetailerEntryDetails_Cols = "UId,EntryMode,ReasonId";

    private static final String tbl_retailerContactupload_cols = "RetailerID,contactname,ContactName_LName,ContactNumber," +
            "contact_title,contact_title_lovid,IsPrimary,Email,salutationLovId,IsEmailNotificationReq,CPID";
    private static final String tbl_retailerAddressupload_cols = "RetailerID,Address1,Address2,Address3,ContactNumber,City,latitude,longitude,"
            + "email,FaxNo,pincode,State,IsPrimary,Mobile,Region,Country,District";
    private static final String tbl_retailerAttributeupload_cols = "RetailerId,AttributeId,LevelId";
    private static final String tbl_retailerEditAttributeupload_cols = "Tid,RetailerId,AttributeId,LevelId,Status";
    private static final String tbl_OrderHeaderRequest_cols = "OrderID,OrderDate,RetailerID,DistributorId,OrderValue,LinesPerCall,TotalWeight,Remarks,OrderTime";
    private static final String tbl_OrderDetailRequest_cols = "OrderID,ProductID,Qty,uomid,Price,LineValue,Weight,uomcount,HsnCode,RetailerID";
    private static final String tbl_RetailerPotential_cols = "rid,pid,volume,facing,display,IsOwn,Price";

    //Loyalty Points Uploaded Tables and fields
    public static final String tbl_loyaltyredemptionheader = "LoyaltyRedemptionHeader";
    public static final String tbl_tbl_loyaltyredemptionheader_cols = "UID,RetailerId,LoyaltyId,TotalPoints,Date,PointsTypeID";
    public static final String tbl_loyaltyredemptiondetail = "LoyaltyRedemptionDetail";
    public static final String tbl_loyaltyredemptiondetail_cols = "UID,BenefitId,Qty,Points";
    public static final String tbl_LoyaltyPoints = "LoyaltyPoints";
    public static final String tbl_LoyaltyPoints_cols = "RetailerId,LoyaltyId,BalancePoints,PointsTypeID";


    public static final String tbl_SOSHeader_Proj = "SOSHeader_Proj";
    public static final String tbl_SOSHeader_Proj_cols = "uid,retailerId,date";
    public static final String tbl_SOSDetail_Proj = "SOSDetail_Proj";
    public static final String tbl_SOSDetail_Proj_cols = "uid,groupId,pid,actual,isOwn,inTarget,target";


    public static final String tbl_UserEditDetail = "UserEditDetail";
    public static final String tbl_UserEditDetail_cols = "Tid,UserID,Code,Value";

    public static final String tbl_ModuleActivityDetails = "ModuleActivityDetails";
    public static final String tbl_ModuleActivityDetails_cols = "Tid,DistributorId,UserId,Date,ModuleCode,Activity,Latitude,longitude,GpsAccuracy";

    public static final String tbl_jointcallacknowledgement = "JointCallAcknowledgement";
    public static final String tbl_jointcallacknowledgement_cols = "Userid,Username,Beat,Retailer,Date,Value,Refid,AckDate,Upload";
    public static final String tbl_jointcallacknowledgement_upload_cols = "Refid,AckDate";

    private static final String tbl_OrderDeliveryDetail = "OrderDeliveryDetail";
    private static final String tbl_OrderDeliveryDetail_cols = "orderId,productId,uomId,qty,uomCount,price,taxPrice,lineValue";

    private static final String tbl_NonFieldActivity = "NonFieldActivity";
    private static final String tbl_NonFieldActivity_cols = "Uid,UserId,Date,ReasonId,Remarks,DistributorId";

    public static final String tbl_display_scheme_enrollment_header = "DisplaySchemeEnrollmentHeader";
    public static final String tbl_display_scheme_enrollment_cols = "Tid,Date,UserId,DistributorId,RetailerId,SchemeId,SlabId";

    public static final String tbl_display_scheme_tracking_header = "DisplaySchemeTrackingHeader";
    public static final String tbl_display_scheme_tracking_cols = "Tid,Date,UserId,DistributorId,RetailerId,SchemeId,SlabId,IsAvailable";

    public static final String tbl_date_wise_plan = "DatewisePlan";
    public static final String tbl_date_wise_plan_cols = "PlanId,DistributorId,UserId,Date,EntityId,EntityType,Status,Sequence,StartTime,EndTime,VisitStatus,PlanSource,planStatus";

    public static final String tbl_retailer_kpi_modified = "RetailerKPIModifiedDetail";
    public static final String tbl_retailer_kpi_modified_cols = "KPIId,KPITypeLovId,KPIParamLovId,Target";

    public static final String tbl_planogram_image_detail = "PlanogramImageDetails";
    public static final String tbl_planogram_image_detail_cols = "Tid,PId,imageName,mappingid,imagePath,imageId";

    private static final String tbl_JointCallDetail = "JointCallDetail";
    private static final String tbl_JointCallDetail_cols = "Uid,UserId,JointCallUserId,TimeIn,TimeOut,DateTime,Remarks";

    private static final String tbl_RetailerScoreHeader = "RetailerScoreHeader";
    private static final String tbl_RetailerScoreHeader_cols = "Tid,RetailerId,Date,Score";

    private static final String tbl_RetailerScoreDetail = "RetailerScoreDetails";
    private static final String tbl_RetailerScoreDetail_cols = "Tid,ModuleCode,Weightage,Score";

    private static final String tbl_CollectionDueHeader = "CollectionDueHeader";
    private static final String tbl_CollectionDueHeader_cols = "Date,SubmittedDate,RetailerId,uid,DistributorId,ParentDistributorId";


    private static final String tbl_CollectionDueDetails = "CollectionDueDetails";
    private static final String tbl_CollectionDueDetails_cols = "InvoiceNo,ReasonId,uid";

    public static final String tbl_order_delivery_status = "OrderDeliveryStatus";
    public static final String tbl_order_delivery_status_cols = "orderId,refId,status";


    public static final String tbl_movement_tracking_history = "MovementTrackingHistory";
    public static final String tbl_movement_tracking_history_cols = "userid,latitude,longitude,date_time";

    public static final String tbl_DigitalContent_Tracking_Header = "DigitalContentTrackingHeader";
    public static final String tbl_DigitalContent_Tracking_Header_cols = "UId,DId,RetailerId,Date";

    public static final String tbl_DigitalContent_Tracking_Detail = "DigitalContentTrackingDetail";
    public static final String tbl_DigitalContent_Tracking_Detail_cols = "UId,StartTime,EndTime,PId,isFastForwarded";

    private static final String tbl_Planorama = "Planorama";
    private static final String tbl_Planorama_cols = "uid,date,retailerId,comments,NoOfPhotos,ReferenceNo";

    private static final String tbl_Planorama_image = "PlanoramaImages";
    private static final String tbl_Planorama_image_cols = "uid,ImageName";

    public static final String tbl_picklist = "PickListStatus";
    public static final String tbl_picklist_cols = "PickListId,Status";
    public static final String tbl_picklist_invoice = "PickListInvoiceStatus";
    public static final String tbl_picklist_invoice_cols = "PickListId,InvoiceId,Status";

    public static final String tbl_retailer_notes = "RetailerNotes";
    public static final String tbl_retailer_notes_cols = "Tid,RetailerId,Date,Time,Title,Description,userId,NoteId,ModifiedDateTime,Status";

    private static final String tbl_AnswerScoreDetails = "AnswerScoreDetail";
    private static final String tbl_AnswerScoreDetails_cols = "Uid,SurveyId,qid,score";

    private static final String tbl_RetailerLocationDeviation = "RetailerLocationDeviation";
    private static final String tbl_RetailerLocationDeviation_cols = "Tid,ReasonID,RetailerID,Date,Type,ExpectedRadius,ActualRadius,OutletTimeStampID";

    public static final String tbl_DisplayAssetHeader = "DisplayAssetTrackingHeader";
    private static final String tbl_DisplayAsseteader_cols = "Uid,RetailerId,ridSF,visitId,Date,status,ownShare,competitorShare";

    public static final String tbl_DisplayAssetTDetails = "DisplayAssetTrackingDetails";
    private static final String tbl_DisplayAssetDetail_cols = "Uid,CompetitorId,DisplayAssetId,count,weightage,score";

    public static final String tbl_SyncLogDetails = "SyncLogDetails";
    public static final String tbl_SyncLogDetails_cols = "TransactionId,UserId,AppVersionNumber,OSName,OSVersion,DeviceName,SyncType,StartTime,EndTime,PhotosCount,SyncStatus,DownloadedDate,TotalCount";

    public static final HashMap<String, String> uploadColumn = new HashMap<>();

    static {

        uploadColumn.put(tbl_orderHeader, tbl_orderHeader_cols);
        uploadColumn.put(tbl_orderDetails, tbl_orderDetails_cols);
        uploadColumn.put(tbl_orderReturnDetails, tbl_orderReturnDetails_cols);
        uploadColumn.put(tbl_OrderFreeIssues, tbl_OrderFreeIssues_cols);
        uploadColumn.put(tbl_InvoiceFreeIssues, tbl_InvoiceFreeIssues_cols);
        uploadColumn.put(tbl_closingStockDetails, tbl_closingStockDetails_cols);

        uploadColumn.put(tbl_OutletTimestampupload,
                tbl_OutletTimestampupload_cols);
        uploadColumn.put(tbl_outlet_time_stamp_detail,
                tbl_outlet_time_stamp_detail_cols);
        uploadColumn.put(tbl_RetailerEntryDetails,
                tbl_RetailerEntryDetails_Cols);

        uploadColumn.put(tbl_OutletTimestampImagesupload,
                tbl_OutletTimestampImageupload_cols);
        uploadColumn.put(tbl_retailerMasterupload,
                tbl_retailerMasterupload_cols);


        uploadColumn.put(tbl_deviateReasontableupload,
                tbl_deviateReasontableupload_cols);
        uploadColumn.put(tbl_SbdMerchandisingHeaderupload,
                tbl_SbdMerchandisingHeaderupload_cols);
        uploadColumn.put(tbl_SbdMerchandisingDetailupload,
                tbl_SbdMerchandisingDetailupload_cols);
        uploadColumn.put(tbl_closingStockHeader, tbl_closingStockHeader_cols);
        uploadColumn.put(tbl_DayClose, tbl_DayClose_cols);
        uploadColumn.put(tbl_Photocaptureupload, tbl_Photocaptureupload_cols);
        uploadColumn.put(tbl_NonProductiveTable, tbl_NonProductiveTable_cols);
        uploadColumn.put(tbl_NonProductiveModuleTable, tbl_NonProductiveModuleTable_cols);
        uploadColumn.put(tbl_DailyTargetPlanned, tbl_DailyTargetPlanned_cols);
        uploadColumn.put(tbl_Payment, tbl_PaymentUpload_cols);
        uploadColumn.put(tbl_CollectionDocument, tbl_CollectionDocument_cols);
        uploadColumn.put(tbl_SalesReturnHeader, tbl_SalesReturnHeader_cols);
        uploadColumn.put(tbl_SalesReturnDetails, tbl_SalesReturnDetails_cols);
        uploadColumn.put(tbl_AnswerHeader, tbl_AnswerHeader_cols);
        uploadColumn.put(tbl_AnswerDetail, tbl_AnswerDetail_cols);
        uploadColumn.put(tbl_AnswerImageDetail, tbl_AnswerImageDetail_cols);
        uploadColumn.put(tbl_InvoiceHeaderUpload, tbl_InvoiceHeaderUpload_cols);
        uploadColumn.put(tbl_InvoiceDetailsUpload, tbl_InvoiceDetailsUpload_cols);
        uploadColumn.put(tbl_vanload, tbl_vanload_cols);
        uploadColumn.put(tbl_StockProposalMaster, tbl_StockProposalMaster_cols);
        uploadColumn.put(tbl_odameter, tbl_odameter_cols);
        uploadColumn.put(tbl_stock_apply, tbl_stock_apply_cols);
        uploadColumn.put(tbl_vanunload_details, tbl_vanunload_details_cols);
        uploadColumn.put(tbl_AssetHeader, tbl_AssetHeader_Cols);
        uploadColumn.put(tbl_AssetDetail, tbl_AssetDetail_Cols);
        uploadColumn.put(tbl_AssetImgInfo, tbl_AssetImgInfo_Cols);
        uploadColumn.put(tbl_SIH, tbl_SIH_cols);
        uploadColumn.put(tbl_Free_SIH, tbl_Free_SIH_cols);
        uploadColumn.put(tbl_NewOutletImage, tbl_NEWOUTLETIMAGE_cols);

        uploadColumn.put(tbl_splitted_order, tbl_splitted_order_cols);
        uploadColumn.put(tbl_PromotionHeader, tbl_PromotionHeader_cols);
        uploadColumn.put(tbl_PromotionDetail, tbl_PromotionDetail_cols);
        uploadColumn.put(tbl_SOS_Tracking_Header, tbl_SOS_Tracking_Header_cols);
        uploadColumn.put(tbl_SOS_Tracking_Detail, tbl_SOS_Tracking_Detail_cols);
        uploadColumn.put(tbl_SOS_Tracking_Parent_Detail, tbl_SOS_Tracking_Parent_Detail_cols);
        uploadColumn.put(tbl_SOS__Block_Tracking_Detail, tbl_SOS_Block_Tracking_Detail_cols);
        uploadColumn.put(tbl_SOD_Tracking_Header, tbl_SOD_Tracking_Header_cols);
        uploadColumn.put(tbl_SOD_Tracking_Detail, tbl_SOD_Tracking_Detail_cols);
        uploadColumn.put(tbl_SOD_Asset_Tracking_Header, tbl_SOD_Asset_Tracking_Header_cols);
        uploadColumn.put(tbl_SOD_Asset_Tracking_Detail, tbl_SOD_Asset_Tracking_Detail_cols);
        uploadColumn.put(tbl_SOD_Assets_Detail, tbl_SOD_Assets_Detail_cols);
        uploadColumn.put(tbl_SOSKU_Tracking_Header, tbl_SOSKU_Tracking_Header_cols);
        uploadColumn.put(tbl_SOSKU_Tracking_Detail, tbl_SOSKU_Tracking_Detail_cols);
        uploadColumn.put(tbl_MonthlyPlanHeaderMaster, tbl_MonthlyPlanHeaderMaster_cols);
        uploadColumn.put(tbl_MonthlyPlanDetail, tbl_MonthlyPlanDetail_cols);
        uploadColumn.put(tbl_credit_note, tbl_credit_note_cols);
        uploadColumn.put(tbl_NearExpiryHeader, tbl_NearExpiryHeader_cols);
        uploadColumn.put(tbl_NearExpiryDetail, tbl_NearExpiryDetail_cols);
        uploadColumn.put(tbl_PlanogramHeader, tbl_PlanogramHeader_cols);
        uploadColumn.put(tbl_PlanogramDetail, tbl_PlanogramDetail_cols);
        uploadColumn.put(tbl_AssetAddDelete, tbl_AssetAddDelete_cols);
        uploadColumn.put(tbl_AssetService, tbl_AssetService_cols);
        uploadColumn.put(tbl_outletjoincall, tbl_outletjoincall_cols);
        uploadColumn.put(tbl_PriceHeader, tbl_PriceHeader_cols);
        uploadColumn.put(tbl_PriceDetail, tbl_PriceDetail_cols);
        uploadColumn.put(tbl_CompetitorHeader, tbl_CompetitorHeader_cols);
        uploadColumn.put(tbl_CompetitorDetails, tbl_CompetitorDetails_cols);
        uploadColumn.put(tbl_attendancedetail, tbl_attendancedetail_cols);
        uploadColumn.put(tbl_leavedetail, tbl_leavedetail_cols);
        uploadColumn.put(tbl_EmptyReconciliationHeader, tbl_EmptyReconciliationHeader_cols);
        uploadColumn.put(tbl_EmptyReconciliationDetail, tbl_EmptyReconciliationDetail_cols);
        uploadColumn.put(tbl_SubDepotSettlement, tbl_SubDepotSettlement_cols);
        uploadColumn.put(tbl_invoicetaxDetails, tbl_invoicetaxDetails_cols);
        uploadColumn.put(tbl_ordertaxDetails, tbl_ordertaxDetails_cols);
        uploadColumn.put(tbl_invoice_return_detail, tbl_invoice_return_detail_cols);
        uploadColumn.put(tbl_scheme_details, tbl_SchemeDetail_cols);
        uploadColumn.put(tbl_SchemeFreeProductDetail, tbl_SchemeFreeProductDetail_cols);
        uploadColumn.put(tbl_TaskConfigurationMaster, tbl_TaskConfigurationMaster_cols);
        uploadColumn.put(tbl_TaskMaster, tbl_TaskMaster_cols);
        uploadColumn.put(tbl_TaskImageDetails, tbl_TaskImageDetails_cols);
        uploadColumn.put(tbl_InvoiceDiscountDetail, tbl_InvoiceDiscountDetail_cols);
        uploadColumn.put(tbl_OrderDiscountDetail, tbl_OrderDiscountDetail_cols);
        uploadColumn.put(tbl_RetailerEditHeader, tbl_RetailerEditHeader_cols);
        uploadColumn.put(tbl_RetailerEditDetail, tbl_RetailerEditDetail_cols);
        uploadColumn.put(tbl_RetailerContactEdit, tbl_RetailerContactEdit_cols);
        uploadColumn.put(tbl_RetailerVerification, tbl_RetailerVerification_cols);
        uploadColumn.put(tbl_delivery_header, tbl_delivery_header_cols);
        uploadColumn.put(tbl_delivery_detail, tbl_delivery_detail_cols);
        uploadColumn.put(tbl_RoadActivityHeader, tbl_RoadActivityHeader_cols);
        uploadColumn.put(tbl_RoadActivityDetail, tbl_RoadActivityDetail_cols);
        uploadColumn.put(tbl_activity_jointcall, tbl_activity_jointcall_cols);
        uploadColumn.put(tbl_DistInvoiceDetails, tbl_DistInvoiceDetails_cols);
        uploadColumn.put(tbl_DistTimeStampHeader, tbl_DistTimeStampHeader_cols);
        uploadColumn.put(tbl_DistTimeStampDetails, tbl_DistTimeStampDetails_cols);
        uploadColumn.put(tbl_distributor_closingstock_header, tbl_distributor_closingstock_header_cols);
        uploadColumn.put(tbl_distributor_closingstock_detail, tbl_distributor_closingstock_detail_cols);
        uploadColumn.put(tbl_distributor_order_header, tbl_distributor_order_header_cols);
        uploadColumn.put(tbl_distributor_order_detail, tbl_distributor_order_detail_cols);
        uploadColumn.put(tbl_TaskExecutionDetails, getTbl_TaskExecutionDetails_cols);
        uploadColumn.put(tbl_SOD_Tracking_Block_Detail, tbl_SOD_Tracking_Block_Detail_cols);
        uploadColumn.put(tbl_SOD_Tracking_Parent_Detail, tbl_SOD_Tracking_Parent_Detail_cols);
        uploadColumn.put(tbl_UserFeedBack, tbl_UserFeedBack_cols);
        uploadColumn.put(tbl_InvoiceSerialNO, tbl_InvoiceSerialNO_cols);
        uploadColumn.put(tbl_van_delivery_header, tbl_van_delivery_header_cols);
        uploadColumn.put(tbl_van_delivery_detail, tbl_van_delivery_detail_cols);
        uploadColumn.put(tbl_nearbyRetailer, tbl_nearbyRetailer_cols);
        uploadColumn.put(tbl_nearbyEditRequest, tbl_nearbyEditRequest_cols);
        uploadColumn.put(tbl_retailerEditAttribute, tbl_retailerEditAttributeupload_cols);
        uploadColumn.put(tbl_RetailerEditPriorityProducts, tbl_RetailerEditPriorityProducts_cols);
        uploadColumn.put(tbl_SalesReturn_tax_Details, tb_SalesReturnTaxDetails_cols);
        uploadColumn.put(tbl_SalesReturnReplacementDetails, tbl_SalesReturnReplacementDetails_cols);

        uploadColumn.put(tbl_AttendanceTimeDetails, tbl_AttendanceTimeDetails_cols);
        uploadColumn.put(tbl_PaymentDiscount_Detail, tbl_PaymentDiscountDetail_Cols);
        uploadColumn.put(tbl_leaveapprovaldetails, tbl_leaveapprovaldetails_cols);

        uploadColumn.put(tbl_expensedetails, tbl_expensedetails_cols);
        uploadColumn.put(tbl_expenseheader, tbl_expenseheader_cols);
        uploadColumn.put(tbl_expenseimagedetails, tbl_expenseimagedetails_cols);
        uploadColumn.put(tbl_retailercontractrenewal, tbl_retailercontractrenewal_cols);
        uploadColumn.put(tbl_newretailersurveyresultheader, tbl_newretailersurveyresultheader_cols);
        uploadColumn.put(tbl_newretailersurveyresultdetail, tbl_newretailersurveyresultdetail_cols);
        uploadColumn.put(tbl_retailerpriorityproducts, tbl_retailerpriorityproducts_cols);

        uploadColumn.put(tbl_location_tracking, tbl_location_tracking_cols);
        uploadColumn.put(tbl_retailerContact,
                tbl_retailerContactupload_cols);
        uploadColumn.put(tbl_retailerAddress,
                tbl_retailerAddressupload_cols);
        uploadColumn.put(tbl_orderHeaderRequest, tbl_OrderHeaderRequest_cols);
        uploadColumn.put(tbl_orderDetailRequest, tbl_OrderDetailRequest_cols);
        uploadColumn.put(tbl_retailerPotential, tbl_RetailerPotential_cols);

        uploadColumn.put(tbl_contactAvailability, tbl_ContactAvailability_Cols);
        uploadColumn.put(tbl_ContactAvailabilityEdit, tbl_ContactAvailabilityEdit_Cols);


        uploadColumn.put(tbl_loyaltyredemptionheader, tbl_tbl_loyaltyredemptionheader_cols);
        uploadColumn.put(tbl_loyaltyredemptiondetail, tbl_loyaltyredemptiondetail_cols);
        uploadColumn.put(tbl_LoyaltyPoints, tbl_LoyaltyPoints_cols);
        uploadColumn.put(tbl_retailerAttribute, tbl_retailerAttributeupload_cols);

        uploadColumn.put(tbl_UserEditDetail, tbl_UserEditDetail_cols);
        uploadColumn.put(tbl_ModuleActivityDetails, tbl_ModuleActivityDetails_cols);
        uploadColumn.put(tbl_jointcallacknowledgement, tbl_jointcallacknowledgement_upload_cols);
        uploadColumn.put(tbl_OrderDeliveryDetail, tbl_OrderDeliveryDetail_cols);
        uploadColumn.put(tbl_NonFieldActivity, tbl_NonFieldActivity_cols);
        uploadColumn.put(tbl_display_scheme_enrollment_header, tbl_display_scheme_enrollment_cols);
        uploadColumn.put(tbl_display_scheme_tracking_header, tbl_display_scheme_tracking_cols);
        uploadColumn.put(tbl_date_wise_plan, tbl_date_wise_plan_cols);
        uploadColumn.put(tbl_retailer_kpi_modified, tbl_retailer_kpi_modified_cols);
        uploadColumn.put(tbl_JointCallDetail, tbl_JointCallDetail_cols);
        uploadColumn.put(tbl_planogram_image_detail, tbl_planogram_image_detail_cols);

        uploadColumn.put(tbl_RetailerScoreHeader, tbl_RetailerScoreHeader_cols);
        uploadColumn.put(tbl_RetailerScoreDetail, tbl_RetailerScoreDetail_cols);

        uploadColumn.put(tbl_CollectionDueHeader, tbl_CollectionDueHeader_cols);
        uploadColumn.put(tbl_CollectionDueDetails, tbl_CollectionDueDetails_cols);
        uploadColumn.put(tbl_DenominationDetails, tbl_DenominationDetails_cols);
        uploadColumn.put(tbl_DenominationHeader, tbl_DenominationHeader_cols);

        uploadColumn.put(tbl_SerializedAssetHeader, tbl_SerializedAssetHeader_cols);
        uploadColumn.put(tbl_SerializedAssetDetail, tbl_SerializedAssetDetail_cols);
        uploadColumn.put(tbl_SerializedAssetImageDetail, tbl_SerializedAssetImageDetail_cols);
        uploadColumn.put(tbl_SerializedAssetTransfer, tbl_SerializedAssetTransfer_cols);
        uploadColumn.put(tbl_SerializedAssetTransferImages, tbl_SerializedAssetTransferImg__cols);
        uploadColumn.put(tbl_SerializedAssetServiceRequest, tbl_SerializedAssetServiceRequest_cols);
        uploadColumn.put(tbl_SerializedAssetUpdate,tbl_SerializedAssetUpdate_cols);
        uploadColumn.put(tbl_SerializedAssetApproval,tbl_SerializedAssetApproval_cols);

        uploadColumn.put(tbl_DigitalContent_Tracking_Header, tbl_DigitalContent_Tracking_Header_cols);
        uploadColumn.put(tbl_DigitalContent_Tracking_Detail, tbl_DigitalContent_Tracking_Detail_cols);
        uploadColumn.put(tbl_AnswerScoreDetails, tbl_AnswerScoreDetails_cols);

        uploadColumn.put(tbl_Planorama, tbl_Planorama_cols);
        uploadColumn.put(tbl_Planorama_image, tbl_Planorama_image_cols);
        uploadColumn.put(tbl_RetailerLocationDeviation, tbl_RetailerLocationDeviation_cols);
        uploadColumn.put(tbl_TripMaster, tbl_TripMaster_cols);
        uploadColumn.put(tbl_SyncLogDetails, tbl_SyncLogDetails_cols);
        uploadColumn.put(tbl_DisplayAssetHeader, tbl_DisplayAsseteader_cols);
        uploadColumn.put(tbl_DisplayAssetTDetails, tbl_DisplayAssetDetail_cols);

        uploadColumn.put(tbl_retailer_notes, tbl_retailer_notes_cols);
    }

    public static final HashMap<String, String> uploadColumnWithRetailer = new HashMap<>();

    static {

        uploadColumnWithRetailer.put(tbl_orderHeader, tbl_orderHeader_cols);
        uploadColumnWithRetailer.put(tbl_orderDetails, tbl_orderDetails_cols);
        uploadColumnWithRetailer.put(tbl_orderReturnDetails,
                tbl_orderReturnDetails_cols);
        uploadColumnWithRetailer.put(tbl_closingStockDetails,
                tbl_closingStockDetails_cols);

        uploadColumnWithRetailer.put(tbl_OutletTimestampupload,
                tbl_OutletTimestampupload_cols);
        uploadColumnWithRetailer.put(tbl_outlet_time_stamp_detail,
                tbl_outlet_time_stamp_detail_cols);
        uploadColumnWithRetailer.put(tbl_RetailerEntryDetails,
                tbl_RetailerEntryDetails_Cols);

        uploadColumnWithRetailer.put(tbl_retailerMasterupload,
                tbl_retailerMasterupload_cols);

        uploadColumnWithRetailer.put(tbl_deviateReasontableupload,
                tbl_deviateReasontableupload_cols);
        uploadColumnWithRetailer.put(tbl_SbdMerchandisingHeaderupload,
                tbl_SbdMerchandisingHeaderupload_cols);
        uploadColumnWithRetailer.put(tbl_SbdMerchandisingDetailupload,
                tbl_SbdMerchandisingDetailupload_cols);
        uploadColumnWithRetailer.put(tbl_closingStockHeader,
                tbl_closingStockHeader_cols);
        uploadColumnWithRetailer.put(tbl_Photocaptureupload,
                tbl_Photocaptureupload_cols);
        uploadColumnWithRetailer.put(tbl_NonProductiveTable,
                tbl_NonProductiveTable_cols);
        uploadColumnWithRetailer.put(tbl_NonProductiveModuleTable,
                tbl_NonProductiveModuleTable_cols);
        uploadColumnWithRetailer.put(tbl_Payment, tbl_PaymentUpload_cols);
        uploadColumnWithRetailer.put(tbl_CollectionDocument, tbl_CollectionDocument_cols);
        uploadColumnWithRetailer.put(tbl_SalesReturnHeader,
                tbl_SalesReturnHeader_cols);
        uploadColumnWithRetailer.put(tbl_SalesReturnDetails,
                tbl_SalesReturnDetails_cols);
        uploadColumnWithRetailer.put(tbl_AnswerHeader, tbl_AnswerHeader_cols);
        uploadColumnWithRetailer.put(tbl_AnswerDetail, tbl_AnswerDetail_cols);
        uploadColumnWithRetailer.put(tbl_AnswerImageDetail, tbl_AnswerImageDetail_cols);
        uploadColumnWithRetailer.put(tbl_InvoiceHeaderUpload,
                tbl_InvoiceHeaderUpload_cols);
        uploadColumnWithRetailer.put(tbl_InvoiceDetailsUpload,
                tbl_InvoiceDetailsUpload_cols);
        uploadColumnWithRetailer.put(tbl_AssetHeader, tbl_AssetHeader_Cols);
        uploadColumnWithRetailer.put(tbl_AssetDetail, tbl_AssetDetail_Cols);
        uploadColumnWithRetailer.put(tbl_NewOutletImage,
                tbl_NEWOUTLETIMAGE_cols);


        uploadColumnWithRetailer.put(tbl_splitted_order,
                tbl_splitted_order_cols);

        uploadColumnWithRetailer.put(tbl_PromotionHeader,
                tbl_PromotionHeader_cols);
        uploadColumnWithRetailer.put(tbl_PromotionDetail,
                tbl_PromotionDetail_cols);
        uploadColumnWithRetailer.put(tbl_SOS_Tracking_Header,
                tbl_SOS_Tracking_Header_cols);
        uploadColumnWithRetailer.put(tbl_SOS_Tracking_Detail,
                tbl_SOS_Tracking_Detail_cols);

        uploadColumnWithRetailer.put(tbl_SOD_Tracking_Header, tbl_SOD_Tracking_Header_cols);
        uploadColumnWithRetailer.put(tbl_SOD_Tracking_Detail, tbl_SOD_Tracking_Detail_cols);
        uploadColumnWithRetailer.put(tbl_SOD_Asset_Tracking_Header, tbl_SOD_Asset_Tracking_Header_cols);
        uploadColumnWithRetailer.put(tbl_SOD_Asset_Tracking_Detail, tbl_SOD_Asset_Tracking_Detail_cols);
        uploadColumnWithRetailer.put(tbl_SOD_Assets_Detail, tbl_SOD_Assets_Detail_cols);
        uploadColumnWithRetailer.put(tbl_SOSKU_Tracking_Header, tbl_SOSKU_Tracking_Header_cols);
        uploadColumnWithRetailer.put(tbl_SOSKU_Tracking_Detail, tbl_SOSKU_Tracking_Detail_cols);
        uploadColumnWithRetailer.put(tbl_MonthlyPlanHeaderMaster, tbl_MonthlyPlanHeaderMaster_cols);
        uploadColumnWithRetailer.put(tbl_MonthlyPlanDetail, tbl_MonthlyPlanDetail_cols);
        uploadColumnWithRetailer.put(tbl_credit_note, tbl_credit_note_cols);
        uploadColumnWithRetailer.put(tbl_NearExpiryHeader, tbl_NearExpiryHeader_cols);
        uploadColumnWithRetailer.put(tbl_NearExpiryDetail, tbl_NearExpiryDetail_cols);
        uploadColumnWithRetailer.put(tbl_PlanogramHeader, tbl_PlanogramHeader_cols);
        uploadColumnWithRetailer.put(tbl_PlanogramDetail, tbl_PlanogramDetail_cols);
        uploadColumnWithRetailer.put(tbl_PriceHeader, tbl_PriceHeader_cols);
        uploadColumnWithRetailer.put(tbl_PriceDetail, tbl_PriceDetail_cols);
        uploadColumnWithRetailer.put(tbl_CompetitorHeader, tbl_CompetitorHeader_cols);
        uploadColumnWithRetailer.put(tbl_CompetitorDetails, tbl_CompetitorDetails_cols);


        uploadColumnWithRetailer.put(tbl_EmptyReconciliationHeader,
                tbl_EmptyReconciliationHeader_cols);
        uploadColumnWithRetailer.put(tbl_EmptyReconciliationDetail,
                tbl_EmptyReconciliationDetail_cols);

        uploadColumnWithRetailer
                .put(tbl_scheme_details, tbl_SchemeDetail_cols);
        uploadColumnWithRetailer.put(tbl_SchemeFreeProductDetail,
                tbl_SchemeFreeProductDetail_cols);
        uploadColumnWithRetailer.put(tbl_invoicetaxDetails,
                tbl_invoicetaxDetails_cols);
        uploadColumnWithRetailer.put(tbl_ordertaxDetails,
                tbl_ordertaxDetails_cols);
        uploadColumnWithRetailer.put(tbl_InvoiceDiscountDetail,
                tbl_InvoiceDiscountDetail_cols);
        uploadColumnWithRetailer.put(tbl_OrderDiscountDetail,
                tbl_OrderDiscountDetail_cols);

        uploadColumnWithRetailer.put(tbl_AssetService, tbl_AssetService_cols);
        uploadColumnWithRetailer.put(tbl_AssetAddDelete, tbl_AssetAddDelete_cols);
        uploadColumnWithRetailer.put(tbl_invoice_return_detail, tbl_invoice_return_detail_cols);

        uploadColumnWithRetailer.put(tbl_RetailerVerification, tbl_RetailerVerification_cols);

        uploadColumnWithRetailer.put(tbl_TaskExecutionDetails, getTbl_TaskExecutionDetails_cols);
        uploadColumnWithRetailer.put(tbl_InvoiceSerialNO, tbl_InvoiceSerialNO_cols);
        uploadColumnWithRetailer.put(tbl_van_delivery_header, tbl_van_delivery_header_cols);
        uploadColumnWithRetailer.put(tbl_van_delivery_detail, tbl_van_delivery_detail_cols);
        uploadColumnWithRetailer.put(tbl_RetailerEditPriorityProducts, tbl_RetailerEditPriorityProducts_cols);
        uploadColumnWithRetailer.put(tbl_SalesReturn_tax_Details, tb_SalesReturnTaxDetails_cols);
        uploadColumnWithRetailer.put(tbl_SalesReturnReplacementDetails, tbl_SalesReturnReplacementDetails_cols);

        uploadColumnWithRetailer.put(tbl_retailerContact, tbl_retailerContactupload_cols);
        uploadColumnWithRetailer.put(tbl_retailerAddress, tbl_retailerAddressupload_cols);
        uploadColumnWithRetailer.put(tbl_retailerAttribute, tbl_retailerAttributeupload_cols);
        uploadColumnWithRetailer.put(tbl_retailerEditAttribute, tbl_retailerEditAttributeupload_cols);
        uploadColumnWithRetailer.put(tbl_orderHeaderRequest, tbl_OrderHeaderRequest_cols);
        uploadColumnWithRetailer.put(tbl_orderDetailRequest, tbl_OrderDetailRequest_cols);
        uploadColumnWithRetailer.put(tbl_retailerPotential, tbl_RetailerPotential_cols);
        uploadColumnWithRetailer.put(tbl_display_scheme_enrollment_header, tbl_display_scheme_enrollment_cols);
        uploadColumnWithRetailer.put(tbl_display_scheme_tracking_header, tbl_display_scheme_tracking_cols);


        uploadColumnWithRetailer.put(tbl_contactAvailability, tbl_ContactAvailability_Cols);
        uploadColumnWithRetailer.put(tbl_ContactAvailabilityEdit, tbl_ContactAvailabilityEdit_Cols);

        uploadColumnWithRetailer.put(tbl_CollectionDueHeader, tbl_CollectionDueHeader_cols);
        uploadColumnWithRetailer.put(tbl_CollectionDueDetails, tbl_CollectionDueDetails_cols);

        uploadColumnWithRetailer.put(tbl_SerializedAssetHeader, tbl_SerializedAssetHeader_cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetDetail, tbl_SerializedAssetDetail_cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetImageDetail, tbl_SerializedAssetImageDetail_cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetTransfer, tbl_SerializedAssetTransfer_cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetTransferImages, tbl_SerializedAssetTransferImg__cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetServiceRequest, tbl_SerializedAssetServiceRequest_cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetUpdate,tbl_SerializedAssetUpdate_cols);
        uploadColumnWithRetailer.put(tbl_SerializedAssetApproval,tbl_SerializedAssetApproval_cols);

        uploadColumnWithRetailer.put(tbl_DigitalContent_Tracking_Header, tbl_DigitalContent_Tracking_Header_cols);
        uploadColumnWithRetailer.put(tbl_DigitalContent_Tracking_Detail, tbl_DigitalContent_Tracking_Detail_cols);
        uploadColumnWithRetailer.put(tbl_AnswerScoreDetails, tbl_AnswerScoreDetails_cols);
        uploadColumnWithRetailer.put(tbl_Planorama, tbl_Planorama_cols);
        uploadColumnWithRetailer.put(tbl_Planorama_image, tbl_Planorama_image_cols);

        uploadColumnWithRetailer.put(tbl_OrderFreeIssues, tbl_OrderFreeIssues_cols);
        uploadColumnWithRetailer.put(tbl_InvoiceFreeIssues, tbl_InvoiceFreeIssues_cols);
        uploadColumnWithRetailer.put(tbl_RetailerLocationDeviation, tbl_RetailerLocationDeviation_cols);

        uploadColumnWithRetailer.put(tbl_DisplayAssetHeader, tbl_DisplayAsseteader_cols);
        uploadColumnWithRetailer.put(tbl_DisplayAssetTDetails, tbl_DisplayAssetDetail_cols);
    }

    public static final HashMap<String, String> uploadColumnWithOutRetailer = new HashMap<>();

    static {

        uploadColumnWithOutRetailer.put(tbl_DayClose, tbl_DayClose_cols);
        uploadColumnWithOutRetailer.put(tbl_StockProposalMaster,
                tbl_StockProposalMaster_cols);
        uploadColumnWithOutRetailer.put(tbl_odameter, tbl_odameter_cols);
        uploadColumnWithOutRetailer.put(tbl_stock_apply, tbl_stock_apply_cols);
        uploadColumnWithOutRetailer.put(tbl_vanunload_details,
                tbl_vanunload_details_cols);
        uploadColumnWithOutRetailer.put(tbl_SIH, tbl_SIH_cols);
        uploadColumnWithOutRetailer.put(tbl_Free_SIH, tbl_Free_SIH_cols);
        uploadColumnWithOutRetailer.put(tbl_DailyTargetPlanned,
                tbl_DailyTargetPlanned_cols);
        uploadColumnWithOutRetailer.put(tbl_TaskConfigurationMaster,
                tbl_TaskConfigurationMaster_cols);
        uploadColumnWithOutRetailer.put(tbl_TaskMaster, tbl_TaskMaster_cols);
        uploadColumnWithOutRetailer.put(tbl_TaskImageDetails, tbl_TaskImageDetails_cols);

        uploadColumnWithOutRetailer.put(tbl_SOS_Tracking_Parent_Detail,
                tbl_SOS_Tracking_Parent_Detail_cols);
        uploadColumnWithOutRetailer.put(tbl_SOS__Block_Tracking_Detail,
                tbl_SOS_Block_Tracking_Detail_cols);
        uploadColumnWithOutRetailer.put(tbl_vanload, tbl_vanload_cols);
        uploadColumnWithOutRetailer.put(tbl_outletjoincall, tbl_outletjoincall_cols);
        uploadColumnWithOutRetailer.put(tbl_SubDepotSettlement, tbl_SubDepotSettlement_cols);

        uploadColumnWithOutRetailer.put(tbl_RoadActivityHeader, tbl_RoadActivityHeader_cols);
        uploadColumnWithOutRetailer.put(tbl_RoadActivityDetail, tbl_RoadActivityDetail_cols);
        uploadColumnWithOutRetailer.put(tbl_DistInvoiceDetails, tbl_DistInvoiceDetails_cols);
        uploadColumnWithOutRetailer.put(tbl_DistTimeStampHeader, tbl_DistTimeStampHeader_cols);
        uploadColumnWithOutRetailer.put(tbl_DistTimeStampDetails, tbl_DistTimeStampDetails_cols);
        uploadColumnWithOutRetailer.put(tbl_distributor_closingstock_header, tbl_distributor_closingstock_header_cols);
        uploadColumnWithOutRetailer.put(tbl_distributor_closingstock_detail, tbl_distributor_closingstock_detail_cols);
        uploadColumnWithOutRetailer.put(tbl_distributor_order_header, tbl_distributor_order_header_cols);
        uploadColumnWithOutRetailer.put(tbl_distributor_order_detail, tbl_distributor_order_detail_cols);
        uploadColumnWithOutRetailer.put(tbl_UserFeedBack, tbl_UserFeedBack_cols);
        uploadColumnWithOutRetailer.put(tbl_SOD_Tracking_Block_Detail, tbl_SOD_Tracking_Block_Detail_cols);
        uploadColumnWithOutRetailer.put(tbl_SOD_Tracking_Parent_Detail, tbl_SOD_Tracking_Parent_Detail_cols);
        uploadColumnWithOutRetailer.put(tbl_RetailerEditHeader, tbl_RetailerEditHeader_cols);
        uploadColumnWithOutRetailer.put(tbl_RetailerEditDetail, tbl_RetailerEditDetail_cols);
        uploadColumnWithOutRetailer.put(tbl_RetailerContactEdit, tbl_RetailerContactEdit_cols);
        uploadColumnWithOutRetailer.put(tbl_OutletTimestampImagesupload,
                tbl_OutletTimestampImageupload_cols);
        uploadColumnWithOutRetailer.put(tbl_nearbyRetailer, tbl_nearbyRetailer_cols);
        uploadColumnWithOutRetailer.put(tbl_nearbyEditRequest, tbl_nearbyEditRequest_cols);
        uploadColumnWithOutRetailer.put(tbl_retailerEditAttribute, tbl_retailerEditAttributeupload_cols);
        uploadColumnWithOutRetailer.put(tbl_RetailerEditPriorityProducts, tbl_RetailerEditPriorityProducts_cols);

        uploadColumnWithOutRetailer.put(tbl_AttendanceTimeDetails, tbl_AttendanceTimeDetails_cols);
        uploadColumnWithOutRetailer.put(tbl_location_tracking, tbl_location_tracking_cols);

        uploadColumnWithOutRetailer.put(tbl_UserEditDetail, tbl_UserEditDetail_cols);
        uploadColumnWithOutRetailer.put(tbl_PaymentDiscount_Detail, tbl_PaymentDiscountDetail_Cols);
        uploadColumnWithOutRetailer.put(tbl_ModuleActivityDetails, tbl_ModuleActivityDetails_cols);
        uploadColumnWithOutRetailer.put(tbl_OrderDeliveryDetail, tbl_OrderDeliveryDetail_cols);
        uploadColumnWithOutRetailer.put(tbl_NonFieldActivity, tbl_NonFieldActivity_cols);
        uploadColumnWithOutRetailer.put(tbl_retailer_kpi_modified, tbl_retailer_kpi_modified_cols);
        uploadColumnWithOutRetailer.put(tbl_JointCallDetail, tbl_JointCallDetail_cols);
        uploadColumnWithOutRetailer.put(tbl_planogram_image_detail, tbl_planogram_image_detail_cols);
        uploadColumnWithOutRetailer.put(tbl_DenominationDetails, tbl_DenominationDetails_cols);
        uploadColumnWithOutRetailer.put(tbl_DenominationHeader, tbl_DenominationHeader_cols);
        uploadColumnWithOutRetailer.put(tbl_TripMaster, tbl_TripMaster_cols);
        uploadColumnWithOutRetailer.put(tbl_retailer_notes, tbl_retailer_notes_cols);

        //---------Missed Tables added in Common Retailerwise Upload ---------------------------//


        uploadColumnWithOutRetailer.put(tbl_AssetImgInfo, tbl_AssetImgInfo_Cols);

        uploadColumnWithOutRetailer.put(tbl_attendancedetail, tbl_attendancedetail_cols);
        uploadColumnWithOutRetailer.put(tbl_leavedetail, tbl_leavedetail_cols);
        uploadColumnWithOutRetailer.put(tbl_delivery_header, tbl_delivery_header_cols);
        uploadColumnWithOutRetailer.put(tbl_delivery_detail, tbl_delivery_detail_cols);
        uploadColumnWithOutRetailer.put(tbl_activity_jointcall, tbl_activity_jointcall_cols);
        uploadColumnWithOutRetailer.put(tbl_leaveapprovaldetails, tbl_leaveapprovaldetails_cols);

        uploadColumnWithOutRetailer.put(tbl_expensedetails, tbl_expensedetails_cols);
        uploadColumnWithOutRetailer.put(tbl_expenseheader, tbl_expenseheader_cols);
        uploadColumnWithOutRetailer.put(tbl_expenseimagedetails, tbl_expenseimagedetails_cols);
        uploadColumnWithOutRetailer.put(tbl_retailercontractrenewal, tbl_retailercontractrenewal_cols);
        uploadColumnWithOutRetailer.put(tbl_newretailersurveyresultheader, tbl_newretailersurveyresultheader_cols);
        uploadColumnWithOutRetailer.put(tbl_newretailersurveyresultdetail, tbl_newretailersurveyresultdetail_cols);
        uploadColumnWithOutRetailer.put(tbl_retailerpriorityproducts, tbl_retailerpriorityproducts_cols);


        uploadColumnWithOutRetailer.put(tbl_loyaltyredemptionheader, tbl_tbl_loyaltyredemptionheader_cols);
        uploadColumnWithOutRetailer.put(tbl_loyaltyredemptiondetail, tbl_loyaltyredemptiondetail_cols);
        uploadColumnWithOutRetailer.put(tbl_LoyaltyPoints, tbl_LoyaltyPoints_cols);
        uploadColumnWithOutRetailer.put(tbl_jointcallacknowledgement, tbl_jointcallacknowledgement_upload_cols);

        uploadColumnWithOutRetailer.put(tbl_RetailerScoreHeader, tbl_RetailerScoreHeader_cols);
        uploadColumnWithOutRetailer.put(tbl_RetailerScoreDetail, tbl_RetailerScoreDetail_cols);
        uploadColumnWithOutRetailer.put(tbl_SyncLogDetails, tbl_SyncLogDetails_cols);

        uploadColumnWithOutRetailer.put(tbl_date_wise_plan, tbl_date_wise_plan_cols);
    }

    public static final HashMap<String, String> uploadLocationTrackingColumn = new HashMap<>();

    static {
        uploadLocationTrackingColumn.put(tbl_location_tracking,
                tbl_location_tracking_cols);
    }


    public static final HashMap<String, String> uploadMovementTrackingHistoryColumn = new HashMap<>();

    static {
        uploadMovementTrackingHistoryColumn.put(tbl_movement_tracking_history,
                tbl_movement_tracking_history_cols);
    }

    public static final HashMap<String, String> uploadAttendanceColumn = new HashMap<>();

    static {
        uploadAttendanceColumn.put(tbl_attendancedetail,
                tbl_attendancedetail_cols);
        uploadAttendanceColumn.put(tbl_AttendanceTimeDetails,
                tbl_AttendanceTimeDetails_cols);
    }

    public static final HashMap<String, String> uploadSIHTable = new HashMap<>();

    static {
        uploadSIHTable.put(tbl_SIH,
                tbl_SIH_cols);
        uploadSIHTable.put(tbl_ExcessStockInHand,
                tbl_ExcessStockInHand_cols);
        uploadSIHTable.put(tbl_NonSalableSIHMaster,
                tbl_NonSalableSIHMaster_cols);
        uploadSIHTable.put(tbl_Free_SIH,
                tbl_Free_SIH_cols);

    }

    public static final HashMap<String, String> uploadLPTable = new HashMap<>();

    static {
        uploadLPTable.put(tbl_LoyaltyPoints,
                tbl_LoyaltyPoints_cols);

    }

    public static final HashMap<String, String> uploadStockApplyTable = new HashMap<>();

    static {

        uploadStockApplyTable.put(tbl_stock_apply,
                tbl_stock_apply_cols);
    }

    public static final HashMap<String, String> uploadReallocTable = new HashMap<>();

    static {

        uploadReallocTable.put(tbl_ReallocationHeader,
                tbl_ReallocationHeader_Cols);
        uploadReallocTable.put(tbl_Reallocationdetail, tbl_Reallocationdetail_Cols);
    }

    public static final HashMap<String, String> uploadInvoiceSequenceNo = new HashMap<>();

    static {
        uploadInvoiceSequenceNo.put(tbl_TransactionSequence,
                tbl_TransactionSequence_cols);
    }

    public static final String VISIT_DAYS_COLUMN_NAME = "VisitDays";

    public static final HashMap<String, String> uploadNewRetailerColumn = new HashMap<>();

    static {
        uploadNewRetailerColumn.put(tbl_retailerMasterupload,
                tbl_retailerMasterupload_cols);
        uploadNewRetailerColumn.put(tbl_retailerContact,
                tbl_retailerContactupload_cols);
        uploadNewRetailerColumn.put(tbl_retailerAddress,
                tbl_retailerAddressupload_cols);
        uploadNewRetailerColumn.put(tbl_nearbyRetailer, tbl_nearbyRetailer_cols);
        uploadNewRetailerColumn.put(tbl_retailerpriorityproducts, tbl_retailerpriorityproducts_cols);
        uploadNewRetailerColumn.put(tbl_retailerAttribute, tbl_retailerAttributeupload_cols);
        uploadNewRetailerColumn.put(tbl_orderHeaderRequest, tbl_OrderHeaderRequest_cols);
        uploadNewRetailerColumn.put(tbl_retailerPotential, tbl_RetailerPotential_cols);

        uploadNewRetailerColumn.put(tbl_contactAvailability, tbl_ContactAvailability_Cols);
        uploadNewRetailerColumn.put(tbl_ContactAvailabilityEdit, tbl_ContactAvailabilityEdit_Cols);

    }

    public static final HashMap<String, String> statusReportTables = new HashMap<>();

    static {
        statusReportTables.put(tbl_orderHeader, "Order");
        statusReportTables.put(tbl_OutletTimestamp, "Outlet Visit");
        statusReportTables.put(tbl_SalesReturnHeader, "Sales Return");
        statusReportTables.put(tbl_closingStockHeader, "Stock Check");
        statusReportTables.put(tbl_distributor_closingstock_header, "Primary Stock");
        statusReportTables.put(tbl_AnswerHeader, "Survey");
        statusReportTables.put(tbl_PriceHeader, "Price Check");
        statusReportTables.put(tbl_outletjoincall, "JointCall");
        statusReportTables.put(tbl_expenseheader, "Expense");
        statusReportTables.put(tbl_Photocaptureupload, "Photo Capture");
        statusReportTables.put(tbl_AttendanceTimeDetails, "Attendance");
        statusReportTables.put(tbl_leavedetail, "Leave");
        statusReportTables.put(tbl_delivery_header, "Order Fulfillment");
        statusReportTables.put(tbl_retailerMasterupload, "New Retailer");
        statusReportTables.put(tbl_AssetHeader, "Asset Tracking");

    }

    /**
     * Used to upload data for orderdelivery status.
     */
    public static final HashMap<String, String> uploadOrderDeliveryStatusTable = new HashMap<>();

    static {
        uploadOrderDeliveryStatusTable.put(tbl_order_delivery_status,
                tbl_order_delivery_status_cols);
    }


    public static final HashMap<String, String> uploadPickListStatusTable = new HashMap<>();

    static {

        uploadPickListStatusTable.put(tbl_picklist,
                tbl_picklist_cols);
        uploadPickListStatusTable.put(tbl_picklist_invoice,
                tbl_picklist_invoice_cols);
    }

    public static final HashMap<String, String> uploadTripTable = new HashMap<>();

    static {

        uploadTripTable.put(tbl_TripMaster,
                tbl_TripMaster_cols);
    }

}
