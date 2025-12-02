package com.Perfume_e_commerce.models.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreditCard {
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    public void maskCardNumber() {
        if (cardNumber != null && cardNumber.length() > 4) {
            this.cardNumber = "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
    }
}
