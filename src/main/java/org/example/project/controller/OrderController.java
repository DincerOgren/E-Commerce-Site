package org.example.project.controller;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.example.project.payload.OrderDTO;
import org.example.project.payload.OrderRequestDTO;
import org.example.project.payload.StripePaymentDTO;
import org.example.project.service.OrderService;
import org.example.project.service.StripeService;
import org.example.project.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    OrderService orderService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    StripeService stripeService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod,
                                                  @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        OrderDTO order = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(order, HttpStatus.CREATED);
    }

    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> createStripeClientSecret(@RequestBody StripePaymentDTO stripePaymentDTO) throws StripeException {

        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDTO);

        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);

    }
}