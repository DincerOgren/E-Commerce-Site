package org.example.project.controller;

import jakarta.validation.Valid;
import org.example.project.config.AppConstants;
import org.example.project.payload.CategoryDTO;
import org.example.project.payload.CategoryResponse;
import org.example.project.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class CategoryController {

    // ORM classını otomatik database e kaydediyo query ile kurmana gerek kalmıyo
    // JPA is kinda like orm but in java process i believe


    @Autowired
    private CategoryService categoryService;
//    public CategoryController(CategoryService categoryService) {
//
//        this.categoryService = categoryService;
//    }




//    @GetMapping("/echo")                                        // required mesaj yazmassan error vermiyo
//    public ResponseEntity<String> echoMessage(@RequestParam(name = "message",required = false) String message) {
//    //public ResponseEntity<String> echoMessage(@RequestParam(name = "message",defaultValue = "Hello World!") String message)
//        return new ResponseEntity<>(message, HttpStatus.OK);
//    }
    @GetMapping("/public/categories")
    //@RequestMapping(value ="/public/categories" ,method = RequestMethod.GET)
    public ResponseEntity<CategoryResponse> getAllCategories(
            @RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER,required = false) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE,required = false) Integer pageSize,
            @RequestParam(name = "sortBy",defaultValue = AppConstants.SORT_CATEGORIES_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder",defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder, Sort sort){

        CategoryResponse categories = categoryService.getCategories(pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(categories,HttpStatus.OK);
    }

    @PostMapping(("/admin/categories"))
    //@RequestMapping(value ="/admin/categories" ,method = RequestMethod.POST)
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CategoryDTO categoryDTO) {
        CategoryDTO createdCat = categoryService.createCategory(categoryDTO);

//        return ResponseEntity.status(HttpStatus.OK).body("CCreated succesfully");
//        return  ResponseEntity.ok("Created Successfully");

        return new ResponseEntity<>(createdCat, HttpStatus.CREATED);
    }
    @DeleteMapping(("/admin/categories/{categoryId}"))
    //@RequestMapping(value ="/admin/categories/{categoryId}" ,method = RequestMethod.DELETE)
    public ResponseEntity<CategoryDTO> deleteCategory(@PathVariable Long categoryId) {

        return new ResponseEntity<>(categoryService.removeCategory(categoryId), HttpStatus.OK);
    }
    @PutMapping(("/admin/categories/{categoryId}"))
    //@RequestMapping(value ="/admin/categories/{categoryId}" ,method = RequestMethod.PUT)
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long categoryId,@Valid @RequestBody CategoryDTO categoryDTO) {

        return new ResponseEntity<>(categoryService.updateCategory(categoryId,categoryDTO), HttpStatus.OK);
    }
}
