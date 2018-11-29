package com.zw.face.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.sql.ResultSet;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import com.zw.face.Api.BaiduAIApi;
import com.zw.face.DB.DBUtils;
import com.zw.face.UI.FaceClient;

public class CompareRunnable implements Runnable {

	private Mat img;
	private String UUID;
	private String faceToken;
	private boolean isDeFace = false;
	private static boolean isSuccess = false;

	public Mat getImg() {
		return img;
	}

	public void setImg(Mat img) {
		this.img = img;
	}

	public String getUUID() {
		return UUID;
	}

	public void setUUID(String uuid) {
		UUID = uuid;
	}

	public String getFaceToken() {
		return faceToken;
	}

	public void setFaceToken(String faceToken) {
		this.faceToken = faceToken;
	}

	public boolean isDeFace() {
		return isDeFace;
	}

	public void setDeFace(boolean isDeFace) {
		this.isDeFace = isDeFace;
	}

	public static void setSuccess(boolean isSuccess) {
		CompareRunnable.isSuccess = isSuccess;
	}

	public static boolean isSuccess() {
		return isSuccess;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		synchronized (UUID) {
			Imgcodecs.imwrite("Camera\\" + getUUID() + ".png", getImg());
			ResultSet rs = DBUtils.select(String.format("select open_id from tb_record where status = 0 and id = '%s'", getUUID()));
			try {
				if (!rs.isClosed()&&rs.next()) {
					isDeFace = true;
					System.out.println("验证中...");
					String res = BaiduAIApi.Compare(getFaceToken(),new FileInputStream(new File("Camera\\" + getUUID() + ".png")));
					int error_code = new JSONObject(res).getInt("error_code");
					if (error_code == 0) {
						JSONObject result = new JSONObject(res).getJSONObject("result");
						Double score = result.getDouble("score");
						if (score > 90) {
							DBUtils.execute(String.format("update tb_record set status = 1 where id = '%s'", getUUID()));
							isSuccess = true;
							TTS.Speak("打卡成功");
							JOptionPane.showMessageDialog(null, "验证通过", "标题", JOptionPane.WARNING_MESSAGE);
							FaceClient.CameraFlag = false;
						} else {
							isSuccess = false;
							System.out.println("验证不通过");
						}
					}
					isDeFace = false;
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}