package com.example.demo;

import javax.jws.WebService;

@WebService
public interface HelloWorld {
    String sayHi(String greeting);
}