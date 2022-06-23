package com.gentest.gencode;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-21 11:00
 **/
@Data
public class CaseInput {


    @ApiModelProperty(value = "测试用例所在包名")
    private String testCasePackage;

    @ApiModelProperty(value = "测试用例输出位置")
    private String outputDir;

    @ApiModelProperty(value = "作者")
    private String author;

    @ApiModelProperty(value = "注释")
    private String comment;

}
