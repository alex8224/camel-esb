package com.example.demo;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService(endpointInterface = "com.example.demo.HelloWorld")
public class HelloWorldImpl implements HelloWorld{
    @WebMethod
    @Override
    public String sayHi(String greeting) {
        return "hello " + greeting + " world!";
    }
}
