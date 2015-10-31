package Utils;

import zjn.applicantion.App;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

public class MobileUtils {
	/**
	 * “˛≤ÿ ‰»Î∑®»Ìº¸≈Ã
	 * @param view attachview
	 * @see {@link InputMethodManager.hideSoftInputFromWindow}
	 */
	public static void hideInputMethod(View view) {
		InputMethodManager imm = (InputMethodManager) App.sContext
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm.isActive()) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
