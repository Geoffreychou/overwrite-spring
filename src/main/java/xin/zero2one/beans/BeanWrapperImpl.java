package xin.zero2one.beans;

/**
 * @author ZJD
 * @date 2019/2/13
 */
public class BeanWrapperImpl implements BeanWrapper {

    private Object originalBean;

    private Object proxyBean;


    public BeanWrapperImpl(Object instance){
        this.originalBean = instance;
        this.proxyBean = instance;
    }

    public Object getOriginalBean() {
        return originalBean;
    }

    public Object getProxyBean() {
        return proxyBean;
    }
}
