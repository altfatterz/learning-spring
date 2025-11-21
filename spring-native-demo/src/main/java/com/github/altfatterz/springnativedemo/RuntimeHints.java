package com.github.altfatterz.springnativedemo;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

public class RuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(org.springframework.aot.hint.RuntimeHints hints, ClassLoader classLoader) {
        hints.reflection().registerType(InternalCustomer.class, MemberCategory.DECLARED_FIELDS);
    }
}
