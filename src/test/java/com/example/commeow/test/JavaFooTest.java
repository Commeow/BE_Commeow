package com.example.commeow.test;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
class JavaFooTest {

    private JavaFoo javaFoo = new JavaFoo();

    @Test
    public void partiallyCoveredHelloMethodTest() {
        String actual = javaFoo.hello("펭");
        assertEquals(actual, "하");
    }

}