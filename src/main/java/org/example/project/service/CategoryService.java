package org.example.project.service;

import org.example.project.model.Category;
import org.example.project.payload.CategoryDTO;
import org.example.project.payload.CategoryResponse;

import java.util.List;

public interface CategoryService {

    CategoryResponse getCategories(Integer pageNum, Integer pageSize,String sortBy,String sortOrder);

    CategoryDTO createCategory(CategoryDTO category);

    CategoryDTO removeCategory(Long categoryId);

    CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO);
}
