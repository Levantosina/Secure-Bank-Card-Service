package io.github.levantosina.bankcardmanagement.repository;

import io.github.levantosina.bankcardmanagement.model.CardEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardRepository extends JpaRepository<CardEntity, Long> {
    void deleteCardByCardId(Long id);
    boolean existsCardByCardId(Long id);
    List<CardEntity> findAllByUserUserId(Long userId);
}
