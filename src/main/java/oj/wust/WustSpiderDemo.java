package oj.wust;

import cn.hutool.core.text.UnicodeUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import utils.FileUtil;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class WustSpiderDemo {

    public static void main(String[] args) {

        System.out.println(new Date());

        List<WustOnlineJudgeEntity> wustOnlineJudgeEntityList = new ArrayList<>();// 所有的题目

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            for (int offsetNum = 0; offsetNum <= 400; offsetNum += 200) {
                System.out.println("正在下载第" + offsetNum / 200 + "页");
                int offset = offsetNum, limit = 200;
                URI uri = new URIBuilder("https://oj.wust-acm.top/api/problem")
                        .addParameter("offset", String.valueOf(offset))
                        .addParameter("limit", String.valueOf(limit))
                        .build();
                HttpGet httpGet = new HttpGet(uri);
                httpClient.execute(httpGet, new HttpClientResponseHandler<Object>() {
                    @Override
                    public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                        // 返回json格式的数据, 同时汉字转换成了unicode
                        String html = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
                        html = UnicodeUtil.toString(html);// 将unicode转换回汉字
//                        FileUtil.writeToFile("src/main/resources/oj/" + offset + ".json", html);
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
                            wustOnlineJudgeEntityList.add(wustOnlineJudgeEntity);
                        }
                        return null;
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(wustOnlineJudgeEntityList.size());
        System.out.println(new Date());
        FileUtil.writeToFile("src/main/resources/oj/wustoj.json", JSON.toJSONString(wustOnlineJudgeEntityList));
    }

}

class WustOnlineJudgeEntity {

    private String[] tags;

    private String title;

    private String description;

    private String input_description;

    private String output_description;

    private String[] samples_input;

    private String[] samples_output;

    private String hint;

    private String difficulty;

    private String source;

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInput_description() {
        return input_description;
    }

    public void setInput_description(String input_description) {
        this.input_description = input_description;
    }

    public String getOutput_description() {
        return output_description;
    }

    public void setOutput_description(String output_description) {
        this.output_description = output_description;
    }

    public String[] getSamples_input() {
        return samples_input;
    }

    public void setSamples_input(String[] samples_input) {
        this.samples_input = samples_input;
    }

    public String[] getSamples_output() {
        return samples_output;
    }

    public void setSamples_output(String[] samples_output) {
        this.samples_output = samples_output;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }
}
