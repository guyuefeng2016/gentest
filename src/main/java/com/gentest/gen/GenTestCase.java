package com.gentest.gen;

import com.alibaba.fastjson.JSONObject;
import com.gentest.annotation.GenAnnotation;
import com.gentest.common.GenCommon;
import com.gentest.common.GenSpringContextHolder;
import com.gentest.enums.GenCtx;
import com.gentest.exception.TypeNotSupportException;
import com.gentest.gencode.CaseInput;
import com.gentest.gencode.TestCaseClassInfo;
import com.gentest.gencode.TestCaseMethodInfo;
import com.gentest.report.GenReport;
import com.gentest.vo.GenCtxVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
import java.math.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-14 11:17
 **/
@Slf4j
public class GenTestCase extends AbstractGenTestCase{


    /**
     * 全局控制字段,用户输入优先级高于配置优先级
     *
     * @param testPackage 测试类所在包位置 【classpath路径】如：com/safeheron/gateway/repository
     * @param applicationClass SpringBoot启动类
     * @param args main方法args
     * @param onlyClassNameArr 只执行某些类的某些方法 规则：class全类名[包名+类名]:methodName ， 多个methodName以#号分割 ，要测试多个类，以逗号分割（如果不指定当前字段，则默认测试testPackage包名下所有类）
     *                         例如 测试类AommonRepository2下面的test8、test9两个方法，测试MpcTaskManagerRepository下面的descreseTimeOutCreatePartyTaskCount、getMpcTaskSessionIdIp方法，
     *                         {"com.safeheron.gateway.repository.AommonRepository2:test8#test9", "com.safeheron.gateway.repository.MpcTaskManagerRepository:descreseTimeOutCreatePartyTaskCount#getMpcTaskSessionIdIp"}
     * @param inputLogInfo 是否输出info日志
     * @param inputLogPerformance 是否输出性能日志
     * @param generateCase 是否输出测试用例
     * @param caseInput 指定测试用例的信息【 测试用例所在包名，测试用例输出目录，作者，注释】
     */
    public static void test(String testPackage, Class applicationClass, String[] args, String[] onlyClassNameArr, Boolean inputLogInfo, Boolean inputLogPerformance, Boolean generateCase, CaseInput caseInput) {
        try {
            Map<String, List<String>> onlyClassNameList = new HashMap<>();
            if (onlyClassNameArr != null && onlyClassNameArr.length > 0){
                for (String clazzMethod: onlyClassNameArr){
                    String[] split = clazzMethod.split(":");
                    String key = split[0];
                    String value = "";

                    if (StringUtils.isEmpty(key)){
                        continue;
                    }
                    String clazz = key.trim();
                    String methodStr = "";

                    if (split.length == 2){
                        value = split[1];
                    }

                    if (StringUtils.isNotEmpty(value)){
                        methodStr = split[1].trim();
                        String[] methodArr = methodStr.split("#");
                        onlyClassNameList.put(clazz, Arrays.asList(methodArr));
                    } else {
                        onlyClassNameList.put(clazz, new ArrayList<>());
                    }
                }
            }
            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            CachingMetadataReaderFactory cachingMetadataReaderFactory = new CachingMetadataReaderFactory();

            Iterator<String> clazzIterator = null;
            Resource[] resources = null;
            if (CollectionUtils.isEmpty(onlyClassNameList)){
                resources = pathMatchingResourcePatternResolver.getResources("classpath*:" + testPackage + "/**/*.class");
            } else {
                resources = new Resource[onlyClassNameList.size()];
                clazzIterator = onlyClassNameList.keySet().iterator();
            }
            ClassLoader loader = ClassLoader.getSystemClassLoader();
            ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(applicationClass, args);

            List<TestCaseClassInfo> testCaseInfoList = new LinkedList<>();
            for (Resource resource : resources) {
                try {
                    boolean filterMethod = false;
                    List<String> filtermMethodList = new ArrayList<>();
                    String className = "";
                    MetadataReader reader = null;
                    if (CollectionUtils.isEmpty(onlyClassNameList)){
                        reader = cachingMetadataReaderFactory.getMetadataReader(resource);
                        className = reader.getClassMetadata().getClassName();
                    } else {
                        className = clazzIterator.next();
                        List<String> methodList = onlyClassNameList.get(className);
                        if (!CollectionUtils.isEmpty(methodList)) {
                            filterMethod = true;
                            filtermMethodList = methodList;
                        }
                    }

                    Class classz = loader.loadClass(className);;
                    if (classz.isInterface()) {
                        continue;
                    }
                    if (className.indexOf("GenTestCase") != -1) {
                        continue;
                    }
                    Object bean;
                    try {
                        bean = GenSpringContextHolder.getBean(classz);
                        if (bean == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        GenCommon.error("无法解析 class: {} ， e=", classz,e);
                        continue;
                    }
                    List<TestCaseMethodInfo> testCaseMethodsInfoList = new LinkedList<>();
                    TestCaseClassInfo caseClassInfo = new TestCaseClassInfo();
                    caseClassInfo.setTestCaseRepositoryPackage(className);
                    caseClassInfo.setTestCaseClassName(classz.getSimpleName());
                    caseClassInfo.setTestCaseMethodInfos(testCaseMethodsInfoList);
                    testCaseInfoList.add(caseClassInfo);

                    GenCtx clazzGenCtx = GenAnnotation.parseClassAnnotation(classz);
                    if (clazzGenCtx != null) {
                        if (clazzGenCtx.skip()) {
                            continue;
                        }
                        GenReport.newReportDataResource(clazzGenCtx.printPriority());
                    } else {
                        GenReport.newReportDataResource(Integer.MAX_VALUE);
                    }
                    GenReport.addExtraReport(" 当前测试类: " + className);

                    Method[] methods = classz.getDeclaredMethods();
                    for (Method method : methods) {
                        method.setAccessible(true);
                        String methodName = method.getName();
                        GenCtxVo genCtxVo = GenAnnotation.parseAnnotation(classz, method);
                        if (genCtxVo.getSkipMethod()) {
                            continue;
                        }
                        if (filterMethod && !filtermMethodList.contains(methodName)) {
                            continue;
                        }
                        GenCommon.info(genCtxVo.getLogOnlyErr(), "class: {}, methodName:{} ", classz, methodName);

                        AtomicBoolean singleAnnotation = new AtomicBoolean(true);
                        AtomicBoolean hasDataAnnotation = new AtomicBoolean(false);
                        Map<Integer, String> annotationMap = GenAnnotation.parseAnnotationValue(singleAnnotation, hasDataAnnotation, method);

                        Type[] genericParameterTypes = method.getGenericParameterTypes();
                        int paramsLength = genericParameterTypes.length;
                        Object[] paramArgs = new Object[paramsLength];
                        AtomicInteger argIndex = new AtomicInteger(0);

                        TestCaseMethodInfo caseMethodInfo = new TestCaseMethodInfo();
                        caseMethodInfo.setMethodArgCount(paramsLength);
                        caseMethodInfo.setTestCaseMethodName(methodName);
                        Map<Integer, StringBuilder> methodResultSourceCode = caseMethodInfo.getMethodSourceCode();
                        try {
                            for (Type genericType : genericParameterTypes) {
                                Map<Integer, StringBuilder> methodSourceCode = convertType(genericType, paramArgs, argIndex, annotationMap);
                                if (!Modifier.isPrivate(method.getModifiers())) {
                                    if (methodSourceCode.size() > 0){
                                        methodSourceCode.forEach((key,val) -> {
                                            methodResultSourceCode.put(methodResultSourceCode.size(),val);
                                        });
                                    }
                                }
                            }
                            if (!Modifier.isPrivate(method.getModifiers())) {
                                testCaseMethodsInfoList.add(caseMethodInfo);
                            }
                        } catch (Exception e){
                            log.error("e=",e);
                            continue;
                        }
                        doInvoke(method, bean, paramArgs, genCtxVo, inputLogInfo, inputLogPerformance, caseMethodInfo);
                    }
                } catch (Exception e){
                    log.error("解析失败 resource:{}, e=",resource,e);
                    continue;
                }
            }
            generateTestCase(generateCase, testCaseInfoList, caseInput, applicationClass);
            printTestCase();
            exit(configurableApplicationContext);
        } catch (Exception e){
            log.error("e=",e);
        }
    }


    /**
     *
     * @param method
     * @param bean
     * @param paramArgs
     * @param genCtxVo
     * @param inputLogInfo
     * @param inputLogPerformance
     */
    private static void doInvoke(Method method, Object bean, Object[] paramArgs, GenCtxVo genCtxVo, Boolean inputLogInfo, Boolean inputLogPerformance, TestCaseMethodInfo caseMethodInfo){
        String methodName = method.getName();
        try{
            Boolean enableThread = genCtxVo.getEnableThread();
            Integer threadCount = genCtxVo.getThreadCount();
            Integer executeCount = genCtxVo.getExecuteCount();
            ExecutorService pool = null;
            if (enableThread) {
                pool = Executors.newFixedThreadPool(threadCount);
            }
            AtomicReference<Object> resultRef = new AtomicReference<>();
            AtomicReference<Exception> threadExceptionRef = new AtomicReference<>();
            long startTime = System.currentTimeMillis();

            for (int i=0; i<executeCount; i++) {
                if (enableThread) {
                    pool.execute(()->{
                        try {
                            resultRef.set(method.invoke(bean, paramArgs));
                        } catch (Exception e) {
                            threadExceptionRef.set(e);
                        }
                    });
                } else {
                    try {
                        resultRef.set(method.invoke(bean, paramArgs));
                    } catch (Exception e) {
                        threadExceptionRef.set(e);
                    }
                }
            }
            if (!Modifier.isPrivate(method.getModifiers())) {
                caseMethodInfo.setReturnType(method.getGenericReturnType().getTypeName());
                caseMethodInfo.setReturnObj(resultRef.get());
            }

            Exception exception = threadExceptionRef.get();
            if (exception != null){
                throw exception;
            }
            long endTime = System.currentTimeMillis();
            if (enableThread){
                pool.shutdown();
            }
            Boolean logPerformance = genCtxVo.getLogPerformance();
            if (inputLogPerformance != null){
                logPerformance = inputLogPerformance;
            }
            if (logPerformance){
                StringBuffer sb = GenReport.addPerformanceReport("methodName: ").append(methodName).append(", 执行次数：").append(executeCount).append(" 次");
                if (enableThread){
                    sb.append(", 线程数：").append(threadCount);
                }
                sb.append(", spend time: ").append(endTime-startTime).append(" ms").append("\n");
            }
            Boolean logOnlyErr = genCtxVo.getLogOnlyErr();
            if (inputLogInfo != null){
                logOnlyErr = !inputLogInfo;
            }
            if (!logOnlyErr){
                GenReport.addInfoReport("invoke start , methodName: ").append(methodName).append("\n");
            }
            if (!logOnlyErr){
                GenReport.addInfoReport("invoke result, methodName: ").append(methodName).
                        append(", 参数: ").append(Arrays.asList(paramArgs)).append(", result: ").append(resultRef.get()).append("\n\n");
            }
        } catch (Exception e){
            Throwable target = ((InvocationTargetException) e).getTargetException();
            StackTraceElement[] stackTrace = target.getStackTrace();
            GenReport.addErrorReport("invoke method fail, methodName: ").append(methodName).append(", 参数: ").
                    append(Arrays.asList(paramArgs)).append("\r\n\t\tException: ").append(target.toString()).append("\n").append(Arrays.asList(stackTrace)).append("\n\n");
        }
    }

    /**
     *
     * @param genericType
     * @param paramArgs
     * @param argIndex
     * @param annotationMap
     * @return
     */
    public static Map<Integer, StringBuilder> convertType(Type genericType, Object[] paramArgs, AtomicInteger argIndex, Map<Integer, String> annotationMap){
        Map<Integer, StringBuilder> sourceCodeMap = new LinkedHashMap<>();
        String annotationValue = annotationMap.get(argIndex.get());
        if (StringUtils.isNotEmpty(annotationValue)){
            String[] split = annotationValue.split("#");
            int randomIndex = RandomUtils.nextInt(0, split.length);
            annotationValue = split[randomIndex];
        } else {
            if (annotationValue != null){
                paramArgs[argIndex.getAndIncrement()] = annotationValue;
                return sourceCodeMap;
            }
        }

        if (genericType instanceof ParameterizedType) {
            Class<?> rawType = ((ParameterizedTypeImpl) genericType).getRawType();
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();

            String typeName = genericType.getTypeName();
            parseParameterizedType(typeName, rawType, annotationValue, actualTypeArguments, paramArgs, argIndex, sourceCodeMap);
        } else {
            Class genericTypeAssign = (Class) genericType;
            if (genericTypeAssign.isArray()){
                Class componentType = genericTypeAssign.getComponentType();
                String componentName = componentType.getName();

                Integer currentVarIndex = sourceCodeMap.size();
                StringBuilder currentSourceCode = new StringBuilder();
                int arrLen = 1;
                if (StringUtils.isNotEmpty(annotationValue)){
                    arrLen = annotationValue.split(",").length;
                }
                currentSourceCode.append(componentName+"[] var").append(argIndex.get()).append(currentVarIndex).append(" = new "+componentName+"[").append(arrLen).append("];");
                sourceCodeMap.put(currentVarIndex, currentSourceCode);

                if (StringUtils.isNotEmpty(annotationValue)){
                    List<Object> resListVal = new LinkedList<>();
                    doConvertListValueType(componentType, annotationValue, resListVal, sourceCodeMap, argIndex);

                    Object arrObj = Array.newInstance(componentType, arrLen);
                    int localVarIndex = currentVarIndex+1;
                    for (int i=0; i<arrLen; i++){
                        Array.set(arrObj, i, resListVal.get(i));
                        StringBuilder currentSourceCode2 = new StringBuilder();
                        Integer currentVarIndex2 = sourceCodeMap.size();
                        currentSourceCode2.append("var").append(argIndex.get()).append(currentVarIndex).append("[").append(i).append("]").append(" = ").append("var").append(argIndex.get()).append(localVarIndex++).append(";");
                        sourceCodeMap.put(currentVarIndex2, currentSourceCode2);
                    }
                    paramArgs[argIndex.getAndIncrement()] = arrObj;
                } else {
                    Object[] newParams = new Object[1];
                    int newParamsIndex = 0;
                    doConvertNoValueType(componentType, newParams, newParamsIndex, sourceCodeMap, argIndex);

                    StringBuilder currentSourceCode2 = new StringBuilder();
                    currentSourceCode2.append("var").append(argIndex.get()).append(currentVarIndex).append("[0]").append(" = ").append("var").append(argIndex.get()).append(currentVarIndex+1).append(";");
                    sourceCodeMap.put(sourceCodeMap.size(), currentSourceCode2);

                    Object arrObj = Array.newInstance(componentType, 1);
                    Array.set(arrObj, 0, newParams[newParamsIndex]);
                    paramArgs[argIndex.getAndIncrement()] = arrObj;
                }
            } else {
                if (StringUtils.isNotEmpty(annotationValue)){
                    List<Object> resListVal = new LinkedList<>();
                    doConvertValueType(genericTypeAssign, new Object[]{annotationValue}, resListVal, sourceCodeMap, argIndex);
                    paramArgs[argIndex.getAndIncrement()] = resListVal.get(0);
                } else {
                    Boolean conveterNoDataType = doConvertNoValueType(genericTypeAssign, paramArgs, argIndex.get(), sourceCodeMap, argIndex);
                    if (conveterNoDataType){
                        argIndex.getAndIncrement();
                    }
                }
            }
        }
        return sourceCodeMap;
    }


    /**
     *
     * @param actualTypeArgClass
     * @param datas
     * @param resList
     * @param sourceCodeMap
     * @param argIndex
     * @return
     */
    public static Boolean doConvertValueType(Class actualTypeArgClass, Object[] datas, List<Object> resList, Map<Integer, StringBuilder> sourceCodeMap, AtomicInteger argIndex){
        Boolean conveterSimpleTypeFlag = conveterSimpleType(actualTypeArgClass, datas, resList, sourceCodeMap, null, argIndex);
        if (!conveterSimpleTypeFlag){
            Boolean aBoolean = converBeanType("", actualTypeArgClass, null, datas, resList, sourceCodeMap, argIndex);
            if (!aBoolean){
                throw new TypeNotSupportException(actualTypeArgClass.getTypeName());
            }
        }
        return true;
    }

    /**
     *
     * @param actualTypeArgClass
     * @param newParams
     * @param paramsIndex
     * @param sourceCodeMap
     * @param argIndex
     * @return
     */
    public static Boolean doConvertNoValueType(Class actualTypeArgClass, Object[] newParams, Integer paramsIndex, Map<Integer, StringBuilder> sourceCodeMap, AtomicInteger argIndex){
        Boolean aBoolean = adapterSimpleType(actualTypeArgClass, newParams, paramsIndex, sourceCodeMap, null, argIndex);
        if (!aBoolean){
            Boolean adapterType = adapterBeanType("", actualTypeArgClass, null, newParams, paramsIndex, sourceCodeMap, argIndex);
            if (!adapterType){
                throw new TypeNotSupportException(actualTypeArgClass.getTypeName());
            }
        }
        return true;
    }

    /**
     *
     * @param actualTypeArgClass
     * @param annotationValue
     * @param resListVal
     * @param sourceCodeMap
     * @param argIndex
     * @return
     */
    public static Boolean doConvertListValueType(Class actualTypeArgClass, String annotationValue, List<Object> resListVal, Map<Integer, StringBuilder> sourceCodeMap, AtomicInteger argIndex){
        String[] splitArr = annotationValue.split(",");
        Boolean conveterSimpleTypeFlag = conveterSimpleType(actualTypeArgClass, splitArr, resListVal, sourceCodeMap, null, argIndex);
        if (!conveterSimpleTypeFlag) {
            for (String split : splitArr) {
                Boolean aBoolean = converBeanType("", actualTypeArgClass, null, new Object[]{split}, resListVal, sourceCodeMap, argIndex);
                if (!aBoolean) {
                    throw new TypeNotSupportException(actualTypeArgClass.getTypeName());
                }
            }
        }
        return true;
    }

    /**
     * 解析map范型
     * @param typeName
     * @param annotationValue
     * @param actualTypeArguments
     * @param paramArgs
     * @param argIndex
     * @param sourceCodeMap
     * @return
     */
    public static Map doConvertTypeMap(String typeName, String annotationValue, Type[] actualTypeArguments, Object[] paramArgs, AtomicInteger argIndex, Map<Integer, StringBuilder> sourceCodeMap){
        List<Object> resListKey = new LinkedList<>();
        List<Object> resListVal = new LinkedList<>();
        Map map1;
        if (StringUtils.isNotEmpty(annotationValue)) {
            map1 = JSONObject.parseObject(annotationValue, Map.class);
        } else {
            map1 = new HashMap();
        }
        Object[] objectsKey = map1.keySet().toArray();
        Object[] objectsVal = map1.values().toArray();
        Type actualTypeArgumentKey = actualTypeArguments[0];
        Type actualTypeArgumentVal = actualTypeArguments[1];

        int currentSourceSize = sourceCodeMap.size();
        conveterSimpleTypeSourceCode(sourceCodeMap, typeName, "new LinkedHashMap<>()", argIndex);

        List<Integer> localKeyIndex = new LinkedList<>();
        if (actualTypeArgumentKey instanceof ParameterizedType){
            Class<?> actualRawTypeInner = ((ParameterizedTypeImpl) actualTypeArgumentKey).getRawType();
            Type[] actualTypeArgsInnerArr = ((ParameterizedType) actualTypeArgumentKey).getActualTypeArguments();
            String keyTypeName = actualTypeArgumentKey.getTypeName();

            if (objectsKey != null && objectsKey.length >0) {
                for (Object objKey : objectsKey) {
                    localKeyIndex.add(sourceCodeMap.size());
                    Object o = parseParameterizedType(keyTypeName, actualRawTypeInner, objKey.toString(), actualTypeArgsInnerArr, paramArgs, argIndex, sourceCodeMap);
                    if (o != null) {
                        argIndex.decrementAndGet();
                    }
                    resListKey.add(o);
                }
            } else {
                localKeyIndex.add(sourceCodeMap.size());
                Object o = parseParameterizedType(keyTypeName, actualRawTypeInner, "", actualTypeArgsInnerArr, paramArgs, argIndex, sourceCodeMap);
                if (o != null) {
                    argIndex.decrementAndGet();
                }
                resListKey.add(o);
            }
        } else {
            if (StringUtils.isNotEmpty(annotationValue)) {
                int size = sourceCodeMap.size();
                for (Object objk : objectsKey) {
                    localKeyIndex.add(size++);
                }
                doConvertValueType((Class) actualTypeArgumentKey, objectsKey, resListKey, sourceCodeMap, argIndex);
            } else {
                localKeyIndex.add(sourceCodeMap.size());
                Object[] newParams = new Object[1];
                int newParamsIndex = 0;
                doConvertNoValueType((Class) actualTypeArgumentKey, newParams, newParamsIndex, sourceCodeMap, argIndex);
                resListKey.add(newParams[newParamsIndex]);
            }
        }

        List<Integer> localValIndex = new LinkedList<>();
        if (actualTypeArgumentVal instanceof ParameterizedType) {
            Class<?> actualRawTypeInner = ((ParameterizedTypeImpl) actualTypeArgumentVal).getRawType();
            Type[] actualTypeArgsInnerArr = ((ParameterizedType) actualTypeArgumentVal).getActualTypeArguments();
            String valTypeName = actualTypeArgumentVal.getTypeName();

            if (objectsVal != null && objectsVal.length >0) {
                for (Object objVal : objectsVal) {
                    localValIndex.add(sourceCodeMap.size());
                    Object o = parseParameterizedType(valTypeName, actualRawTypeInner, objVal.toString(), actualTypeArgsInnerArr, paramArgs, argIndex, sourceCodeMap);
                    if (o != null){
                        argIndex.decrementAndGet();
                    }
                    resListVal.add(o);
                }
            } else {
                localValIndex.add(sourceCodeMap.size());
                Object o = parseParameterizedType(valTypeName, actualRawTypeInner, "", actualTypeArgsInnerArr, paramArgs, argIndex, sourceCodeMap);
                if (o != null){
                    argIndex.decrementAndGet();
                }
                resListVal.add(o);
            }
        } else {
            if (StringUtils.isNotEmpty(annotationValue)) {
                int size = sourceCodeMap.size();
                for (Object objk : objectsVal) {
                    localValIndex.add(size++);
                }
                doConvertValueType((Class) actualTypeArgumentVal, objectsVal, resListVal, sourceCodeMap, argIndex);
            } else {
                localValIndex.add(sourceCodeMap.size());
                Object[] newParams = new Object[1];
                int newParamsIndex = 0;
                doConvertNoValueType((Class) actualTypeArgumentVal, newParams, newParamsIndex, sourceCodeMap, argIndex);
                resListVal.add(newParams[newParamsIndex]);
            }
        }

        int keyLen = resListKey.size();
        Map map = new HashMap();

        for (int j = 0; j < keyLen; j++) {
            map.put(resListKey.get(j), resListVal.get(j));
            StringBuilder currentSourceCode = new StringBuilder();
            currentSourceCode.append("var").append(argIndex.get()).append(currentSourceSize).append(".put(").append("var").append(argIndex.get()).append(localKeyIndex.get(j)).append(",").append("var").append(argIndex.get()).append(localValIndex.get(j)).append(");");
            sourceCodeMap.put(sourceCodeMap.size(), currentSourceCode);
        }
        paramArgs[argIndex.getAndIncrement()] = map;
        return map;
    }

    /**
     * 解析list范型
     * @param typeName
     * @param annotationValue
     * @param actualTypeArguments
     * @param paramArgs
     * @param argIndex
     * @param sourceCodeMap
     * @return
     */
    public static List doConvertTypeList(String typeName, String annotationValue, Type[] actualTypeArguments, Object[] paramArgs, AtomicInteger argIndex, Map<Integer, StringBuilder> sourceCodeMap){
        List<Object> resListVal = new LinkedList<>();
        int currentSourceSize = sourceCodeMap.size();
        conveterSimpleTypeSourceCode(sourceCodeMap, typeName, "new LinkedList<>()", argIndex);

        Type actualTypeArgsInner = actualTypeArguments[0];
        if (actualTypeArgsInner instanceof ParameterizedType) {
            Class<?> actualRawTypeInner = ((ParameterizedTypeImpl) actualTypeArgsInner).getRawType();
            Type[] actualTypeArgsInnerArr = ((ParameterizedType) actualTypeArgsInner).getActualTypeArguments();
            String innerTypeName = actualTypeArgsInner.getTypeName();

            Object o = parseParameterizedType(innerTypeName, actualRawTypeInner, annotationValue, actualTypeArgsInnerArr, paramArgs, argIndex, sourceCodeMap);
            if (o != null){
                argIndex.decrementAndGet();
            }
            resListVal.add(o);

            StringBuilder currentSourceCode = new StringBuilder();
            currentSourceCode.append("var").append(argIndex.get()).append(currentSourceSize).append(".add(var").append(argIndex.get()).append(currentSourceSize+1).append(");");
            sourceCodeMap.put(sourceCodeMap.size(), currentSourceCode);

            paramArgs[argIndex.getAndIncrement()] = resListVal;
            return resListVal;
        } else {
            if (StringUtils.isNotEmpty(annotationValue)) {
                doConvertListValueType((Class) actualTypeArgsInner, annotationValue, resListVal, sourceCodeMap, argIndex);
            } else {
                Object[] newParams = new Object[1];
                int newParamsIndex = 0;
                doConvertNoValueType((Class) actualTypeArgsInner, newParams, newParamsIndex, sourceCodeMap, argIndex);
                resListVal.add(newParams[newParamsIndex]);
            }

            int localVarIndex = currentSourceSize+1;
            for (Object o: resListVal){
                StringBuilder currentSourceCode = new StringBuilder();
                currentSourceCode.append("var").append(argIndex.get()).append(currentSourceSize).append(".add(var").append(argIndex.get()).append(localVarIndex++).append(");");
                sourceCodeMap.put(sourceCodeMap.size(), currentSourceCode);
            }
            paramArgs[argIndex.getAndIncrement()] = resListVal;
            return resListVal;
        }
    }

    /**
     *
     * @param typeName
     * @param rawType
     * @param annotationValue
     * @param actualTypeArguments
     * @param paramArgs
     * @param argIndex
     * @param sourceCodeMap
     * @return
     */
    public static Object parseParameterizedType(String typeName, Class<?> rawType, String annotationValue, Type[] actualTypeArguments, Object[] paramArgs, AtomicInteger argIndex, Map<Integer, StringBuilder> sourceCodeMap){
        Object obj = null;
        if (rawType.isAssignableFrom(Map.class)){
            obj = doConvertTypeMap(typeName, annotationValue, actualTypeArguments, paramArgs, argIndex, sourceCodeMap);
        } else if (rawType.isAssignableFrom(List.class)){
            obj = doConvertTypeList(typeName, annotationValue, actualTypeArguments, paramArgs, argIndex, sourceCodeMap);
        } else {
            List<Object> resListVal = new LinkedList<>();
            Boolean convertFlag;
            if (StringUtils.isNotEmpty(annotationValue)) {
                convertFlag = converBeanType(typeName, rawType, actualTypeArguments, new Object[]{annotationValue}, resListVal, sourceCodeMap, argIndex);
            } else {
                Object[] newParams = new Object[1];
                int newI= 0;
                convertFlag = adapterBeanType(typeName, rawType, actualTypeArguments, newParams, newI, sourceCodeMap, argIndex);
                if (convertFlag){
                    resListVal.add(newParams[newI]);
                }
            }
            if (convertFlag) {
                Object o = resListVal.get(0);
                paramArgs[argIndex.getAndIncrement()] = o;
                obj = o;
            }
        }
        return obj;
    }


    /**
     *
     * @param genericTypeAssign
     * @param objectDatas
     * @param resList
     * @param sourceCodeMap
     * @param sourceCodeResultList
     * @param argIndex
     * @return
     */
    private static Boolean conveterSimpleType(Class genericTypeAssign, Object[] objectDatas, List<Object> resList, Map<Integer, StringBuilder> sourceCodeMap, List<Object> sourceCodeResultList, AtomicInteger argIndex){
        boolean flag = false;
        if (objectDatas == null || objectDatas.length == 0){
            return flag;
        }
        List<String> arrData = Arrays.stream(objectDatas).map(x -> x.toString()).collect(Collectors.toList());

        if (genericTypeAssign.isAssignableFrom(Object.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(v);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Object", "\""+v+"\"", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add("\"" + v + "\"");
                }
            }
            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Boolean.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                boolean aBoolean = Boolean.getBoolean(v);
                resList.add(aBoolean);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Boolean", aBoolean, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(aBoolean);
                }
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Character.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                Character character = Character.valueOf(v.toCharArray()[0]);
                resList.add(character);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Character", character, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(character);
                }
            }
            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Byte.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                byte b = Byte.parseByte(v);
                resList.add(b);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Byte", b, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(b);
                }
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Short.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                short i = Short.parseShort(v);
                resList.add(i);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Short", i, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(i);
                }
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Integer.class) ) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                int i = Integer.parseInt(v);
                resList.add(i);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Integer", i, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(i);
                }
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Long.class)) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                long l = Long.parseLong(v);
                resList.add(l);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Long", l+"L", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(l + "L");
                }
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Float.class)) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                float f = Float.parseFloat(v);
                resList.add(f);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Float", f+"f", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(f + "f");
                }
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Double.class)) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                double v1 = Double.parseDouble(v);
                resList.add(v1);

                conveterSimpleTypeSourceCode(sourceCodeMap, "Double", v1+"d", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v1 + "d");
                }
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(String.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(v);

                conveterSimpleTypeSourceCode(sourceCodeMap, "String", "\""+v+"\"", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add("\"" + v + "\"");
                }
            }

            flag = true;
        } else if (genericTypeAssign.isPrimitive()){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                List<Object> objects = converPrimitive(genericTypeAssign, Arrays.asList(v), sourceCodeMap, sourceCodeResultList, argIndex);
                resList.add(objects.get(0));
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Date.class)){
            for (String v: arrData){
                try {
                    String[] s = v.split("\\$");
                    v = s[RandomUtils.nextInt(0,s.length)];
                    Date date = DateUtils.parseDate(v, "yyyy-MM-dd HH:mm:ss");
                    resList.add(date);

                    conveterSimpleTypeSourceCode(sourceCodeMap, "Date", "DateUtils.parseDate("+v+", \"yyyy-MM-dd HH:mm:ss\")", argIndex);
                    if (sourceCodeResultList != null) {
                        sourceCodeResultList.add("DateUtils.parseDate(" + v + ", \"yyyy-MM-dd HH:mm:ss\")");
                    }
                } catch (Exception e){
                    log.error("parseDate error, e=",e);
                }
            }
            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(LocalDateTime.class)){
            for (String v: arrData){
                try {
                    String[] s = v.split("\\$");
                    v = s[RandomUtils.nextInt(0,s.length)];
                    LocalDateTime parse = LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    resList.add(parse);

                    conveterSimpleTypeSourceCode(sourceCodeMap, "LocalDateTime", "LocalDateTime.parse("+v+", DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\"))", argIndex);
                    if (sourceCodeResultList != null) {
                        sourceCodeResultList.add("LocalDateTime.parse(" + v + ", DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\"))");
                    }
                } catch (Exception e){
                    log.error("parseDate error, e=",e);
                }
            }
            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(BigDecimal.class)) {
            for (String v: arrData){
                try {
                    String[] s = v.split("\\$");
                    v = s[RandomUtils.nextInt(0,s.length)];
                    BigDecimal bigDecimal = new BigDecimal(v);
                    resList.add(bigDecimal);

                    conveterSimpleTypeSourceCode(sourceCodeMap, "BigDecimal", "new BigDecimal("+v+")", argIndex);
                    if (sourceCodeResultList != null) {
                        sourceCodeResultList.add("new BigDecimal("+v+")");
                    }
                } catch (Exception e){
                    log.error("parseDate error, e=",e);
                }
            }
            flag = true;
        }
        return flag;
    }

    /**
     *
     * @param genericTypeAssign
     * @param srcList
     * @param sourceCodeMap
     * @param sourceCodeResultList
     * @param argIndex
     * @return
     */
    private static List<Object> converPrimitive(Class genericTypeAssign, List<String> srcList, Map<Integer, StringBuilder> sourceCodeMap, List<Object> sourceCodeResultList, AtomicInteger argIndex){
        List<Object> destList = new LinkedList<>();
        if (CollectionUtils.isEmpty(srcList)){
            return destList;
        }
        if (genericTypeAssign.isAssignableFrom(int.class)){
            destList.addAll(srcList.stream().map(x->{
                int v = Integer.parseInt(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "int", v, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v);
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(short.class)){
            destList.addAll(srcList.stream().map(x->{
                short v = Short.parseShort(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "short", v, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v);
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(float.class)){
            destList.addAll(srcList.stream().map(x->{
                float v = Float.parseFloat(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "float", v+"f", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v+"f");
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(double.class)){
            destList.addAll(srcList.stream().map(x->{
                double v = Double.parseDouble(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "double", v+"d", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v+"d");
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(long.class)){
            destList.addAll(srcList.stream().map(x->{
                long v = Long.parseLong(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "long", v+"L", argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v+"L");
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(boolean.class)){
            destList.addAll(srcList.stream().map(x->{
                boolean v = Boolean.parseBoolean(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "boolean", v, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v);
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(byte.class)){
            destList.addAll(srcList.stream().map(x->{
                byte v = Byte.parseByte(x);
                conveterSimpleTypeSourceCode(sourceCodeMap, "byte", v, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v);
                }
                return v;
            }).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(char.class)){
            destList.addAll(srcList.stream().map(x-> {
                char v = x.toCharArray()[0];
                conveterSimpleTypeSourceCode(sourceCodeMap, "char", v, argIndex);
                if (sourceCodeResultList != null) {
                    sourceCodeResultList.add(v);
                }
                return v;
            }).collect(Collectors.toList()));
        }
        return destList;
    }

    /**
     *
     * @param typeName
     * @param genericTypeAssign
     * @param actualTypeArguments
     * @param objectsVal
     * @param resList
     * @param sourceCodeMap
     * @param argIndex
     * @return
     */
    private static Boolean converBeanType(String typeName, Class genericTypeAssign, Type[] actualTypeArguments, Object[] objectsVal, List<Object> resList, Map<Integer, StringBuilder> sourceCodeMap, AtomicInteger argIndex){
        boolean flag = false;
        String clazzName = genericTypeAssign.getName();

        if (genericTypeAssign.isEnum()) {
            Object[] enumConstants = genericTypeAssign.getEnumConstants();
            if (enumConstants == null){
                return false;
            }
            int length = enumConstants.length;
            if (length == 0){
                log.error("您的枚举类型没有成员: {}",genericTypeAssign);
                return false;
            }
            Object o = null;
            String enumKey = objectsVal[0].toString();
            for (Object enums: enumConstants){
                if (enums.toString().toLowerCase().indexOf(enumKey.toLowerCase()) != -1){
                    o = enums;
                    break;
                }
            }
            conveterSimpleTypeSourceCode(sourceCodeMap, clazzName, clazzName+"."+o, argIndex);
            resList.add(o);
            flag = true;
        } else {
            if (objectsVal == null || objectsVal.length == 0){
                return false;
            }

            Integer localBeanIndex = sourceCodeMap.size();
            StringBuilder sourceCode = new StringBuilder();
            String oldClassName = clazzName;
            if (StringUtils.isNotEmpty(typeName)){
                clazzName = typeName;
            }
            sourceCode.append(clazzName).append(" var").append(argIndex.get()).append(localBeanIndex).append(" = ").append("new").append(" ").append(oldClassName).append("();");
            sourceCodeMap.put(localBeanIndex, sourceCode);

            Map keyMap = JSONObject.parseObject(objectsVal[0].toString(), Map.class);
            List<Object> innerList = new LinkedList<>();

            int actualTypeArgumentIndex = -1;
            if (actualTypeArguments != null && actualTypeArguments.length > 0){
                actualTypeArgumentIndex = 0;
            }
            try {
                Field[] declaredFields = genericTypeAssign.getDeclaredFields();
                Object o = genericTypeAssign.newInstance();

                for (Field field : declaredFields) {
                    if (Modifier.isFinal(field.getModifiers())) {
                        continue;
                    }
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    if (keyMap.containsKey(fieldName)){
                        String s = keyMap.get(fieldName).toString();
                        String[] arr = s.split("\\$");
                        s = arr[RandomUtils.nextInt(0,arr.length)];
                        AtomicBoolean fieldParameterizedTypeFlag = new AtomicBoolean(false);
                        Class genericTypeClass = adapterBeanFieldParameterizedType(s, field, fieldParameterizedTypeFlag, sourceCodeMap, o, localBeanIndex, argIndex, actualTypeArguments, actualTypeArgumentIndex);

                        if (!fieldParameterizedTypeFlag.get()){
                            if (genericTypeClass == null){
                                continue;
                            }
                            StringBuilder resultBuilder = new StringBuilder();
                            List<Object> sourceCodeResultList = new LinkedList<>();
                            Boolean convertSuss = conveterSimpleType(genericTypeClass, new Object[]{s}, innerList, null, sourceCodeResultList, argIndex);
                            resultBuilder.append(sourceCodeResultList.get(0));
                            setBeanFiledSourceCode(fieldName, sourceCodeMap, localBeanIndex, resultBuilder, argIndex);

                            if (convertSuss) {
                                field.set(o, innerList.get(innerList.size()-1));
                            }
                        }
                    } else {
                        AtomicBoolean fieldParameterizedTypeFlag = new AtomicBoolean(false);
                        Class genericTypeClass = adapterBeanFieldParameterizedType("", field, fieldParameterizedTypeFlag, sourceCodeMap, o, localBeanIndex, argIndex, actualTypeArguments, actualTypeArgumentIndex);

                        if (!fieldParameterizedTypeFlag.get()){
                            if (genericTypeClass == null){
                                continue;
                            }
                            Object[] newParams = new Object[1];
                            int newI= 0;
                            StringBuilder resultBuilder = new StringBuilder();
                            Boolean convertSuss = adapterSimpleType(genericTypeClass, newParams, newI, null, resultBuilder, argIndex);
                            setBeanFiledSourceCode(fieldName, sourceCodeMap, localBeanIndex, resultBuilder, argIndex);

                            if (convertSuss){
                                field.set(o, newParams[0]);
                            }
                        }
                    }
                }
                resList.add(o);
                flag = true;
            } catch (Exception e) {
                flag = false;
            }
        }

        return flag;
    }

    /**
     *
     * @param fieldValue
     * @param field
     * @param fieldParameterizedTypeFlag
     * @param sourceCodeMap
     * @param o
     * @param localBeanIndex
     * @param argIndex
     * @param actualTypeArguments
     * @param actualTypeArgumentIndex
     * @return
     * @throws IllegalAccessException
     */
    private static Class adapterBeanFieldParameterizedType(String fieldValue , Field field, AtomicBoolean fieldParameterizedTypeFlag, Map<Integer, StringBuilder> sourceCodeMap, Object o, Integer localBeanIndex, AtomicInteger argIndex, Type[] actualTypeArguments, int actualTypeArgumentIndex) throws IllegalAccessException {
        Class genericTypeClass = null;
        Type genericType = field.getGenericType();
        try {
            genericTypeClass = (Class) genericType;
        } catch (Exception e){
            if (genericType instanceof ParameterizedType) {
                fieldParameterizedTypeFlag.set(true);
                Class<?> fieldRawType = ((ParameterizedTypeImpl) genericType).getRawType();
                Type[] fieldActualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();

                Integer currentVarIndex = sourceCodeMap.size();
                Object[] fieldParamArgs = new Object[1];
                AtomicInteger fieldArgIndex = new AtomicInteger(0);
                parseParameterizedType(genericType.getTypeName(), fieldRawType, fieldValue, fieldActualTypeArguments, fieldParamArgs, fieldArgIndex, sourceCodeMap);

                field.set(o, fieldParamArgs[0]);

                StringBuilder resultBuilder = new StringBuilder();
                resultBuilder.append("var0").append(currentVarIndex);
                setBeanFiledSourceCode(field.getName(), sourceCodeMap, localBeanIndex, resultBuilder, argIndex);
            } else if (actualTypeArgumentIndex != -1){
                fieldParameterizedTypeFlag.set(false);
                genericTypeClass = (Class) actualTypeArguments[actualTypeArgumentIndex++];
            }
        }
        return genericTypeClass;
    }


    /**
     *
     * @param genericTypeAssign
     * @param paramArgs
     * @param i
     * @param sourceCodeMap
     * @param resultBuilder
     * @param argIndex
     * @return
     */
    private static Boolean adapterSimpleType(Class genericTypeAssign, Object[] paramArgs, int i, Map<Integer, StringBuilder> sourceCodeMap, StringBuilder resultBuilder, AtomicInteger argIndex){
        boolean flag = false;
        if (genericTypeAssign.isAssignableFrom(Boolean.class)){
            boolean b = RandomUtils.nextBoolean();
            paramArgs[i] = b;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Boolean", b, argIndex);
            conveterSimpleResultSouceCode(resultBuilder, b);

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Character.class)){
            Character character = Character.valueOf((char) RandomUtils.nextInt(70, 120));
            paramArgs[i] = character;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Character", character, argIndex);
            conveterSimpleResultSouceCode(resultBuilder, character);

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Byte.class)){
            int i1 = RandomUtils.nextInt(0, 2);
            paramArgs[i] = i1;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Byte", i1, argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1);

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Short.class)){
            int i1 = RandomUtils.nextInt(0, 32767);
            paramArgs[i] = i1;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Short", i1, argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1);

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Integer.class) ) {
            int i1 = RandomUtils.nextInt(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Integer", i1, argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1);

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Long.class)) {
            long i1 = RandomUtils.nextLong(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Long", i1+"L", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1+"L");

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Float.class)) {
            float i1 = RandomUtils.nextFloat(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Float", i1+"f", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1+"f");

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Double.class)) {
            Double i1 = RandomUtils.nextDouble(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            conveterSimpleTypeSourceCode(sourceCodeMap, "Double", i1+"d", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1+"d");

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(String.class)){
            paramArgs[i] = "guyuefeng-test-string";

            conveterSimpleTypeSourceCode(sourceCodeMap, "String", "\""+paramArgs[i]+"\"", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, "\""+paramArgs[i]+"\"");

            flag = true;
        } else if (genericTypeAssign.isPrimitive()){
            int i1 = RandomUtils.nextInt(0, 128);
            paramArgs[i] = i1;
            converPrimitive(genericTypeAssign, Arrays.asList(i1+""), sourceCodeMap, null, argIndex);
            conveterSimpleResultSouceCode(resultBuilder, i1);
            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Object.class)){
            paramArgs[i] = "guyuefeng-test-object";

            conveterSimpleTypeSourceCode(sourceCodeMap, "Object", "\""+paramArgs[i]+"\"", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, "\""+paramArgs[i]+"\"");

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Date.class)){
            paramArgs[i] = new Date();

            conveterSimpleTypeSourceCode(sourceCodeMap, "Date", "new Date()", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, "new Date()");

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(LocalDateTime.class)){
            paramArgs[i] = LocalDateTime.now();

            conveterSimpleTypeSourceCode(sourceCodeMap, "LocalDateTime", "LocalDateTime.now()", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, "LocalDateTime.now()");

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(BigDecimal.class)) {
            int i1 = RandomUtils.nextInt(0, 128);
            BigDecimal bigDecimal = new BigDecimal(i1);
            paramArgs[i] = bigDecimal;

            conveterSimpleTypeSourceCode(sourceCodeMap, "BigDecimal", "new BigDecimal("+i1+")", argIndex);
            conveterSimpleResultSouceCode(resultBuilder, "new BigDecimal("+i1+")");

            flag = true;
        }

        return flag;
    }

    /**
     *
     * @param typeName
     * @param genericTypeAssign
     * @param actualTypeArguments
     * @param paramArgs
     * @param i
     * @param sourceCodeMap
     * @param argIndex
     * @return
     */
    private static Boolean adapterBeanType(String typeName, Class genericTypeAssign, Type[] actualTypeArguments, Object[] paramArgs, int i, Map<Integer, StringBuilder> sourceCodeMap, AtomicInteger argIndex){
        boolean flag = true;
        String clazzName = genericTypeAssign.getName();

        if (genericTypeAssign.isEnum()) {
            Object[] enumConstants = genericTypeAssign.getEnumConstants();
            if (enumConstants == null){
                return flag;
            }
            int length = enumConstants.length;
            if (length == 0){
                log.error("您的枚举类型没有成员: {}",genericTypeAssign);
                return !flag;
            }
            int constantIndex = RandomUtils.nextInt(0, length);
            paramArgs[i] = enumConstants[constantIndex];

            conveterSimpleTypeSourceCode(sourceCodeMap, clazzName, clazzName+"."+paramArgs[i], argIndex);
        } else {
            int actualTypeArgumentIndex = -1;
            if (actualTypeArguments != null && actualTypeArguments.length > 0){
                actualTypeArgumentIndex = 0;
            }

            Integer localBeanIndex = sourceCodeMap.size();
            StringBuilder sourceCode = new StringBuilder();
            String oldClassName = clazzName;
            if (StringUtils.isNotEmpty(typeName)){
                clazzName = typeName;
            }
            sourceCode.append(clazzName).append(" var").append(argIndex.get()).append(localBeanIndex).append(" = ").append("new").append(" ").append(oldClassName).append("();");
            sourceCodeMap.put(localBeanIndex, sourceCode);

            try {
                Object o = genericTypeAssign.newInstance();
                Field[] declaredFields = genericTypeAssign.getDeclaredFields();
                for (Field field : declaredFields){
                    if (Modifier.isFinal(field.getModifiers())){
                        continue;
                    }
                    field.setAccessible(true);

                    String fieldName = field.getName();
                    AtomicBoolean fieldParameterizedTypeFlag = new AtomicBoolean(false);
                    Class genericTypeClass = adapterBeanFieldParameterizedType("", field, fieldParameterizedTypeFlag, sourceCodeMap, o, localBeanIndex, argIndex, actualTypeArguments, actualTypeArgumentIndex);

                    if (!fieldParameterizedTypeFlag.get()){
                        if (genericTypeClass == null){
                            continue;
                        }

                        Object[] newParams = new Object[1];
                        int newI= 0;
                        StringBuilder resultBuilder = new StringBuilder();
                        Boolean aBoolean = adapterSimpleType(genericTypeClass, newParams, newI, null, resultBuilder, argIndex);
                        setBeanFiledSourceCode(fieldName, sourceCodeMap, localBeanIndex, resultBuilder, argIndex);

                        if (aBoolean){
                            field.set(o, newParams[0]);
                        }
                    }
                }
                paramArgs[i] = o;
            } catch (Exception e){
                flag = false;
            }
        }
        return flag;
    }

}
