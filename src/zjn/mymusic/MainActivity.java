package zjn.mymusic;

import zjn.fragment.CdFragment;
import zjn.fragment.CompassFragment;
import zjn.fragment.LocalFragment;
import zjn.fragment.SearchFragment;
import zjn.fragment.UserFragment;
import zjn.service.PlayService;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

public class MainActivity extends BaseActivity implements OnClickListener
{	
	public static final int TAB_USER = 0;
	public static final int TAB_CD = 1;
	public static final int TAB_SEARCH = 2;
	public static final int TAB_COMPASS = 3;
	public static final int TAB_SONGLIST = 4;
	
	public static final int TOP_JUMP = 5;
	public static final int TOP_MENU = 6;
	
	private TextView mTitle;
	
	private LinearLayout mTabuser;
	private LinearLayout mTabcd;
	private LinearLayout mTabsearch;
	private LinearLayout mTabcompass;

	private ImageButton mImguser;
	private ImageButton mImgcd;
	private ImageButton mImgsearch;
	private ImageButton mImgcompass;
	private ImageButton mImgtopjump;
	private ImageButton mImgmenu;

	private Fragment mTab01;
	private Fragment mTab02;
	private Fragment mTab03;
	private Fragment mTab04;
	private Fragment mTab05;
	
	private PopupWindow mPopupWindow;
	private View mPopShownView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		
		initView();
		initEvent();
		setSelect(0);
	}
	
	private void initEvent()
	{
		mTabuser.setOnClickListener(this);
		mTabcd.setOnClickListener(this);
		mTabsearch.setOnClickListener(this);
		mTabcompass.setOnClickListener(this);
		
		mImgtopjump.setOnClickListener(this);
		mImgmenu.setOnClickListener(this);
	}

	private void initView()
	{
		mTabuser = (LinearLayout) findViewById(R.id.id_tab_user);
		mTabcd = (LinearLayout) findViewById(R.id.id_tab_cd);
		mTabsearch = (LinearLayout) findViewById(R.id.id_tab_search);
		mTabcompass = (LinearLayout) findViewById(R.id.id_tab_compass);
		
		mImgtopjump = (ImageButton) findViewById(R.id.id_tab_topjump);
		mImguser = (ImageButton) findViewById(R.id.id_tab_user_img);
		mImgcd = (ImageButton) findViewById(R.id.id_tab_cd_img);
		mImgsearch = (ImageButton) findViewById(R.id.id_tab_search_img);
		mImgcompass = (ImageButton) findViewById(R.id.id_tab_compass_img);
		mImgmenu = (ImageButton) findViewById(R.id.id_img_menu);
		
		mTitle = (TextView) findViewById(R.id.title);
		mPopShownView = findViewById(R.id.view_pop_show);
	}

	public void setSelect(int i)
	{
		FragmentManager fm = getSupportFragmentManager();
		FragmentTransaction transaction = fm.beginTransaction();
		hideFragment(transaction);
		// 把图片设置为亮的
		// 设置内容区域
		switch (i)
		{
		case TAB_USER:
			if (mTab01 == null)
			{
				mTab01 = new UserFragment();
				transaction.add(R.id.id_content, mTab01);
			} else
			{
				transaction.show(mTab01);
			}
			mImguser.setImageResource(R.drawable.icon_user_selected);
			mTitle.setText("我的音乐");
			
			break;
		case TAB_CD:
			if (mTab02 == null)
			{
				mTab02 = new CdFragment();
				transaction.add(R.id.id_content, mTab02);
			} else
			{
				transaction.show(mTab02);
				
			}
			mImgcd.setImageResource(R.drawable.icon_cd_selected);
			mTitle.setText("音乐架");
			break;
		case TAB_SEARCH:
			if (mTab03 == null)
			{
				mTab03 = new SearchFragment();
				transaction.add(R.id.id_content, mTab03);
			} else
			{
				transaction.show(mTab03);
			}
			mImgsearch.setImageResource(R.drawable.icon_search_selected);
			mTitle.setText("搜索");
			break;
		case TAB_COMPASS:
			if (mTab04 == null)
			{
				mTab04 = new CompassFragment();
				transaction.add(R.id.id_content, mTab04);
			} else
			{
				transaction.show(mTab04);
			}
			mImgcompass.setImageResource(R.drawable.icon_compass_selected);
			mTitle.setText("发现");
			break;
		case TAB_SONGLIST:
			if (mTab05 == null)
			{
				mTab05 = new LocalFragment();
				transaction.add(R.id.id_content, mTab05);
			} else
			{
				transaction.show(mTab05);
			}
			resetImgs();
			setVisibility(TOP_MENU);
			mTitle.setText("本地音乐");
			break;
			
		default:
			break;
		}

		transaction.commit();
	}

	private void setVisibility(int pos) {
		switch (pos) {
		case TOP_MENU:
			mImgmenu.setVisibility(View.VISIBLE);
			mImgtopjump.setVisibility(View.GONE);
			break;

		case TOP_JUMP:
			mImgmenu.setVisibility(View.GONE);
			mImgtopjump.setVisibility(View.VISIBLE);
			break;
			
		default:
			break;
		}
	}

	private void hideFragment(FragmentTransaction transaction)
	{
		setVisibility(TOP_JUMP);
		if (mTab01 != null)
		{
			transaction.hide(mTab01);
		}
		if (mTab02 != null)
		{
			transaction.hide(mTab02);
		}
		if (mTab03 != null)
		{
			transaction.hide(mTab03);
		}
		if (mTab04 != null)
		{
			transaction.hide(mTab04);
		}
		if (mTab05 != null)
		{
			transaction.hide(mTab05);
			
		}
	}

	@Override
	public void onClick(View v)
	{
		resetImgs();
		switch (v.getId())
		{
		case R.id.id_tab_user:
			setSelect(TAB_USER);
			break;
		case R.id.id_tab_cd:
			setSelect(TAB_CD);
			break;
		case R.id.id_tab_search:
			setSelect(TAB_SEARCH);
			break;
		case R.id.id_tab_compass:
			setSelect(TAB_COMPASS);
			break;
		case R.id.id_tab_topjump:
			startActivity(new Intent(this, PlayActivity.class));
			break;
		case R.id.tv_pop_exit:
			stopService(new Intent(this, PlayService.class));
			//stopService(new Intent(this, DownloadService.class));
		case R.id.tv_pop_shutdown:
			finish();
		case R.id.tv_pop_cancel:
			if(mPopupWindow != null && mPopupWindow.isShowing()) mPopupWindow.dismiss();
			onPopupWindowDismiss();
	 		break;
		default:
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			if(mPopupWindow != null && mPopupWindow.isShowing()) {
				mPopupWindow.dismiss();
				return true;
			}
			
			onShowMenu();
			return true;
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public void onPopupWindowShown() {
		mPopShownView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.layer_show_anim));
		mPopShownView.setVisibility(View.VISIBLE);
	}
	
	public void onPopupWindowDismiss() {
		mPopShownView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.layer_gone_anim));
		mPopShownView.setVisibility(View.GONE);
	}
	
	private void onShowMenu() {
		onPopupWindowShown();
		if(mPopupWindow == null) {
			View view = View.inflate(this, R.layout.exit_pop_layout, null);
			View shutdown = view.findViewById(R.id.tv_pop_shutdown);
			View exit = view.findViewById(R.id.tv_pop_exit);
			View cancel = view.findViewById(R.id.tv_pop_cancel);
			
			// 不需要共享变量， 所以放这没事
			shutdown.setOnClickListener(this);
			exit.setOnClickListener(this);
			cancel.setOnClickListener(this);
			
			mPopupWindow = new PopupWindow(view,
					LinearLayout.LayoutParams.MATCH_PARENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);
			mPopupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
			mPopupWindow.setAnimationStyle(R.style.popwin_anim);
			mPopupWindow.setFocusable(true);
			mPopupWindow.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss() {
					onPopupWindowDismiss();
				}
			});
		}
		
		mPopupWindow.showAtLocation(getWindow().getDecorView(), 
				Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, 0);
	}
	
	/**
	 * 切换图片至暗色
	 */
	private void resetImgs()
	{
		mImguser.setImageResource(R.drawable.icon_user_normal);
		mImgcd.setImageResource(R.drawable.icon_cd_normal);
		mImgsearch.setImageResource(R.drawable.icon_search_normal);
		mImgcompass.setImageResource(R.drawable.icon_compass_normal);
	}

	public PlayService getPlayService() {
		return mPlayService;
	}
	
	@Override
	public void onPublish(int progress) {
		
	}

	@Override
	public void onChange(int position) {
		
	}
}
