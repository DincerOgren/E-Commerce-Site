package org.example.project.controller;

import org.example.project.model.Cart;
import org.example.project.payload.CartDTO;
import org.example.project.repositories.CartRepository;
import org.example.project.service.CartService;
import org.example.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class CartController {

    @Autowired
    private CartService cartService;


    @PostMapping("/carts/products/{productId}/quantity/{quantity}")
    public ResponseEntity<CartDTO> addProductToCart(@PathVariable Long productId,
                                                    @PathVariable Integer quantity) {

        CartDTO cartDTO = cartService.addProductToCart(productId, quantity);

        return ResponseEntity.ok(cartDTO);
    }


    @GetMapping("/carts")
    public  ResponseEntity<List<CartDTO>> getCarts() {
        List<CartDTO> cartDTOs = cartService.getAllCarts();

        return ResponseEntity.ok(cartDTOs);
    }

    @GetMapping("/carts/users/cart")
    public ResponseEntity<CartDTO> getUserCart() {
        //String email = authUtil.loggedInEmail();
        //Cart cart = cartRepository.findCartByEmail(email);

        // if u want multiple products in future
//        CartDTO cartDTO = cartService.getCart(email,cart.getCartId());
        CartDTO cartDTO = cartService.getCart();

        return ResponseEntity.ok(cartDTO);
    }

    @PutMapping("/cart/products/{productId}/quantity/{operation}")
    public ResponseEntity<CartDTO> updateCartProduct(@PathVariable Long productId,
                                                     @PathVariable String operation) {

        CartDTO cartDTO = cartService.updateProductQuantityInCart(productId,
                operation.equalsIgnoreCase("delete")?-1:1);

        return ResponseEntity.ok(cartDTO);
    }

    @DeleteMapping("carts/{cartId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromCart(@PathVariable Long cartId,
                                                        @PathVariable Long productId) {
        String status = cartService.deleteProductFromCart(cartId,productId);

        return ResponseEntity.ok(status);
    }
}
