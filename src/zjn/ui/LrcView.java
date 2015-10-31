package zjn.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import zjn.mymusic.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

@SuppressLint("DrawAllocation")
public class LrcView extends View {
	private static final int SCROLL_TIME = 500;
	private static final String DEFAULT_TEXT = "���޸��";
	
	private List<String> mLrcs = new ArrayList<String>(); // ��Ÿ��
	private List<Long> mTimes = new ArrayList<Long>(); // ���ʱ��

	private long mNextTime = 0l; // ������һ�俪ʼ��ʱ��

	private int mViewWidth; // view�Ŀ��
	private int mLrcHeight; // lrc����ĸ߶�
	private int mRows;      // ������
	private int mCurrentLine = 0; // ��ǰ��
	private int mOffsetY;   // y�ϵ�ƫ��
	private int mMaxScroll; // ��󻬶�����=һ�и�ʸ߶�+��ʼ��

	private float mTextSize; // ����
	private float mDividerHeight; // �м��
	
	private Rect mTextBounds;

	private Paint mNormalPaint; // ���������
	private Paint mCurrentPaint; // ��ǰ��ʵĴ�С

	private Bitmap mBackground;
	
	private Scroller mScroller;

	public LrcView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mScroller = new Scroller(context, new LinearInterpolator());
		inflateAttributes(attrs);
	}

	// ��ʼ������
	private void inflateAttributes(AttributeSet attrs) {
		// <begin>
		// �����Զ�������
		TypedArray ta = getContext().obtainStyledAttributes(attrs,
				R.styleable.Lrc);
		mTextSize = ta.getDimension(R.styleable.Lrc_textSize, 50.0f);
		mRows = ta.getInteger(R.styleable.Lrc_rows, 5);
		mDividerHeight = ta.getDimension(R.styleable.Lrc_dividerHeight, 0.0f);

		int normalTextColor = ta.getColor(R.styleable.Lrc_normalTextColor,
				0xffffffff);
		int currentTextColor = ta.getColor(R.styleable.Lrc_currentTextColor,
				0xff00ffde);
		ta.recycle();
		// </end>

		// ����lrc���ĸ߶�
		mLrcHeight = (int) (mTextSize + mDividerHeight) * mRows + 5;

		mNormalPaint = new Paint();
		mCurrentPaint = new Paint();
		
		// ��ʼ��paint
		mNormalPaint.setTextSize(mTextSize);
		mNormalPaint.setColor(normalTextColor);
		mNormalPaint.setAntiAlias(true);
		mCurrentPaint.setTextSize(mTextSize);
		mCurrentPaint.setColor(currentTextColor);
		mCurrentPaint.setAntiAlias(true);
		
		mTextBounds = new Rect();
		mCurrentPaint.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), mTextBounds);
		mMaxScroll = (int) (mTextBounds.height() + mDividerHeight);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// ��������view�ĸ߶�
		int measuredHeightSpec = MeasureSpec.makeMeasureSpec(mLrcHeight, MeasureSpec.AT_MOST);
		super.onMeasure(widthMeasureSpec, measuredHeightSpec);
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		// ��ȡview���
		mViewWidth = getMeasuredWidth();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBackground != null) {
			canvas.drawBitmap(Bitmap.createScaledBitmap(
					mBackground, mViewWidth, mLrcHeight, true),
					new Matrix(), null);
		}
		
		float centerY = (getMeasuredHeight() +
				mTextBounds.height() - mDividerHeight) / 2;
		if (mLrcs.isEmpty() || mTimes.isEmpty()) {
			canvas.drawText(DEFAULT_TEXT, 
					(mViewWidth - mCurrentPaint.measureText(DEFAULT_TEXT)) / 2,
					centerY, mCurrentPaint);
			return;
		}

		float offsetY = mTextBounds.height() + mDividerHeight;
		String currentLrc = mLrcs.get(mCurrentLine);
		float currentX = (mViewWidth - mCurrentPaint.measureText(currentLrc)) / 2;
		// ����ǰ��
		canvas.drawText(currentLrc, currentX, centerY - mOffsetY, mCurrentPaint);
		
		int firstLine = mCurrentLine - mRows / 2;
		firstLine = firstLine <= 0 ? 0 : firstLine;
		int lastLine = mCurrentLine + mRows / 2 + 2;
		lastLine = lastLine >= mLrcs.size() - 1 ? mLrcs.size() - 1 : lastLine;
		
		// ����ǰ�������
		for (int i = mCurrentLine - 1,j=1; i >= firstLine; i--,j++) {
			String lrc = mLrcs.get(i);
			float x = (mViewWidth - mNormalPaint.measureText(lrc)) / 2;
			canvas.drawText(lrc, x, centerY - j * offsetY - mOffsetY, mNormalPaint);
		}

		// ����ǰ�������
		for (int i = mCurrentLine + 1,j=1; i <= lastLine; i++,j++) {
			String lrc = mLrcs.get(i);
			float x = (mViewWidth - mNormalPaint.measureText(lrc)) / 2;
			canvas.drawText(lrc, x, centerY + j * offsetY - mOffsetY, mNormalPaint);
		}
	}
	
	@Override
	public void computeScroll() {
		if(mScroller.computeScrollOffset()) {
			mOffsetY = mScroller.getCurrY();
			if(mScroller.isFinished()) {
				int cur = mScroller.getCurrX();
				mCurrentLine = cur <= 1 ? 0 : cur - 1;
				mOffsetY = 0;
			}
			postInvalidate();
		}
	}

	// ����ʱ��
	private Long parseTime(String time) {
		// 03:02.12
		String[] min = time.split(":");
		String[] sec = min[1].split("\\.");
		
		long minInt = Long.parseLong(min[0].replaceAll("\\D+", "")
				.replaceAll("\r", "").replaceAll("\n", "").trim());
		long secInt = Long.parseLong(sec[0].replaceAll("\\D+", "")
				.replaceAll("\r", "").replaceAll("\n", "").trim());
		long milInt = Long.parseLong(sec[1].replaceAll("\\D+", "")
				.replaceAll("\r", "").replaceAll("\n", "").trim());
		
		return minInt * 60 * 1000 + secInt * 1000 + milInt * 10;
	}

	// ����ÿ��
	private String[] parseLine(String line) {
		Matcher matcher = Pattern.compile("\\[\\d.+\\].+").matcher(line);
		// ������磺[xxx]����ɶҲû�еģ���return��
		if (!matcher.matches()) {
			System.out.println("throws " + line);
			return null;
		}

		line = line.replaceAll("\\[", "");
		String[] result = line.split("\\]");
		result[0] = String.valueOf(parseTime(result[0]));

		return result;
	}

	// �ⲿ�ṩ����
	// ���뵱ǰ����ʱ��
	public synchronized void changeCurrent(long time) {
		// �����ǰʱ��С����һ�俪ʼ��ʱ��
		// ֱ��return
		if (mNextTime > time) {
			return;
		}
		
		// ÿ�ν�����������ŵ�ʱ��
		for (int i = 0; i < mTimes.size(); i++) {
			// �������ʱ����ڴ�������ʱ��
			// ��ô���ھ�Ӧ����ʾ���ʱ��ǰ��Ķ�Ӧ����һ��
			// ÿ�ζ�������ʾ���ǲ���Ҫ�жϣ�����������ʾ�Ͳ�ˢ����
			if (mTimes.get(i) > time) {
				mNextTime = mTimes.get(i);
				mScroller.abortAnimation();
				mScroller.startScroll(i, 0, 0, mMaxScroll, SCROLL_TIME);
//				mNextTime = mTimes.get(i);
//				mCurrentLine = i <= 1 ? 0 : i - 1;
				postInvalidate();
				return;
			}
		}
	}
	
	public void onDrag(int progress) {
		for(int i=0;i<mTimes.size();i++) {
			if(Integer.parseInt(mTimes.get(i).toString()) > progress) {
				mNextTime = i == 0 ? 0 : mTimes.get(i-1);
				return;
			}
		}
	}

	// �ⲿ�ṩ����
	// ����lrc��·��
	public void setLrcPath(String path) {
		reset();
		File file = new File(path);
		if (!file.exists()) {
			postInvalidate();
			return;
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

			String line = "";
			String[] arr;
			while (null != (line = reader.readLine())) {
				arr = parseLine(line);
				if (arr == null) continue;

				// �����������ֻ��һ��
				if (arr.length == 1) {
					String last = mLrcs.remove(mLrcs.size() - 1);
					mLrcs.add(last + arr[0]);
					continue;
				}
				mTimes.add(Long.parseLong(arr[0]));
				mLrcs.add(arr[1]);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(reader != null) {
				try {
					reader.close();		
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void reset() {
		mLrcs.clear();
		mTimes.clear();
		mCurrentLine = 0;
		mNextTime = 0l;
	}
	
	// �Ƿ����ø��
	public boolean hasLrc() {
		return mLrcs != null && !mLrcs.isEmpty();
	}

	// �ⲿ�ṩ����
	// ���ñ���ͼƬ
	public void setBackground(Bitmap bmp) {
		mBackground = bmp;
	}
}
