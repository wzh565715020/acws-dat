package com.tyyd.framework.dat.taskdispatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class SpringTaskExecuterTest5 {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/taskexecuter5.xml");
        System.in.read();
    }

}
