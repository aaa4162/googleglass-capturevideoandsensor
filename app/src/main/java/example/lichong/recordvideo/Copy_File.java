package example.lichong.recordvideo;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class Copy_File {

    public boolean CopyFile(String fromFile, String toFile) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(fromFile);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(fromFile); //读入原文件
                FileOutputStream fs = new FileOutputStream(toFile);
                byte[] buffer = new byte[1444];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    //System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
                Log.i("CopyFile:"," succeed");
                return true;
            }
        } catch (Exception e) {
            System.out.println("复制单个文件操作出错");
            Log.e("",e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

