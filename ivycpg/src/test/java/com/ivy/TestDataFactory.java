package com.ivy;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by velraj.p on 6/25/2018.
 */

public abstract class TestDataFactory {

    private static String ValidActivationResponse = "{\n" +
            "  \"Table\": [\n" +
            "    {\n" +
            "      \"ActivationRefNo\": 16047,\n" +
            "      \"ApplicationName\": \"P&G MALAYSIA\",\n" +
            "      \"SyncServiceURL\": \"https://test2.ivymobileapps.com/Idist_my_png_msync/MobileWebService.asmx\",\n" +
            "      \"OrganizationID\": 55,\n" +
            "      \"ApplicationID\": 56,\n" +
            "      \"OrganizationName\": \"P&G MY TEST\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";

    private static String ValidActivationFailureResponse = "{\n" +
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


    private static String validateImEiResponse = "{\n" +
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


    public static JSONObject getValidActivationObject() {

        try {
            return new JSONObject(ValidActivationResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }


    public static JSONObject getValidActivationFailureObject() {

        try {
            return new JSONObject(ValidActivationFailureResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public static JSONObject getValidateImEiResponse() {

        try {
            return new JSONObject(validateImEiResponse);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }
}
