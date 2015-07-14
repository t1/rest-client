package com.github.t1.rest;

import org.junit.Test;

public class FormBuilderTest {
    @Test
    public void shouldBuildEmptyForm() {
        FormBuilder.form().build();
    }
}
