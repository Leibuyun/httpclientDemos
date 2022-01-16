package zbj;

import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.text.csv.CsvWriter;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.seimicrawler.xpath.JXDocument;
import org.seimicrawler.xpath.JXNode;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 猪八戒服务平台
 * https://beijing.zbj.com/
 */
public class ZBJSpider {

    public static void main(String[] args) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {

            String kw = "小程序开发";
            URI uri = new URIBuilder("https://beijing.zbj.com/search/f")
                    .addParameter("kw", kw)
                    .build();
            HttpGet httpGet = new HttpGet(uri);

            httpClient.execute(httpGet, new HttpClientResponseHandler<Object>() {
                @Override
                public Object handleResponse(ClassicHttpResponse response) throws HttpException, IOException {

                    List<StoreEntity> storeEntityList = new ArrayList<>();

                    JXDocument doc = JXDocument.create(EntityUtils.toString(response.getEntity()));
                    List<JXNode> nodes = doc.selN("//div[@class='new-service-wrap']/div");

                    Pattern pattern = Pattern.compile(".*?(?<amount>\\d+).*?");

                    nodes.forEach(node->{
                        StoreEntity entity = new StoreEntity();
                        entity.setCompany(node.asElement().select("p.text-overflow").text());
                        entity.setPlace(node.asElement().select("span[title]").text());
                        entity.setPrice(node.asElement().select("span.price").text());
                        String amount = node.asElement().select("span.amount").text();
                        Matcher matcher = pattern.matcher(amount);
                        if (matcher.matches()){
                            entity.setTotal(Integer.parseInt(matcher.group("amount")));
                        }
                        entity.setMajor(node.asElement().select("p.title").text());
                        StringBuilder tag = new StringBuilder();
                        List<JXNode> tags = node.sel("//span[@class='service-tag']");
                        for (JXNode t : tags) {
                            tag.append(t.asElement().text());
                            tag.append(',');
                        }
                        if (tag.length() >= 1)
                            tag.deleteCharAt(tag.length() - 1);
                        entity.setTag(tag.toString());
                        storeEntityList.add(entity);
                    });
                    System.out.println(nodes.size());
                    storeEntityList.forEach(System.out::println);
                    // 保存至csv文件 [可以直接在IDEA里面预览, 如果是excel打开, 需要先保存ANSI格式(如果电脑是Windows), 否则乱码]
                    CsvWriter writer = CsvUtil.getWriter(new File("src/main/resources/zbj/" + kw + ".csv"), StandardCharsets.UTF_8);
                    writer.writeBeans(storeEntityList);
                    return null;
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}

class StoreEntity{

    private String company; // 公司名

    private String place;   // 地点

    private String price;   // 价格

    private Integer total;  // 成交数量

    private String major;   // 主要业务

    private String tag;     // 承诺内容

    @Override
    public String toString() {
        return "StoreEntity{" +
                "company='" + company + '\'' +
                ", place='" + place + '\'' +
                ", price='" + price + '\'' +
                ", total=" + total +
                ", major='" + major + '\'' +
                ", tag='" + tag + '\'' +
                '}';
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(String major) {
        this.major = major;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
