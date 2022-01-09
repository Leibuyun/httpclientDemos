package oj.leetcode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import utils.FileUtil;

import java.io.IOException;
import java.util.*;

public class LeetCodeSpiderDemo {

    private static List<LeetCodeOnlineJudgeEntity> leetCodeOnlineJudgeEntityList = new ArrayList<>();

    private static List<BasicHeader> headers = new ArrayList<>();

    private static CloseableHttpClient httpClient;

    private static String uri = "https://leetcode-cn.com/graphql/";

    static {
        headers.add(new BasicHeader("origin", "https://leetcode-cn.com"));

        httpClient = HttpClients.custom()
                .setDefaultHeaders(headers)
                .build();
    }

    // 问题集合的json参数体
    private static List<NameValuePair> getQuestionsPairList(int skip, int limit) {
        List<NameValuePair> pairList = new ArrayList<>();
        pairList.add(new BasicNameValuePair("query", "\n    query problemsetQuestionList($categorySlug: String, $limit: Int, $skip: Int, $filters: QuestionListFilterInput) {\n  problemsetQuestionList(\n    categorySlug: $categorySlug\n    limit: $limit\n    skip: $skip\n    filters: $filters\n  ) {\n    hasMore\n    total\n    questions {\n      acRate\n      difficulty\n      freqBar\n      frontendQuestionId\n      isFavor\n      paidOnly\n      solutionNum\n      status\n      title\n      titleCn\n      titleSlug\n      topicTags {\n        name\n        nameTranslated\n        id\n        slug\n      }\n      extra {\n        hasVideoSolution\n        topCompanyTags {\n          imgUrl\n          slug\n          numSubscribed\n        }\n      }\n    }\n  }\n}\n    "));
        Map<String, Object> map = new HashMap<>();
        map.put("skip", String.valueOf(skip));
        map.put("limit", String.valueOf(limit));
        pairList.add(new BasicNameValuePair("variables", JSON.toJSONString(map)));
        return pairList;
    }

    // 某个详细问题的json参数体
    private static List<NameValuePair> getQuestionPairList(String titleSlug) {
        List<NameValuePair> pairList = new ArrayList<>();
        pairList.add(new BasicNameValuePair("query", "query questionData($titleSlug: String!) {\n  question(titleSlug: $titleSlug) {\n    questionId\n    questionFrontendId\n    categoryTitle\n    boundTopicId\n    title\n    titleSlug\n    content\n    translatedTitle\n    translatedContent\n    isPaidOnly\n    difficulty\n    likes\n    dislikes\n    isLiked\n    similarQuestions\n    contributors {\n      username\n      profileUrl\n      avatarUrl\n      __typename\n    }\n    langToValidPlayground\n    topicTags {\n      name\n      slug\n      translatedName\n      __typename\n    }\n    companyTagStats\n    codeSnippets {\n      lang\n      langSlug\n      code\n      __typename\n    }\n    stats\n    hints\n    solution {\n      id\n      canSeeDetail\n      __typename\n    }\n    status\n    sampleTestCase\n    metaData\n    judgerAvailable\n    judgeType\n    mysqlSchemas\n    enableRunCode\n    envInfo\n    book {\n      id\n      bookName\n      pressName\n      source\n      shortDescription\n      fullDescription\n      bookImgUrl\n      pressImgUrl\n      productUrl\n      __typename\n    }\n    isSubscribed\n    isDailyQuestion\n    dailyRecordStatus\n    editorType\n    ugcQuestionId\n    style\n    exampleTestcases\n    __typename\n  }\n}\n"));
        pairList.add(new BasicNameValuePair("operationName", "questionData"));
        Map<String, Object> map = new HashMap<>();
        map.put("titleSlug", titleSlug);
        pairList.add(new BasicNameValuePair("variables", JSON.toJSONString(map)));
        return pairList;
    }

