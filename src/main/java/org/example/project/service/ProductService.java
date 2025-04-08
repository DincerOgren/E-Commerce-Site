package org.example.project.service;

import jakarta.validation.Valid;
import org.example.project.model.Product;
import org.example.project.payload.ProductDTO;
import org.example.project.payload.ProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {
    ProductDTO addProduct(long categoryId, @Valid ProductDTO productDTO);

    ProductResponse getProductByCategory(long categoryId);

    ProductResponse getAllProducts();

    ProductResponse getProductByKeyword(String keyword);

    ProductDTO updateProduct(long productId, ProductDTO productDTO);

    ProductDTO deleteProduct(long productId);

    ProductDTO updateProductImage(long productId, MultipartFile image) throws IOException;
}
