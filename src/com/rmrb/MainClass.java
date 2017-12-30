package com.rmrb;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

public class MainClass {
	public static void main(String[] args) throws ParseException {
		DownLoadControl control = new DownLoadControl();
		Scanner scanner = new Scanner(System.in);
		System.out.println("请选择操作:");
		System.out.println("1.下载当天报纸");
		System.out.println("2.下载指定日期的报纸");
		System.out.println("3.退出");
		int c = scanner.nextInt();
		switch (c) {
		case 1:
			control.downLoad();
			break;
		case 2:
			System.out.println("请输入日期，格式是 yyyy-mm-dd");
			SimpleDateFormat formatFirst = new SimpleDateFormat("yyyy-MM-dd");
			String dateStr = scanner.next();
			Date date = formatFirst.parse(dateStr);
			// Date直接获取年月日的getYear等方法已经不建议使用了，所以这里得转换成Calendar
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			int year = cal.get(Calendar.YEAR);
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			// Calendar获取的月份比实际月份小1，但是这里并不需要增1，否则在那边初始化Calendar的时候又要减1，这就多余了
			control.setDate(year, month, day);
			control.downLoad();
			break;
		case 3:
			break;
		}
		scanner.close();
	}
}
