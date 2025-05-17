package org.example.project.service;

import jakarta.transaction.Transactional;
import org.example.project.exceptions.APIException;
import org.example.project.exceptions.ResourceNotFoundException;
import org.example.project.model.Cart;
import org.example.project.model.CartItem;
import org.example.project.model.Product;
import org.example.project.payload.CartDTO;
import org.example.project.payload.CartItemDTO;
import org.example.project.payload.ProductDTO;
import org.example.project.repositories.CartItemRepository;
import org.example.project.repositories.CartRepository;
import org.example.project.repositories.ProductRepository;
import org.example.project.util.AuthUtil;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private AuthUtil authUtil;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        //Find existing cart or create one
        Cart cart = createCart();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product" ,"productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cart.getCartId(), productId);

        if(cartItem != null) {
            throw new APIException("Product "+product.getProductName()+" already exist in cart.");
        }

        if (product.getQuantity() == 0) {
            throw new APIException("Product is not available");
        }

        if(quantity>=product.getQuantity()) {
            throw new APIException("Quantity exceeds limit, max limit: "+product.getQuantity());
        }



        CartItem newCartItem = new CartItem();

        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());

        cartItemRepository.save(newCartItem);

        // Generally like this, but we don't do it for test purposes
        //product.setQuantity(product.getQuantity() - quantity);

        product.setQuantity(product.getQuantity());

        cart.setTotalPrice(cart.getTotalPrice()+(product.getSpecialPrice()*quantity));

        cartRepository.save(cart);

        CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

        List<CartItem> cartItems = cart.getCartItems();


        Stream<ProductDTO> productStream = cartItems.stream().map(item ->{
            ProductDTO map = modelMapper.map(item, ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });

        cartDTO.setProducts(productStream.collect(Collectors.toList()));

        return cartDTO;

    }

    @Override
    public List<CartDTO> getAllCarts() {
        if(cartRepository.count() == 0) {
            throw new APIException("No cart found");
        }

        List<Cart> carts = cartRepository.findAll();

        List<CartDTO> cartDTOList = carts.stream().map(cart ->{
            CartDTO cartDTO = modelMapper.map(cart,CartDTO.class);

            List<ProductDTO> productDTOs = cart.getCartItems().stream().map(cartItem->{
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(), ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity());
                return productDTO;
            }).toList();


            cartDTO.setProducts(productDTOs);
            return cartDTO;
        }).toList();

        return cartDTOList;
    }

    @Override
    public CartDTO getCart() {
        String email = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(email);
        if (userCart == null) {
            throw new APIException("No cart found");
        }

        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);
        userCart.getCartItems().forEach(cartItem -> cartItem.getProduct().setQuantity(cartItem.getQuantity()));

        List<ProductDTO> products = userCart.getCartItems().stream()
                .map(item-> modelMapper.map(item.getProduct(),ProductDTO.class))
                .toList();

        cartDTO.setProducts(products);

        return cartDTO;

    }


    @Transactional
    @Override
    public CartDTO updateProductQuantityInCart(Long productId, Integer quantity) {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if (userCart == null) {
            throw new ResourceNotFoundException("Cart","email",authUtil.loggedInEmail());
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product" ,"productId",productId));


        if (product.getQuantity() == 0){
            throw new APIException(product.getProductName()+" is not available");
        }


        if (product.getQuantity() < quantity) {

            throw new APIException("Quantity exceeds limit max limit: "+product.getQuantity());

        }

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(userCart.getCartId(),productId);

        if (cartItem == null) {
            throw new APIException("Product "+product.getProductName()+" does not exist in cart.");
        }

        int newQuantity = cartItem.getQuantity() + quantity;

        if(newQuantity<0){
            throw new APIException("The resulting quantity is negative");
        }

        boolean isDeleted =false;
        if(newQuantity == 0){
            deleteProductFromCart(userCart.getCartId(),productId);
            isDeleted = true;
        }
        else {

            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItem.setDiscount(product.getDiscount());
            userCart.setTotalPrice(userCart.getTotalPrice() + (cartItem.getProductPrice() * quantity));
            cartRepository.save(userCart);
        }


        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0 && !isDeleted){

            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }



        CartDTO cartDTO = modelMapper.map(userCart,CartDTO.class);

        List<ProductDTO> productDTOS = userCart.getCartItems().stream().map(item ->{
            ProductDTO pDTO = modelMapper.map(item.getProduct(),ProductDTO.class);
            pDTO.setQuantity(item.getQuantity());
            return pDTO;
        }).toList();

        cartDTO.setProducts(productDTOS);


        return cartDTO;

    }


    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId,Long productId) {

        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(()-> new ResourceNotFoundException("Cart","cartId",cartId));

         Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product" ,"productId",productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId,productId);
        if (cartItem==null){
            throw new APIException("Product "+product.getProductName()+" does not exist in cart.");
        }


        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));

        // If u remove product stock while adding to cart use this as get stocks back
//        Product cartItemProduct = cartItem.getProduct();
//        cartItemProduct.setQuantity(product.getQuantity() + cartItem.getQuantity());
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId,productId);

        // I think this is necessary
        //cartRepository.save(cart);

        return "Product "+product.getProductName()+" successfully deleted from cart.";
    }

    @Transactional
    @Override
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {

        String email = authUtil.loggedInEmail();

        Cart existingCart = cartRepository.findCartByEmail(email);

        if (existingCart == null) {
            existingCart = new Cart();
            existingCart.setTotalPrice(0.0);
            existingCart.setUser(authUtil.loggedInUser());
            existingCart = cartRepository.save(existingCart);
        } else
        {
            cartItemRepository.deleteAllByCartId(existingCart.getCartId());
        }

        double totalPrice = 0.0;

        for (CartItemDTO cartItemDTO : cartItems) {
            Long productId= cartItemDTO.getProductId();
            Integer quantity = cartItemDTO.getQuantity();

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product" ,"productId",productId));

            //product.setQuantity(product.getQuantity() - quantity);
            totalPrice+=product.getSpecialPrice()*quantity;

            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setCart(existingCart);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItemRepository.save(cartItem);

        }

        existingCart.setTotalPrice(totalPrice);
        cartRepository.save(existingCart);
        return "Cart successfully created/updated.";
    }


    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);

        if (cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart!!!");
        }

        double cartPrice = cart.getTotalPrice()
                - (cartItem.getProductPrice() * cartItem.getQuantity());

        cartItem.setProductPrice(product.getSpecialPrice());

        cart.setTotalPrice(cartPrice
                + (cartItem.getProductPrice() * cartItem.getQuantity()));

        cartItem = cartItemRepository.save(cartItem);
    }



    private Cart createCart(){
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null){
            return userCart;
        }

        Cart cart = new Cart();
        cart.setTotalPrice(0.0);
        cart.setUser(authUtil.loggedInUser());
        Cart newCart = cartRepository.save(cart);

        return newCart;
    }
}
