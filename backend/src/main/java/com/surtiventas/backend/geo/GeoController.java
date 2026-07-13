package com.surtiventas.backend.geo;

import com.surtiventas.backend.geo.dto.DeliveryPoint;
import com.surtiventas.backend.geo.dto.StorePoint;
import com.surtiventas.backend.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Geolocation read endpoints for the route maps. */
@RestController
@RequestMapping("/api/geo")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GeoController {

    private final GeoService geoService;

    @GetMapping("/customers")
    @PreAuthorize("hasAnyRole('VENDEDOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<StorePoint>> stores() {
        return ResponseEntity.ok(geoService.stores());
    }

    @GetMapping("/deliveries")
    @PreAuthorize("hasAnyRole('CONDUCTOR', 'ADMINISTRADOR')")
    public ResponseEntity<List<DeliveryPoint>> deliveries(@AuthenticationPrincipal CustomUserDetails driver) {
        return ResponseEntity.ok(geoService.deliveries(driver.getId()));
    }
}
