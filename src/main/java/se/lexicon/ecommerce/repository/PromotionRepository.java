package se.lexicon.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import se.lexicon.ecommerce.domain.Promotion;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {

    @Query("""
            select p
            from Promotion p
            where p.startDate <= :date
              and (p.endDate is null or p.endDate >= :date)
            order by p.startDate desc
            """)
    List<Promotion> findActiveOn(@Param("date") LocalDate date);

    Optional<Promotion> findByCodeIgnoreCase(String code);

    List<Promotion> findByStartDateAfter(LocalDate date);

    List<Promotion> findByEndDateBefore(LocalDate date);

    List<Promotion> findByEndDateIsNull();

    default List<Promotion> findActiveToday() {
        return findActiveOn(LocalDate.now());
    }
}
