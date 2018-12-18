package com.tyyd.framework.dat.core;

import com.tyyd.framework.dat.core.constant.Constants;
import com.tyyd.framework.dat.core.spi.ServiceLoader;
import com.tyyd.framework.dat.remoting.serialize.RemotingSerializable;

import org.junit.Test;

/**
 * @author Robert HG (254963746@qq.com)
 */
public class ConstantsTest {

    @Test
    public void test() {
    	RemotingSerializable serializable  = ServiceLoader.load(RemotingSerializable.class, Constants.ADAPTIVE);
System.out.println(serializable.getId());
        System.out.println(Constants.USER_HOME + "/.task");
    }
}
