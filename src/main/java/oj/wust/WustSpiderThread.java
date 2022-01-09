package oj.wust;

import cn.hutool.core.text.UnicodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.pool.PoolConcurrencyPolicy;
import org.apache.hc.core5.pool.PoolReusePolicy;
import org.apache.hc.core5.util.Timeout;
import utils.FileUtil;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

public class WustSpiderThread {

    private static ExecutorService fixedThreadPool;

    private static ExecutorCompletionService<List<WustOnlineJudgeEntity>> executorCompletionService;

    private static final PoolingHttpClientConnectionManager manager;

    private static final RequestConfig globalRequestConfig;

    private static final CloseableHttpClient httpClient;

    public static List<WustOnlineJudgeEntity> wustOnlineJudgeEntityList = new ArrayList<>();

    static {
        fixedThreadPool = Executors.newFixedThreadPool(8);

        executorCompletionService = new ExecutorCompletionService<>(fixedThreadPool);

        manager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(100)           // 设置最大连接数
                .setMaxConnPerRoute(10)         // 设置每个连接的最大连接数
                .setConnPoolPolicy(PoolReusePolicy.LIFO)    // 设置连接池重用策略: LIFO(平等的重用所有连接) FILO(重用尽可能少的连接)
                .setPoolConcurrencyPolicy(PoolConcurrencyPolicy.STRICT) // 设置连接池并发策略: STRICT(保证严格连接最大限制) LAX(不强制最大策略)
                .build();

        globalRequestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(5))
                .setResponseTimeout(Timeout.ofSeconds(5))
                .setRedirectsEnabled(true)
                .build();

