package com.tyyd.framework.dat.taskdispatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class SpringTaskDispatchTest7 {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/taskdispatcher7.xml");
        System.in.read();
    }

}
