package finvibe.insight.modules.news.infra.client;

import finvibe.insight.modules.news.dto.MarketCategoryChangeRateResponse;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.bind.annotation.PathVariable;

@HttpExchange("/api/market/market")
public interface HttpMarketClient {

    @GetExchange("/categories/{categoryId}/change-rate")
    MarketCategoryChangeRateResponse getCategoryChangeRate(@PathVariable Long categoryId);
}
