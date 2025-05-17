package org.example.project.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import org.example.project.payload.StripePaymentDTO;

public interface StripeService {

    PaymentIntent paymentIntent(StripePaymentDTO stripePaymentDTO) throws StripeException;
}
