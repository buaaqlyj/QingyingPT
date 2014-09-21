package cn.edu.hit.pt.impl;

import java.io.File;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.TextView;
import cn.edu.hit.pt.R;
import cn.edu.hit.pt.widget.CustomToast;

public class CacheCleaner extends AsyncTask<TextView, Long, Void>{
	private TextView tv;
	private Context context;
	
	public CacheCleaner(Context context) {
		this.context = context;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		new CustomToast(context, CustomToast.TYPE_SUCCESS)
		.setTitle(context.getString(R.string.operation_success))
		.setText(context.getString(R.string.cache_removed)).create().show();
    	tv.setText("");
		super.onPostExecute(result);
	}

	@Override
	protected Void doInBackground(TextView... params) {
		tv = params[0];
		if(!DirectoryUtil.cacheDirectory.equals("")){
			deleteFolderFile(DirectoryUtil.cacheDirectory, true);
			DirectoryUtil.checkdir(context);
		}
		return null;
	}

    public void deleteFolderFile(String filePath, boolean deleteThisPath){
        if (!TextUtils.isEmpty(filePath)) {
            File file = new File(filePath);

            if (file.isDirectory()) {// 处理目录 
                File files[] = file.listFiles();
                for (int i = 0; i < files.length; i++) {
                    deleteFolderFile(files[i].getAbsolutePath(), true);
                }
            }
            if (deleteThisPath) {
                if (!file.isDirectory()) {// 如果是文件，删除
                    file.delete();
                } else {// 目录
                    if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除
                        file.delete();
                    }
                }
            }
        }
    }
}
