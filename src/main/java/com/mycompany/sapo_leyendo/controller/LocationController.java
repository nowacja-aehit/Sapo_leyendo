package com.mycompany.sapo_leyendo.controller;

import com.mycompany.sapo_leyendo.dto.LocationCreateRequest;
import com.mycompany.sapo_leyendo.model.Location;
import com.mycompany.sapo_leyendo.model.LocationType;
import com.mycompany.sapo_leyendo.model.Zone;
import com.mycompany.sapo_leyendo.service.LocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class LocationController {

    @Autowired
    private LocationService locationService;

    @GetMapping
    public List<Location> getAllLocations() {
        return locationService.getAllLocations();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Location> getLocationById(@PathVariable Integer id) {
        return locationService.getLocationById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Location createLocation(@RequestBody LocationCreateRequest request) {
        return locationService.createLocationFromRequest(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLocation(@PathVariable Integer id) {
        locationService.deleteLocation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/zones")
    public List<Zone> getAllZones() {
        return locationService.getAllZones();
    }

    @PostMapping("/zones")
    public Zone createZone(@RequestBody Zone zone) {
        return locationService.saveZone(zone);
    }

    @GetMapping("/types")
    public List<LocationType> getAllLocationTypes() {
        return locationService.getAllLocationTypes();
    }

    @PostMapping("/types")
    public LocationType createLocationType(@RequestBody LocationType locationType) {
        return locationService.saveLocationType(locationType);
    }
}
