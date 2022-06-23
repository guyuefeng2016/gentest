package com.gentest.gencode;

import lombok.Data;

import java.util.LinkedList;
import java.util.List;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-21 11:00
 **/
@Data
public class GenCaseInfo {

    private String testPackage;
    private String repositoryPackage;
    private String repositoryName;
    private String author;
    private String comment;

    private List<SouceCodeInfo> methodList = new LinkedList<>();

}
