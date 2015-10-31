package Utils;

import zjn.applicantion.App;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class ImageTools {
	/**
	 * ����ͼƬ
	 * @param bmp
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bmp) {
		return scaleBitmap(bmp, (int) (App.sScreenWidth * 0.13));
	}
	
	/**
	 * ����ͼƬ
	 * @param bmp
	 * @param size
	 * @return
	 */
	public static Bitmap scaleBitmap(Bitmap bmp, int size) {
		return Bitmap.createScaledBitmap(bmp, size, size, true);
	}
	
	/**
	 * �����ļ�uri����ͼƬ
	 * @param uri
	 * @return
	 */
	public static Bitmap scaleBitmap(String uri, int size) {
		return scaleBitmap(BitmapFactory.decodeFile(uri), size);
	}
	
	/**
	 * �����ļ�uri����ͼƬ
	 * @param uri
	 * @return
	 */
	public static Bitmap scaleBitmap(String uri) {
		return scaleBitmap(BitmapFactory.decodeFile(uri));
	}
	
	/**
	 * ������ԴͼƬ
	 * @param res
	 * @return
	 */
	public static Bitmap scaleBitmap(int res) {
		return scaleBitmap(BitmapFactory.decodeResource(App.sContext.getResources(), res));
	}
	
	/**
	 * ����Բ��ͼƬ
	 * @deprecated
	 * @param src
	 * @return
	 */
	public static Bitmap createCircleBitmap(Bitmap src) {
		int size = (int) (App.sScreenWidth * 0.13);
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setARGB(255, 241, 239, 229);
		
		Bitmap target = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(target);
		canvas.drawCircle(size / 2, size / 2, size / 2, paint);
		
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP));
		canvas.drawBitmap(src, 0, 0, paint);
		
		return target;
	}
	
	/**
	 * @deprecated
	 * @param uri
	 * @return
	 */
	public static Bitmap createCircleBitmap(String uri) {
		return createCircleBitmap(BitmapFactory.decodeFile(uri));
	}
	
	/**
	 * @deprecated
	 * @param res
	 * @return
	 */
	public static Bitmap createCircleBitmap(int res) {
		return createCircleBitmap(BitmapFactory.decodeResource(App.sContext.getResources(), res));
	}
}
