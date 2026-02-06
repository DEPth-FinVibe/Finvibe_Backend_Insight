package finvibe.insight.shared.application.port.out;

import finvibe.insight.shared.domain.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    Optional<Category> findById(Long id);

    List<Category> findAll();
}
