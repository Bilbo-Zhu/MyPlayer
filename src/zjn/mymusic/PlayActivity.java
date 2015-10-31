package zjn.mymusic;

import java.util.ArrayList;

import zjn.applicantion.App;
import zjn.info.Music;
import zjn.ui.CDView;
import zjn.ui.LrcView;
import zjn.ui.PagerIndicator;
import Utils.ImageTools;
import Utils.MusicIconLoader;
import Utils.MusicUtils;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayActivity extends BaseActivity implements OnClickListener, OnSeekBarChangeListener, OnPageChangeListener{
	private int pos;
	
	private ViewPager mViewPager;
	private ImageView mImagPlayBack;
	private TextView mTextMusicTitle;
	private CDView mCdView;
	private SeekBar mPlaySeekBar; 
	private TextView mTextArtistTitle;
	private ImageButton mStartPlayButton;
	private PagerIndicator mPagerIndicator;
	private LrcView mLrcViewOnFirstPage; 
	private LrcView mLrcViewOnSecondPage;
	
	private ArrayList<View> mViewPagerContent = new ArrayList<View>(2);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.play_activity);
		getIntentPos();
		setupViews();
		initEvent();
//		allowBindService();
	}
	
	private void disPlay(int position) {
		// TODO Auto-generated method stub
		Music music = MusicUtils.sMusicList.get(position);
		
		mTextMusicTitle.setText(music.getTitle());
		mTextArtistTitle.setText(music.getArtist());
		mPlaySeekBar.setMax(music.getLength());
		Bitmap bmp = MusicIconLoader.getInstance().load(music.getImage());
		if(bmp == null) bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon);
		mCdView.setImage(ImageTools.scaleBitmap(bmp, (int)(App.sScreenWidth * 0.4)));
		
		if(mPlayService.isPlaying()) {
			mCdView.start();
			mStartPlayButton.setImageResource(R.drawable.player_btn_pause_normal);
		}else {
			mCdView.pause();
			mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
		}
	}

	private void getIntentPos() {
		// TODO Auto-generated method stub
		Intent intent = getIntent();
		pos = intent.getIntExtra("pos", -1);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		allowUnbindService();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		allowBindService();
	}



	private void initEvent() {
		// TODO Auto-generated method stub
		initViewPagerContent();
		mImagPlayBack.setOnClickListener(this);
		mViewPager.setOnPageChangeListener(this);
		mPlaySeekBar.setOnSeekBarChangeListener(this);
		mViewPager.setAdapter(mPagerAdapter);
		mPagerIndicator.create(mViewPagerContent.size());
			}

	private void setupViews() {
		// TODO Auto-generated method stub
		mViewPager = (ViewPager) findViewById(R.id.vp_play_container);
		mImagPlayBack = (ImageView) findViewById(R.id.iv_play_back);
		mStartPlayButton = (ImageButton) findViewById(R.id.ib_play_start);
		mTextMusicTitle = (TextView) findViewById(R.id.tv_music_title);
		mTextArtistTitle = (TextView) findViewById(R.id.play_singer);
		mPlaySeekBar =(SeekBar) findViewById(R.id.sb_play_progress);
		mPagerIndicator = (PagerIndicator) findViewById(R.id.pi_play_indicator);
		
		// 动态设置seekbar的margin
		MarginLayoutParams p = (MarginLayoutParams) mPlaySeekBar.getLayoutParams();
			p.leftMargin = (int) (App.sScreenWidth * 0.1);
			p.rightMargin = (int) (App.sScreenWidth * 0.1);
	}
	
	/**
	 * 上一曲
	 * @param view
	 */
	public void pre(View view) {
		mPlayService.pre(); // 上一曲
	}
	
	/**
	 * 播放 or 暂停
	 * @param view
	 */
	public void play(View view) {
		if(mPlayService.isPlaying()) {
			mPlayService.pause(); // 暂停
			//mCdView.pause();
			mStartPlayButton.setImageResource(R.drawable.player_btn_play_normal);
		}else {
			pos = mPlayService.resume();
			disPlay(pos);
			//onPlay(mPlayService.resume()); // 播放	
		}
	}
	
	//private void onPlay(int position) {
		//Music music = MusicUtils.sMusicList.get(position);
		//Log.d("Log", "music.getTitle()---->"+music.getTitle());
		//mTextMusicTitle.setText(music.getTitle());
		
	//}

	/**
	 * 上一曲
	 * @param view
	 */
	public void next(View view) {
		mPlayService.next(); // 上一曲
	}

	private void setLrc(int position) {
		Music music = MusicUtils.sMusicList.get(position);
		String lrcPath = MusicUtils.getLrcDir() + music.getTitle() + ".lrc";
		Log.d("Log", "lrcPath----->" + lrcPath);
		mLrcViewOnFirstPage.setLrcPath(lrcPath);
		mLrcViewOnSecondPage.setLrcPath(lrcPath);
	}
	
	@Override
	public void onPublish(int progress) {
		// TODO Auto-generated method stub
		mPlaySeekBar.setProgress(progress);
		if(mLrcViewOnFirstPage.hasLrc()) mLrcViewOnFirstPage.changeCurrent(progress);
		if(mLrcViewOnSecondPage.hasLrc()) mLrcViewOnSecondPage.changeCurrent(progress);
	}

	@Override
	public void onChange(int position) {
		// TODO Auto-generated method stub
		disPlay(position);
		Log.d("Log", "onChange---->Lrc");
		setLrc(position);
	}

	private PagerAdapter mPagerAdapter = new PagerAdapter() {
		@Override
		public int getCount() {
			return mViewPagerContent.size();
		}

		@Override
		public boolean isViewFromObject(View view, Object obj) {
			return view == obj;
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(mViewPagerContent.get(position));
			return mViewPagerContent.get(position);
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			((ViewPager) container).removeView((View) object);
		}
	};
	
	/**
	 * 初始化viewpager的内容
	 */
	private void initViewPagerContent() {
		View cd = View.inflate(this, R.layout.play_pager_item_1, null);
		mCdView = (CDView) cd.findViewById(R.id.play_cdview);
		mTextArtistTitle = (TextView) cd.findViewById(R.id.play_singer);
		mLrcViewOnFirstPage = (LrcView) cd.findViewById(R.id.play_first_lrc);
		
		View lrcView = View.inflate(this, R.layout.play_pager_item_2, null);
		mLrcViewOnSecondPage = (LrcView) lrcView.findViewById(R.id.play_first_lrc_2);
		
		mViewPagerContent.add(cd);
		mViewPagerContent.add(lrcView);
	}
	
	@Override
	public void onClick(View mView) {
		// TODO Auto-generated method stub
		switch (mView.getId()) {
		case R.id.iv_play_back:
			finish();
			break;

		default:
			break;
		}
	}

	@Override
	public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStartTrackingTouch(SeekBar arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		int progress = seekBar.getProgress();
		mPlayService.seek(progress);
		mLrcViewOnFirstPage.onDrag(progress);
		mLrcViewOnSecondPage.onDrag(progress);
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		if (position == 0) {
			if (mPlayService.isPlaying())
				mCdView.start();
		} else {
			mCdView.pause();
		}
		mPagerIndicator.current(position);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
