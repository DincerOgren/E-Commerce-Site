package org.example.project.service;

import org.example.project.payload.CategoryDTO;
import org.example.project.payload.CategoryResponse;


public interface CategoryService {

    CategoryResponse getCategories(Integer pageNum, Integer pageSize,String sortBy,String sortOrder);

    CategoryDTO createCategory(CategoryDTO category);

    CategoryDTO removeCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
}
