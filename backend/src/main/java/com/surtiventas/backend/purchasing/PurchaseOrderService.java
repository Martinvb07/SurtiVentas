package com.surtiventas.backend.purchasing;

import com.surtiventas.backend.common.exception.ResourceNotFoundException;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.product.ProductService;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderCreateRequest;
import com.surtiventas.backend.purchasing.dto.PurchaseOrderLineRequest;
import com.surtiventas.backend.security.CustomUserDetails;
import com.surtiventas.backend.supplier.Supplier;
import com.surtiventas.backend.supplier.SupplierRepository;
import com.surtiventas.backend.user.Role;
import com.surtiventas.backend.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderStatusHistoryRepository historyRepository;
    private final PurchaseOrderStateMachine stateMachine;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public Page<PurchaseOrder> search(Long supplierId, PurchaseOrderStatus status, Pageable pageable) {
        return purchaseOrderRepository.findAll(PurchaseOrderSpecifications.withFilters(supplierId, status), pageable);
    }

    public PurchaseOrder findById(Long id) {
        return purchaseOrderRepository.findWithAssociationsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase order not found: " + id));
    }

    public List<PurchaseOrderStatusHistory> getHistory(Long purchaseOrderId) {
        findById(purchaseOrderId);
        return historyRepository.findByPurchaseOrderIdOrderByChangedAtAsc(purchaseOrderId);
    }

    @Transactional
    public PurchaseOrder create(PurchaseOrderCreateRequest request, CustomUserDetails actingUser) {
        Supplier supplier = supplierRepository.findById(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found: " + request.supplierId()));
        User user = actingUser.getUser();

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .orderNumber(generateOrderNumber())
                .supplier(supplier)
                .status(PurchaseOrderStatus.BORRADOR)
                .expectedDate(request.expectedDate())
                .createdBy(user)
                .build();

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderLineRequest lineRequest : request.lines()) {
            Product product = productRepository.findById(lineRequest.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + lineRequest.productId()));
            BigDecimal subtotal = lineRequest.unitCost().multiply(BigDecimal.valueOf(lineRequest.quantity()));
            total = total.add(subtotal);

            PurchaseOrderLine line = PurchaseOrderLine.builder()
                    .product(product)
                    .quantity(lineRequest.quantity())
                    .unitCost(lineRequest.unitCost())
                    .subtotal(subtotal)
                    .build();
            purchaseOrder.addLine(line);
        }
        purchaseOrder.setTotalAmount(total);

        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);
        recordHistory(purchaseOrder, null, PurchaseOrderStatus.BORRADOR, user, "Orden de compra creada");

        return findById(purchaseOrder.getId());
    }

    @Transactional
    public PurchaseOrder transition(Long id, PurchaseOrderStatus targetStatus, String note, CustomUserDetails actingUser) {
        PurchaseOrder purchaseOrder = findById(id);
        User user = actingUser.getUser();
        Role role = user.getRole();

        PurchaseOrderStatus currentStatus = purchaseOrder.getStatus();
        stateMachine.validate(currentStatus, targetStatus, role);

        purchaseOrder.setStatus(targetStatus);
        purchaseOrder = purchaseOrderRepository.save(purchaseOrder);

        if (targetStatus == PurchaseOrderStatus.INGRESADA) {
            enterGoodsIntoInventory(purchaseOrder, actingUser);
        }

        recordHistory(purchaseOrder, currentStatus, targetStatus, user, note);

        return findById(purchaseOrder.getId());
    }

    /**
     * The admin entering a RECIBIDA order into inventory (INGRESADA) drives an
     * audited stock entry per line through ProductService, reusing the same
     * stock_movement ledger the inventory module writes to. The warehouse's
     * earlier RECIBIDA step only records physical arrival, without touching stock.
     */
    private void enterGoodsIntoInventory(PurchaseOrder purchaseOrder, CustomUserDetails actingUser) {
        for (PurchaseOrderLine line : purchaseOrder.getLines()) {
            productService.adjustStock(
                    line.getProduct().getId(),
                    line.getQuantity(),
                    "Ingreso a inventario OC " + purchaseOrder.getOrderNumber(),
                    actingUser);
        }
    }

    private void recordHistory(PurchaseOrder purchaseOrder, PurchaseOrderStatus from, PurchaseOrderStatus to,
                                User changedBy, String note) {
        PurchaseOrderStatusHistory history = PurchaseOrderStatusHistory.builder()
                .purchaseOrder(purchaseOrder)
                .fromStatus(from)
                .toStatus(to)
                .changedBy(changedBy)
                .note(note)
                .build();
        historyRepository.save(history);
    }

    private String generateOrderNumber() {
        return "OC-" + Instant.now().toEpochMilli();
    }
}
