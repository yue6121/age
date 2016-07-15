package com.uestc.age;

import java.io.File;
import java.io.IOException;

import com.facepp.error.FaceppParseException;
import com.facepp.http.HttpRequests;
import com.facepp.http.PostParameters;
import com.facepp.result.FaceppResult;
import com.facepp.result.FaceppResult.JsonType;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class showActivity extends Activity {
	
	private static final int MSG_SUCCESS = 0;// 成功的标识
    private static final int MSG_FAILURE = 1;// 失败的标识
	private Thread thread;
	private ImageView imageView;
	private Button button4;
	Bitmap bm = null;

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_show);
        
        imageView = (ImageView)findViewById(R.id.imageView1);
        button4= (Button)findViewById(R.id.button4);
        
        button4.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(showActivity.this, photoActivity.class);
				startActivity(intent);
				finish();
			}
		});
        
        
        File file = new File("/sdcard/1.jpg");
        if(file.exists()){
        	Toast.makeText(getApplication(), "正在分析图片\n请保证网络流畅", Toast.LENGTH_SHORT).show();
        	bm = BitmapFactory.decodeFile("/sdcard/1.jpg");
        	imageView.setImageBitmap(bm);
        }
        else{
        	Intent intent_back = new Intent(showActivity.this, photoActivity.class);
			startActivity(intent_back);
        }
        /*Intent intent=getIntent();
        if(intent != null){
        	Toast.makeText(getApplication(), "正在分析图片\n请保证网络流畅", Toast.LENGTH_SHORT).show();
        	bm = intent.getParcelableExtra("bitmap");
        	imageView.setImageBitmap(bm);
        }
        else{
        	Intent intent_back = new Intent(showActivity.this, photoActivity.class);
			startActivity(intent_back);
        }*///////不能传大于40k的！！！
        
		thread = new Thread(runnable);
		thread.start();
        
	}
	
	
	
	FaceppResult result;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_SUCCESS:
				detectResult(result);
				imageView.setImageBitmap(bm);
				int number = -1;
				try {
					number = result.get("face").getCount();
				} catch (FaceppParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(number ==0)
					Toast.makeText(getApplication(), "没有检测到人脸，请重试！", Toast.LENGTH_LONG).show();
				else if (number > 1) {
					Toast.makeText(getApplication(), "检测到多个人脸，请重试！", Toast.LENGTH_LONG).show();
				}
				else if (number == 1) {
					//分析activity
					
					/*String face_id =null;
					try {
						face_id = result.get("face").get(0).get("face_id").toString();
						System.out.println(face_id);
					} catch (FaceppParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Toast.makeText(getApplication(), face_id, Toast.LENGTH_LONG).show();*/
					
					String gender = null;
					try {
						gender = result.get("face").get(0).get("attribute").get("gender").get("value", JsonType.STRING).toString();
					} catch (FaceppParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(gender.equals("Female")){
						Toast toast = Toast.makeText(getApplication(), "我猜你是女的！", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					else {
						Toast toast = Toast.makeText(getApplication(), "我猜你是男的！", Toast.LENGTH_SHORT);
						toast.setGravity(Gravity.CENTER, 0, 0);
						toast.show();
					}
					
					String age = null;
					String range = null;
					try {
						age = result.get("face").get(0).get("attribute").get("age").get("value",JsonType.STRING).toString();
						range = result.get("face").get(0).get("attribute").get("age").get("range",JsonType.STRING).toString();
					} catch (FaceppParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					//Toast toast = Toast.makeText(getApplication(), "我猜你："+age+" ± "+range+"岁", Toast.LENGTH_SHORT);
					Toast toast = Toast.makeText(getApplication(), "我猜你："+age+" 岁", Toast.LENGTH_LONG);
					toast.setGravity(Gravity.CENTER, 0, 0);
					toast.show();
					
					
				}
				else {
					Toast.makeText(getApplication(), "未知问题，请重试！", Toast.LENGTH_LONG).show();
				}
				
				
				break;
				
			case MSG_FAILURE:
				Toast.makeText(getApplication(), "网络问题！", Toast.LENGTH_LONG).show();

			default:
				break;
			}

		};
	};
	
	
	public void detectResult(FaceppResult rst) {
		//Log.v(TAG, rst.toString());
		
		//use the red paint
		Paint paint = new Paint();
		paint.setColor(Color.RED);
		paint.setStrokeWidth(Math.max(bm.getWidth(), bm.getHeight()) / 100f);

		//create a new canvas
		Bitmap bitmap = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), bm.getConfig());
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bm, new Matrix(), null);
		
		
		try {
			//find out all faces
			final int count = rst.get("face").getCount();
			for (int i = 0; i < count; ++i) {
				float x, y, w, h;
				//get the center point
				x = (float)rst.get("face").get(i).get("position").get("center").get("x").toDouble().doubleValue();
				y = (float)rst.get("face").get(i).get("position").get("center").get("y").toDouble().doubleValue();

				//get face size
				w = (float)rst.get("face").get(i).get("position").get("width").toDouble().doubleValue();
				h = (float)rst.get("face").get(i).get("position").get("height").toDouble().doubleValue();
				
				//change percent value to the real size
				x = x / 100 * bm.getWidth();
				w = w / 100 * bm.getWidth()*0.7f;
				y = y / 100 * bm.getHeight();
				h = h / 100 * bm.getHeight()*0.7f;

				//draw the box to mark it out
				canvas.drawLine(x - w, y - h, x - w, y + h, paint);
				canvas.drawLine(x - w, y - h, x + w, y - h, paint);
				canvas.drawLine(x + w, y + h, x - w, y + h, paint);
				canvas.drawLine(x + w, y + h, x + w, y - h, paint);
			}
			
			//save new image
			bm = bitmap;

		} catch (FaceppParseException e) {
			e.printStackTrace();

		}
		
	};
	
	Runnable runnable = new Runnable() {
		public void run() {
		HttpRequests httpRequests = new HttpRequests("270ff76b7c212533a3079aa0be82a3c5","hsWv6TPO6nc_PteGNxAPMwihBEBQQpYz",true);
		//final FaceppResult result;
		try {
			PostParameters postParameters =new PostParameters();
			File file = new File("/sdcard/1.jpg");
			postParameters.setImg(file);
			postParameters.getMultiPart().writeTo(System.out);
			result = httpRequests.detectionDetect(postParameters);
			System.out.println(result);
			
		} catch (FaceppParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			handler.obtainMessage(MSG_FAILURE).sendToTarget();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		handler.obtainMessage(MSG_SUCCESS,result).sendToTarget();
		}
	};

}
