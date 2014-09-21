package cn.edu.hit.pt;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class WelcomePage extends Fragment{
	
	public static WelcomePage newInstance(int page) {
		WelcomePage f = new WelcomePage();
		Bundle bundle = new Bundle();
		bundle.putInt("page", page);
		f.setArguments(bundle);
		return f;
	}
	
	public int getArgumentsType() {
		return getArguments().getInt("page", 0);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		int page = getArgumentsType();
		int resource = R.layout.welcome_p1;
		switch (page) {
			case 1:
				resource = R.layout.welcome_p1;
				break;
			case 2:
				resource = R.layout.welcome_p2;
				break;
			case 3:
				resource = R.layout.welcome_p3;
				break;
			case 4:
				resource = R.layout.welcome_p4;
				break;
	
			default:
				resource = R.layout.welcome_p1;
				break;
		}
		
		View mView = inflater.inflate(resource, null);
		if(page == 4){
			Button btnStart = (Button)mView.findViewById(R.id.btnStart);
			btnStart.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					getActivity().finish();
				}
			});
		}
		return mView;
	}
}
