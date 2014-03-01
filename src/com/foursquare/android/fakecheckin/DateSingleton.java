package com.foursquare.android.fakecheckin;

import java.util.Calendar;

public class DateSingleton {

	private static String str = null;

	public static String getDate() {
		if (str == null) {
		Calendar cal = Calendar.getInstance();
		
			String y = "" + cal.get(Calendar.YEAR);
			String m = "" + cal.get(Calendar.MONTH);
			String d = "" + cal.get(Calendar.DAY_OF_MONTH);
			if (m.length() == 1)
				m = "0" + m;
			if (d.length() == 1)
				d = "0" + m;
			str=y + m + d;
		}
		return str;
		

	}
}
