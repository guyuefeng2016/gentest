package com.gentest.gencode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试用例方法信息
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-21 13:24
 **/
@Data
public class TestCaseMethodInfo {

    @ApiModelProperty(value = "测试用例方法名")
    private String testCaseMethodName;

    @ApiModelProperty(value = "参数个数")
    private Integer methodArgCount;

    @ApiModelProperty(value = "souceCode")
    private Map<Integer, StringBuilder> methodSourceCode = new LinkedHashMap<>();

    @ApiModelProperty(value = "返回类型")
    private String returnType;

    @ApiModelProperty(value = "返回数据")
    private Object returnObj;
}
