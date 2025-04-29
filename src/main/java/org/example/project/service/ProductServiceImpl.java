package org.example.project.service;

import org.example.project.exceptions.APIException;
import org.example.project.exceptions.ResourceNotFoundException;
import org.example.project.model.Cart;
import org.example.project.model.Category;
import org.example.project.model.Product;
import org.example.project.payload.CartDTO;
import org.example.project.payload.ProductDTO;
import org.example.project.payload.ProductResponse;
import org.example.project.repositories.CartRepository;
import org.example.project.repositories.CategoryRepository;
import org.example.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

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

    @Value("${image.base.url}")
    private String imageBaseUrl;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartService cartService;

    @Override
    public ProductDTO addProduct(long categoryId, ProductDTO productDTO) {

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));


        boolean isProductExist = false;

        List<Product> products = category.getProducts();
        for (Product product : products) {
            if (productDTO.getProductName().equals(product.getProductName())) {
                isProductExist = true;
                break;
            }
        }

        if (!isProductExist) {

            Product product = modelMapper.map(productDTO, Product.class);

            product.setCategory(category);
            double specialPrice =product.getPrice() - ((product.getDiscount() *0.01)*product.getPrice());
            product.setImage("default.png");
            product.setSpecialPrice(specialPrice);
            Product savedProduct = productRepository.save(product);

            return modelMapper.map(savedProduct, ProductDTO.class);
        }
        else
        {
            throw new APIException("Product with product name " + productDTO.getProductName() + " already exists");
        }



    }

    @Override
    public ProductResponse getProductByCategory(long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","categoryId",categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product> products = productPage.getContent();

        if (products.isEmpty())
            throw new APIException("There is no product in category "+ category.getCategoryName());

        List<ProductDTO> productDTOs = products.stream()
                .map(product -> modelMapper
                .map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();

        productResponse.setProducts(productDTOs);

        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder,String keyword,String category) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Specification<Product> spec =  Specification.where(null);
        if (keyword != null && !keyword.isEmpty()){
            spec = spec.and((root,query,criteriaBuilder)->
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("productName")), "%"+keyword.toLowerCase()+"%"));
        }
        if (category != null && !category.isEmpty()&& !category.equalsIgnoreCase("all")){
            spec = spec.and((root,query,criteriaBuilder)->
                    criteriaBuilder.like(root.get("category").get("categoryName"), category));
        }
        Page<Product> productPage = productRepository.findAll(spec,pageDetails);

        List<Product> products = productPage.getContent();


        if (products.isEmpty()) {
            throw new APIException("No products found.");
        }


        List<ProductDTO> productDTOs = products.stream()
                .map(product ->{
                    ProductDTO pDTO=modelMapper.map(product, ProductDTO.class);
                    pDTO.setImage(constructImageUrl(product.getImage()));
                    return pDTO;
                })
                .toList();

        ProductResponse productResponse = new ProductResponse();

        productResponse.setProducts(productDTOs);

        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;

    }

    private String constructImageUrl(String imageName) {
    return imageBaseUrl.endsWith("/") ? imageBaseUrl + imageName : imageBaseUrl + "/" + imageName;
    }

    @Override
    public ProductResponse getProductByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> productPage = productRepository.findByProductNameLikeIgnoreCase('%'+keyword+'%',pageDetails);

        List<Product> products = productPage.getContent();

        if (products.isEmpty()) {
            throw new APIException("No products found for keyword " + keyword);
        }

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper
                        .map(product, ProductDTO.class))
                .toList();

        ProductResponse productResponse = new ProductResponse();

        productResponse.setProducts(productDTOS);

        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setLastPage(productPage.isLast());

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        Product product = modelMapper.map(productDTO, Product.class);

        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());
        productFromDb.setSpecialPrice(product.getSpecialPrice());

        Product savedProduct = productRepository.save(productFromDb);

        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();

            cartDTO.setProducts(products);

            return cartDTO;

        }).toList();

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);
    }

//    @Override
//    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {
//        Product productFromDb = productRepository.findById(productId)
//                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
//
//        Product product = modelMapper.map(productDTO, Product.class);
//
//        productFromDb.setProductName(product.getProductName());
//        productFromDb.setDescription(product.getDescription());
//        productFromDb.setQuantity(product.getQuantity());
//        productFromDb.setDiscount(product.getDiscount());
//        productFromDb.setPrice(product.getPrice());
//        productFromDb.setSpecialPrice(product.getSpecialPrice());
//
//        Product savedProduct = productRepository.save(productFromDb);
//
//        List<Cart> carts = cartRepository.findCartsByProductId(productId);
//
//        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
//            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
//
//            List<ProductDTO> products = cart.getCartItems().stream()
//                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
//
//            cartDTO.setProducts(products);
//
//            return cartDTO;
//
//        }).toList();
//
//        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));
//
//        return modelMapper.map(savedProduct, ProductDTO.class);
//    }

    @Override
    public ProductDTO deleteProduct(long productId) {
        Product productToDelete = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product","productId",productId));

        List<Cart> carts = cartRepository.findCartsByProductId(productId);
        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));

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
