package com.ivy.location;

import com.google.android.gms.location.LocationRequest;

public class ConfigData {
	private static int powerAccuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;

	public static int getPowerAccuracy() {
		return powerAccuracy;
	}

	public static void setPowerAccuracy(int powerAccuracy) {
		if (powerAccuracy == LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
				|| powerAccuracy == LocationRequest.PRIORITY_HIGH_ACCURACY
				|| powerAccuracy == LocationRequest.PRIORITY_LOW_POWER
				|| powerAccuracy == LocationRequest.PRIORITY_NO_POWER)
			ConfigData.powerAccuracy = powerAccuracy;
		else
			ConfigData.powerAccuracy = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;
	}
	
}
