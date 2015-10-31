package zjn.ui;

import zjn.mymusic.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint("NewApi") public class PagerIndicator extends LinearLayout{
	public PagerIndicator(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PagerIndicator(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		setOrientation(HORIZONTAL);
		setGravity(Gravity.CENTER);
	}
	
	/**
	 * ��������ҳ
	 * @param total ��������
	 */
	public void create(int total) {
		for (int i = 0; i < total; i++) {
			ImageView iv = new ImageView(getContext());
			iv.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			iv.setPadding(2, 2, 2, 2);
			
			iv.setImageResource(i == 0 ? R.drawable.play_page_selected: 
				R.drawable.play_page_unselected);
			addView(iv);
		}
	}
	
	/**
	 * ɾ��������
	 * @param index ɾ���ڼ���
	 */
	public void removeAt(int index) {
		removeViewAt(index);
		requestLayout();
		invalidate();
	}
	
	/**
	 * ��ǰ������ʾ�ڼ�ҳ
	 * @param current	��ǰҳ��
	 */
	public void current(int current) {
		final int COUNT = getChildCount();
		ImageView iv;
		for (int i = 0; i < COUNT; i++) {
			iv = (ImageView) getChildAt(i);
			if (i == current) {
				iv.setImageResource(R.drawable.play_page_selected);
			} else {
				iv.setImageResource(R.drawable.play_page_unselected);
			}
		}
	}
}
