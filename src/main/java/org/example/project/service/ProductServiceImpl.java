package org.example.project.service;

import org.example.project.exceptions.ResourceNotFoundException;
import org.example.project.model.Category;
import org.example.project.model.Product;
import org.example.project.payload.ProductDTO;
import org.example.project.payload.ProductResponse;
import org.example.project.repositories.CategoryRepository;
import org.example.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(long categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        Product product = modelMapper.map(productDTO, Product.class);

        product.setCategory(category);
        double specialPrice =product.getPrice() - ((product.getDiscount() *0.01)*product.getPrice());
        product.setImage("default.png");
        product.setSpecialPrice(specialPrice);
        Product savedProduct = productRepository.save(product);

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductResponse getProductByCategory(long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        List<Product> products = productRepository.findByCategoryOrderByPriceAsc(category);

        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper
                .map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse(productDTOS);

        return productResponse;
    }

    @Override
    public ProductResponse getAllProducts() {
        List<Product> products = productRepository.findAll();

        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse(productDTOs);

        return productResponse;

    }

    @Override
    public ProductResponse getProductByKeyword(String keyword) {

        List<Product> products = productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%');

        List<ProductDTO> productDTOS = products.stream().map(product -> modelMapper
                        .map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse(productDTOS);

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(long productId, ProductDTO newProductDTO) {

        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        Product newProduct = modelMapper.map(newProductDTO, Product.class);

        productToUpdate.setProductName(newProduct.getProductName());
        productToUpdate.setPrice(newProduct.getPrice());
        productToUpdate.setDiscount(newProduct.getDiscount());
        productToUpdate.setSpecialPrice(newProduct.getSpecialPrice());
        productToUpdate.setQuantity(newProduct.getQuantity());

        Product savedProduct = productRepository.save(productToUpdate);

       return modelMapper.map(savedProduct, ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(long productId) {
        Product productToDelete = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        productRepository.delete(productToDelete);

        return modelMapper.map(productToDelete, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(long productId, MultipartFile image) throws IOException {
        Product productToUpdate = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        
        String fileName = fileService.uploadImage(path, image);

        productToUpdate.setImage(fileName);
        Product updatedProduct = productRepository.save(productToUpdate);

        return modelMapper.map(updatedProduct, ProductDTO.class);
    }


}
