package com.example.qrcheckin;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
// OpenAI, 2024, ChatGPT, Test Initials function
public class ProfileImageGeneratorTest {

    @Test
    public void getInitials_ReturnsCorrectInitials() {
        String name = "John Doe";
        String initials = ProfileImageGenerator.getInitials(name);
        assertEquals("JD", initials);
    }

    @Test
    public void test_single_word() {
        String name = "Alice";
        String initials = ProfileImageGenerator.getInitials(name);
        assertEquals("A", initials);
    }

    @Test
    public void test_three_words() {
        String name = "John Doe Smith";
        String initials = ProfileImageGenerator.getInitials(name);
        assertEquals("JDS", initials);
    }
}

