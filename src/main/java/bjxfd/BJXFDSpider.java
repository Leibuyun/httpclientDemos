package bjxfd;


import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 北京新发地
 * http://www.xinfadi.com.cn/priceDetail.html
 * 可以控制limit, current, pubDateStartTime等
 *
 * 学习使用LocalDate处理时间
 */
public class BJXFDSpider {

    public static void main(String[] args) {

        try(CloseableHttpClient httpClient = HttpClients.createDefault()){
            String url = "http://www.xinfadi.com.cn/getPriceData.html";
            HttpPost httpPost = new HttpPost(url);

            int limit = 200, current = 1;
            LocalDate pubDateStartTime = LocalDate.of(2021, 12, 4);
            LocalDate pubDateEndTime = pubDateStartTime.plusMonths(1);
            // 设置参数
            List<NameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("limit", String.valueOf(limit)));
            pairList.add(new BasicNameValuePair("current", String.valueOf(current)));
            pairList.add(new BasicNameValuePair("pubDateStartTime", pubDateStartTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))));
            pairList.add(new BasicNameValuePair("pubDateEndTime", pubDateEndTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"))));
            pairList.add(new BasicNameValuePair("prodPcatid", ""));
            httpPost.setEntity(new UrlEncodedFormEntity(pairList));

            httpClient.execute(httpPost, new HttpClientResponseHandler<Object>() {
                @Override
                public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                    System.out.println(EntityUtils.toString(response.getEntity()));
                    return null;
                }
            });

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}