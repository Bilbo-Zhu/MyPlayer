package zjn.engine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import Utils.Constants;
import Utils.L;
import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class GetDownloadInfo {
	private static final int GET_MUSIC_SUCCESS = 5;
	private static final int GET_LRC_SUCCESS = 6;
	private static final int GET_MUSIC_FAILED = 7;
	private static final int GET_LRC_FAILED = 8;
	private static final String TAG = GetDownloadInfo.class.getSimpleName();

	private static GetDownloadInfo sInstance;
	private ExecutorService mThreadPool;
	private Handler mHandler;
	private OnDownloadGettedListener mListener;

	/**
	 * ������
	 * 
	 * @return
	 */
	public static GetDownloadInfo getInstance() {
		if (sInstance == null)
			sInstance = new GetDownloadInfo();
		return sInstance;
	}

	@SuppressLint("HandlerLeak")
	private GetDownloadInfo() {
		/*
		 * newCachedThreadPool()����һ���ɸ�����Ҫ�������̵߳��̳߳أ�
		 * ��������ǰ������߳̿���ʱ���������ǡ�����ִ�кܶ�����첽����ĳ������
		 * �� ��Щ�̳߳�ͨ������߳������ܡ����� execute
		 * ��������ǰ������̣߳�����߳̿��ã��� ��������߳�û�п��õģ�
		 * �򴴽�һ�����̲߳���ӵ����С���ֹ���ӻ������Ƴ���Щ���� 60
		 * ����δ��ʹ�õ��̡߳���ˣ���ʱ�䱣�ֿ��е��̳߳ز���ʹ���κ���Դ��
		 *  ע�⣬����ʹ�� ThreadPoolExecutor
		 * ���췽�����������������Ե�ϸ�ڲ�ͬ�����糬ʱ���������̳߳ء�
		 */
		mThreadPool = Executors.newCachedThreadPool();

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case GET_MUSIC_SUCCESS:
					if (mListener != null)
						mListener.onMusic(msg.arg1, (String) msg.obj);
					break;
				case GET_MUSIC_FAILED:
					if (mListener != null)
						mListener.onMusic(-1, null);
					break;
				case GET_LRC_SUCCESS:
					if (mListener != null)
						mListener.onLrc(msg.arg1, (String) msg.obj);
					break;
				case GET_LRC_FAILED:
					if (mListener != null)
						mListener.onLrc(-1, null);
					break;
				}
			}
		};
	}

	/**
	 * ���ûص��ӿڵĶ���ķ��� �÷������ⲿ���ã�
	 * ��ʼ�������е�OnDownloadGettedListener�����mListener
	 * 
	 * @param l
	 * @return
	 */
	public GetDownloadInfo setListener(OnDownloadGettedListener l) {
		mListener = l;
		return this;
	}

	// �����µ��̼߳����̳߳�ִ������
	public void parse(final int position, final String url) {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				L.l(TAG, "postion=" + position + ",url=" + url);
				String songId = getSongId(url);
				L.l(TAG, "�������ĸ���ID =" + songId);
				getDownloadUrl(position, songId);
				getLrcUrl(position, url);
			}
		});
	}

	private String getSongId(String url) {
		String temp = url.replaceFirst("/song/", "");
		if (!temp.contains("/"))
			return temp;
		return temp.substring(0, temp.indexOf("/"));
	}

	private void getLrcUrl(final int position, final String song) {
		String url = Constants.MUSIC_URL + song;
		Log.i(TAG, "���ص�ַ=" + url);
		try {
			Document doc = Jsoup
					.connect(url)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64)" +
							" AppleWebKit/537.36"
									+ " (KHTML, like Gecko)" +
									" Chrome/42.0.2311.22 Safari/537.36")
					.timeout(3000).get();
			Elements elements = doc.select(".down-lrc-btn");
			if (elements.size() <= 0)
				throw new Exception();
			String json = elements.get(0).attr("data-lyricdata");
			JSONObject jsonObject = new JSONObject(json);
			String result = jsonObject.getString("href");

			Message msg = mHandler.obtainMessage(GET_LRC_SUCCESS, result);
			msg.arg1 = position;
			msg.sendToTarget();
		} catch (Exception e) {
			mHandler.sendEmptyMessage(GET_LRC_FAILED);
			e.printStackTrace();
		}
	}

	private void getDownloadUrl(final int position, final String songId) {
		// http://music.baidu.com/song/241873/download?__o=%2Fsearch%2Fsong
		String url = Constants.MUSIC_URL + "/song/" + songId
				+ "/download?__o=%2Fsearch%2Fsong";
		try {
			Document doc = Jsoup
					.connect(url)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64)" +
							" AppleWebKit/537.36"
									+ " (KHTML, like Gecko)" +
									" Chrome/42.0.2311.22 Safari/537.36")
					.timeout(60 * 1000).get();
			Elements targetElements = doc.select("a[data-btndata]");
			if (targetElements.size() <= 0)
				throw new Exception();
			for (Element e : targetElements) {
				if (e.attr("href").contains(".mp3")) {
					String result = e.attr("href");
					Message msg = mHandler.obtainMessage(GET_MUSIC_SUCCESS,
							result);
					msg.arg1 = position;
					msg.sendToTarget();
					return;
				}

				if (e.attr("href").startsWith("/vip")) {
					targetElements.remove(e);
				}
			}

			if (targetElements.size() <= 0)
				throw new Exception();

			String result = targetElements.get(0).attr("href");
			Message msg = mHandler.obtainMessage(GET_MUSIC_SUCCESS, result);
			msg.arg1 = position;
			msg.sendToTarget();
		} catch (Exception e) {
			mHandler.sendEmptyMessage(GET_MUSIC_FAILED);
			e.printStackTrace();
		}
	}

	/**
	 * �ص��ӿ� ����ɸ����ļ��͸���ļ�������֮�󣬻ص���Ӧ�ķ���������ݵĸ���
	 */
	public interface OnDownloadGettedListener {
		public void onMusic(int position, String url);

		public void onLrc(int position, String url);
	}
}
