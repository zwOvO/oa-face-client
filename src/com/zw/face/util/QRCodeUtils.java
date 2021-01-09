package com.zw.face.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

public class QRCodeUtils {

	/**
	 * 生成图像
	 *
	 * @throws WriterException
	 * @throws IOException
	 */
	/*
	 * 定义二维码的宽高
	 */
	private static int WIDTH=300;
	private static int HEIGHT=300;
	private static String FORMAT="png";//二维码格式
	//生成二维码
	public static void createZxingqrCode(String content){
		//定义二维码参数
		Map hints=new HashMap();
		hints.put(EncodeHintType.CHARACTER_SET, "utf-8");//设置编码
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);//设置容错等级
		hints.put(EncodeHintType.MARGIN, 2);//设置边距默认是5
		String imageName = "Temp\\QRCode_" + content + ".png";
		try {
			BitMatrix bitMatrix=new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, WIDTH, HEIGHT, hints);
			Path path = new File(imageName).toPath();
			MatrixToImageWriter.writeToPath(bitMatrix, FORMAT, path);//写到指定路径下
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}