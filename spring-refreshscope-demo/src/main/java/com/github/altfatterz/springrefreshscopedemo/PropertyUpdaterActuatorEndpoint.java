package com.github.altfatterz.springrefreshscopedemo;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

@Component
@Endpoint(id = "routes-config")
public class PropertyUpdaterActuatorEndpoint {

    private PropertyUpdaterService propertyUpdaterService;

    public PropertyUpdaterActuatorEndpoint(PropertyUpdaterService propertyUpdaterService) {
        this.propertyUpdaterService = propertyUpdaterService;
    }

    @WriteOperation
    public String updateRoute(@Selector String name, String value) {
        propertyUpdaterService.updateProperty(name, value);
        return "Property updated. Remember to call the actuator /actuator/refresh";
    }
}
