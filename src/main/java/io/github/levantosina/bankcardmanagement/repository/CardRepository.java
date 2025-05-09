package io.github.levantosina.bankcardmanagement.repository;

import io.github.levantosina.bankcardmanagement.model.CardEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;



public interface CardRepository extends JpaRepository<CardEntity, Long> {
    void deleteCardByCardId(Long id);
    boolean existsCardByCardId(Long id);
    Page<CardEntity> findByUserUserId(Long userId, Pageable pageable);
    Page<CardEntity> findByUserUserIdAndCardHolderNameContaining(Long userId, String cardHolderName, Pageable pageable);

}
