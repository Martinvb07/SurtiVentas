package com.surtiventas.backend.purchasing;

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
 * The scanned supplier invoice attached to a purchase order, plus the OCR
 * result and the reconciliation snapshot against the order total. One row per
 * purchase order (a re-scan replaces the previous one).
 *
 * <p>The large columns are mapped with explicit {@code LONGVARBINARY} /
 * {@code LONGVARCHAR} JDBC types rather than {@code @Lob}: on MySQL that keeps
 * Hibernate's schema {@code validate} in agreement with the LONGBLOB/LONGTEXT
 * columns without the classic {@code @Lob} type-mismatch surprises.
 */
@Entity
@Table(name = "purchase_order_invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrderInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false, unique = true)
    private PurchaseOrder purchaseOrder;

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

    /** Candidate line items the parser detected, stored as a JSON array. */
    @JdbcTypeCode(SqlTypes.LONGVARCHAR)
    @Column(name = "parsed_lines", columnDefinition = "LONGTEXT")
    private String parsedLines;

    /** Grand total detected on the invoice, or null when none could be read. */
    @Column(name = "detected_total", precision = 14, scale = 2)
    private BigDecimal detectedTotal;

    /** Snapshot of the purchase order total at scan time, for reconciliation. */
    @Column(name = "po_total", nullable = false, precision = 14, scale = 2)
    private BigDecimal poTotal;

    /** True when the detected total matches the order total within tolerance. */
    @Column(name = "matched", nullable = false)
    private boolean matched;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
