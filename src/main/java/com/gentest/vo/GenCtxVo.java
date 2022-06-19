package com.gentest.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
* <br>
* <b>功能：</b> vo类<br>
* <b>作者：</b>guyuefeng<br>
* <b>日期：</b>2022<br>
*/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class GenCtxVo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "是否跳过对当前类的测试")
    private Boolean skipClass = false;

    @ApiModelProperty(value = "是否跳过对当前方法的测试")
    private Boolean skipMethod = false;

    @ApiModelProperty(value = "是否只打印error错误")
    private Boolean logOnlyErr = false;

    @ApiModelProperty(value = "是否打印性能报告")
    private Boolean logPerformance = true;

    @ApiModelProperty(value = "类的打印优先级，值越大，越往后打印")
    private Integer printClassPriority = Integer.MAX_VALUE;

    @ApiModelProperty(value = "是否开启多线程执行，属性仅支持方法级别")
    private Boolean enableThread = false;

    @ApiModelProperty(value = "线程个数")
    private Integer threadCount = 1;

    @ApiModelProperty(value = "执行次数")
    private Integer executeCount = 1;
}