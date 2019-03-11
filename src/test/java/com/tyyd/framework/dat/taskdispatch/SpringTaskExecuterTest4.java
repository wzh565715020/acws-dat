package com.tyyd.framework.dat.taskdispatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class SpringTaskExecuterTest4 {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/taskexecuter4.xml");
        System.in.read();
    }

}
