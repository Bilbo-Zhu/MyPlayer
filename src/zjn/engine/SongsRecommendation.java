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
	 * 回调接口，传递数据给Activity或者Fragment
	 * 非常好用的数据传递方式
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
		// 创建单线程池
		mThreadPool = Executors.newSingleThreadExecutor();
	}

	/**
	 * 设置回调接口OnRecommendationListener类的对象mListener
	 * 
	 * @param l
	 * @return
	 */
	public SongsRecommendation setListener(OnRecommendationListener l) {
		mListener = l;
		return this;
	}
	/**
	 * 真正执行网页解析的方法
	 * 线程池中开启新的线程执行解析，解析完成之后发送消息
	 * 将结果传递到主线程中
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
			 * 一下方法调用请参考官网
			 * 说明：timeout设置请求时间，不宜过短。
			 * 时间过短导致异常，无法获取。
			 */
			Document doc = Jsoup
					.connect(URL)
					.userAgent(
							"Mozilla/5.0 (Windows NT 6.1; Win64; x64)" +
							" AppleWebKit/537.36"
									+ " (KHTML, like Gecko)" +
									" Chrome/42.0.2311.22 Safari/537.36")
					.timeout(60 * 1000).get();
			//select为选择器，请参考官网说明
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
				searchResult.setAlbum("最新推荐");
				searchResults.add(searchResult);
			}
			return searchResults;
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * 回调接口 获取数据之后，通过该接口设置数据传递
	 */
	public interface OnRecommendationListener {
		public void onRecommend(ArrayList<SearchResult> results);
	}
}
