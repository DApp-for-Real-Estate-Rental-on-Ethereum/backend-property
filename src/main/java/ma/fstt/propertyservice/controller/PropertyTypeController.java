package ma.fstt.propertyservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.CreatePropertyTypeRequest;
import ma.fstt.propertyservice.dto.responses.PropertyTypeResponse;
import ma.fstt.propertyservice.service.interfaces.PropertyTypeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/property-types")
@RequiredArgsConstructor
public class PropertyTypeController {

    private final PropertyTypeService propertyTypeService;

    @PostMapping
    public ResponseEntity<PropertyTypeResponse> create(
            @Valid @RequestBody CreatePropertyTypeRequest request) {
        return ResponseEntity.ok(propertyTypeService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PropertyTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CreatePropertyTypeRequest request) {
        return ResponseEntity.ok(propertyTypeService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyTypeResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(propertyTypeService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<PropertyTypeResponse>> getAll() {
        return ResponseEntity.ok(propertyTypeService.getAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        propertyTypeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
