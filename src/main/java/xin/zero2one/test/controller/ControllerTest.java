package xin.zero2one.test.controller;

import xin.zero2one.stereotype.Autowired;
import xin.zero2one.stereotype.Controller;
import xin.zero2one.test.service.impl.ServiceTestImpl;

/**
 * @author ZJD
 * @date 2019/2/13
 */
@Controller("ctest")
public class ControllerTest {

    @Autowired
    private ServiceTestImpl serviceTest;


    public void hello(String msg){
        serviceTest.hello(msg);
    }
}
