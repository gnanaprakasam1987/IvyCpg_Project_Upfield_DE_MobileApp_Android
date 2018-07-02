package com.ivy;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by velraj.p on 6/25/2018.
 */

public abstract class TestDataFactory {

    private static String VALID_ACTIVATION_RESPONSE = "{\"Table\":[{\"ActivationRefNo\":16047,\"ApplicationName\":\"P&G MALAYSIA\",\"SyncServiceURL\":\"https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx\",\"OrganizationID\":55,\"ApplicationID\":56,\"OrganizationName\":\"P&G MY TEST\"}]}";

    private static String INVALID_ACTIVATION_RESPONSE = "{\n" +
            "  \"Table\": [\n" +
            "    {\n" +
            "      \"ActivationRefNo\": 16047,\n" +
            "      \"ApplicationName\": \"P&G MALAYSIA\",\n" +
            "      \"SyncServiceURL\": \"\",\n" +
            "      \"OrganizationID\": 55,\n" +
            "      \"ApplicationID\": 56,\n" +
            "      \"OrganizationName\": \"P&G MY TEST\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    private static String INVALID_ACTIVATION_INVALID_RESPONSE = "{\n" +
            "  \"\": [\n" +
            "    {\n" +
            "      \"ActivationRefNo\": 16047,\n" +
            "      \"ApplicationName\": \"P&G MALAYSIA\",\n" +
            "      \"SyncServiceURL\": \"\",\n" +
            "      \"OrganizationID\": 55,\n" +
            "      \"ApplicationID\": 56,\n" +
            "      \"OrganizationName\": \"P&G MY TEST\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static String VALID_IMEI_RESPONSE = "{\n" +
            "    \"Table\": [\n" +
            "        {\n" +
            "            \"ActivationRefNo\": 16047,\n" +
            "            \"ApplicationName\": \"P&G MALAYSIA\",\n" +
            "            \"SyncServiceURL\": \"https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx\",\n" +
            "            \"OrganizationID\": 55,\n" +
            "            \"ApplicationID\": 56,\n" +
            "            \"OrganizationName\": \"P&G MY TEST\"\n" +
            "        },\n" +
            "  {\n" +
            "            \"ActivationRefNo\": 16048,\n" +
            "            \"ApplicationName\": \"P&G MALAYSIA\",\n" +
            "            \"SyncServiceURL\": \"https://uat.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx\",\n" +
            "            \"OrganizationID\": 55,\n" +
            "            \"ApplicationID\": 56,\n" +
            "            \"OrganizationName\": \"P&G MY UAT\"\n" +
            "        }\n" +
            "    ]\n" +
            "}\n";


    private static String singleImEiResponse = "{\n" +
            "  \"Table\": [\n" +
            "    {\n" +
            "      \"ActivationRefNo\": 16047,\n" +
            "      \"ApplicationName\": \"P&G MALAYSIA\",\n" +
            "      \"SyncServiceURL\": \"https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx\",\n" +
            "      \"OrganizationID\": 55,\n" +
            "      \"ApplicationID\": 56,\n" +
            "      \"OrganizationName\": \"P&G MY TEST\"\n" +
            "    }\n" +
            "    \n" +
            "  ]\n" +
            "}";


    private static String EMPTY_ARRAY ="{\n" +
            "  \"Table\": [\n" +
            "    \n" +
            "    \n" +
            "  ]\n" +
            "}";



    public static JSONObject getValidActivationObject() {

        try {
            return new JSONObject(VALID_ACTIVATION_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }


    public static JSONObject getValidActivationFailureObject() {

        try {
            return new JSONObject(INVALID_ACTIVATION_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getValidImeiResponse() {

        try {
            return new JSONObject(VALID_IMEI_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getValidateSingleImEiResponse() {

        try {
            return new JSONObject(singleImEiResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getInValidResponse() {

        try {
            return new JSONObject(INVALID_ACTIVATION_INVALID_RESPONSE);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getValidActivationWithEmptyArray() {
        try {
            return new JSONObject(EMPTY_ARRAY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }
}
