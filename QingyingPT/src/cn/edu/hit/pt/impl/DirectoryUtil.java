package cn.edu.hit.pt.impl;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

public class DirectoryUtil {
	public static String rootDirectory = "";
	public static String cacheDirectory = "";
	public static String avatarDirectory = "";
	public static String torrentImageDirectory = "";
	public static String forumImageDirectory = "";
	public static String cameraDirectory = "";
	public static String downloadDirectory = "";
	public static String qrcodeDirectory = "";
	public static String sysDBName = "SYSTEM";
	public static String userDBName;
	public static File current_userDirectory = null;

	public static boolean checkdir(Context context){
		
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			rootDirectory = Environment.getExternalStorageDirectory() + "/QingyingPT";
			qrcodeDirectory = rootDirectory + "/qrcode/";
			cacheDirectory = rootDirectory + "/cache";
			avatarDirectory = cacheDirectory + "/avatar/";
			torrentImageDirectory = cacheDirectory + "/torrent_img/";
			forumImageDirectory = cacheDirectory + "/forum_img/";
			cameraDirectory = cacheDirectory + "/camera/";
			downloadDirectory = cacheDirectory + "/download/";

			setDirectory(rootDirectory);
			setDirectory(qrcodeDirectory);
			setDirectoryNoMedia(cacheDirectory);
			setDirectoryNoMedia(avatarDirectory);
			setDirectoryNoMedia(torrentImageDirectory);
			setDirectoryNoMedia(forumImageDirectory);
			setDirectoryNoMedia(cameraDirectory);
			setDirectoryNoMedia(downloadDirectory);
			
			return true;
		}
		else {
			return false;
		}
	}

	public static void setDirectory(String Dir){
		File file = new File(Dir);
		if (!file.exists()) {
			file.mkdir();
		}
	}
	
	public static void setDirectoryNoMedia(String Dir){
			File file = new File(Dir);
			if (!file.exists()) {
				file.mkdir();
			}
		file = new File(Dir + "/.nomedia");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 移除用户数据库
	 */
	public static void deleteUser(Context context, long userid){
		String path = MD5Util.MD5("Database:" + userid);
		deleteFolderFile(context.getDatabasePath(path).toString(), true);
	}
	
	public static String getCacheSize(){
		File file = new File(cacheDirectory);
		float size = getFolderSize(file);
        
        if(size > 1048576)
        	return (float)Math.round(size*100/1048576)/100 + "M";
        else if(size > 0)
        	return (float)Math.round(size*100/1024)/100 + "K";
        else
        	return "";
	}
	
    public static float getFolderSize(File file){
    	float size = 0;
    	if(!file.exists()) return 0;
        File[] fileList = file.listFiles();
        for(int i = 0; i < fileList.length; i++){
            if (fileList[i].isDirectory()){
                size = size + getFolderSize(fileList[i]);
            }else{
                size = size + fileList[i].length();
            }
        }
        return size;
    }
    
    public static void deleteFolderFile(String filePath, boolean deleteThisPath){
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
