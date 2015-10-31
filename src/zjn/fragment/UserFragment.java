package zjn.fragment;

import zjn.mymusic.MainActivity;
import zjn.mymusic.R;
import Utils.MusicUtils;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class UserFragment extends Fragment implements OnClickListener{
	private RelativeLayout mTablocalmusic;
	private TextView songSize;
	private MainActivity mActivity;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mActivity = (MainActivity)activity;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState)
	{
		View mView = inflater.inflate(R.layout.tab01, container, false);
		initView(mView);
		initEvents();
		return mView;
	}

	private void initEvents() {
		mTablocalmusic.setOnClickListener(this);
		//songSize.setText(MusicUtils.sMusicList.size());
	}

	private void initView(View mView) {
		mTablocalmusic = (RelativeLayout) mView.findViewById(R.id.id_tab_localmusic);
		songSize = (TextView) mView.findViewById(R.id.song_size);
	}

	@Override
	public void onClick(View mView) {
		switch (mView.getId()) {
		case R.id.id_tab_localmusic:
			mActivity.setSelect(4);
			break;

		default:
			break;
		}
	}
}
