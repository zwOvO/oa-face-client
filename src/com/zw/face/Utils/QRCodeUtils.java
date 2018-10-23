package com.zw.face.Utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.zw.face.Config.Config;

public class QRCodeUtils {

	/**
	 * 生成图像
	 *
	 * @throws WriterException
	 * @throws IOException
	 */
	public static String createQrcode(String content) {
		String qrcodeFilePath = "";
		try {
			int width = 300;
			int height = 300;
			// 二维码的图片格式
			String qrcodeFormat = "png";
			/**
			 * 设置二维码的参数
			 */
			HashMap<EncodeHintType, String> hints = new HashMap<EncodeHintType, String>();
			// 内容所使用编码
			hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
			BitMatrix bitMatrix = new MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, width, height, hints);
			URL url = new URL(Config.UrlQRCode +  content);
			System.out.println(url.toString());
			DataInputStream dataInputStream = new DataInputStream(url.openStream());
			String imageName = "Temp\\QRCode_" + content + ".png";

			FileOutputStream fileOutputStream = new FileOutputStream(new File(imageName));
			ByteArrayOutputStream output = new ByteArrayOutputStream();

			byte[] buffer = new byte[1024];
			int length;

			while ((length = dataInputStream.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}
			fileOutputStream.write(output.toByteArray());
			dataInputStream.close();
			fileOutputStream.close();
			// ImageIO.write(image, qrcodeFormat, QrcodeFile);
			MatrixToImageWriter.writeToStream(bitMatrix, qrcodeFormat, output);// (bitMatrix, qrcodeFormat, QrcodeFile);
			qrcodeFilePath = "OK";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return qrcodeFilePath;
	}

	public static byte[] readInputStream(InputStream inputStream) throws IOException {
		byte[] buffer = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((len = inputStream.read(buffer)) != -1) {
			bos.write(buffer, 0, len);
		}

		bos.close();
		return bos.toByteArray();
	}

	/**
	 * 解析图像
	 */
	public static void deCode(String filePath) {
		BufferedImage image;
		try {
			image = ImageIO.read(new File(filePath));
			LuminanceSource source = new BufferedImageLuminanceSource(image);
			Binarizer binarizer = new HybridBinarizer(source);
			BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
			Map<DecodeHintType, Object> hints = new HashMap<DecodeHintType, Object>();
			hints.put(DecodeHintType.CHARACTER_SET, "UTF-8");
			Result result = new MultiFormatReader().decode(binaryBitmap, hints);// 对图像进行解码
			System.out.println("图片中内容：  ");
			System.out.println("content： " + result.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
	}
}