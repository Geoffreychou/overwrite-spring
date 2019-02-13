package xin.zero2one.beans.factory.support;

import org.apache.commons.lang3.StringUtils;
import xin.zero2one.beans.factory.config.BeanDefinition;
import xin.zero2one.stereotype.Controller;
import xin.zero2one.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author ZJD
 * @date 2019/2/12
 */
public class BeanDefinitionReader {

    private Map<String,String> configs = new ConcurrentHashMap<>();

    private Set<String> scannerClasses = new HashSet<>();

    private static final String SCAN_PACKAGE_KEY = "scanPackage";

    public BeanDefinitionReader(String... configLocations) {
        for (String configLocation : configLocations){
            try {
                loadConfigs(configLocation.replace("classpath:", ""));
            } catch (IOException e) {
               throw new RuntimeException("read config file error");
            }
        }
        String scanPackage = configs.get(SCAN_PACKAGE_KEY);
        doScanner(scanPackage);
    }

    private void loadConfigs(String configLocation) throws IOException {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(configLocation);
        Properties properties = new Properties();
        try {
            properties.load(in);
        } catch (IOException e) {
            throw e;
        }
        for(Map.Entry<Object,Object> entry : properties.entrySet()){
            configs.put((String)entry.getKey(), (String)entry.getValue());
        }
    }

    private void doScanner(String scannerPackage){
        URL resource = this.getClass().getClassLoader().getResource(StringUtils.replace(scannerPackage, ".", "/"));
        File basePackage = new File(resource.getFile());
        File[] files = basePackage.listFiles();
        for (File file : files){
            if (file.isDirectory()){
                doScanner(scannerPackage + "." + file.getName());
            } else {
                scannerClasses.add(scannerPackage + "." + StringUtils.substringBefore(file.getName(), ".class"));
            }
        }
    }

    public Map<String, String> getConfigs() {
        return configs;
    }

    public Set<String> getScannerClasses() {
        return scannerClasses;
    }

    public BeanDefinition registerBean(Class<?> clazz){
        BeanDefinition beanDefinition = new BeanDefinition();
        beanDefinition.setBeanClassName(clazz.getName());
        beanDefinition.setBeanFactoryName(getBeanFactoryName(clazz));
        return beanDefinition;
    }

    private String getBeanFactoryName(Class<?> clazz){
        if (clazz.isAnnotationPresent(Controller.class)){
            Controller controller = clazz.getDeclaredAnnotation(Controller.class);
            if (StringUtils.isNotBlank(controller.value().trim())){
                return controller.value().trim();
            }
        }
        if (clazz.isAnnotationPresent(Service.class)){
            Service service = clazz.getDeclaredAnnotation(Service.class);
            if (StringUtils.isNotBlank(service.value().trim())){
                return service.value().trim();
            }
        }

        return StringUtils.uncapitalize(StringUtils.substringAfterLast(clazz.getName(), "."));
    }
}
