package com.gentest.gencode;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;
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
    private Map<String, String> mapSouceCode;

}
