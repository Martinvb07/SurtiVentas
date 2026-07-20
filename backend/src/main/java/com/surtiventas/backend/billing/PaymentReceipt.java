package com.surtiventas.backend.billing;

import com.surtiventas.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * The scanned proof of payment (comprobante) attached to a {@link Payment},
 * plus the OCR result and the reconciliation of the detected amount against the
 * registered payment amount. One receipt per payment (a re-scan replaces it).
 *
 * <p>Mirrors {@code purchasing.PurchaseOrderInvoice}: the large columns use
 * explicit LONGVARBINARY/LONGVARCHAR JDBC types so Hibernate schema
 * {@code validate} agrees with the LONGBLOB/LONGTEXT columns on MySQL.
 */
@Entity
@Table(name = "payment_receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    private Payment payment;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @JdbcTypeCode(SqlTypes.LONGVARBINARY)
    @Column(name = "file_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] fileData;

    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "extracted_text", columnDefinition = "LONGTEXT")
    private String extractedText;

    /** Amount detected on the receipt, or null when none could be read. */
    @Column(name = "detected_amount", precision = 14, scale = 2)
    private BigDecimal detectedAmount;

    /** Reference/transaction number detected on the receipt, if any. */
    @Column(name = "detected_reference", length = 100)
    private String detectedReference;

    /** Snapshot of the payment amount at scan time, for reconciliation. */
    @Column(name = "payment_amount", nullable = false, precision = 14, scale = 2)
    private BigDecimal paymentAmount;

    /** True when the detected amount matches the payment amount within tolerance. */
    @Column(name = "matched", nullable = false)
    private boolean matched;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
