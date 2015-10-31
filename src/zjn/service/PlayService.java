package zjn.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zjn.mymusic.PlayActivity;
import zjn.mymusic.R;

import Utils.Constants;
import Utils.ImageTools;
import Utils.L;
import Utils.MusicIconLoader;
import Utils.MusicUtils;
import Utils.SpUtils;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.RemoteViews;

public class PlayService extends Service implements OnCompletionListener {
	private static final String TAG =
			PlayService.class.getSimpleName();
	
	private MediaPlayer mPlayer;
	
	private int mPlayingPosition;
	private RemoteViews remoteViews;//֪ͨ������
	private Notification notification;//֪ͨ��
	private OnMusicEventListener mListener;
	private NotificationManager notificationManager;
	
	private Boolean readyNotification = false;
	
	private ExecutorService mProgressUpdatedListener = Executors.newSingleThreadExecutor();
	
	public class PlayBinder extends Binder {
		public PlayService getService() {
			return PlayService.this;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		MusicUtils.initMusicList();
		mPlayingPosition = (Integer) SpUtils.get(this, Constants.PLAY_POS, 0);
		mPlayer = new MediaPlayer();
		mPlayer.setOnCompletionListener(this);
		
		// ��ʼ���½��ȵ��߳�
		mProgressUpdatedListener.execute(mPublishProgressRunnable);
		
		if(MusicUtils.sMusicList.size() != 0)
		{
			startNotification();
			readyNotification = true;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new PlayBinder();
	}
	
	/**
	 * ���½��ȵ��߳�
	 */
	private Runnable mPublishProgressRunnable = new Runnable() {
		@Override
		public void run() {
			for(;;) {
				if(mPlayer != null && mPlayer.isPlaying() && 
						mListener != null) {
					mListener.onPublish(mPlayer.getCurrentPosition());
				}
				
				SystemClock.sleep(200);
			}
		}
	};
	
	private void startNotification() {
		/**
		 * �÷�����Ȼ��������ʱ������ͨ�ã�
		 */
		PendingIntent pendingIntent = PendingIntent
				.getActivity(PlayService.this,
				0, new Intent(PlayService.this, PlayActivity.class), 0);
		remoteViews = new RemoteViews(getPackageName(),
				R.layout.play_notification);
		notification = new Notification(R.drawable.icon,
				"�������ڲ���", System.currentTimeMillis());
		notification.contentIntent = pendingIntent;
		notification.contentView = remoteViews;
		//���λ������֪ͨ��һֱ����
		notification.flags =Notification.FLAG_ONGOING_EVENT;
		
		Intent intent = new Intent(PlayService.class.getSimpleName());
		intent.putExtra("BUTTON_NOTI", 1);
		PendingIntent preIntent = PendingIntent.getBroadcast(
				PlayService.this,
				1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_pre, preIntent);
		
		intent.putExtra("BUTTON_NOTI", 2);
		PendingIntent pauseIntent = PendingIntent.getBroadcast(
				PlayService.this,
				2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_pause, pauseIntent);
		
		intent.putExtra("BUTTON_NOTI", 3);
		PendingIntent nextIntent = PendingIntent.getBroadcast
				(PlayService.this,
				3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_next, nextIntent);
		
		intent.putExtra("BUTTON_NOTI", 4);
		PendingIntent exit = PendingIntent.getBroadcast(PlayService.this,
				4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(
				R.id.music_play_notifi_exit, exit);
		
		notificationManager = (NotificationManager)
				getSystemService(NOTIFICATION_SERVICE);
		setRemoteViews();
		
		/**
		 * ע��㲥������
		 * ���ܣ�
		 * ����֪ͨ����ť����¼� 
		 */
		IntentFilter filter = new IntentFilter(
				PlayService.class.getSimpleName());
		MyBroadCastReceiver receiver = new MyBroadCastReceiver();
		registerReceiver(receiver, filter);
	}
	
	/**
	 * ����
	 * @param position �����б��λ��
	 * @return ��ǰ���ŵ�λ��
	 */
	public int play(int position) {
		if(position < 0) position = 0;
		if(position >= MusicUtils.sMusicList.size()) position = MusicUtils.sMusicList.size() - 1;
		
		try {
			mPlayer.reset();
			
			mPlayer.setDataSource(MusicUtils.sMusicList.get(position).getUri());
			mPlayer.prepare();
			
			start();
			if(mListener != null) mListener.onChange(position);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		mPlayingPosition = position;
		SpUtils.put(Constants.PLAY_POS, mPlayingPosition);
		if(!readyNotification){
			startNotification();
		}else{
			setRemoteViews();
		}
		return mPlayingPosition;
	}

	private void start() {
		mPlayer.start();
	}
	
	/**
	 * �Ƿ����ڲ���
	 * @return
	 */
	public boolean isPlaying() {
		return mPlayer != null&& mPlayer.isPlaying(); 
	}
	
	/**
	 * ��������
	 * @return ��ǰ���ŵ�λ�� Ĭ��Ϊ0
	 */
	public int resume() {
		if(isPlaying()){
			return -1;
		}else if(mPlayingPosition <= 0 || mPlayingPosition >= MusicUtils.sMusicList.size()){
			mPlayingPosition = 0;
			play(mPlayingPosition);
			setRemoteViews();
			return mPlayingPosition;
		}else{
			mPlayer.start();
			setRemoteViews();
			return mPlayingPosition;
		}
	}
	
	/**
	 * ��ͣ����
	 * @return ��ǰ���ŵ�λ��
	 */
	public int pause() {
		if(!isPlaying()) return -1;
		mPlayer.pause();
		setRemoteViews();
		return mPlayingPosition;
	}
	
	/**
	 * ��һ��
	 * @return ��ǰ���ŵ�λ��
	 */
	public int next() {
		if(mPlayingPosition >= MusicUtils.sMusicList.size() - 1) {
			return play(0);
		}
		setRemoteViews();
		return play(mPlayingPosition + 1);
	}
	
	/**
	 * ��һ��
	 * @return ��ǰ���ŵ�λ��
	 */
	public int pre() {
		if(mPlayingPosition <= 0) {
			return play(MusicUtils.sMusicList.size() - 1);
		}
		setRemoteViews();
		return play(mPlayingPosition - 1);
	}

	/**
	 * ��ȡ���ڲ��ŵ�λ��
	 * @return
	 */
	public int getPlayingPosition() {
		return mPlayingPosition;
	}
	
	/**
	 * ���ûص�
	 * @param l
	 */
	public void setOnMusicEventListener(OnMusicEventListener l) {
		mListener = l;
	}
	
	@Override
	public void onCompletion(MediaPlayer mPlayer) {
		next();
	}
	
	/**
	 * ���ֲ��Żص��ӿ�
	 */
	public interface OnMusicEventListener {
		public void onPublish(int percent);
		public void onChange(int position);
	}

	public void seek(int progress) {
		if(!isPlaying()) return;
		mPlayer.seekTo(progress);
	}
	
	public void setRemoteViews(){
		L.l(TAG, "���롪����setRemoteViews()");
		remoteViews.setTextViewText(R.id.music_name,
				MusicUtils.sMusicList.get(
						getPlayingPosition()).getTitle());
		remoteViews.setTextViewText(R.id.music_author,
				MusicUtils.sMusicList.get(
						getPlayingPosition()).getArtist());
		Bitmap icon = MusicIconLoader.getInstance().load(
				MusicUtils.sMusicList.get(
						getPlayingPosition()).getImage());
		remoteViews.setImageViewBitmap(R.id.music_icon,icon == null
				? ImageTools.scaleBitmap(R.drawable.icon)
						: ImageTools
				.scaleBitmap(icon));
		if (isPlaying()) {
			remoteViews.setImageViewResource(R.id.music_play_pause,
					R.drawable.btn_notification_player_stop_normal);
		}else {
			remoteViews.setImageViewResource(R.id.music_play_pause,
					R.drawable.btn_notification_player_play_normal);
		}
		//֪ͨ������
		notificationManager.notify(5, notification);
	}

	private class MyBroadCastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					PlayService.class.getSimpleName())) {
				L.l(TAG, "MyBroadCastReceiver�ࡪ����onReceive����");
				L.l(TAG, "button_noti-->"
				+intent.getIntExtra("BUTTON_NOTI", 0));
				switch (intent.getIntExtra("BUTTON_NOTI", 0)) {
				case 1:
					pre();
					break;
				case 2:
					if (isPlaying()) {
						pause(); // ��ͣ
					} else {
						resume(); // ����
					}
					break;
				case 3:
					next();
					break;
				case 4:
					if (isPlaying()) {
						pause();
					}
					//ȡ��֪ͨ��
					notificationManager.cancel(5);
					break;
				default:
					break;
				}
			}
			if (mListener != null) {
				mListener.onChange(getPlayingPosition());
			}
		}
	}
	
}
