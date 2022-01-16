package wustjwc;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class WustJwcSpider {

    private static String encodeInp(String input) {
        final String keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
        int i = 0, chr1 = 0, chr2 = 0, chr3 = 0;
        StringBuilder output = new StringBuilder();
        do {
            chr1 = input.codePointAt(i++);
            int flag = 1;
            try {
                chr2 = input.codePointAt(i++);
            } catch (StringIndexOutOfBoundsException e) {
                flag = 2;
            }
            try {
                chr3 = input.codePointAt(i++);
            } catch (StringIndexOutOfBoundsException e) {
                flag = 3;
            }
            int enc1 = chr1 >> 2;
            int enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
            int enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
            int enc4 = chr3 & 63;
            if (flag == 2)
                enc3 = enc4 = 64;
            else if (flag == 3)
                enc4 = 64;
            output.append(keyStr.charAt(enc1))
                    .append(keyStr.charAt(enc2))
                    .append(keyStr.charAt(enc3))
                    .append(keyStr.charAt(enc4));
        } while (i < input.length());
        return output.toString();
    }

    public static void main(String[] args) throws IOException {

        BasicCookieStore cookieStore = new BasicCookieStore();
        String username = "201913136025";
        String password = "xxx";
        String encoded = encodeInp(username) + "%%%" + encodeInp(password);
        try(CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore).build()){
            List<BasicNameValuePair> pairList = new ArrayList<>();
            pairList.add(new BasicNameValuePair("encoded", encoded));
            HttpPost httpPost = new HttpPost("http://bkjx.wust.edu.cn/jsxsd/xk/LoginToXk");
            httpPost.setEntity(new UrlEncodedFormEntity(pairList));
            httpClient.execute(httpPost, new HttpClientResponseHandler<Object>() {
                @Override
                public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                    System.out.println(response.getCode());
                    EntityUtils.consume(response.getEntity());
                    return null;
                }
            });
            cookieStore.getCookies().forEach(System.out::println);
            httpClient.execute(new HttpGet("http://bkjx.wust.edu.cn/jsxsd/kscj/cjcx_list?kksj=2021-2022-1"), new HttpClientResponseHandler<Object>() {
                @Override
                public Object handleResponse(ClassicHttpResponse classicHttpResponse) throws HttpException, IOException {
                    System.out.println(EntityUtils.toString(classicHttpResponse.getEntity()));
                    return null;
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
