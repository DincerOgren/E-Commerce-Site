package org.example.project.service;

import org.example.project.payload.CartDTO;
import org.example.project.payload.CartItemDTO;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface CartService {

    CartDTO addProductToCart( Long productId,  Integer quantity);

    List<CartDTO> getAllCarts();

    //CartDTO getCart(String email, Long cartId);
    CartDTO getCart();

    CartDTO updateProductQuantityInCart(Long productId, Integer delete);

    String deleteProductFromCart(Long cartId, Long productId);

    String createOrUpdateCartWithItems(List<CartItemDTO> cartItems);

    void updateProductInCarts(Long cartId, Long productId);
}
