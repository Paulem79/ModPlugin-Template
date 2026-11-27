package com.example.template.common;

import com.example.template.common.wrapper.LoggerWrapper;

public interface Template<T> {
    T get();

    LoggerWrapper logger();
}
