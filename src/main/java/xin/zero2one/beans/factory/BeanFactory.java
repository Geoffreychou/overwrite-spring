package xin.zero2one.beans.factory;

/**
 * @author ZJD
 * @date 2019/2/12
 * BeanFactory 对IOC容器的基本行为作了定义
 */

public interface BeanFactory {


    String FACTORY_BEAN_PREFIX = "&";

    public Object getBean(String name);




}
