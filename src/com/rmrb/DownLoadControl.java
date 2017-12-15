package com.rmrb;

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
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfReader;
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
		destFileName = "d:/" + format.format(cal.getTime()) + ".pdf";
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
				os = new FileOutputStream("d:/" + i + ".pdf");
				cacheFiles.add("d:/" + i + ".pdf");
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
			List<InputStream> pdfs = new ArrayList<InputStream>();
			for (String filePath : cacheFiles) {
				pdfs.add(new FileInputStream(filePath));
			}
			System.out.println("destFileName =" + destFileName);
			OutputStream output = new FileOutputStream(destFileName);
			concatPDFs(pdfs, output, true);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void concatPDFs(List<InputStream> streamOfPDFFiles, OutputStream outputStream, boolean paginate) {

		Document document = new Document();
		try {
			List<InputStream> pdfs = streamOfPDFFiles;
			List<PdfReader> readers = new ArrayList<PdfReader>();
			int totalPages = 0;
			Iterator<InputStream> iteratorPDFs = pdfs.iterator();

			// Create Readers for the pdfs.
			while (iteratorPDFs.hasNext()) {
				InputStream pdf = iteratorPDFs.next();
				PdfReader pdfReader = new PdfReader(pdf);
				readers.add(pdfReader);
				totalPages += pdfReader.getNumberOfPages();
			}
			// Create a writer for the outputstream
			PdfWriter writer = PdfWriter.getInstance(document, outputStream);

			document.open();
			BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
			PdfContentByte cb = writer.getDirectContent(); // Holds the PDF
			// data

			PdfImportedPage page;
			int currentPageNumber = 0;
			int pageOfCurrentReaderPDF = 0;
			Iterator<PdfReader> iteratorPDFReader = readers.iterator();

			// Loop through the PDF files and add to the output.
			while (iteratorPDFReader.hasNext()) {
				PdfReader pdfReader = iteratorPDFReader.next();

				// Create a new page in the target for each source page.
				while (pageOfCurrentReaderPDF < pdfReader.getNumberOfPages()) {
					document.newPage();
					pageOfCurrentReaderPDF++;
					currentPageNumber++;
					page = writer.getImportedPage(pdfReader, pageOfCurrentReaderPDF);
					cb.addTemplate(page, 0, 0);

					// Code for pagination.
					if (paginate) {
						cb.beginText();
						cb.setFontAndSize(bf, 9);
						cb.showTextAligned(PdfContentByte.ALIGN_CENTER, "" + currentPageNumber + " of " + totalPages,
								520, 5, 0);
						cb.endText();
					}
				}
				pageOfCurrentReaderPDF = 0;
			}
			outputStream.flush();
			document.close();
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (document.isOpen())
				document.close();
			try {
				if (outputStream != null)
					outputStream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
