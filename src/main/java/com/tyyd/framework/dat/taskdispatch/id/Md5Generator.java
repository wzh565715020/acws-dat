package com.tyyd.framework.dat.taskdispatch.id;

import com.tyyd.framework.dat.core.commons.utils.Md5Encrypt;

public class Md5Generator implements IdGenerator{
    @Override
    public String generate() {
        return Md5Encrypt.md5("");
    }
}
