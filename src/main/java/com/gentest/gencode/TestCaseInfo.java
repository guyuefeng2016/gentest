package com.gentest.gencode;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-21 13:24
 **/
@Data
public class TestCaseInfo {
    private String testCaseRepositoryPackage;
    private String testCaseClassName;
    private List<Map<String, Object[]>> testCaseMethodsMapList;
    private List<Map<Integer, StringBuilder>> methodSourceCode;
    private List<Map<String, Object>> returnTypeList;
}
