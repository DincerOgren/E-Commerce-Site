package org.example.project.service;

import org.example.project.exceptions.APIException;
import org.example.project.exceptions.ResourceNotFoundException;
import org.example.project.model.Category;
import org.example.project.payload.CategoryDTO;
import org.example.project.payload.CategoryResponse;
import org.example.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {
    //private List<Category> categories = new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CategoryResponse getCategories(Integer pageNumber, Integer pageSize,String sortBy,String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageDetails = PageRequest.of(pageNumber, pageSize,sortByAndOrder);
        Page<Category> categoryPage  = categoryRepository.findAll(pageDetails);

        List<Category> categories = categoryPage.getContent();

       if(categories.isEmpty()){
            throw new APIException("No Category Found");
       }

       List<CategoryDTO> categoriesDTO = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();


        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoriesDTO);

        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());


        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category existCat = categoryRepository.findByCategoryName(categoryDTO.getCategoryName());
        if (existCat != null) {
            throw new APIException("Cant create category with name "+categoryDTO.getCategoryName()+" because it already exists");
        }

        Category catToSave = modelMapper.map(categoryDTO,Category.class);

        categoryRepository.save(catToSave);

        return categoryDTO;
    }

    //        Category catToDelete = categories.stream()
//                .filter( c -> c.getCategoryId().equals(categoryId))
//                .findFirst().orElseThrow(null);
    @Override
    public CategoryDTO removeCategory(Long categoryId) {

        Category categoryToRemove = categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","catID",categoryId));

        categoryRepository.delete(categoryToRemove);

        return modelMapper.map( categoryToRemove,CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(Long categoryId, CategoryDTO categoryDTO) {
        Optional<Category> optionalCategory =  categoryRepository.findById(categoryId);
        Category categoryToUpdate = optionalCategory
                .orElseThrow(() -> new ResourceNotFoundException("Category","catID",categoryId));


       // categoryToUpdate.setCategoryId(category.getCategoryId());
        Category category = modelMapper.map(categoryDTO,Category.class);
        categoryToUpdate.setCategoryName(category.getCategoryName());

        categoryToUpdate =  categoryRepository.save(categoryToUpdate);

        return modelMapper.map(categoryToUpdate,CategoryDTO.class);

    }



}
