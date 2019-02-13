package xin.zero2one.context.support;

import org.apache.commons.lang3.StringUtils;
import xin.zero2one.beans.BeanWrapperImpl;
import xin.zero2one.beans.factory.config.BeanDefinition;
import xin.zero2one.beans.factory.support.BeanDefinitionReader;
import xin.zero2one.context.ApplicationContext;
import xin.zero2one.stereotype.Autowired;
import xin.zero2one.stereotype.Controller;
import xin.zero2one.stereotype.Service;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author ZJD
 * @date 2019/2/12
 */
public class MyApplicationContext implements ApplicationContext {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    private Map<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private Map<String, BeanWrapperImpl> beanWrapperMap = new ConcurrentHashMap<>();

    private Map<String, Object> beanCacheMap = new ConcurrentHashMap<>();


    public MyApplicationContext(String configLocations){

        this.configLocations = configLocations.split(",");
        refresh();

    }

    private void refresh() {
        //定位
        this.reader = new BeanDefinitionReader(this.configLocations);

        //加载
        Set<String> scannerClasses = reader.getScannerClasses();

        //注册
        doRegister(scannerClasses);


    }



    private void doRegister(Set<String> scannerClasses) {
        for(String clazzName : scannerClasses){
            Class<?> clazz = null;
            try {
                clazz = Class.forName(clazzName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("class not found " + clazz);
            }
            if (clazz.isInterface()){
                continue;
            }

            BeanDefinition beanDefinition = this.reader.registerBean(clazz);
            beanDefinitionMap.put(beanDefinition.getBeanFactoryName(), beanDefinition);

            Class<?>[] interfaces = clazz.getInterfaces();
            for(Class in : interfaces){
                beanDefinitionMap.put(in.getName(), beanDefinition);
            }
        }

        for(Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()){
            getBean(entry.getKey());
        }

    }


    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        if (null == beanDefinition){
            throw new RuntimeException("bean not found by name : " + beanName);
        }
        if (this.beanWrapperMap.containsKey(beanName)){
            return this.beanWrapperMap.get(beanName).getProxyBean();
        }
        Object instance = initializeBean(beanDefinition);

        BeanWrapperImpl beanWrapper = new BeanWrapperImpl(instance);
        this.beanWrapperMap.put(beanName, beanWrapper);
        return beanWrapper.getProxyBean();
    }

    private Object initializeBean(BeanDefinition beanDefinition){
        String beanFactoryName = beanDefinition.getBeanFactoryName();
        if (this.beanCacheMap.containsKey(beanFactoryName)){
            return this.beanCacheMap.get(beanFactoryName);
        } else {
            Class<?> clazz = null;
            Object instance = null;
            try {
                clazz = Class.forName(beanDefinition.getBeanClassName());
                instance = clazz.newInstance();
                populateBean(instance, beanDefinition);
                this.beanCacheMap.put(beanFactoryName, instance);
            } catch (ClassNotFoundException e) {
               throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }

            return instance;
        }
    }

    private void populateBean(Object instance, BeanDefinition beanDefinition) {
        Class<?> clazz = instance.getClass();
        if (!clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(Service.class)){
            return;
        }
        Field[] fields = clazz.getDeclaredFields();

        for(Field field : fields){
            Autowired autowired = field.getDeclaredAnnotation(Autowired.class);
            if (null == autowired) {
                continue;
            }
            String fieldName;
            if (StringUtils.isBlank(autowired.value().trim())){
                fieldName = field.getType().getName();
            } else {
                fieldName = autowired.value().trim();
            }

            if (!this.beanWrapperMap.containsKey(fieldName)){
//                String implClassName = getImplClass(fieldName);
                fieldName = getFactoryBeanName(fieldName);
                getBean(fieldName);
            }
            field.setAccessible(true);

            try {
                field.set(instance, this.beanWrapperMap.get(fieldName).getProxyBean());
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private String getFactoryBeanName(String className){
        return this.beanDefinitionMap
                .values().stream()
                .filter(beanDefinition -> className.equals(beanDefinition.getBeanClassName()))
                .findFirst()
                .get()
                .getBeanFactoryName();
    }

    private String getImplClass(String className){
        Class<?> fieldClazz = null;
        try {
            fieldClazz = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (fieldClazz.isInterface()){
            ServiceLoader<?> load = ServiceLoader.load(fieldClazz);
            Iterator<?> iterator = load.iterator();
            while (iterator.hasNext()){
                String name = iterator.next().getClass().getName();
                if (beanWrapperMap.containsKey(name)){
                    className = getImplClass(name);
                }
            }

        }
        return className;
    }
}
