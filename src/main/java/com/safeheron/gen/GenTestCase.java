package com.safeheron.gen;

import com.alibaba.fastjson.JSONObject;
import com.safeheron.annotation.GenAnnotation;
import com.safeheron.common.GenDingPush;
import com.safeheron.common.GenCommon;
import com.safeheron.common.GenSpringContextHolder;
import com.safeheron.enums.GenCtx;
import com.safeheron.exception.TypeNotSupportException;
import com.safeheron.report.GenReport;
import com.safeheron.vo.GenCtxVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.*;
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
public class GenTestCase {

//    public static void main(String[] args) {
//        String testPackage = "com/safeheron/gateway/repository";
//
//        test(testPackage, WebApplication.class, args, new String[]{"com.safeheron.gateway.repository.AommonRepository2:test8#test9",
//                "com.safeheron.gateway.repository.MpcTaskManagerRepository:descreseTimeOutCreatePartyTaskCount#getMpcTaskSessionIdIp" },null, null);
//    }

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
     */
    public static void test(String testPackage, Class applicationClass,  String[] args, String[] onlyClassNameArr, Boolean inputLogInfo, Boolean inputLogPerformance) {
        try {
            Map<String, List<String>> onlyClassNameList = new HashMap<>();
            if (onlyClassNameArr != null && onlyClassNameArr.length > 0){
                for (String clazzMethod: onlyClassNameArr){
                    String[] split = clazzMethod.split(":");
                    String clazz = split[0].trim();
                    String methodStr = split[1].trim();
                    String[] methodArr = methodStr.split("#");
                    onlyClassNameList.put(clazz, Arrays.asList(methodArr));
                }
            }

            PathMatchingResourcePatternResolver pathMatchingResourcePatternResolver = new PathMatchingResourcePatternResolver();
            CachingMetadataReaderFactory cachingMetadataReaderFactory = new CachingMetadataReaderFactory();
            Resource[] resources = pathMatchingResourcePatternResolver.getResources("classpath*:" + testPackage + "/**/*.class");
            ClassLoader loader = ClassLoader.getSystemClassLoader();

            SpringApplication.run(applicationClass, args);

            boolean filterMethod = false;
            List<String> filtermMethodList = new ArrayList<>();

            for (Resource resource : resources) {
                try {
                    MetadataReader reader = cachingMetadataReaderFactory.getMetadataReader(resource);
                    String className = reader.getClassMetadata().getClassName();
                    Class classz = loader.loadClass(className);

                    if (classz.isInterface()) {
                        continue;
                    }
                    if (className.indexOf("GenTestCase") != -1) {
                        continue;
                    }

                    if (!CollectionUtils.isEmpty(onlyClassNameList)) {
                        if (!onlyClassNameList.containsKey(className)) {
                            continue;
                        }
                        List<String> methodList = onlyClassNameList.get(className);
                        if (!CollectionUtils.isEmpty(methodList)) {
                            filterMethod = true;
                            filtermMethodList = methodList;
                        }
                    }

                    Object bean;
                    try {
                        bean = GenSpringContextHolder.getBean(classz);
                        if (bean == null) {
                            continue;
                        }
                    } catch (Exception e) {
                        log.error("e=",e);
                        log.error("error: {}",e.getMessage());
                        GenCommon.error("无法解析 class: {}", classz);
                        continue;
                    }

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

                        Boolean logOnlyErr = genCtxVo.getLogOnlyErr();

                        GenCommon.info(logOnlyErr, "class: {}, methodName:{} ", classz, methodName);

                        AtomicBoolean singleAnnotation = new AtomicBoolean(true);
                        AtomicBoolean hasDataAnnotation = new AtomicBoolean(false);
                        Map<Integer, String> annotationMap = GenAnnotation.parseAnnotationValue(singleAnnotation, hasDataAnnotation, method);

                        Type[] genericParameterTypes = method.getGenericParameterTypes();
                        int paramsLength = genericParameterTypes.length;
                        Object[] paramArgs = new Object[paramsLength];
                        AtomicInteger argIndex = new AtomicInteger(0);

                        for (Type genericType : genericParameterTypes) {
                            convertType(genericType, paramArgs, argIndex, annotationMap);
                        }

                        doInvoke(method, bean, paramArgs, genCtxVo, inputLogInfo, inputLogPerformance);
                    }
                } catch (Exception e){
                    log.error("解析失败 resource:{}, e=",resource,e);
                    continue;
                }
            }
            Queue<String> queue = GenReport.printReport(false);

            if (!CollectionUtils.isEmpty(queue)) {
//                GenDingPush dingDingPush = GenSpringContextHolder.getBean(GenDingPush.class);
                StringBuilder dingSb = new StringBuilder();

                while (queue.size() > 0) {
                    dingSb.append(queue.poll());
                }
                while (dingSb.length() > 0) {
                    if (dingSb.length() > 4000) {
                        String dingText = dingSb.substring(0, 4000);
//                        dingDingPush.sendPush(dingText);
                        System.out.println(dingText);
                        dingSb.delete(0, 4000);
                    } else {
//                        dingDingPush.sendPush(dingSb.toString());
                        System.out.println(dingSb.toString());
                        dingSb.delete(0, dingSb.length());
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (Exception e) {
                    }
                }
            }
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
    private static void doInvoke(Method method, Object bean, Object[] paramArgs, GenCtxVo genCtxVo, Boolean inputLogInfo, Boolean inputLogPerformance){
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
                    resultRef.set(method.invoke(bean, paramArgs));
                }
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
     */
    public static void convertType(Type genericType, Object[] paramArgs, AtomicInteger argIndex, Map<Integer, String> annotationMap){
        String annotationValue = annotationMap.get(argIndex.get());

        if (StringUtils.isNotEmpty(annotationValue)){
            String[] split = annotationValue.split("#");
            int randomIndex = RandomUtils.nextInt(0, split.length);
            annotationValue = split[randomIndex];
        }

        if (genericType instanceof ParameterizedType) {
            Class<?> rawType = ((ParameterizedTypeImpl) genericType).getRawType();
            Type[] actualTypeArguments = ((ParameterizedType) genericType).getActualTypeArguments();

            parseParameterizedType(rawType, annotationValue, actualTypeArguments, paramArgs, argIndex);
        } else {
            Class genericTypeAssign = (Class) genericType;

            if (genericTypeAssign.isArray()){
                Class componentType = genericTypeAssign.getComponentType();

                if (StringUtils.isNotEmpty(annotationValue)){
                    List<Object> resListVal = new LinkedList<>();
                    doConvertListValueType(componentType, annotationValue, resListVal);

                    int arrLen = resListVal.size();
                    Object arrObj = Array.newInstance(componentType, arrLen);

                    for (int i=0; i<arrLen; i++){
                        Array.set(arrObj, i, resListVal.get(i));
                    }
                    paramArgs[argIndex.getAndIncrement()] = arrObj;
                } else {
                    Object[] newParams = new Object[1];
                    int newParamsIndex = 0;
                    doConvertNoValueType(componentType, newParams, newParamsIndex);

                    Object arrObj = Array.newInstance(componentType, 1);
                    Array.set(arrObj, 0, newParams[newParamsIndex]);
                    paramArgs[argIndex.getAndIncrement()] = arrObj;
                }
            } else {
                if (StringUtils.isNotEmpty(annotationValue)){
                    List<Object> resListVal = new LinkedList<>();
                    doConvertValueType(genericTypeAssign, new Object[]{annotationValue}, resListVal);

                    paramArgs[argIndex.getAndIncrement()] = resListVal.get(0);
                } else {
                    Boolean conveterNoDataType = doConvertNoValueType(genericTypeAssign, paramArgs, argIndex.get());
                    if (conveterNoDataType){
                        argIndex.getAndIncrement();
                    }
                }
            }
        }
    }


    /**
     *
     * @param actualTypeArgClass
     * @param datas
     * @param resList
     * @return
     */
    public static Boolean doConvertValueType(Class actualTypeArgClass, Object[] datas, List<Object> resList){
        Boolean conveterSimpleTypeFlag = conveterSimpleType(actualTypeArgClass, datas, resList);
        if (!conveterSimpleTypeFlag){
            Boolean aBoolean = converBeanType(actualTypeArgClass, null, datas, resList);
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
     * @param argIndex
     * @return
     */
    public static Boolean doConvertNoValueType(Class actualTypeArgClass, Object[] newParams, Integer argIndex){
        Boolean aBoolean = adapterSimpleType(actualTypeArgClass, newParams, argIndex);
        if (!aBoolean){
            Boolean adapterType = adapterBeanType(actualTypeArgClass, null, newParams, argIndex);
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
     * @return
     */
    public static Boolean doConvertListValueType(Class actualTypeArgClass, String annotationValue, List<Object> resListVal){
        String[] splitArr = annotationValue.split(",");
        Boolean conveterSimpleTypeFlag = conveterSimpleType(actualTypeArgClass, splitArr, resListVal);
        if (!conveterSimpleTypeFlag) {
            for (String split : splitArr) {
                Boolean aBoolean = converBeanType(actualTypeArgClass, null, new Object[]{split}, resListVal);
                if (!aBoolean) {
                    throw new TypeNotSupportException(actualTypeArgClass.getTypeName());
                }
            }
        }
        return true;
    }

    /**
     *
     * @param annotationValue
     * @param actualTypeArguments
     * @param paramArgs
     * @param argIndex
     * @return
     */
    public static Map doConvertTypeMap(String annotationValue, Type[] actualTypeArguments, Object[] paramArgs, AtomicInteger argIndex){
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

        if (actualTypeArgumentKey instanceof ParameterizedType){
            Class<?> actualRawTypeInner = ((ParameterizedTypeImpl) actualTypeArgumentKey).getRawType();
            Type[] actualTypeArgsInnerArr = ((ParameterizedType) actualTypeArgumentKey).getActualTypeArguments();

            if (objectsKey != null && objectsKey.length >0) {
                for (Object objKey : objectsKey) {
                    Object o = parseParameterizedType(actualRawTypeInner, objKey.toString(), actualTypeArgsInnerArr, paramArgs, argIndex);
                    if (o != null) {
                        argIndex.decrementAndGet();
                    }
                    resListKey.add(o);
                }
            } else {
                Object o = parseParameterizedType(actualRawTypeInner, "", actualTypeArgsInnerArr, paramArgs, argIndex);
                if (o != null) {
                    argIndex.decrementAndGet();
                }
                resListKey.add(o);
            }
        } else {
            if (StringUtils.isNotEmpty(annotationValue)) {
                doConvertValueType((Class) actualTypeArgumentKey, objectsKey, resListKey);
            } else {
                Object[] newParams = new Object[1];
                int newParamsIndex = 0;
                doConvertNoValueType((Class) actualTypeArgumentKey, newParams, newParamsIndex);
                resListKey.add(newParams[newParamsIndex]);
            }
        }

        if (actualTypeArgumentVal instanceof ParameterizedType) {
            Class<?> actualRawTypeInner = ((ParameterizedTypeImpl) actualTypeArgumentVal).getRawType();
            Type[] actualTypeArgsInnerArr = ((ParameterizedType) actualTypeArgumentVal).getActualTypeArguments();

            if (objectsVal != null && objectsVal.length >0) {
                for (Object objVal : objectsVal) {
                    Object o = parseParameterizedType(actualRawTypeInner, objVal.toString(), actualTypeArgsInnerArr, paramArgs, argIndex);
                    if (o != null){
                        argIndex.decrementAndGet();
                    }
                    resListVal.add(o);
                }
            } else {
                Object o = parseParameterizedType(actualRawTypeInner, "", actualTypeArgsInnerArr, paramArgs, argIndex);
                if (o != null){
                    argIndex.decrementAndGet();
                }
                resListVal.add(o);
            }
        } else {
            if (StringUtils.isNotEmpty(annotationValue)) {
                doConvertValueType((Class) actualTypeArgumentVal, objectsVal, resListVal);
            } else {
                Object[] newParams = new Object[1];
                int newParamsIndex = 0;
                doConvertNoValueType((Class) actualTypeArgumentVal, newParams, newParamsIndex);
                resListVal.add(newParams[newParamsIndex]);
            }
        }

        int keyLen = resListKey.size();
        Map map = new HashMap();
        for (int j = 0; j < keyLen; j++) {
            map.put(resListKey.get(j), resListVal.get(j));
        }
        paramArgs[argIndex.getAndIncrement()] = map;
        return map;
    }

    /**
     *
     * @param annotationValue
     * @param actualTypeArguments
     * @param paramArgs
     * @param argIndex
     * @return
     */
    public static List doConvertTypeList(String annotationValue, Type[] actualTypeArguments, Object[] paramArgs, AtomicInteger argIndex){
        List<Object> resListVal = new LinkedList<>();

        Type actualTypeArgsInner = actualTypeArguments[0];
        if (actualTypeArgsInner instanceof ParameterizedType) {
            Class<?> actualRawTypeInner = ((ParameterizedTypeImpl) actualTypeArgsInner).getRawType();
            Type[] actualTypeArgsInnerArr = ((ParameterizedType) actualTypeArgsInner).getActualTypeArguments();

            Object o = parseParameterizedType(actualRawTypeInner, annotationValue, actualTypeArgsInnerArr, paramArgs, argIndex);
            if (o != null){
                argIndex.decrementAndGet();
            }
            resListVal.add(o);
            paramArgs[argIndex.getAndIncrement()] = resListVal;
            return resListVal;
        } else {
            if (StringUtils.isNotEmpty(annotationValue)) {
                doConvertListValueType((Class) actualTypeArgsInner, annotationValue, resListVal);
            } else {
                Object[] newParams = new Object[1];
                int newParamsIndex = 0;
                doConvertNoValueType((Class) actualTypeArgsInner, newParams, newParamsIndex);
                resListVal.add(newParams[newParamsIndex]);
            }
            paramArgs[argIndex.getAndIncrement()] = resListVal;
            return resListVal;
        }
    }

    /**
     *
     * @param rawType
     * @param annotationValue
     * @param actualTypeArguments
     * @param paramArgs
     * @param argIndex
     * @return
     */
    public static Object parseParameterizedType(Class<?> rawType, String annotationValue, Type[] actualTypeArguments, Object[] paramArgs, AtomicInteger argIndex){
        Object obj = null;
        if (rawType.isAssignableFrom(Map.class)){
            obj = doConvertTypeMap(annotationValue, actualTypeArguments, paramArgs, argIndex);
        } else if (rawType.isAssignableFrom(List.class)){
            obj = doConvertTypeList(annotationValue, actualTypeArguments, paramArgs, argIndex);
        } else {
            List<Object> resListVal = new LinkedList<>();

            Boolean convertFlag;
            if (StringUtils.isNotEmpty(annotationValue)) {
                convertFlag = converBeanType(rawType, actualTypeArguments, new Object[]{annotationValue}, resListVal);
            } else {
                Object[] newParams = new Object[1];
                int newI= 0;
                convertFlag = adapterBeanType(rawType, actualTypeArguments, newParams, newI);
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
     * @return
     */
    private static Boolean conveterSimpleType(Class genericTypeAssign, Object[] objectDatas, List<Object> resList){
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
            }
            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Boolean.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Boolean.getBoolean(v));
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Character.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Character.valueOf(v.toCharArray()[0]));
            }
            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Byte.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Byte.parseByte(v));
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Short.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Short.parseShort(v));
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Integer.class) ) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Integer.parseInt(v));
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Long.class)) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Long.parseLong(v));
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Float.class)) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Float.parseFloat(v));
            }

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Double.class)) {
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(Double.parseDouble(v));
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(String.class)){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                resList.add(v);
            }

            flag = true;
        } else if (genericTypeAssign.isPrimitive()){
            for (String v: arrData){
                String[] s = v.split("\\$");
                v = s[RandomUtils.nextInt(0,s.length)];
                List<Object> objects = converPrimitive(genericTypeAssign, Arrays.asList(v));
                resList.add(objects.get(0));
            }

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Date.class)){
            for (String v: arrData){
                try {
                    String[] s = v.split("\\$");
                    v = s[RandomUtils.nextInt(0,s.length)];
                    resList.add(DateUtils.parseDate(v, "yyyy-MM-dd HH:mm:ss"));
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
                    resList.add(LocalDateTime.parse(v, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
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
     * @return
     */
    private static List<Object> converPrimitive(Class genericTypeAssign, List<String> srcList){
        List<Object> destList = new LinkedList<>();
        if (CollectionUtils.isEmpty(srcList)){
            return destList;
        }
        if (genericTypeAssign.isAssignableFrom(int.class)){
            destList.addAll(srcList.stream().map(x->Integer.parseInt(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(short.class)){
            destList.addAll(srcList.stream().map(x->Short.parseShort(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(float.class)){
            destList.addAll(srcList.stream().map(x->Float.parseFloat(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(double.class)){
            destList.addAll(srcList.stream().map(x->Double.parseDouble(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(long.class)){
            destList.addAll(srcList.stream().map(x->Long.parseLong(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(boolean.class)){
            destList.addAll(srcList.stream().map(x->Boolean.parseBoolean(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(byte.class)){
            destList.addAll(srcList.stream().map(x->Byte.parseByte(x)).collect(Collectors.toList()));
        } else if (genericTypeAssign.isAssignableFrom(char.class)){
            destList.addAll(srcList.stream().map(x-> x.toCharArray()[0]).collect(Collectors.toList()));
        }
        return destList;
    }

    /**
     *
     * @param genericTypeAssign
     * @param actualTypeArguments
     * @param objectsVal
     * @param resList
     * @return
     */
    private static Boolean converBeanType(Class genericTypeAssign, Type[] actualTypeArguments, Object[] objectsVal, List<Object> resList){
        boolean flag = false;

        if (genericTypeAssign.isEnum()) {
            Object[] enumConstants = genericTypeAssign.getEnumConstants();
            if (enumConstants == null){
                return flag;
            }
            int length = enumConstants.length;
            if (length == 0){
                log.error("您的枚举类型没有成员: {}",genericTypeAssign);
                return flag;
            }

            Object o = null;
            String enumKey = objectsVal[0].toString();
            for (Object enums: enumConstants){
                if (enums.toString().toLowerCase().indexOf(enumKey.toLowerCase()) != -1){
                    o = enums;
                    break;
                }
            }

            resList.add(o);
            flag = true;
        } else {
            if (objectsVal == null || objectsVal.length == 0){
                return flag;
            }

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

                    if (keyMap.containsKey(field.getName())){
                        String s = keyMap.get(field.getName()).toString();
                        String[] arr = s.split("\\$");
                        s = arr[RandomUtils.nextInt(0,arr.length)];

                        Type genericType = field.getGenericType();
                        Class genericTypeClass = null;
                        try {
                            genericTypeClass = (Class) genericType;
                        } catch (Exception e){
                            if (actualTypeArgumentIndex != -1){
                                genericTypeClass = (Class) actualTypeArguments[actualTypeArgumentIndex++];
                            }
                        }

                        if (genericTypeClass == null){
                            continue;
                        }

                        Boolean convertSuss = conveterSimpleType(genericTypeClass, new Object[]{s}, innerList);
                        if (convertSuss) {
                            field.set(o, innerList.get(innerList.size()-1));
                        }
                    } else {
                        Object[] newParams = new Object[1];
                        int newI= 0;

                        Type genericType = field.getGenericType();
                        Class genericTypeClass = null;
                        try {
                            genericTypeClass = (Class) genericType;
                        } catch (Exception e){
                            if (actualTypeArgumentIndex != -1){
                                genericTypeClass = (Class) actualTypeArguments[actualTypeArgumentIndex++];
                            }
                        }

                        if (genericTypeClass == null){
                            continue;
                        }

                        Boolean convertSuss = adapterSimpleType(genericTypeClass, newParams, newI);

                        if (convertSuss){
                            field.set(o, newParams[0]);
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
     * @param genericTypeAssign
     * @param paramArgs
     * @param i
     * @return
     */
    private static Boolean adapterSimpleType(Class genericTypeAssign, Object[] paramArgs, int i){
        boolean flag = false;
        if (genericTypeAssign.isAssignableFrom(Boolean.class)){
            boolean b = RandomUtils.nextBoolean();
            paramArgs[i] = b;

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Character.class)){
            Character character = Character.valueOf((char) RandomUtils.nextInt(70, 120));
            paramArgs[i] = character;

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Byte.class)){
            int i1 = RandomUtils.nextInt(0, 2);
            paramArgs[i] = i1;

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Short.class)){
            int i1 = RandomUtils.nextInt(0, 32767);
            paramArgs[i] = i1;

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Integer.class) ) {
            int i1 = RandomUtils.nextInt(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Long.class)) {
            long i1 = RandomUtils.nextLong(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Float.class)) {
            float i1 = RandomUtils.nextFloat(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            flag = true;
        }  else if (genericTypeAssign.isAssignableFrom(Double.class)) {
            Double i1 = RandomUtils.nextDouble(0, Integer.MAX_VALUE);
            paramArgs[i] = i1;

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(String.class)){
            paramArgs[i] = "guyuefeng-test-string";

            flag = true;
        } else if (genericTypeAssign.isPrimitive()){
            int i1 = RandomUtils.nextInt(0, 128);
            paramArgs[i] = i1;

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Object.class)){
            paramArgs[i] = "guyuefeng-test-object";

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(Date.class)){
            paramArgs[i] = new Date();

            flag = true;
        } else if (genericTypeAssign.isAssignableFrom(LocalDateTime.class)){
            paramArgs[i] = LocalDateTime.now();

            flag = true;
        }

        return flag;
    }

    /**
     *
     * @param genericTypeAssign
     * @param actualTypeArguments
     * @param paramArgs
     * @param i
     * @return
     */
    private static Boolean adapterBeanType(Class genericTypeAssign, Type[] actualTypeArguments, Object[] paramArgs, int i){
        boolean flag = true;

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
        } else {
            int actualTypeArgumentIndex = -1;
            if (actualTypeArguments != null && actualTypeArguments.length > 0){
                actualTypeArgumentIndex = 0;
            }

            try {
                Object o = genericTypeAssign.newInstance();
                Field[] declaredFields = genericTypeAssign.getDeclaredFields();
                for (Field field : declaredFields){
                    if (Modifier.isFinal(field.getModifiers())){
                        continue;
                    }
                    field.setAccessible(true);

                    Type genericType = field.getGenericType();

                    Class genericTypeClass = null;
                    try {
                        genericTypeClass = (Class) genericType;
                    } catch (Exception e){
                        if (actualTypeArgumentIndex != -1){
                            genericTypeClass = (Class) actualTypeArguments[actualTypeArgumentIndex++];
                        }
                    }

                    if (genericTypeClass == null){
                        continue;
                    }

                    Object[] newParams = new Object[1];
                    int newI= 0;

                    Boolean aBoolean = adapterSimpleType(genericTypeClass, newParams, newI);
                    if (aBoolean){
                        field.set(o, newParams[0]);
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
