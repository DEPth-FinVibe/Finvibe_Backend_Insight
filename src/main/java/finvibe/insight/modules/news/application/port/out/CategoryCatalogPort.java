package finvibe.insight.modules.news.application.port.out;

import finvibe.insight.shared.domain.CategoryInfo;

import java.util.List;

public interface CategoryCatalogPort {

    List<CategoryInfo> getAll();

    List<CategoryInfo> refresh();
}
