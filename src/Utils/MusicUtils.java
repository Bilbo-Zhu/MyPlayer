package Utils;

import java.io.File;
import java.util.ArrayList;

import android.os.Environment;

import zjn.applicantion.App;
import zjn.info.Music;

public class MusicUtils {
	// ��Ÿ����б�
		public static ArrayList<Music> sMusicList = new ArrayList<Music>();

		public static void initMusicList() {
			// ��ȡ�����б�
			sMusicList.clear();
			sMusicList.addAll(LocalMusicUtils.queryMusic(getBaseDir()));
		}

		/**
		 * ��ȡ�ڴ濨��
		 * @return
		 */
		public static String getBaseDir() {
			String dir = null;
			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
				dir = Environment.getExternalStorageDirectory() + File.separator;
			} else {
				dir = App.sContext.getFilesDir() + File.separator;
			}

			return dir;
		}

		/**
		 * ��ȡӦ�ó���ʹ�õı���Ŀ¼
		 * @return
		 */
		public static String getAppLocalDir() {
			String dir = null;

			if (!Environment.getExternalStorageState().equals(Environment.MEDIA_UNMOUNTED)) {
				dir = Environment.getExternalStorageDirectory() + File.separator
						+ "Music" + File.separator;
			} else {
				dir = App.sContext.getFilesDir() + File.separator + "Music" + File.separator;
			}

			return mkdir(dir);
		}

		/**
		 * ��ȡ���ִ��Ŀ¼
		 * @return
		 */
		public static String getMusicDir() {
			String musicDir = getAppLocalDir();
			return mkdir(musicDir);
		}

		/**
		 * ��ȡ��ʴ��Ŀ¼
		 * 
		 * @return
		 */
		public static String getLrcDir() {
			String lrcDir = getAppLocalDir();
			return mkdir(lrcDir);
		}

		/**
		 * �����ļ���
		 * @param dir
		 * @return
		 */
		public static String mkdir(String dir) {
			File f = new File(dir);
			if (!f.exists()) {
				for (int i = 0; i < 5; i++) {
					if(f.mkdirs()) return dir;
				}
				return null;
			}
			
			return dir;
		}
}
