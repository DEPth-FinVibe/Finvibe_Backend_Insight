package finvibe.insight.modules.news.infra.client;

import finvibe.insight.modules.news.application.port.out.MarketCategoryChangeRatePort;
import finvibe.insight.modules.news.dto.MarketCategoryChangeRateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class MarketCategoryChangeRateAdapter implements MarketCategoryChangeRatePort {

    private final HttpMarketClient httpMarketClient;

    @Override
    public BigDecimal fetchAverageChangePct(Long categoryId) {
        try {
            MarketCategoryChangeRateResponse response = httpMarketClient.getCategoryChangeRate(categoryId);
            return response == null ? null : response.averageChangePct();
        } catch (Exception ex) {
            return null;
        }
    }
}
