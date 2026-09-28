package se.lexicon.ecommerce.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.lexicon.ecommerce.domain.Category;
import se.lexicon.ecommerce.dto.CategoryResponse;
import se.lexicon.ecommerce.exception.DuplicateResourceException;
import se.lexicon.ecommerce.exception.InvalidRequestException;
import se.lexicon.ecommerce.exception.ResourceNotFoundException;
import se.lexicon.ecommerce.repository.CategoryRepository;

import java.util.List;
import java.util.Objects;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = Objects.requireNonNull(categoryRepository, "categoryRepository must not be null");
    }

    @Override
    @Transactional
    public CategoryResponse create(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestException("category name must not be blank");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > 100) {
            throw new InvalidRequestException("category name must be at most 100 characters");
        }
        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            throw new DuplicateResourceException("category already exists: " + normalizedName);
        }

        return toResponse(categoryRepository.save(new Category(normalizedName)));
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Objects.requireNonNull(id, "category id must not be null");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("category not found: " + id));
        return toResponse(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).toList();
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName());
    }
}
