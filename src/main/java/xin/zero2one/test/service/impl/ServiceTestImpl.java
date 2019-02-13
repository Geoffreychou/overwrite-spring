package xin.zero2one.test.service.impl;

import xin.zero2one.stereotype.Service;
import xin.zero2one.test.service.IServiceTest;


/**
 * @author ZJD
 * @date 2019/2/13
 */
@Service
public class ServiceTestImpl implements IServiceTest {

    @Override
    public void hello(String msg) {
        System.out.println("hello msg : " + msg);
    }

}
