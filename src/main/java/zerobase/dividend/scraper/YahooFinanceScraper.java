package zerobase.dividend.scraper;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import zerobase.dividend.model.Company;
import zerobase.dividend.model.Dividend;
import zerobase.dividend.model.ScrapedResult;
import zerobase.dividend.model.constants.Month;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceScraper implements Scraper {

    /*
    Heap : 클래스의 멤버 변수 저장 / Stack : 클래스의 내부 함수에서 사용하는 변수 저장 -> 함수 호출시마다 메모리 할당
    모든 변수를 멤버 변수로 만들면 좋은가? No! Heap 공간 정리 -> Garbage Collection 수행빈도 증가 -> 성능 저하
     */
    private static final String STATISTICS_URL = "https://finance.yahoo.com/quote/%s/history?period1=%d&period2=%d&interval=1mo";
    private static final String SUMMARY_URL = "https://finance.yahoo.com/quote/%s?p=%s";

    private static final long START_TIME = 86400;   // 60 * 60 * 24 (1일은 86400초)

    @Override
    public ScrapedResult scrap(Company company) {

        var scrapResult = new ScrapedResult();
        scrapResult.setCompany(company);

        try {
            long now = System.currentTimeMillis() / 1000;   // 현재시간을 초로 나타낸것

            String url = String.format(STATISTICS_URL, company.getTicker(), START_TIME, now);
            Connection connection = Jsoup.connect(url);
            Document document = connection.get();

            Elements parsingDivs = document.getElementsByAttributeValue("data-test", "historical-prices");
            Element tableEle = parsingDivs.get(0);
            Element tbody = tableEle.children().get(1);
            /*
            우리가 가져오고 싶은 값이 <table class="W(100%) M(0)" data-test="historical-prices"> 안에 있어
            그 속성값에 해당하는 요소가 여러개일수 있음, 우리는 그중 0번째를 원함
            tableEle 는 가져온 테이블 전체, 우리가 원하는 값은 tbody 에 있음
            children 메소드 사용하면 thead tbody tfoot 을 0 1 2 인덱스로 가져올수있다
             */

            List<Dividend> dividends = new ArrayList<>();

            for (Element e : tbody.children()) {
                String txt = e.text();
                if (!txt.endsWith("Dividend")) {
                    continue;
                }

                // txt 는 "Apr 27, 2023 0.5 Dividend" 형식
                String[] splits = txt.split(" ");
                int month = Month.strToNumber(splits[0]);
                int day = Integer.valueOf(splits[1].replace(",", ""));
                int year = Integer.valueOf(splits[2]);
                String dividend = splits[3];

                if (month < 0) {
                    throw new RuntimeException("Unexpected Month enum value -> " + splits[0]);
                }

                dividends.add(new Dividend(LocalDateTime.of(year, month, day, 0, 0), dividend));
            }
            scrapResult.setDividends(dividends);

        } catch (IOException e) {
            // TODO error handling
            e.printStackTrace();
        }

        return scrapResult;
    }

    @Override
    public Company scrapCompanyByTicker(String ticker) {

        String url = String.format(SUMMARY_URL, ticker, ticker);

        try {
            Document document = Jsoup.connect(url).get();
            Element titleEle = document.getElementsByTag("h1").get(0);
            String title = titleEle.text().split(" - ")[1].trim();

            return new Company(ticker, title);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;   // ticker 에 해당하는 회사가 없을 경우 null 반환
    }

}
