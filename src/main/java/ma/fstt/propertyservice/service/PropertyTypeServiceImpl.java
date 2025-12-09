package ma.fstt.propertyservice.service;

import lombok.RequiredArgsConstructor;
import ma.fstt.propertyservice.dto.requests.CreatePropertyTypeRequest;
import ma.fstt.propertyservice.dto.responses.PropertyTypeResponse;
import ma.fstt.propertyservice.model.PropertyType;
import ma.fstt.propertyservice.repository.PropertyTypeRepository;
import ma.fstt.propertyservice.service.interfaces.PropertyTypeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyTypeServiceImpl implements PropertyTypeService {

    private final PropertyTypeRepository repository;

    @Override
    public PropertyTypeResponse create(CreatePropertyTypeRequest request) {
        PropertyType type = new PropertyType();
        type.setType(request.getType());

        PropertyType saved = repository.save(type);

        return mapToResponse(saved);
    }

    @Override
    public PropertyTypeResponse update(Long id, CreatePropertyTypeRequest request) {
        PropertyType type = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property type not found"));

        type.setType(request.getType());
        PropertyType updated = repository.save(type);

        return mapToResponse(updated);
    }

    @Override
    public PropertyTypeResponse getById(Long id) {
        PropertyType type = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property type not found"));

        return mapToResponse(type);
    }

    @Override
    public List<PropertyTypeResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Property type not found");
        }
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id);
    }

    private PropertyTypeResponse mapToResponse(PropertyType type) {
        PropertyTypeResponse r = new PropertyTypeResponse();
        r.setId(type.getId());
        r.setType(type.getType());
        return r;
    }
}
