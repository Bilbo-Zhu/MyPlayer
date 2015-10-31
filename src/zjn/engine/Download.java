package zjn.engine;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.os.Handler;
import android.os.Message;

import Utils.L;

public class Download implements Serializable{
	private static final long serialVersionUID = 0x00001000L;
	private static final int START = 1;					// ��ʼ����
	private static final int PUBLISH = 2;				// ���½���
	private static final int PAUSE = 3;					// ��ͣ����
	private static final int CANCEL = 4;				// ȡ������
	private static final int ERROR = 5;					// ���ش���
	private static final int SUCCESS = 6;				// ���سɹ�
	private static final int GOON = 7;  				// ��������
	
	private static final String UA = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/37.0.2041.4 Safari/537.36";
	
	private static ExecutorService mThreadPool;    		// �̳߳�
	
	static {
		mThreadPool = Executors.newFixedThreadPool(5);  // Ĭ��5�� 
	}
		
	private int mDownloadId;							// ����id
	private String mFileName; 							// ���ر����ļ���
	private String mUrl; 								// ���ص�ַ
	private String mLocalPath;							// ���ش��Ŀ¼

	private boolean isPause = false; 					// �Ƿ���ͣ
	private boolean isCanceled = false;					// �Ƿ��ֶ�ֹͣ����
	
	private OnDownloadListener mListener;     			// ������
	
	/**
	 * ���������̳߳صĴ�С
	 * @param maxSize ͬʱ���ص�����߳���
	 */
	public static void configDownloadTheadPool(int maxSize) {
		mThreadPool = Executors.newFixedThreadPool(maxSize);
	}
	
	/**
	 * �����������
	 * @param downloadId ���������id
	 * @param url        ���ص�ַ
	 * @param localPath	  ���ش�ŵ�ַ
	 */
	public Download(int downloadId, String url, String localPath) {
		if (!new File(localPath).getParentFile().exists()) {
			new File(localPath).getParentFile().mkdirs();
		}
		
		L.l("���ص�ַ", url);
		
		mDownloadId = downloadId;
		mUrl = url;
		String[] tempArray = url.split("/");
		mFileName = tempArray[tempArray.length-1];
		mLocalPath = localPath.replaceAll("\"|\\(|\\)", "");
	}
	
	/**
	 * ���ü�����
	 * @param listener �������ؼ�����
	 * @return this
	 */
	public Download setOnDownloadListener(OnDownloadListener listener) {
		mListener = listener;
		return this;
	}
	
	/**
	 * ��ȡ�ļ���
	 * @return �ļ���
	 */
	public String getFileName() {
		return mFileName;
	}
	
	public String getLocalFileName() {
		String[] split = mLocalPath.split(File.separator);
		return split[split.length-1];
	}

