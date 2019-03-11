package com.tyyd.framework.dat.taskdispatch;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;

public class SpringTaskDispatchTest5 {

    public static void main(String[] args) throws IOException {
        new ClassPathXmlApplicationContext("/spring/taskdispatcher5.xml");
        System.in.read();
    }

}
