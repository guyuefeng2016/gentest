package com.gentest.gencode;

import lombok.Data;

import java.util.Map;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-21 11:00
 **/
@Data
public class SouceCodeInfo {

    private Object returnObj;
    private String returnType;
    private Integer argSize;
    private String methodName;
    private Map<String, String> mapSouceCode;

}
