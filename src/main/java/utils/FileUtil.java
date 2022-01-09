package utils;

import java.io.File;
import java.io.FileWriter;

public class FileUtil {

    public static void writeToFile(String path, String content){
        File file = new File(path);
        if (!file.exists()) {
            if (file.getParentFile() != null && file.getParentFile().mkdirs())
                System.out.println("创建文件夹成功");
        }
        try (final FileWriter fileWriter = new FileWriter(file)) {
            fileWriter.write(content); // 写入文件
        } catch (Exception e) {
            System.out.println("写入文件失败");
        }
    }

}
