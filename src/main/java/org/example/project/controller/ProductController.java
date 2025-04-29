package org.example.project.controller;

import jakarta.validation.Valid;
import org.example.project.config.AppConstants;
import org.example.project.payload.ProductDTO;
import org.example.project.payload.ProductResponse;
import org.example.project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    private ProductService productService;


    @PostMapping(("/admin/categories/{categoryId}/product"))
    //@RequestMapping(value ="/admin/categories" ,method = RequestMethod.POST)
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable long categoryId) {
        ProductDTO createdProductDTO = productService.addProduct(categoryId, productDTO);

//        return ResponseEntity.status(HttpStatus.OK).body("CCreated successfully");
//        return  ResponseEntity.ok("Created Successfully");

        return new ResponseEntity<>(createdProductDTO, HttpStatus.CREATED);
    }


    @GetMapping(("/public/products"))
    public ResponseEntity<ProductResponse> getAllProducts(
            @RequestParam(name = "keyword",required = false) String keyword,
            @RequestParam(name = "category",required = false) String category,
            @RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder
    ) {

        ProductResponse productResponse = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrder,keyword,category);

        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping(("/public/categories/{categoryId}/products"))
    public ResponseEntity<ProductResponse> getProductByCategory(
            @RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder,
            @PathVariable long categoryId) {
        ProductResponse productResponse = productService.getProductByCategory(categoryId,pageNumber,pageSize,sortBy,sortOrder);

        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping(("/public/products/keyword/{keyword}"))
    public ResponseEntity<ProductResponse> getProductByKeyword(
            @RequestParam(name = "pageNumber",defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
            @RequestParam(name = "pageSize",defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
            @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_PRODUCTS_BY,required = false) String sortBy,
            @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR,required = false) String sortOrder,
            @PathVariable String keyword) {
        ProductResponse productResponse = productService.getProductByKeyword(keyword,pageNumber,pageSize,sortBy,sortOrder);

        return new ResponseEntity<>(productResponse, HttpStatus.OK);

    }

    @PutMapping(("/admin/products/{productId}"))
    //@RequestMapping(value ="/admin/categories" ,method = RequestMethod.POST)
    public ResponseEntity<ProductDTO> updateProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable long productId) {

        ProductDTO updatedProduct = productService.updateProduct(productId, productDTO);

//        return ResponseEntity.status(HttpStatus.OK).body("CCreated successfully");
//        return  ResponseEntity.ok("Created Successfully");

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }

    @DeleteMapping("/admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable long productId) {
        ProductDTO deletedProduct = productService.deleteProduct(productId);

        return new ResponseEntity<>(deletedProduct, HttpStatus.OK);
    }

    @PutMapping("/admin/products/{productId}/image")
    public  ResponseEntity<ProductDTO> updateProductImage(@PathVariable long productId,
                                                          @RequestParam("image")MultipartFile image) throws IOException {
        ProductDTO updatedProduct = productService.updateProductImage(productId,image);

        return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
    }
}