package com.gentest.annotation;

import com.gentest.enums.GenCtx;
import com.gentest.enums.GenParam;
import com.gentest.enums.GenParams;
import com.gentest.vo.GenCtxVo;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: guyuefeng
 * @create: 2022-06-17 17:58
 **/
public class GenAnnotation {

    private static GenCtx CLASS_GEN_CTX = null;
    private static String CLASSZ_NAME = "";
    private static GenCtx METHOD_GEN_CTX = null;
    private static String METHOD_NAME = "";

    /**
     *
     * @param classz
     * @return
     */
    public static GenCtx parseClassAnnotation(Class classz){
        String classzName = classz.getName();

        if (!classzName.equals(CLASSZ_NAME) || CLASS_GEN_CTX == null){
            CLASSZ_NAME = classzName;
            CLASS_GEN_CTX = AnnotationUtils.findAnnotation(classz, GenCtx.class);
        }
        return CLASS_GEN_CTX;
    }

    /**
     *
     * @param method
     * @return
     */
    public static GenCtx parseMethodAnnotation(Method method){
        String methodName = method.toString();

        if (!methodName.equals(METHOD_NAME) || METHOD_GEN_CTX == null){
            METHOD_NAME = methodName;
            METHOD_GEN_CTX = method.getAnnotation(GenCtx.class);
        }
        return METHOD_GEN_CTX;
    }

    /**
     * @param classz
     * @param method
     * @return
     */
    public static GenCtxVo parseAnnotation(Class classz, Method method){
        GenCtxVo genCtxVo = new GenCtxVo();
        GenCtx classGenCtx = parseClassAnnotation(classz);
        GenCtx methodGenCtx = parseMethodAnnotation(method);

        String methodName = method.getName();
        int methodModifiers = method.getModifiers();

        if (methodGenCtx != null) {
            Boolean skipMethod = skipMethod(methodName, methodGenCtx, methodModifiers);
            genCtxVo.setSkipMethod(methodGenCtx.skip() || skipMethod);
            genCtxVo.setLogOnlyErr(methodGenCtx.logOnlyErr());
            genCtxVo.setLogPerformance(methodGenCtx.logPerformance());
            genCtxVo.setEnableThread(methodGenCtx.enableThread());
            genCtxVo.setThreadCount(methodGenCtx.threadCount());
            genCtxVo.setExecuteCount(methodGenCtx.executeCount());
        } else if (classGenCtx != null) {
            Boolean skipMethod = skipMethod(methodName, classGenCtx, methodModifiers);
            genCtxVo.setSkipMethod(skipMethod);
            genCtxVo.setSkipClass(classGenCtx.skip());
            genCtxVo.setLogOnlyErr(classGenCtx.logOnlyErr());
            genCtxVo.setLogPerformance(classGenCtx.logPerformance());
            genCtxVo.setPrintClassPriority(classGenCtx.printPriority());
        }

        return genCtxVo;
    }

    /**
     *
     * @param singleAnnotation
     * @param hasDataAnnotation
     * @param method
     * @return
     */
    public static Map<Integer, String> parseAnnotationValue(AtomicBoolean singleAnnotation, AtomicBoolean hasDataAnnotation, Method method){
        Map<Integer, String> annotationMap = new LinkedHashMap<>();

        GenParam genParamAnnotation = method.getAnnotation(GenParam.class);
        if (genParamAnnotation != null){
            hasDataAnnotation.set(true);
            String value = genParamAnnotation.value();

            if (StringUtils.isEmpty(value)){
                hasDataAnnotation.set(false);
            } else {
                annotationMap.put(genParamAnnotation.argIndex(), value);
            }
        } else {
            singleAnnotation.set(false);
            GenParams genParamsAnnotation = method.getAnnotation(GenParams.class);
            if (genParamsAnnotation != null) {
                hasDataAnnotation.set(true);
                GenParam[] values = genParamsAnnotation.value();

                int argIndex =0;
                for (GenParam genParam : values) {
                    int index = genParam.argIndex();
                    String value = genParam.value();
//                    if (StringUtils.isEmpty(value)) {
//                        continue;
//                    }
                    if (index == 0){
                        index = argIndex++;
                    }
                    annotationMap.put(index, value);
                }
                if (annotationMap.size() == 0) {
                    hasDataAnnotation.set(false);
                }
            }
        }
        return annotationMap;
    }

    private static Boolean skipMethod(String methodName, GenCtx genCtxAnnotation, int methodModifiers){
        Boolean skipMethod = false;
        if (genCtxAnnotation.skipPrivate() && Modifier.isPrivate(methodModifiers)){
            skipMethod = true;
        }
        if (genCtxAnnotation.skipNotPublic() && !Modifier.isPublic(methodModifiers)){
            skipMethod = true;
        }
        List<String> onlyMethods = Arrays.stream(genCtxAnnotation.testOnlyMethod()).collect(Collectors.toList());
        if (onlyMethods.size() > 0 && !onlyMethods.contains(methodName)){
            skipMethod = true;
        }
        List<String> excludeMethods = Arrays.stream(genCtxAnnotation.testExcludeMethod()).collect(Collectors.toList());
        if (excludeMethods.size() > 0 && excludeMethods.contains(methodName)){
            skipMethod = true;
        }
        return skipMethod;
    }
}
