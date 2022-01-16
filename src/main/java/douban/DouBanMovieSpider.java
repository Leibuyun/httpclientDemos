package douban;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 豆瓣电影 Top 250
 * https://movie.douban.com/top250
 */
public class DouBanMovieSpider {

    public static void main(String[] args) {

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36")
                .build();) {
            List<DoubanMovieEntity> entityList = new ArrayList<>();
            for (int i = 0; i < 250; i += 25) {
                HttpGet httpGet = new HttpGet("https://movie.douban.com/top250?start=" + i);
                httpClient.execute(httpGet, new HttpClientResponseHandler<Object>() {
                    @Override
                    public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {
                        String html = EntityUtils.toString(response.getEntity());
                        html = html.replaceAll("\\s", "");// 去除所有的空白字符
                        Pattern pattern = Pattern.compile("class=\"title\">(?<name>.*?)</span.*?<br>(?<year>\\d+).*?class.*?v:average\">(?<rate>\\d+\\.?\\d+).*?(?<peopleNum>\\d+)人评价");
                        Matcher matcher = pattern.matcher(html);
                        while (matcher.find()){
                            DoubanMovieEntity entity = new DoubanMovieEntity();
                            entity.setName(matcher.group("name"));
                            entity.setYear(Integer.parseInt(matcher.group("year")));
                            entity.setGrade(Double.parseDouble(matcher.group("rate")));
                            entity.setPeopleNum(Integer.parseInt(matcher.group("peopleNum")));
                            entityList.add(entity);
                        }
                        return null;
                    }
                });
            }
            entityList.forEach(System.out::println);
            System.out.println(entityList.size());
        } catch (Exception e) {

        }

    }

}

class DoubanMovieEntity{

    private String name; // 电影名

    private Integer year; // 年份

    private Double grade;// 评分

    private Integer peopleNum; // 打分人数

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public Integer getPeopleNum() {
        return peopleNum;
    }

    public void setPeopleNum(Integer peopleNum) {
        this.peopleNum = peopleNum;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "name='" + name + '\'' +
                ", year=" + year +
                ", grade=" + grade +
                ", peopleNum=" + peopleNum +
                '}';
    }
}