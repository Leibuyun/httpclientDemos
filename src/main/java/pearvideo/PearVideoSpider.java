package pearvideo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.seimicrawler.xpath.JXDocument;
import utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * 梨视频
 * https://www.pearvideo.com/
 */
public class PearVideoSpider {

    public static void main(String[] args) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String url = "https://www.pearvideo.com/";

            httpClient.execute(new HttpGet(url), new HttpClientResponseHandler<Object>() {
                @Override
                public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                    String html = EntityUtils.toString(response.getEntity());
//                    FileUtil.writeToFile("src/main/resources/pearvideos/index.html", html);
                    JXDocument doc = JXDocument.create(html);
                    // 获取前5个视频
                    List<Object> hrefs = doc.sel("//ul[@id='actwapSlideList']/li/a/@href");
                    hrefs.forEach(href -> {
                        int contId = Integer.parseInt(href.toString().replaceAll("[^\\d]", ""));
                        double mrd = Math.random();
                        String detailUrl = url + "videoStatus.jsp?contId=" + contId + "&mrd=" + mrd;
                        try {
                            HttpGet httpGet = new HttpGet(detailUrl);
                            httpGet.setHeader("Referer", "https://www.pearvideo.com/video_" + contId);
                            httpClient.execute(httpGet, new HttpClientResponseHandler<Object>() {
                                @Override
                                public Object handleResponse(ClassicHttpResponse responseDetail) throws HttpException, IOException {
                                    JSONObject resData = JSON.parseObject(EntityUtils.toString(responseDetail.getEntity()));
                                    String systemTimeStr = resData.getString("systemTime");
                                    String srcUrl = resData.getJSONObject("videoInfo")
                                            .getJSONObject("videos")
                                            .getString("srcUrl").replaceAll(systemTimeStr, "cont-" + contId);
                                    // https://video.pearvideo.com/mp4/adshort/20220112/1642110256720-15816131_adpkg-ad_hd.mp4
                                    // https://video.pearvideo.com/mp4/adshort/20220112/cont-1644002-15816131_adpkg-ad_hd.mp4
//                                    System.out.println(srcUrl);
                                    HttpGet downloadMp4HttpGet = new HttpGet(srcUrl);
                                    httpClient.execute(downloadMp4HttpGet, new HttpClientResponseHandler<Object>() {
                                        @Override
                                        public Object handleResponse(ClassicHttpResponse mp4Response) throws HttpException, IOException {
                                            if (mp4Response.getCode() == 200) {
                                                mp4Response.getEntity().writeTo(new FileOutputStream(new File("src/main/resources/pearvideos/" + contId + ".mp4")));
                                                System.out.println(contId + "下载成功!");
                                            }
                                            return null;
                                        }
                                    });
                                    return null;
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
