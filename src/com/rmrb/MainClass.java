package com.rmrb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class MainClass {
	public static void main(String[] args) throws ParseException {
		DownLoadControl control = new DownLoadControl();
		control.downLoad();
	}
}
