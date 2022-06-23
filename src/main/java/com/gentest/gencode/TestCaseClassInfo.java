package com.gentest.gencode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * 测试用例类信息
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-21 13:24
 **/
@Data
public class TestCaseClassInfo {

    @ApiModelProperty(value = "测试用例包")
    private String testCaseRepositoryPackage;

    @ApiModelProperty(value = "测试用例类名")
    private String testCaseClassName;

    @ApiModelProperty(value = "方法信息")
    private List<TestCaseMethodInfo> testCaseMethodInfos;
}
