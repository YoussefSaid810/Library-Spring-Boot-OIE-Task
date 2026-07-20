package com.example.library.bdd;

import com.example.library.LibraryApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

@CucumberContextConfiguration
@SpringBootTest(classes = LibraryApplication.class)
public class CucumberSpringConfiguration {
}
