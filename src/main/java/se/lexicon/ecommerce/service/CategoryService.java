package se.lexicon.ecommerce.service;

import se.lexicon.ecommerce.dto.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse create(String name);

    CategoryResponse findById(Long id);

    List<CategoryResponse> findAll();
}
