package finvibe.insight.shared.infra.persistence;

import finvibe.insight.shared.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<Category, Long> {
}
