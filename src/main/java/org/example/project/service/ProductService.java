package org.example.project.service;

import jakarta.validation.Valid;
import org.example.project.payload.ProductDTO;
import org.example.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO addProduct(long categoryId, @Valid ProductDTO productDTO);


    ProductResponse getProductByCategory(long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponse getProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,String keyword,String category);


    ProductDTO updateProduct(Long productId, ProductDTO productDTO);

    ProductDTO deleteProduct(long productId);

    ProductDTO updateProductImage(long productId, MultipartFile image) throws IOException;

}
