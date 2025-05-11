package io.github.levantosina.bankcardmanagement.repository;

import io.github.levantosina.bankcardmanagement.model.CardEntity;
import io.github.levantosina.bankcardmanagement.model.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CardRepository extends JpaRepository<CardEntity, Long> {
    void deleteCardByCardId(Long id);
    boolean existsCardByCardId(Long id);
    Page<CardEntity> findByUserUserId(Long userId, Pageable pageable);
    Page<CardEntity> findByUserUserIdAndCardHolderNameContaining(Long userId, String cardHolderName, Pageable pageable);
    @Query("SELECT c FROM CardEntity c WHERE c.blockRequest = true AND c.cardStatus <> 'BLOCKED'")
    List<CardEntity> findByBlockRequestedTrueAndCardStatusNot(CardStatus status);

}
