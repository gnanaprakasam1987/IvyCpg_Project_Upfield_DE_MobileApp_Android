package com.ivy.lib.rest;

import android.util.Log;

import org.apache.http.conn.ConnectTimeoutException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.HeaderProperty;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

public class MyKsoapConnection {
	private String mMETHOD_NAME;
	private String mSOAP_ACTION;
	private String mNAMESPACE;
	private String mURL;
	private StringBuilder mresultData;
	private static int TIMEOUT = 90000;
	private SoapObject request;
	private String sucessCodeValue;

	private static final String JSON_ERROR_TAG = "ErrorInformation";
	private static final String JSON_ERROR_CODE_TAG = "ErrorCode";

	private static final String JSON_SUCCESS_TAG = "ServerResponse";
	private static final String JSON_SUCCESS_CODE_TAG = "DataUpload";
	private static final String JSON_SUCCESS_STATUS_CODE_TAG = "DownloadFlagUpdate";
	private static final String JSON_SUCCESS_LICENSEURL_CODE_TAG = "LicenseURL";
	private static final String JSON_SUCCESS_RESPONSE = "Success";

	private static final int IVY_CODE_CUSTOM = 2002;
	private static final int IVY_CODE_EXCEPTION = 2001;

	private static final String IVY_STATUS_MSG_INVALID_USER = "Invalid User";
	private static final String IVY_STATUS_MSG_SERVER_ERROR = "Server Error";
	private static final String IVY_STATUS_MSG_URL_EMPTY = "No URL found.";
	private static final String IVY_STATUS_MSG_NO_RESPONSE = "Response is NULL";

	private static final String IVY_STATUS_MSG_DATA_ERROR = "Invalid response data";
	private static final String IVY_STATUS_MSG_DATA_PARSING_ERROR = "Error on processing response";
	private static final String IVY_STATUS_MSG_CONNECTION_TIMEOUT = "Connection Timeout";
	private static final String IVY_STATUS_MSG_SOCKET_TIMEOU = "Socket Timeout";
	private static final String IVY_STATUS_MSG_EXCEPTION = "Connection Exception";
	private static final String IVY_STATUS_MSG_EXCEPTION_SOAP_FAULT = "Connection Exception. SOAP Fault.";

	public void create(String method, String url, String soapAction,
			String namespace) {
		this.mMETHOD_NAME = method;
		this.mURL = url;
		this.mSOAP_ACTION = soapAction;
		this.mNAMESPACE = namespace;
		request = new SoapObject(mNAMESPACE, mMETHOD_NAME);
		mresultData = new StringBuilder();

	}

	public void addParam(String var, String val) {
		request.addProperty(var, val);
	}

	public StringBuilder getResult() {
		return mresultData;
	}

	public void connectServer(ResponseListener listener) {
		try {
			if (soapCall(listener)) {
				if (isValidIVYResponse(listener, mresultData.toString())) {
					listener.onSucess(new JSONObject(mresultData.toString()));
				}
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			listener.onFailure(IVY_CODE_EXCEPTION, IVY_STATUS_MSG_EXCEPTION);
		}
	}

	private boolean soapCall(ResponseListener listener) {
		try {
			SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
					SoapEnvelope.VER11);
			envelope.dotNet = true;
			envelope.setOutputSoapObject(request);
			HttpTransportSE httpTransportse = new HttpTransportSE(mURL, TIMEOUT);
			httpTransportse.debug = true;
			Log.d("Sync Time Start", System.currentTimeMillis() + "");
			ArrayList<HeaderProperty> headerPropertyArrayList = new ArrayList<HeaderProperty>();
			headerPropertyArrayList.add(new HeaderProperty("Connection", "close"));
			httpTransportse.call(mSOAP_ACTION, envelope, headerPropertyArrayList);
			Log.d("Sync Time", System.currentTimeMillis() + "");

			if (envelope.bodyIn instanceof SoapFault) {
				String str = ((SoapFault) envelope.bodyIn).faultstring;
				Log.i("Sync Responce Fault", str);
				listener.onFailure(IVY_CODE_EXCEPTION,
						IVY_STATUS_MSG_EXCEPTION_SOAP_FAULT);
				return false;
			} else {
				SoapObject resultSOAPObject = (SoapObject) envelope.bodyIn;
				mresultData.append(resultSOAPObject.getProperty(0).toString());
			}
		} catch (ConnectTimeoutException connTimeOut) {
			listener.onFailure(IVY_CODE_EXCEPTION,
					IVY_STATUS_MSG_CONNECTION_TIMEOUT);
			connTimeOut.printStackTrace();
			return false;
		} catch (SocketTimeoutException socketTimeOut) {
			listener.onFailure(IVY_CODE_EXCEPTION, IVY_STATUS_MSG_SOCKET_TIMEOU);
			socketTimeOut.printStackTrace();
			return false;
		} catch (NoRouteToHostException noRouteException) {
			listener.onFailure(IVY_CODE_EXCEPTION, IVY_STATUS_MSG_EXCEPTION);
			noRouteException.printStackTrace();
			return false;
		} catch (Exception e) {
			// TODO: handle exception
			listener.onFailure(IVY_CODE_EXCEPTION, IVY_STATUS_MSG_EXCEPTION);
			e.printStackTrace();
			return false;

		}
		return true;
	}

