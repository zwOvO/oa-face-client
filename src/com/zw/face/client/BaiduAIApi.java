package com.zw.face.client;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import org.json.JSONObject;

import com.baidu.aip.face.AipFace;
import com.baidu.aip.face.MatchRequest;
import com.zw.face.config.Config;

import sun.misc.BASE64Encoder;

public class BaiduAIApi {
	private static AipFace client;
	static {
		// 初始化一个AipFace
		client = new AipFace(Config.APP_ID, Config.API_KEY, Config.SECRET_KEY);
	}
	
	public static String Compare(String faceToken,InputStream is) {
		// 传入可选参数调用接口
		try {
			System.out.println(faceToken);
			String image1 = faceToken;
	        String image2;
			image2 = image2Base64String(is);
			// image1/image2也可以为url或facetoken, 相应的imageType参数需要与之对应。
	        MatchRequest req1 = new MatchRequest(image1, "FACE_TOKEN");
	        MatchRequest req2 = new MatchRequest(image2, "BASE64");
	        ArrayList<MatchRequest> requests = new ArrayList<MatchRequest>();
	        requests.add(req1);
	        requests.add(req2);
	        JSONObject res = client.match(requests);
	        System.out.println(res.toString());
			return res.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
	
	public static String image2Base64String(InputStream content) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            int length = 0;
            byte[] buffer = new byte[1024];
            while((length = content.read(buffer)) > 0){
                out.write(buffer,0,length);
            }
        } finally {
            if (content != null) content.close();
            if(out != null) out.close();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(out.toByteArray());
    }
}