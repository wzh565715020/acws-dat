package com.tyyd.framework.dat.taskdispatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class SpringTaskExecuterTest6 {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/taskexecuter6.xml");
        System.in.read();
    }

}
