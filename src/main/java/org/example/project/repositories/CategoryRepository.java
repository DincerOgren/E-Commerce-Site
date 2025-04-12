package org.example.project.repositories;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.project.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> getAllByCategoryId(Long categoryId);

    Category findByCategoryName(@NotBlank @Size(min=3, message = "Category name should bigger than 3 characters") String categoryName);
}
