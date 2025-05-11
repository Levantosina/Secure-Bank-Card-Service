package io.github.levantosina.bankcardmanagement.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "card_table")
public class CardEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;
    @Column(name = "card_holder_name")
    private String cardHolderName;
    @Column(name = "encrypted_card_number")
    private String encryptedCardNumber;
    @Column(name = "expiry_date")
    @Convert(converter = YearMonthConverter.class)
    private YearMonth expiryDate;
    @Column(name = "balance")
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    @Column(name = "card_status")
    private CardStatus cardStatus;
    @Column(name="block_requested")
    private boolean blockRequest=false;
    @Column(name = "block_requested_at")
    private LocalDateTime blockRequestedAt;
    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    @JsonBackReference
    private UserAdminEntity user;

}
