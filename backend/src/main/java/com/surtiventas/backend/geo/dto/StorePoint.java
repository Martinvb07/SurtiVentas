package com.surtiventas.backend.geo.dto;

/** A store plotted on the seller's route map. */
public record StorePoint(
        Long id,
        String storeName,
        String ownerName,
        String address,
        double latitude,
        double longitude,
        String classification) {
}
