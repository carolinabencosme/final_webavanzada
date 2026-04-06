package com.hospedaje.cartorder.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CartItemTest {

    @Test
    void prePersistShouldDefaultQuantityToOneWhenMissing() {
        CartItem cartItem = CartItem.builder().build();

        cartItem.applySafeDefaults();

        assertThat(cartItem.getQuantity()).isEqualTo(1);
    }
}
