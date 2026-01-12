package com.Perfume_e_commerce.controllers;
import com.Perfume_e_commerce.dto.request.PaymentRequest;
import com.Perfume_e_commerce.dto.response.PaymentResponse;
import com.Perfume_e_commerce.services.PaymentService;
import com.stripe.exception.StripeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-intent")
    public ResponseEntity<PaymentResponse> createPaymentIntent(@RequestBody PaymentRequest request) {
        try {
            String currency = (request.getCurrency() != null) ? request.getCurrency() : "usd";

            String clientSecret = paymentService.createPaymentIntent(
                    request.getAmount(),
                    currency,
                    request.getEmail()
            );

            return ResponseEntity.ok(new PaymentResponse(clientSecret));
        } catch (StripeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}
