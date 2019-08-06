package qr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QR {

	public static void main(String[] args) {
		if (args.length<1) {
			System.out.println("Need argument");
			return;
		}
		System.out.println(args[0]);
		//String qrCodeData	= "Hello World!";
		//String filePath	= "1.png";
		String charset	= "UTF-8"; // or "ISO-8859-1"
		Map hintMap	= new HashMap();
		hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

//		try {
//			createQRCode(qrCodeData, filePath, charset, hintMap, 200, 200);
//		} catch (WriterException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("QR Code image created successfully!");
		
		File f	= new File(args[0]);
		if (!f.exists()) {
			System.out.println("File not exists");
			return;
		}

		//String dir = f.getAbsolutePath();
		String dir = f.getParent();
		//System.out.println(dir);
//		if (dir.indexOf('/')>0) {
//			if (dir.charAt(dir.length())!='/') dir	= dir+'/';
//		}
//		char slesh = (char) 92;
//		if (dir.indexOf(slesh)>0) {
//			if (dir.charAt(dir.length())!=slesh) dir	= dir+slesh;
//		}
		String fileName	= f.getName();
		//System.out.println(fileName);
		
	    PDDocument document	= null;
		try {
			document	= PDDocument.load(f);
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (document==null){
			System.out.println("Error load file");
			return;
		}
	    PDFRenderer pdfRenderer	= new PDFRenderer(document);
	    StringBuffer beginFilePath = new StringBuffer(dir);
	    
	    char ls = beginFilePath.charAt(beginFilePath.length()-1);
//	    System.out.println(beginFilePath);
//	    System.out.println(ls);
//	    System.out.println(beginFilePath.indexOf("/"));
	    if (beginFilePath.indexOf("/")>=0) {
	    	if (ls!='/') beginFilePath.append('/');
	    }else {
	    	char slesh = (char) 92;
	    	if (ls!=slesh) beginFilePath.append(slesh);
	    }
	    
	    //System.out.println(fileName);
	    
	    beginFilePath.append(fileName);
	    beginFilePath.append('-');
	    
	    String ext	= ".png";
	    String txt	= ".txt";
	    
	    StringBuffer filePath = new StringBuffer();
	    String filPathStr	= null;

	    StringBuffer fileTxtPath = new StringBuffer();
	    String filTxtPathStr	= null;
	    
	    for (int page = 0; page < document.getNumberOfPages(); ++page) {
	        BufferedImage bim	= null;
			try {
				bim = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB);
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (bim==null){
				System.out.println("Error read pdf as image");
				continue;
			}
	        try {
	        	//filePath	= dir+String.format("%s-%d.%s", fileName, page + 1, "png");
	        	
	        	filePath.setLength(0);
	        	filePath.append(beginFilePath);
	        	filePath.append(page);
	        	filePath.append(ext);
	        	filPathStr	= filePath.toString();
	        	
	        	fileTxtPath.setLength(0);
	        	fileTxtPath.append(beginFilePath);
	        	fileTxtPath.append(page);
	        	fileTxtPath.append(txt);
	        	filTxtPathStr	= fileTxtPath.toString(); 
	        	
	        	//System.out.println(filePath.toString());
				ImageIOUtil.writeImage(bim, filPathStr, 300);				
				try {
					FileWriter writer = new FileWriter(filTxtPathStr, false);
					try {
						writer.write(readQRCode(filPathStr, charset, hintMap));
					} catch (NotFoundException e) {
						e.printStackTrace();
					}
					writer.flush();
					writer.close();
					//System.out.println(readQRCode(filPathStr, charset, hintMap));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	    try {
	    	if (document!=null) document.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

//	public static void createQRCode(String qrCodeData, String filePath,
//			String charset, Map hintMap, int qrCodeheight, int qrCodewidth)
//			throws WriterException, IOException {
//		BitMatrix matrix = new MultiFormatWriter().encode(
//				new String(qrCodeData.getBytes(charset), charset),
//				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight, hintMap);
//		MatrixToImageWriter.writeToFile(matrix, filePath.substring(filePath
//				.lastIndexOf('.') + 1), new File(filePath));
//	}

	public static String readQRCode(String filePath, String charset, Map hintMap)
			throws FileNotFoundException, IOException, NotFoundException {
		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
				new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));
		Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap,hintMap);
		return qrCodeResult.getText();
	}	
		
}

