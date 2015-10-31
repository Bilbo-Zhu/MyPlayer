package zjn.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import zjn.info.SearchResult;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import Utils.Constants;

public class SongsRecommendation {
	private static final String URL = Constants.MUSIC_URL
			+ "/top/new/?pst=shouyeTop";
	private static SongsRecommendation sInstance;
	/**
	 * �ص��ӿڣ��������ݸ�Activity����Fragment
	 * �ǳ����õ����ݴ��ݷ�ʽ
	 */
	private OnRecommendationListener mListener;

	private ExecutorService mThreadPool;

	public static SongsRecommendation getInstance() {
		if (sInstance == null)
			sInstance = new SongsRecommendation();
		return sInstance;
	}

	private Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case Constants.SUCCESS:
				if (mListener != null)
					mListener
					.onRecommend((ArrayList<SearchResult>) msg.obj);
				break;
			case Constants.FAILED:
				if (mListener != null)
					mListener.onRecommend(null);
				break;
			}
		}
	};
	
	@SuppressLint("HandlerLeak")
	private SongsRecommendation() {
		// �������̳߳�
		mThreadPool = Executors.newSingleThreadExecutor();
	}

	/**
	 * ���ûص��ӿ�OnRecommendationListener��Ķ���mListener
	 * 
	 * @param l
	 * @return
	 */
	public SongsRecommendation setListener(OnRecommendationListener l) {
		mListener = l;
		return this;
	}
	/**
	 * ����ִ����ҳ�����ķ���
	 * �̳߳��п����µ��߳�ִ�н������������֮������Ϣ
	 * ��������ݵ����߳���
	 */
	public void get() {
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				ArrayList<SearchResult> result = getMusicList();
				if (result == null) {
					mHandler.sendEmptyMessage(Constants.FAILED);
					return;
				}
				mHandler.obtainMessage(Constants.SUCCESS, result)
						.sendToTarget();
			}
		});
	}

	private ArrayList<SearchResult> getMusicList() {
		try {
			/**
			 * һ�·���������ο�����
			 * ˵����timeout��������ʱ�䣬���˹��̡�
			 * ʱ����̵����쳣���޷���ȡ��
			 */
			Document doc = Jsoup
					.connect(URL)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64)" +
							" AppleWebKit/537.36"
									+ " (KHTML, like Gecko)" +
									" Chrome/42.0.2311.22 Safari/537.36")
					.timeout(60 * 1000).get();
			//selectΪѡ��������ο�����˵��
			Elements songTitles = doc.select("span.song-title");
			Elements artists = doc.select("span.author_list");
			ArrayList<SearchResult> searchResults = new ArrayList<SearchResult>();

			for (int i = 0; i < songTitles.size(); i++) {
				SearchResult searchResult = new SearchResult();
				Elements urls = songTitles.get(i).getElementsByTag("a");
				searchResult.setUrl(urls.get(0).attr("href"));
				searchResult.setMusicName(urls.get(0).text());

				Elements artistElements = artists.get(i).getElementsByTag("a");
				searchResult.setArtist(artistElements.get(0).text());
				searchResult.setAlbum("�����Ƽ�");
				searchResults.add(searchResult);
			}
			return searchResults;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * �ص��ӿ� ��ȡ����֮��ͨ���ýӿ��������ݴ���
	 */
	public interface OnRecommendationListener {
		public void onRecommend(ArrayList<SearchResult> results);
	}
}
