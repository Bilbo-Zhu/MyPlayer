package Utils;

import java.util.ArrayList;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import zjn.applicantion.App;
import zjn.info.Music;

public class LocalMusicUtils {
	/**
	 * ����id��ȡ����uri
	 * @deprecated
	 * @param musicId
	 * @return
	 */
	public static String queryMusicById(int musicId) {
		String result = null;
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				new String[] { MediaStore.Audio.Media.DATA },
				MediaStore.Audio.Media._ID + "=?",
				new String[] { String.valueOf(musicId) }, null);

		for (cursor.moveToFirst(); !cursor.isAfterLast();) {
			result = cursor.getString(0);
			break;
		}

		cursor.close();
		return result;
	}

	/**
	 * ��ȡĿ¼�µĸ���
	 * @param dirName
	 */
	public static ArrayList<Music> queryMusic(String dirName) {
		ArrayList<Music> results = new ArrayList<Music>();
		Cursor cursor = App.sContext.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null,
				MediaStore.Audio.Media.DATA + " like ?",
				new String[] { dirName + "Music/" + "%" },
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		if(cursor == null) return results;
		
		// id title singer data time image
		Music music;
		for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
			// �����������
			String isMusic = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC));
			if (isMusic != null && isMusic.equals("")) continue;
			
			String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
			String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
			
			if(isRepeat(title, artist)) continue;
			
			music = new Music();
			music.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)));
			music.setTitle(title.toString());
			music.setArtist(artist.toString());
			music.setUri(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)));
			music.setLength(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			music.setImage(getAlbumImage(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))));
			results.add(music);
		}

		cursor.close();
		return results;
	}
	
	/**
	 * �����������ƺ����������ж��Ƿ��ظ�������
	 * @param title
	 * @param artist
	 * @return
	 */
	private static boolean isRepeat(String title, String artist) {
		for(Music music : MusicUtils.sMusicList) {
			if(title.equals(music.getTitle()) && artist.equals(music.getArtist())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * ���ݸ���id��ȡͼƬ
	 * @param albumId
	 * @return
	 */
	private static String getAlbumImage(int albumId) {
		String result = "";
		Cursor cursor = null;
		try {
			cursor = App.sContext.getContentResolver().query(
					Uri.parse("content://media/external/audio/albums/"
							+ albumId), new String[] { "album_art" }, null,
					null, null);
			for (cursor.moveToFirst(); !cursor.isAfterLast();) {
				result = cursor.getString(0);
				break;
			}
		} catch (Exception e) {
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}

		return null == result ? null : result;
//			ImageTools.scaleBitmap(result);
	}
}