	/**
	 * ��ʼ����
	 * params isGoon�Ƿ�Ϊ��������
	 */
	public void start(final boolean isGoon) {
		// ������Ϣ
		final Handler handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case ERROR:
					mListener.onError(mDownloadId);
					break;
				case CANCEL:
					mListener.onCancel(mDownloadId);
					break;
				case PAUSE:
					mListener.onPause(mDownloadId);
					break;
				case PUBLISH:
					mListener.onPublish(mDownloadId, Long.parseLong(msg.obj.toString()));
					break;
				case SUCCESS:
					mListener.onSuccess(mDownloadId);
					break;
				case START:
					mListener.onStart(mDownloadId, Long.parseLong(msg.obj.toString()));
					break;
				case GOON:
					mListener.onGoon(mDownloadId, Long.parseLong(msg.obj.toString()));
					break;
				}
			}
		};
		
		// ������ʼ����
		mThreadPool.execute(new Runnable() {
			@Override
			public void run() {
				download(isGoon,handler);
			}
		});
	}
	
	/**
	 * ���ط���
	 * @param handler ��Ϣ������
	 */
	private void download(boolean isGoon, Handler handler) {
		Message msg = null;
		L.l("��ʼ���ء�����");
		try {
			RandomAccessFile localFile = new RandomAccessFile(new File(mLocalPath), "rwd");

			DefaultHttpClient client = new DefaultHttpClient();
			client.setParams(getHttpParams());
			HttpGet get = new HttpGet(mUrl);

			long localFileLength = getLocalFileLength();
			final long remoteFileLength = getRemoteFileLength();
			long downloadedLength = localFileLength;
			
			// Զ���ļ�������
			if (remoteFileLength == -1l) {
				L.l("�����ļ�������...");
				localFile.close();
				handler.sendEmptyMessage(ERROR);
				return;
			}

			// �����ļ�����
			if (localFileLength > -1l && localFileLength < remoteFileLength) {
				L.l("�����ļ�����...");
				localFile.seek(localFileLength);
				get.addHeader("Range", "bytes=" + localFileLength + "-"
						+ remoteFileLength);
			}
			
			msg = Message.obtain();
			
			// ������Ǽ�������
			if(!isGoon) {
				// ���Ϳ�ʼ���ص���Ϣ����ȡ�ļ���С����Ϣ
				msg.what = START;
				msg.obj = remoteFileLength;
			}else {
				msg.what = GOON;
				msg.obj = localFileLength;
			}
			
			handler.sendMessage(msg);
			
			HttpResponse response = client.execute(get);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode >= 200 && httpCode <= 300) {
				InputStream in = response.getEntity().getContent();
				byte[] bytes = new byte[1024];
				int len = -1;
				while (-1 != (len = in.read(bytes))) {
					localFile.write(bytes, 0, len);
					downloadedLength += len;
//					Log.log((int)(downloadedLength/(float)remoteFileLength * 100));
					if ((int)(downloadedLength/(float)remoteFileLength * 100) % 10 == 0) {
						// ���͸��½��ȵ���Ϣ
						handler.obtainMessage(PUBLISH, downloadedLength).sendToTarget();
//						Log.log(mDownloadId + "������" + downloadedLength);
					}
					
					// ��ͣ���أ� �˳�����
					if (isPause) {
						// ������ͣ����Ϣ
						handler.sendEmptyMessage(PAUSE);
						L.l("������ͣ...");
						break;
					}
					
					// ȡ�����أ� ɾ���ļ����˳�����
					if (isCanceled) {
						L.l("�ֶ��ر����ء���");
						localFile.close();
						client.getConnectionManager().shutdown();
						new File(mLocalPath).delete();
						// ����ȡ�����ص���Ϣ
						handler.sendEmptyMessage(CANCEL);
						return;
					}
				}

				localFile.close();
				client.getConnectionManager().shutdown();
				// ����������ϵ���Ϣ
				if(!isPause) handler.sendEmptyMessage(SUCCESS);
			}
		} catch (Exception e) {
			e.printStackTrace();
			// �������ش������Ϣ
			handler.sendEmptyMessage(ERROR);
		}
	}

	/**
	 * ��ͣ/��������
	 * param pause �Ƿ���ͣ����
	 * ��ͣ return true
	 * ���� return false
	 */
	public synchronized boolean pause(boolean pause) {
		if(!pause) {
			L.l("��������");
			isPause = false;
			start(true); // ��ʼ����
		}else {
			L.l("��ͣ����");
			isPause = true;
		}
		return isPause;
	}

	/**
	 * �ر����أ� ��ɾ���ļ�
	 */
	public synchronized void cancel() {
		isCanceled = true;
		if(isPause) {
			new File(mLocalPath).delete();
		}
	}

	/**
	 * ��ȡ�����ļ���С
	 * @return �����ļ��Ĵ�С or �����ڷ���-1
	 */
	public synchronized long getLocalFileLength() {
		long size = -1l;
		File localFile = new File(mLocalPath);
		if (localFile.exists()) {
			size = localFile.length();
		}
		L.l("�����ļ���С" + size);
		return size <= 0 ? -1l : size;
	}

	/**
	 * ��ȡԶ���ļ����� or �����ڷ���-1
	 * @return
	 */
	public synchronized long getRemoteFileLength() {
		long size = -1l;
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			client.setParams(getHttpParams());
			HttpGet get = new HttpGet(mUrl);

			HttpResponse response = client.execute(get);
			int httpCode = response.getStatusLine().getStatusCode();
			if (httpCode >= 200 && httpCode <= 300) {
				size = response.getEntity().getContentLength();
			}

			client.getConnectionManager().shutdown();
		} catch (Exception e) {
			e.printStackTrace();
		}

		L.l("Զ���ļ���С" + size);
		return size;
	}

	/**
	 * ����http���� ��������soTimeout
	 * @return HttpParams http����
	 */
	private static HttpParams getHttpParams() {
		HttpParams params = new BasicHttpParams();

		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpProtocolParams.setUseExpectContinue(params, true);
		HttpProtocolParams.setUserAgent(params, UA);
//		ConnManagerParams.setTimeout(params, 10000);
//		HttpConnectionParams.setConnectionTimeout(params, 10000);
		
		return params;
	}
	
	/**
	 * �ر������̳߳�
	 */
	public static void closeDownloadThread() {
		if(null != mThreadPool) {
			mThreadPool.shutdownNow();
		}
	}

	public interface OnDownloadListener {
		public void onStart(int downloadId, long fileSize);  // �ص���ʼ����
		public void onPublish(int downloadId, long size);	 // �ص����½���
		public void onSuccess(int downloadId);				 // �ص����سɹ�
		public void onPause(int downloadId); 				 // �ص���ͣ
		public void onError(int downloadId);				 // �ص����س���
		public void onCancel(int downloadId);			     // �ص�ȡ������
		public void onGoon(int downloadId, long localSize);  // �ص���������
	}
}
