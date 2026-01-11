package com.mycompany.sapo_leyendo.service;

import com.mycompany.sapo_leyendo.dto.LocationCreateRequest;
import com.mycompany.sapo_leyendo.model.Location;
import com.mycompany.sapo_leyendo.model.LocationType;
import com.mycompany.sapo_leyendo.model.Zone;
import com.mycompany.sapo_leyendo.repository.LocationRepository;
import com.mycompany.sapo_leyendo.repository.LocationTypeRepository;
import com.mycompany.sapo_leyendo.repository.ZoneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ZoneRepository zoneRepository;

    @Autowired
    private LocationTypeRepository locationTypeRepository;

    public List<Location> getAllLocations() {
        return locationRepository.findAll();
    }

    public Optional<Location> getLocationById(Integer id) {
        return locationRepository.findById(id);
    }

    public Optional<Location> getLocationByName(String name) {
        return locationRepository.findByName(name);
    }

    public Location saveLocation(Location location) {
        return locationRepository.save(location);
    }

    public void deleteLocation(Integer id) {
        locationRepository.deleteById(id);
    }

    // Zone methods
    public List<Zone> getAllZones() {
        return zoneRepository.findAll();
    }

    public Zone saveZone(Zone zone) {
        return zoneRepository.save(zone);
    }

    public Location createLocationFromRequest(LocationCreateRequest request) {
        Location location = new Location();
        location.setName(request.getName());
        location.setAisle(request.getAisle());
        location.setRack(request.getRack());
        location.setLevel(request.getLevel());
        location.setBin(request.getBin());
        location.setActive(request.getIsActive() != null ? request.getIsActive() : true);
        
        // Znajdź lub utwórz strefę
        if (request.getZone() != null) {
            Zone zone = zoneRepository.findByName(request.getZone())
                    .orElseGet(() -> {
                        Zone newZone = new Zone();
                        newZone.setName(request.getZone());
                        newZone.setTemperatureControlled(false);
                        newZone.setSecure(false);
                        newZone.setAllowMixedSku(true);
                        return zoneRepository.save(newZone);
                    });
            location.setZone(zone);
        }
        
        // Znajdź typ lokacji
        if (request.getLocationTypeId() != null) {
            locationTypeRepository.findById(request.getLocationTypeId())
                    .ifPresent(location::setLocationType);
        }
        
        return locationRepository.save(location);
    }

    // LocationType methods
    public List<LocationType> getAllLocationTypes() {
        return locationTypeRepository.findAll();
    }

    public LocationType saveLocationType(LocationType locationType) {
        return locationTypeRepository.save(locationType);
    }
}
