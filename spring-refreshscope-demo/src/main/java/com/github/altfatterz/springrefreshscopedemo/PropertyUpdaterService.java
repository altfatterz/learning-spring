package com.github.altfatterz.springrefreshscopedemo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PropertyUpdaterService {

    private Logger logger = LoggerFactory.getLogger(PropertyUpdaterService.class);

    private static final String DYNAMIC_PROPERTIES_SOURCE_NAME = "dynamicProperties";

    private ConfigurableEnvironment environment;

    public PropertyUpdaterService(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    public void updateProperty(String key, String value) {
        MutablePropertySources propertySources = environment.getPropertySources();
        if (!propertySources.contains(DYNAMIC_PROPERTIES_SOURCE_NAME)) {
            logger.debug("dynamicProperties PropertySource does not exist, creating the PopertySource and adding the key '{}' with value '{}'", key, value);
            Map<String, Object> dynamicProperties = new HashMap<>();
            dynamicProperties.put(key, value);
            // ensures that our dynamic properties take precedence over other properties in the environment
            propertySources.addFirst(new MapPropertySource(DYNAMIC_PROPERTIES_SOURCE_NAME, dynamicProperties));
        } else {
            logger.debug("dynamicProperties PropertySource exists, updating the key '{}' with value {}", key, value);
            MapPropertySource propertySource = (MapPropertySource) propertySources.get(DYNAMIC_PROPERTIES_SOURCE_NAME);
            propertySource.getSource().put(key, value);
        }
    }
}
