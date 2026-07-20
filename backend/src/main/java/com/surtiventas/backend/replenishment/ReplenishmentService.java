package com.surtiventas.backend.replenishment;

import com.surtiventas.backend.order.OrderRepository;
import com.surtiventas.backend.product.Product;
import com.surtiventas.backend.product.ProductRepository;
import com.surtiventas.backend.replenishment.dto.ReplenishmentItem;
import com.surtiventas.backend.replenishment.dto.SupplierReplenishment;
import com.surtiventas.backend.supplier.SupplierProduct;
import com.surtiventas.backend.supplier.SupplierProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Suggested purchases (automatic replenishment): for every active product at or
 * below its minimum stock, it proposes a reorder quantity that brings it up to a
 * target level — the larger of twice the minimum or the minimum plus the last
 * 30 days of demand — and picks the cheapest registered supplier. Suggestions
 * are grouped by supplier so each group can become one purchase order.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReplenishmentService {

    private static final int DEMAND_DAYS = 30;
    private static final String NO_SUPPLIER = "Sin proveedor";

    private final ProductRepository productRepository;
    private final SupplierProductRepository supplierProductRepository;
    private final OrderRepository orderRepository;

    public List<SupplierReplenishment> getSuggestions() {
        Map<Long, Integer> demand = demandByProduct();

        Map<Long, List<ReplenishmentItem>> bySupplier = new LinkedHashMap<>();
        Map<Long, String> supplierNames = new HashMap<>();
        List<ReplenishmentItem> withoutSupplier = new ArrayList<>();

        for (Product product : productRepository.findLowStock(Pageable.unpaged())) {
            int demand30 = demand.getOrDefault(product.getId(), 0);
            int suggestedQty = suggestedQuantity(product, demand30);
            SupplierProduct cheapest = cheapestSupplier(product.getId());

            BigDecimal unitCost = cheapest != null ? cheapest.getCost() : null;
            BigDecimal estimatedCost = unitCost != null
                    ? unitCost.multiply(BigDecimal.valueOf(suggestedQty)) : null;

            ReplenishmentItem item = new ReplenishmentItem(
                    product.getId(), product.getSku(), product.getName(),
                    product.getStock(), product.getMinStock(), demand30, suggestedQty,
                    unitCost, estimatedCost);

            if (cheapest == null) {
                withoutSupplier.add(item);
            } else {
                Long supplierId = cheapest.getSupplier().getId();
                supplierNames.put(supplierId, cheapest.getSupplier().getName());
                bySupplier.computeIfAbsent(supplierId, k -> new ArrayList<>()).add(item);
            }
        }

        List<SupplierReplenishment> result = new ArrayList<>();
        bySupplier.forEach((supplierId, items) ->
                result.add(new SupplierReplenishment(supplierId, supplierNames.get(supplierId), items, sumEstimated(items))));
        result.sort(Comparator.comparing(SupplierReplenishment::supplierName, String.CASE_INSENSITIVE_ORDER));

        if (!withoutSupplier.isEmpty()) {
            result.add(new SupplierReplenishment(null, NO_SUPPLIER, withoutSupplier, BigDecimal.ZERO));
        }
        return result;
    }

    /** Target level minus what's on hand, at least 1 unit. */
    private int suggestedQuantity(Product product, int demand30) {
        int minStock = product.getMinStock();
        int reorderUpTo = Math.max(minStock * 2, minStock + demand30);
        return Math.max(reorderUpTo - product.getStock(), 1);
    }

    private SupplierProduct cheapestSupplier(Long productId) {
        List<SupplierProduct> suppliers = supplierProductRepository.findByProductIdOrderByCostAsc(productId);
        return suppliers.isEmpty() ? null : suppliers.get(0);
    }

    private Map<Long, Integer> demandByProduct() {
        Instant from = Instant.now().minus(Duration.ofDays(DEMAND_DAYS));
        Map<Long, Integer> demand = new HashMap<>();
        for (Object[] row : orderRepository.soldQuantitiesByProductSince(from)) {
            demand.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return demand;
    }

    private BigDecimal sumEstimated(List<ReplenishmentItem> items) {
        return items.stream()
                .map(ReplenishmentItem::estimatedCost)
                .filter(cost -> cost != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
