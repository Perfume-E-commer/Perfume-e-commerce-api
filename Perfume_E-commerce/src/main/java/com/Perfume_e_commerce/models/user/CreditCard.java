package com.Perfume_e_commerce.models.user;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreditCard {
    private String id;
    private String cardNumber;
    private String cardHolderName;
    private String expiryDate;
    private String cvv;

    private String cardType;

    public String getMaskedNumber() {
        if (cardNumber != null && cardNumber.length() >= 4) {
            return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
        }
        return cardNumber;
    }
}
