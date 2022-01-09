package study;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.fluent.Content;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.config.CharCodingConfig;
import org.apache.hc.core5.http.config.Http1Config;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws Exception{
        CloseableHttpClient httpClient = HttpClients.createDefault();
//        CloseableHttpResponse response = httpClient.execute(new HttpGet(""));
//        EntityUtils.toString(response.getEntity());
//        EntityUtils.consume(response.getEntity());

//        Content content = Request.get("https://jwc.wust.edu.cn/").execute().returnContent();
//        System.out.println(content.asString(StandardCharsets.UTF_8));
        httpClient.execute(new HttpGet("https://jwc.wust.edu.cn/"), new HttpClientResponseHandler<String>() {
            @Override
            public String handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                return null;
            }
        });


    }

}