	private boolean isValidIVYResponse(ResponseListener listener,
			String jsonResponce) {
		try {
			Log.d("Content as it is", jsonResponce);
			JSONObject jsonObjRoot = new JSONObject(jsonResponce);

			Iterator<String> iterator = jsonObjRoot.keys();
			while (iterator.hasNext()) {
				String name = (String) iterator.next();
				if (name.equals(JSON_ERROR_TAG)) {

					JSONArray jsonArray = jsonObjRoot
							.getJSONArray(JSON_ERROR_TAG);

					// Check the lenght of the array, if the error json has more
					// then one element
					// it will be consider as json format error
					if (jsonArray.length() != 1) {
						listener.onFailure(IVY_CODE_EXCEPTION,
								IVY_STATUS_MSG_DATA_PARSING_ERROR);
						return false;
					}

					JSONObject jsonObj = jsonArray.getJSONObject(0);
					String errorCodeValue = jsonObj
							.getString(JSON_ERROR_CODE_TAG);
					listener.onFailure(IVY_CODE_CUSTOM, errorCodeValue);
					return false;
				} else if (name.equals(JSON_SUCCESS_TAG)) {
					JSONArray jsonArray = jsonObjRoot
							.getJSONArray(JSON_SUCCESS_TAG);

					// Check the lenght of the array, if the error json has more
					// then one element
					// it will be consider as json format error
					if (jsonArray.length() != 1) {
						listener.onFailure(IVY_CODE_EXCEPTION,
								IVY_STATUS_MSG_DATA_PARSING_ERROR);
						return false;
					}
					
					JSONObject jsonObj = jsonArray.getJSONObject(0);

				    Iterator columNamesIterator = jsonObj.keys();
					while (columNamesIterator.hasNext()) {
						String keyName = (String) columNamesIterator.next();
						// keyName for check Download status and data upload
						if (keyName.equals(JSON_SUCCESS_STATUS_CODE_TAG)) {
							sucessCodeValue = jsonObj
									.getString(JSON_SUCCESS_STATUS_CODE_TAG);
						} else if (keyName.equals(JSON_SUCCESS_CODE_TAG)) {
							sucessCodeValue = jsonObj
									.getString(JSON_SUCCESS_CODE_TAG);
						}else if(keyName.equals(JSON_SUCCESS_LICENSEURL_CODE_TAG)){
							sucessCodeValue = jsonObj
									.getString(JSON_SUCCESS_LICENSEURL_CODE_TAG);
						} else if(keyName.equals(JSON_SUCCESS_RESPONSE)){
							sucessCodeValue = jsonObj
									.getString(JSON_SUCCESS_RESPONSE);
						}
					}


					if (sucessCodeValue.equals("1") || sucessCodeValue.contains("http"))
						return true;
					else
						listener.onFailure(IVY_CODE_CUSTOM, sucessCodeValue);

					return false;
				} else {
					return true;
				}
			}

		} catch (JSONException jsonException) {
			listener.onFailure(IVY_CODE_EXCEPTION, IVY_STATUS_MSG_DATA_ERROR);
			return false;
		} catch (Exception e) {
			listener.onFailure(IVY_CODE_EXCEPTION,
					IVY_STATUS_MSG_DATA_PARSING_ERROR);
			return false;
		}

		return true;
	}

	/**
	 * 
	 */
	public interface ResponseListener {
		public abstract void onFailure(int status, String message);

		public abstract void onSucess(JSONObject jsonObj);
	}

}
