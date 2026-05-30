package com.uoquo.test;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class NacosTest {

    @Test
    public void testPsw(){
        System.out.println(new BCryptPasswordEncoder().encode("nacos"));

    }
}
