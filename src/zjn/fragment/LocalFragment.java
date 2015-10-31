package zjn.fragment;

import java.io.File;

import zjn.adapter.MusicListAdapter;
import zjn.info.Music;
import zjn.mymusic.MainActivity;
import zjn.mymusic.PlayActivity;
import zjn.mymusic.R;
import Utils.MusicUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;

public class LocalFragment extends Fragment implements OnItemClickListener{
	private ListView mListView;
	private MainActivity mActivity;
	private MusicListAdapter mMusicListAdapter = new MusicListAdapter();
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		
		registerReceiver();
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity) activity;
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View mView = inflater.inflate(R.layout.tab05, container, false);
		setupViews(mView);

		return mView;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		mActivity.allowBindService();
	}
	
	@Override
	public void onStop() {
		super.onStop();
		mActivity.allowUnbindService();
	}

	/**
	 * 注册广播接收器
	 * 在下载歌曲完成或删除歌曲时，更新歌曲列表
	 */
	private void registerReceiver() {
		IntentFilter filter = new IntentFilter( Intent.ACTION_MEDIA_SCANNER_STARTED);
		filter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
		filter.addDataScheme("file");
		mActivity.registerReceiver(mScanSDCardReceiver, filter);
	}
	
	private void setupViews(View mView) {
		mListView = (ListView) mView.findViewById(R.id.lv_music_list);
		mListView.setAdapter(mMusicListAdapter);
		
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(mItemLongClickListener);
	}
	
	private OnItemLongClickListener mItemLongClickListener = 
			new OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			final int pos = position;

			AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
			builder.setTitle("删除该条目");
			builder.setMessage("确认要删除该条目吗?");
			builder.setPositiveButton("删除",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Music music = MusicUtils.sMusicList.remove(pos);
						mMusicListAdapter.notifyDataSetChanged();
						if (new File(music.getUri()).delete()) {
							scanSDCard();
						}
					}
				});
			builder.setNegativeButton("取消", null);
			builder.create().show();
			return true;
		}
	};
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(mActivity, PlayActivity.class);
		intent.putExtra("pos", position);
		startActivity(intent);
		play(position);
	}

	private void scanSDCard() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			// 判断SDK版本是不是4.4或者高于4.4
			String[] paths = new String[]{
					Environment.getExternalStorageDirectory().toString()};
			MediaScannerConnection.scanFile(mActivity, paths, null, null);
		} else {
			Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
			intent.setClassName("com.android.providers.media",
					"com.android.providers.media.MediaScannerReceiver");
			intent.setData(Uri.parse("file://"+ MusicUtils.getMusicDir()));
			mActivity.sendBroadcast(intent);
		}
	}
	
	/**
	 * 播放时高亮当前播放条目
	 * 实现播放的歌曲条目可见，且实现指示标记可见
	 * @param position
	 */
	private void onItemPlay(int position) {
		// 将ListView列表滑动到播放的歌曲的位置，是播放的歌曲可见
		mListView.smoothScrollToPosition(position);
		// 获取上次播放的歌曲的position
		int prePlayingPosition = mMusicListAdapter.getPlayingPosition();
		// 如果上次播放的位置在可视区域内
		// 则手动设置invisible
		if (prePlayingPosition >= mListView.getFirstVisiblePosition()
				&& prePlayingPosition <= mListView
						.getLastVisiblePosition()) {
			int preItem = prePlayingPosition
					- mListView.getFirstVisiblePosition();
			((ViewGroup) mListView.getChildAt(preItem)).getChildAt(0)
					.setVisibility(View.INVISIBLE);
		}

		// 设置新的播放位置
		mMusicListAdapter.setPlayingPosition(position);

		// 如果新的播放位置不在可视区域
		// 则直接返回
		if (mListView.getLastVisiblePosition() < position
				|| mListView.getFirstVisiblePosition() > position)
			return;

		// 如果在可视区域
		// 手动设置改item visible
		int currentItem = position - mListView.getFirstVisiblePosition();
		((ViewGroup) mListView.getChildAt(currentItem)).getChildAt(0)
				.setVisibility(View.VISIBLE);
	}
	
	private void play(int position) {
		int pos = mActivity.getPlayService().play(position);
		onPlay(pos);
	}

	private void onPlay(int pos) {
		onItemPlay(pos);
		//新启动一个线程更新通知栏，防止更新时间过长，导致界面卡顿！
				new Thread(){
					@Override
					public void run() {
						super.run();
						mActivity.getPlayService().setRemoteViews();
					}
				}.start();
	}
	
	private BroadcastReceiver mScanSDCardReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			//L.l(mActivity.class.getSimpleName(), "mScanSDCardReceiver---->onReceive()");
			if(intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
				MusicUtils.initMusicList();
				onMusicListChanged();
			}
		}
	};
	
	/**
	 * 主界面MainActivity.java中调用更新歌曲列表
	 */
	public void onMusicListChanged() {
		mMusicListAdapter.notifyDataSetChanged();
	}
	
	@Override
	public void onDestroy() {
		mActivity.unregisterReceiver(mScanSDCardReceiver);
		super.onDestroy();
	}
}
