package se.lexicon.ecommerce.service;

import se.lexicon.ecommerce.dto.ProductRequest;
import se.lexicon.ecommerce.dto.ProductResponse;

import java.util.List;

public interface ProductService {

    ProductResponse create(ProductRequest request);

    ProductResponse findById(Long id);

    List<ProductResponse> findAll();

    List<ProductResponse> searchByName(String name);
}
