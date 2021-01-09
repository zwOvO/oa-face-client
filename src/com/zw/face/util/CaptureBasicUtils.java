package com.zw.face.util;

import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import com.zw.face.runnable.CompareRunnable;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import com.zw.face.config.Config;

public class CaptureBasicUtils extends JPanel {
	private static final long serialVersionUID = -166714110812799486L;
	private static CompareRunnable compareRunnable = new CompareRunnable();

	public static BufferedImage mat2BI(Mat mat) {
		int dataSize = mat.cols() * mat.rows() * (int) mat.elemSize();
		byte[] data = new byte[dataSize];
		mat.get(0, 0, data);
		int type = mat.channels() == 1 ? BufferedImage.TYPE_BYTE_GRAY : BufferedImage.TYPE_3BYTE_BGR;

		if (type == BufferedImage.TYPE_3BYTE_BGR) {
			for (int i = 0; i < dataSize; i += 3) {
				byte blue = data[i + 0];
				data[i + 0] = data[i + 2];
				data[i + 2] = blue;
			}
		}
		BufferedImage image = new BufferedImage(mat.cols(), mat.rows(), type);
		image.getRaster().setDataElements(0, 0, mat.cols(), mat.rows(), data);

		return image;
	}

	/**
	 * opencv实现人脸检测
	 */
	public static Mat detectFace(Mat img, String uuid,String faceToken) throws Exception {
		// 从配置文件lbpcascade_frontalface.xml中创建一个人脸识别器，该文件位于opencv安装目录中
		CascadeClassifier faceDetector = new CascadeClassifier(
				Config.OPENCV_SRC + "\\sources\\data\\haarcascades\\haarcascade_frontalface_alt.xml");

		// 在图片中检测人脸
		MatOfRect faceDetections = new MatOfRect();

		faceDetector.detectMultiScale(img, faceDetections);
		Rect[] rects = faceDetections.toArray();
		if (rects != null && rects.length >= 1) {
			for (Rect rect : rects) {
				if (rects.length == 1) {
					Imgproc.rectangle(img, new Point(rect.x, rect.y),
							new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 2);
					if (!compareRunnable.isDeFace()) {
						synchronized (rects) {
							compareRunnable.setImg(img);
							compareRunnable.setUUID(uuid);
							compareRunnable.setFaceToken(faceToken);
							new Thread(compareRunnable).start();
						}
					}
				}
			}
		}
		return img;
	}
}