        httpClient = HttpClients.custom()
                .setConnectionManager(manager)
                .setDefaultRequestConfig(globalRequestConfig)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                .build();
    }

    public static void main(String[] args) {

        System.out.println(new Date());
//        List<WustOnlineJudgeEntity> resultList = new ArrayList<>();

        for (int offset = 0; offset <= 400; offset += 200) {
            try {
                URI uri = new URIBuilder("https://oj.wust-acm.top/api/problem")
                        .addParameter("offset", String.valueOf(offset))
                        .addParameter("limit", String.valueOf(200))
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                fixedThreadPool.execute(new WustOnlineJudgeThread(httpClient, httpGet));
//                executorCompletionService.submit(new WustOnlineJudgeThread(httpClient, httpGet));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        fixedThreadPool.shutdown();

//        while (true){
//            if (fixedThreadPool.isTerminated()){
//                System.out.println(new Date());
//                System.out.println(wustOnlineJudgeEntityList.size());
//                break;
//            }
//        }

        // future
//        for (int i = 0; i < 3; i++) {
//            List<WustOnlineJudgeEntity> tmpList = null;
//            try {
//                tmpList = executorCompletionService.take().get();
//            } catch (InterruptedException | ExecutionException e) {
//                e.printStackTrace();
//            }
//            if(tmpList != null)
//                resultList.addAll(tmpList);
//        }
//        System.out.println(resultList.size());
//        System.out.println(new Date());
//        FileUtil.writeToFile("src/main/resources/oj/wustoj_thread.json", JSON.toJSONString(resultList));
    }

}

class WustOnlineJudgeThread implements Runnable {

    private CloseableHttpClient httpClient;

    private HttpGet httpGet;

    public WustOnlineJudgeThread(CloseableHttpClient httpClient, HttpGet httpGet) {
        this.httpClient = httpClient;
        this.httpGet = httpGet;
    }

    @Override
    public void run() {
        try {
            httpClient.execute(httpGet, new HttpClientResponseHandler<Object>() {
                @Override
                public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                    // 返回json格式的数据, 同时汉字转换成了unicode
                    String html = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                    html = UnicodeUtil.toString(html);// 将unicode转换回汉字
                    // 解析Json数据, 序列化
                    final JSONObject jsonObject = JSON.parseObject(html);
                    final JSONArray results = JSON.parseArray(JSON.parseObject(jsonObject.get("data").toString()).get("results").toString());
                    for (int i = 0; i < results.size(); i++) {
                        JSONObject resultObj = results.getJSONObject(i);
                        WustOnlineJudgeEntity wustOnlineJudgeEntity = new WustOnlineJudgeEntity();
                        JSONArray tagsJsonArray = resultObj.getJSONArray("tags");
                        String[] tags = new String[tagsJsonArray.size()];
                        for (int i1 = 0; i1 < tagsJsonArray.size(); i1++) {
                            tags[i1] = tagsJsonArray.getString(i1);
                        }
                        wustOnlineJudgeEntity.setTags(tags);
                        wustOnlineJudgeEntity.setTitle(resultObj.getString("title"));
                        wustOnlineJudgeEntity.setDescription(resultObj.getString("description"));
                        wustOnlineJudgeEntity.setInput_description(resultObj.getString("input_description"));
                        wustOnlineJudgeEntity.setOutput_description(resultObj.getString("output_description"));
                        JSONArray samples = resultObj.getJSONArray("samples");
                        String[] samples_input = new String[samples.size()];
                        String[] samples_output = new String[samples.size()];
                        for (int i1 = 0; i1 < samples.size(); i1++) {
                            samples_input[i1] = samples.getJSONObject(i1).getString("input");
                            samples_output[i1] = samples.getJSONObject(i1).getString("output");
                        }
                        wustOnlineJudgeEntity.setSamples_input(samples_input);
                        wustOnlineJudgeEntity.setSamples_output(samples_output);
                        wustOnlineJudgeEntity.setHint(resultObj.getString("hint"));
                        wustOnlineJudgeEntity.setDifficulty(resultObj.getString("difficulty"));
                        wustOnlineJudgeEntity.setSource(resultObj.getString("source"));
                        WustSpiderThread.wustOnlineJudgeEntityList.add(wustOnlineJudgeEntity);
                    }
                    System.out.println(new Date() + "---" + Thread.currentThread().getId());
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


// 如果使用Future, 需要返回结果
//class WustOnlineJudgeThread implements Callable {
//
//    private CloseableHttpClient httpClient;
//
//    private HttpGet httpGet;
//
//    public WustOnlineJudgeThread(CloseableHttpClient httpClient, HttpGet httpGet) {
//        this.httpClient = httpClient;
//        this.httpGet = httpGet;
//    }
//
//    @Override
//    public List<WustOnlineJudgeEntity> call() throws Exception {
//        return httpClient.execute(httpGet, new HttpClientResponseHandler<List<WustOnlineJudgeEntity>>() {
//            @Override
//            public List<WustOnlineJudgeEntity> handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
//                List<WustOnlineJudgeEntity> wustOnlineJudgeEntityList = new ArrayList<>();
//                // 返回json格式的数据, 同时汉字转换成了unicode
//                String html = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
//                html = UnicodeUtil.toString(html);// 将unicode转换回汉字
////                        FileUtil.writeToFile("src/main/resources/oj/" + offset + ".json", html);
//                // 解析Json数据, 序列化
//                final JSONObject jsonObject = JSON.parseObject(html);
//                final JSONArray results = JSON.parseArray(JSON.parseObject(jsonObject.get("data").toString()).get("results").toString());
//                for (int i = 0; i < results.size(); i++) {
//                    JSONObject resultObj = results.getJSONObject(i);
//                    WustOnlineJudgeEntity wustOnlineJudgeEntity = new WustOnlineJudgeEntity();
//                    JSONArray tagsJsonArray = resultObj.getJSONArray("tags");
//                    String[] tags = new String[tagsJsonArray.size()];
//                    for (int i1 = 0; i1 < tagsJsonArray.size(); i1++) {
//                        tags[i1] = tagsJsonArray.getString(i1);
//                    }
//                    wustOnlineJudgeEntity.setTags(tags);
//                    wustOnlineJudgeEntity.setTitle(resultObj.getString("title"));
//                    wustOnlineJudgeEntity.setDescription(resultObj.getString("description"));
//                    wustOnlineJudgeEntity.setInput_description(resultObj.getString("input_description"));
//                    wustOnlineJudgeEntity.setOutput_description(resultObj.getString("output_description"));
//                    JSONArray samples = resultObj.getJSONArray("samples");
//                    String[] samples_input = new String[samples.size()];
//                    String[] samples_output = new String[samples.size()];
//                    for (int i1 = 0; i1 < samples.size(); i1++) {
//                        samples_input[i1] = samples.getJSONObject(i1).getString("input");
//                        samples_output[i1] = samples.getJSONObject(i1).getString("output");
//                    }
//                    wustOnlineJudgeEntity.setSamples_input(samples_input);
//                    wustOnlineJudgeEntity.setSamples_output(samples_output);
//                    wustOnlineJudgeEntity.setHint(resultObj.getString("hint"));
//                    wustOnlineJudgeEntity.setDifficulty(resultObj.getString("difficulty"));
//                    wustOnlineJudgeEntity.setSource(resultObj.getString("source"));
//                    wustOnlineJudgeEntityList.add(wustOnlineJudgeEntity);
//                }
//                return wustOnlineJudgeEntityList;
//            }
//        });
//    }
//}
