package com.genymobile.scrcpy.log;

import com.genymobile.scrcpy.custom.ScrcpyConfig;
import com.genymobile.scrcpy.util.CustomThreads;
import com.genymobile.scrcpy.util.FileUtils;
import com.genymobile.scrcpy.util.Logger;
import com.genymobile.scrcpy.util.TimeUtils;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
* @author qincunrong
* @date 2023/12/7 11:47
* @email qincunrong@ysten.com
* @description 清除缓存 总共保存7天有日志的天数/最大日志文件不应超过100M
*/
public class LogClear {
    private static long MAX_LOG_LENGTH = 100 * 1024 * 1024;//最大100M日志
    public static final int LOG_RAMIN_DAYS = 7;
    private static String TAG ="LogClear";
    public static void clearLogCache() {
        CustomThreads.submit(()->{
            doClearLogCache();
        });
    }
    private static void doClearLogCache() {
        Logger.i(TAG,"clearLogCache");
        try {
            File logDir = new File(ScrcpyConfig.getLogDir());
            File[] listFiles = logDir.listFiles();
            if (listFiles != null) {
                Arrays.sort(listFiles, new ComparatorByDate());
                for (File file : listFiles) {
                    long modifyTime = file.lastModified();
                    Logger.i(TAG, "fileItem:%s, modifyTime:%s", file.getName(), TimeUtils.formatTime(modifyTime));
                }
                if (listFiles.length > LOG_RAMIN_DAYS) {
                    Logger.i(TAG, "deleteFileByDays >7day");
                    for (int i = 0; i < listFiles.length - LOG_RAMIN_DAYS; i++) {
                        File deleteFile = listFiles[i];
                        FileUtils.delete(deleteFile);
                        Logger.i(TAG, ">7day deleteFile:" + deleteFile.getName());
                    }
                }
            }
            deleteBigLogFile(logDir);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //按文件名日期正序排列
    private static class  ComparatorByDate  implements Comparator<File> {
        @Override
        public int compare(File first, File second) {
            try  {
                String firstFileName = first.getName();
                String secondFileName = second.getName();
                return firstFileName.compareTo(secondFileName);

                //按修改时间会导致删除了最新的文件
//                long firstModifyTime = first.lastModified();
//                long secondModifyTime= second.lastModified();
//                return firstModifyTime - secondModifyTime>0?1:0;
            }  catch  (Exception e) {
                e.printStackTrace();
            }
            return  0 ;
        }
    }


    private static void deleteBigLogFile(File dir) {
        if (FileUtils.getLength(dir) > MAX_LOG_LENGTH) {
            Logger.i(TAG, "deleteFileBySize >100M");
            List<File> files = FileUtils.listFilesInDir(dir, (o1, o2) -> (int) (o2.lastModified() - o1.lastModified()));
            int fileLength = 0;
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                fileLength += FileUtils.getLength(file);
                if (fileLength >= MAX_LOG_LENGTH) {
                    Logger.i(TAG, ">100M deleteFile:" + file.getName());
                    FileUtils.delete(file);
                }
            }
        }
    }
}
