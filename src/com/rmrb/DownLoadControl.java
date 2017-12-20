package com.rmrb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfCopy;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfSmartCopy;
import com.itextpdf.text.pdf.PdfWriter;

public class DownLoadControl {
	private int year = -1;
	private int month = -1;
	private int dayOfMonth = -1;

	// 最终结果pdf名称
	private String destFileName = "";
	private ArrayList<URL> urls = new ArrayList<>();
	private ArrayList<String> cacheFiles = new ArrayList<>();
	private SimpleDateFormat formatFirst = new SimpleDateFormat("yyyy-MM/dd");
	private SimpleDateFormat formatSecond = new SimpleDateFormat("yyyyMMdd");

	public void setDate(int year, int month, int dayOfMonth) {
		this.year = year;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
	}

	private void getUrls() {
		urls.clear();
		Calendar cal;
		// 使用当天日期
		if (year == -1 || month == -1 || dayOfMonth == -1) {
			cal = Calendar.getInstance();
		} else {
			cal = new GregorianCalendar(year, month, dayOfMonth);
		}
		try {
			int page = 1;
			while (true) {
				URL url = new URL("http://paper.people.com.cn/rmrb/page/" + formatFirst.format(cal.getTime()) + "/"
						+ (page < 10 ? ("0" + page) : page) + "/rmrb" + formatSecond.format(cal.getTime())
						+ (page < 10 ? ("0" + page) : page) + ".pdf");
				// 检查地址是否有效
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				int state = connection.getResponseCode();
				if (state == 404) {
					System.out.println("url size =" + urls.size());
					break;
				}
				urls.add(url);
				System.out.println(url.toString());
				page++;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 初始化目标文件名
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		destFileName = "" + format.format(cal.getTime()) + ".pdf";
	}

	// 下载并组合
	public void downLoad() {
		getUrls();
		// 下载
		cacheFiles.clear();
		InputStream is = null;
		OutputStream os = null;
		for (int i = 0; i < urls.size(); i++) {
			try {
				URLConnection connection = urls.get(i).openConnection();
				is = connection.getInputStream();
				os = new FileOutputStream("" + i + ".pdf");
				cacheFiles.add("" + i + ".pdf");
				byte[] buffer = new byte[1024];
				int flag = 0;
				while (-1 != (flag = is.read(buffer, 0, buffer.length))) {
					os.write(buffer, 0, flag);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		// 组合成一个PDF
		try {
			mergeFiles(cacheFiles, destFileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// 删除缓存文件
		File cacheFile;
		for (String fileName : cacheFiles) {
			cacheFile = new File(fileName);
			if (cacheFile.exists()) {
				cacheFile.delete();
			}
		}
	}

	public void mergeFiles(ArrayList<String> files, String result) throws IOException, DocumentException {
		Document document = new Document();
		PdfCopy copy;
		copy = new PdfSmartCopy(document, new FileOutputStream(result));
		document.open();
		PdfReader[] reader = new PdfReader[files.size()];
		for (int i = 0; i < files.size(); i++) {
			reader[i] = new PdfReader(files.get(i));
			copy.addDocument(reader[i]);
			copy.freeReader(reader[i]);
			reader[i].close();
		}
		document.close();
	}
}
