package ma.fstt.propertyservice.service.interfaces;

import ma.fstt.propertyservice.dto.requests.CreatePropertyTypeRequest;
import ma.fstt.propertyservice.dto.responses.PropertyTypeResponse;

import java.util.List;

public interface PropertyTypeService {

    PropertyTypeResponse create(CreatePropertyTypeRequest request);

    PropertyTypeResponse update(Long id, CreatePropertyTypeRequest request);

    PropertyTypeResponse getById(Long id);

    List<PropertyTypeResponse> getAll();

    void delete(Long id);

    boolean existsById(Long id);
}
