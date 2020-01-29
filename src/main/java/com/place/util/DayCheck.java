package com.place.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DayCheck {
	static public String dayCheck() {
		SimpleDateFormat format = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss");
		Date time = new Date();
		String toDay = format.format(time);
		return toDay;
	}
}
