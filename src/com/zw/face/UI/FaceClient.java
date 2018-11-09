package com.zw.face.UI;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import javax.swing.SwingConstants;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import com.zw.face.DB.DBUtils;
import com.zw.face.Utils.CaptureBasicUtils;
import com.zw.face.Utils.CompareRunnable;
import com.zw.face.Utils.QRCodeUtils;
import com.zw.face.Utils.TTS;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.awt.event.ActionEvent;

public class FaceClient {

	private JFrame frame;
	private JPanel panelUserInfo;
	private static JLabel lblStatus;

	private static boolean QRFlag = true;
	public static boolean CameraFlag = false;
	private static JPanel panel;
	private static UUID uuid;
	private static VideoCapture capture;
	private static BufferedImage mImg;

	private static Thread CameraThread;
	private static Thread QRThread;
	
	private static String faceToken;
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					FaceClient window = new FaceClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public FaceClient() {
		deleteDir(new File("Camera"));
		deleteDir(new File("Temp"));
		new File("Camera").mkdir();
		new File("Temp").mkdir();
		uuid = UUID.randomUUID();
		initialize();
	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			// 递归删除目录中的子目录下
			for (int i = 0; i < children.length; i++) {
				dir.delete();
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		// 目录此时为空，可以删除
		return dir.delete();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 850, 500);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		panel = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7887817404159576290L;

			@Override
			protected void paintComponent(Graphics g) {
				// TODO Auto-generated method stub
				super.paintComponent(g);
				if (mImg != null) {
					g.drawImage(mImg, 0, 0, mImg.getWidth(), mImg.getHeight(), this);
				}
			}
		};
		panel.setBounds(10, 10, 600, 441);
		frame.getContentPane().add(panel);
		panel.setLayout(null);

		JPanel panelQRImg = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4293379162489160437L;

			protected void paintComponent(Graphics d) {
				super.paintComponent(d);
				if (uuid != null) {
					File file = new File("Temp\\QRCode_" + uuid.toString().replaceAll("-", "") + ".png");
					file.delete();
				}
				QRCodeUtils.createQrcode(uuid.toString().replaceAll("-", ""));
				ImageIcon icon = new ImageIcon("Temp\\QRCode_" + uuid.toString().replaceAll("-", "") + ".png");
				Image img = icon.getImage();
				d.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), this);
			}

		};
		panelQRImg.repaint();
		panelQRImg.setBounds(620, 10, 204, 193);
		frame.getContentPane().add(panelQRImg);
		JButton button = new JButton("刷新验证");
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				uuid = UUID.randomUUID();
				QRFlag = true;
				CameraFlag = false;
				panel.getGraphics().clearRect(0, 0, panel.getWidth(), panel.getHeight());
				panelUserInfo.setVisible(CameraFlag);
				panelQRImg.repaint();
			}
		});
		button.setBounds(620, 213, 204, 62);
		frame.getContentPane().add(button);

		panelUserInfo = new JPanel();
		panelUserInfo.setLayout(null);
		panelUserInfo.setBounds(620, 285, 204, 166);
		panelUserInfo.setVisible(false);
		frame.getContentPane().add(panelUserInfo);

		JLabel lbl1 = new JLabel("用户名称：");
		lbl1.setHorizontalAlignment(SwingConstants.CENTER);
		lbl1.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lbl1.setBounds(10, 10, 184, 24);
		panelUserInfo.add(lbl1);

		JLabel lblName = new JLabel("");
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lblName.setBounds(10, 36, 184, 24);
		panelUserInfo.add(lblName);

		JLabel lblID = new JLabel("用户ID：");
		lblID.setHorizontalAlignment(SwingConstants.CENTER);
		lblID.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lblID.setBounds(10, 70, 184, 24);
		panelUserInfo.add(lblID);

		JLabel lbl2 = new JLabel("打卡状态");
		lbl2.setHorizontalAlignment(SwingConstants.CENTER);
		lbl2.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lbl2.setBounds(10, 104, 184, 24);
		panelUserInfo.add(lbl2);

		lblStatus = new JLabel("未打卡");
		lblStatus.setHorizontalAlignment(SwingConstants.CENTER);
		lblStatus.setFont(new Font("微软雅黑", Font.PLAIN, 14));
		lblStatus.setBounds(10, 128, 184, 24);
		panelUserInfo.add(lblStatus);
		QRThread = new Thread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				while (true) {
					System.out.print("");
					if (QRFlag) {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Date date = new Date();
						DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
						frame.setTitle("系统时间:" + format.format(date));
						ResultSet res = DBUtils.select(String.format(
								"select username,gender,face_token from tb_user where open_id = (select open_id from tb_record where status = 0 and id='%s')",
								uuid.toString().replaceAll("-", "")));
						try {
							if (res!=null && res.next()) {
								lblName.setText(res.getString("username"));
								TTS.Speak(res.getString("username")+"同学，请开始你的表演");
								lblID.setText("用户性别:" + (res.getInt("gender")==1?"男":"女"));
								faceToken = res.getString("face_token");
								if (capture == null) {
									openCamera(frame);
								}
								QRFlag = false;
								CameraFlag = true;
							} else {
								CameraFlag = false;
								QRFlag = true;
								lblName.setText("");
								lblID.setText("用户性别:");
								lblStatus.setText("");
								if (capture != null) {
									capture.release();
									capture = null;
								}
								panel.getGraphics().clearRect(0, 0, panel.getWidth(), panel.getHeight());
							}
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						panelUserInfo.setVisible(CameraFlag);

					}
				}
			}
		});
		QRThread.start();

	}

	private static void openCamera(JFrame frame) {
		try {
			System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

			Mat capImg = new Mat();
			capture = new VideoCapture(0);
			int height = (int) capture.get(Videoio.CAP_PROP_FRAME_HEIGHT);
			int width = (int) capture.get(Videoio.CAP_PROP_FRAME_WIDTH);
			if (height == 0 || width == 0) {
				throw new Exception("camera not found!");
			}
			Mat temp = new Mat();
			CameraThread = new Thread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					while (true) {
						if (CameraFlag) {
							try {
								if (capImg != null && capture != null) {
									capture.read(capImg);
									Imgproc.cvtColor(capImg, temp, Imgproc.COLOR_RGB2GRAY);
									mImg = CaptureBasicUtils.mat2BI(CaptureBasicUtils.detectFace(capImg, uuid.toString().replaceAll("-", ""),faceToken));
									if (CompareRunnable.isSuccess()) {
										CompareRunnable.setSuccess(false);
										Date date = new Date();
										DateFormat format = new SimpleDateFormat("HH:mm:ss");
										lblStatus.setText(format.format(date) + " 已打卡");
										if (capture != null) {
											capture.release();
											capture = null;
										}
									}
									panel.repaint();
								}
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			});
			CameraThread.start();
		} catch (Exception e) {
			System.out.println("错误:" + e);
		}
	}

}