    // 解析详情页
    private static HttpClientResponseHandler<LeetCodeOnlineJudgeEntity> handlerQuestion = new HttpClientResponseHandler<LeetCodeOnlineJudgeEntity>() {
        @Override
        public LeetCodeOnlineJudgeEntity handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
            try {
                JSONObject question = JSON.parseObject(EntityUtils.toString(response.getEntity()))
                        .getJSONObject("data")
                        .getJSONObject("question");
                if (question.getString("translatedTitle") != null && question.getString("translatedContent") != null) {
                    LeetCodeOnlineJudgeEntity leetCodeOnlineJudgeEntity = new LeetCodeOnlineJudgeEntity();
                    leetCodeOnlineJudgeEntity.setDifficulty(question.getString("difficulty"));
                    leetCodeOnlineJudgeEntity.setTitleSlug(question.getString("titleSlug"));
                    leetCodeOnlineJudgeEntity.setTranslatedContent(question.getString("translatedContent"));
                    leetCodeOnlineJudgeEntity.setTranslatedTitle(question.getString("translatedTitle"));
                    return leetCodeOnlineJudgeEntity;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    };

    // 解析某一页
    private static HttpClientResponseHandler<Object> handlerQuestions = new HttpClientResponseHandler<Object>() {
        @Override
        public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
            try {
                JSONArray questions = JSON.parseObject(EntityUtils.toString(response.getEntity()))
                        .getJSONObject("data")
                        .getJSONObject("problemsetQuestionList")
                        .getJSONArray("questions");
//                System.out.println(questions.size());
//                    System.out.println(questions.getJSONObject(0).getString("titleCn"));
//                    FileUtil.writeToFile("src/main/resources/leetcodeQuestions.json", questions.toJSONString());
                for (int i = 0; i < questions.size(); i++) {
                    HttpPost httpPostQuestion = new HttpPost(uri);
                    httpPostQuestion.setEntity(new UrlEncodedFormEntity(getQuestionPairList(questions.getJSONObject(i).getString("titleSlug"))));
                    LeetCodeOnlineJudgeEntity leetCodeOnlineJudgeEntity = httpClient.execute(httpPostQuestion, handlerQuestion);
                    if (leetCodeOnlineJudgeEntity != null) {
                        System.out.println("正在下载" + leetCodeOnlineJudgeEntity.getTranslatedTitle());
                        leetCodeOnlineJudgeEntityList.add(leetCodeOnlineJudgeEntity);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }
    };


    public static void main(String[] args) {

        final Date dateStart = new Date();
        for (int skip = 0; skip < 2500; skip += 100) {
            HttpPost httpPostQuestions = new HttpPost(uri);
            httpPostQuestions.setEntity(new UrlEncodedFormEntity(getQuestionsPairList(skip, 100)));
            try {
                httpClient.execute(httpPostQuestions, handlerQuestions);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(leetCodeOnlineJudgeEntityList.size());
        FileUtil.writeToFile("src/main/resources/oj/leetcode.json", JSON.toJSONString(leetCodeOnlineJudgeEntityList));
        final Date dateEnd = new Date();
        System.out.println(dateStart);
        System.out.println(dateEnd);

    }

}


class LeetCodeOnlineJudgeEntity {

    private String titleSlug;

    private String translatedTitle;

    private String translatedContent;

    private String difficulty;

    public void setTitleSlug(String titleSlug) {
        this.titleSlug = titleSlug;
    }

    public void setTranslatedTitle(String translatedTitle) {
        this.translatedTitle = translatedTitle;
    }

    public void setTranslatedContent(String translatedContent) {
        this.translatedContent = translatedContent;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getTitleSlug() {
        return titleSlug;
    }

    public String getTranslatedTitle() {
        return translatedTitle;
    }

    public String getTranslatedContent() {
        return translatedContent;
    }

    public String getDifficulty() {
        return difficulty;
    }

    @Override
    public String toString() {
        return "LeetCodeOnlineJudgeEntity{" +
                "titleSlug='" + titleSlug + '\'' +
                ", translatedTitle='" + translatedTitle + '\'' +
                ", translatedContent='" + translatedContent + '\'' +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}