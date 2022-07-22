package com.gentest.gen;

import com.gentest.common.GenSpringContextHolder;
import com.gentest.gencode.CaseInput;
import com.gentest.gencode.GenerateFileService;
import com.gentest.gencode.TestCaseClassInfo;
import com.gentest.report.GenReport;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.boot.ExitCodeGenerator;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.CollectionUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author: guyuefeng
 * @Date: 2022/6/23
 */
@Slf4j
public class AbstractGenTestCase {

    private static final InputType addDefaultInputType = new InputType();
    private static final InputType entityFieldInputType = new InputType();
    private static final Map<String, InputType> entityInputMap = new HashMap<>();

    /**
     * 供外部访问，根据字段名称，添加默认值
     *
     * @param fieldName 类字段名称
     * @param t 值
     * @param <T>
     */
    public static <T> void addEntityDefault(String fieldName, T... t) {
        addDefault(entityFieldInputType, Arrays.asList(t));
        entityInputMap.put(fieldName, entityFieldInputType);
    }

    /**
     * 供外部访问，添加任意基础类型的默认值
     *
     * @param t 值
     * @param <T>
     */
    public static <T> void addDefault(T... t) {
        addDefault(addDefaultInputType, Arrays.asList(t));
    }

    /**
     * @param t
     * @param inputType
     * @param <T>
     */
    private static <T> void addDefault(InputType inputType, List<T> t) {
        if (t.size() <= 0) {
            return;
        }
        Class<?> tclass = t.get(0).getClass();

        if (tclass.isAssignableFrom(Boolean.class)) {
            inputType.inputBoolean.addAll((Collection<? extends Boolean>) t);
        } else if (tclass.isAssignableFrom(Character.class)) {
            inputType.inputCharacter.addAll((Collection<? extends Character>) t);
        } else if (tclass.isAssignableFrom(Byte.class)) {
            inputType.inputByte.addAll((Collection<? extends Byte>) t);
        } else if (tclass.isAssignableFrom(Short.class)) {
            inputType.inputShort.addAll((Collection<? extends Short>) t);
        } else if (tclass.isAssignableFrom(Integer.class)) {
            inputType.inputInteger.addAll((Collection<? extends Integer>) t);
        } else if (tclass.isAssignableFrom(Long.class)) {
            inputType.inputLong.addAll((Collection<? extends Long>) t);
        } else if (tclass.isAssignableFrom(Float.class)) {
            inputType.inputFloat.addAll((Collection<? extends Float>) t);
        } else if (tclass.isAssignableFrom(Double.class)) {
            inputType.inputDouble.addAll((Collection<? extends Double>) t);
        } else if (tclass.isAssignableFrom(String.class)) {
            inputType.inputString.addAll((Collection<? extends String>) t);
        } else if (tclass.isAssignableFrom(Object.class)) {
            inputType.inputObject.addAll(t);
        } else if (tclass.isAssignableFrom(Date.class)) {
            inputType.inputDate.addAll((Collection<? extends Date>) t);
        } else if (tclass.isAssignableFrom(LocalDateTime.class)) {
            inputType.inputLocalDateTime.addAll((Collection<? extends LocalDateTime>) t);
        } else if (tclass.isAssignableFrom(BigDecimal.class)) {
            inputType.inputBigDecimal.addAll((Collection<? extends BigDecimal>) t);
        }
    }

    /**
     * @param fieldName
     * @return
     */
    protected static Object randomObject(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputObject.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputObject.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputObject.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputObject.get(randomIndex);
        }
        return "guyuefeng-test-string";
    }

    /**
     * @param fieldName
     * @return
     */
    protected static String randomString(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputString.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputString.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputString.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputString.get(randomIndex);
        }
        return "guyuefeng-test-string";
    }

    /**
     * @param fieldName
     * @return
     */
    protected static Boolean randomBoolean(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputBoolean.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputBoolean.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputBoolean.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputBoolean.get(randomIndex);
        }
        return RandomUtils.nextBoolean();
    }

    /**
     * @return
     */
    protected static Integer randomInteger(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputInteger.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputInteger.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputInteger.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputInteger.get(randomIndex);
        }
        return RandomUtils.nextInt(0, Integer.MAX_VALUE);
    }

    /**
     * @return
     */
    protected static Character randomChar(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputCharacter.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputCharacter.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputCharacter.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputCharacter.get(randomIndex);
        }
        return Character.valueOf((char) RandomUtils.nextInt(70, 120));
    }

    /**
     * @return
     */
    protected static Byte randomByte(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputByte.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputByte.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputByte.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputByte.get(randomIndex);
        }
        return (byte) RandomUtils.nextInt(0, 2);
    }

    /**
     * @return
     */
    protected static Short randomShort(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputShort.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputShort.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputShort.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputShort.get(randomIndex);
        }
        return (short) RandomUtils.nextInt(0, 32767);
    }

    /**
     * @return
     */
    protected static Long randomLong(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputLong.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputLong.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputLong.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputLong.get(randomIndex);
        }
        return RandomUtils.nextLong(0, Integer.MAX_VALUE);
    }

    /**
     * @return
     */
    protected static Float randomFloat(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputFloat.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputFloat.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputFloat.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputFloat.get(randomIndex);
        }
        return RandomUtils.nextFloat(0, Integer.MAX_VALUE);
    }

    /**
     * @return
     */
    protected static Double randomDouble(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputDouble.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputDouble.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputDouble.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputDouble.get(randomIndex);
        }
        return RandomUtils.nextDouble(0, Integer.MAX_VALUE);
    }

    /**
     * @return
     */
    protected static Date randomDate(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputDate.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputDate.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputDate.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputDate.get(randomIndex);
        }
        return new Date();
    }

    /**
     * @return
     */
    protected static LocalDateTime randomLocalDateTime(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputLocalDateTime.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputLocalDateTime.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputLocalDateTime.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputLocalDateTime.get(randomIndex);
        }
        return LocalDateTime.now();
    }

    /**
     * @return
     */
    protected static BigDecimal randomBigDecimal(String... fieldName) {
        if (fieldName.length > 0) {
            int size = entityFieldInputType.inputBigDecimal.size();
            if (size > 0) {
                int randomIndex = RandomUtils.nextInt(0, size);
                return entityFieldInputType.inputBigDecimal.get(randomIndex);
            }
        }
        int length = addDefaultInputType.inputBigDecimal.size();
        if (length > 0) {
            int randomIndex = RandomUtils.nextInt(0, length);
            return addDefaultInputType.inputBigDecimal.get(randomIndex);
        }
        return new BigDecimal(RandomUtils.nextInt(0, 128));
    }

    /**
     * @param sourceCodeMap
     * @param typeStr
     * @param v
     * @param argIndex
     */
    protected static void conveterSimpleTypeSourceCode(Map<Integer, StringBuilder> sourceCodeMap, String typeStr, Object v, AtomicInteger argIndex) {
        if (sourceCodeMap == null) {
            return;
        }
        StringBuilder currentSourceCode = new StringBuilder();
        int currentSouceCodeIndex = sourceCodeMap.size();
        currentSourceCode.append(typeStr).append(" var").append(argIndex.get()).append(currentSouceCodeIndex).append(" = ").append(v).append(";");
        sourceCodeMap.put(currentSouceCodeIndex, currentSourceCode);
    }


    /**
     * @param resultBuilder
     * @param v
     */
    protected static void conveterSimpleResultSouceCode(StringBuilder resultBuilder, Object v) {
        if (resultBuilder == null) {
            return;
        }
        resultBuilder.append(v);
    }


    /**
     * @param fieldName
     * @param sourceCodeMap
     * @param localBeanIndex
     * @param resultBuilder
     * @param argIndex
     */
    protected static void setBeanFiledSourceCode(String fieldName, Map<Integer, StringBuilder> sourceCodeMap, Integer localBeanIndex, StringBuilder resultBuilder, AtomicInteger argIndex) {
        String setName = fieldName.substring(0, 1).toUpperCase().concat(fieldName.substring(1));
        Integer propertyIndex = sourceCodeMap.size();
        StringBuilder currentSourceCode = new StringBuilder();
        currentSourceCode.append("var").append(argIndex.get()).append(localBeanIndex).append(".set").append(setName).append("(").append(resultBuilder.toString()).append(");");
        ;
        sourceCodeMap.put(propertyIndex, currentSourceCode);
    }


    /**
     * 生成测试用例
     *
     * @param generateCase
     * @param testCaseInfoList
     * @param caseInput
     */
    protected static void generateTestCase(Boolean generateCase, List<TestCaseClassInfo> testCaseInfoList, CaseInput caseInput, Class applicationClass) {
        if (generateCase != null && generateCase) {
            GenerateFileService generateFileService = GenSpringContextHolder.getBean(GenerateFileService.class);
            generateFileService.genTestCase(testCaseInfoList, caseInput, applicationClass);
        }
    }

    /**
     * 打印测试用例
     */
    protected static void printTestCase() {
        Queue<String> queue = GenReport.printReport(false);
        if (!CollectionUtils.isEmpty(queue)) {
            boolean dingPush = false;
            //GenDingPush dingDingPush = null;
            if (dingPush) {
                //dingDingPush = GenSpringContextHolder.getBean(GenDingPush.class);
            }
            StringBuilder dingSb = new StringBuilder();

            while (queue.size() > 0) {
                dingSb.append(queue.poll());
            }
            while (dingSb.length() > 0) {
                if (dingSb.length() > 4000) {
                    String dingText = dingSb.substring(0, 4000);
                    if (dingPush) {
                        //dingDingPush.sendPush(dingText);
                    } else {
                        System.out.println(dingText);
                    }
                    dingSb.delete(0, 4000);
                } else {
                    if (dingPush) {
                        //dingDingPush.sendPush(dingSb.toString());
                    } else {
                        System.out.println(dingSb.toString());
                    }
                    dingSb.delete(0, dingSb.length());
                }
                try {
                    if (dingPush) {
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    /**
     * @param configurableApplicationContext
     */
    protected static void exit(ConfigurableApplicationContext configurableApplicationContext) {
        int exitCode = SpringApplication.exit(configurableApplicationContext, (ExitCodeGenerator) () -> 0);
        System.exit(exitCode);
    }

    @Data
    static class InputType {
        List<Boolean> inputBoolean = new ArrayList<>();
        List<Character> inputCharacter = new ArrayList<>();
        List<Byte> inputByte = new ArrayList<>();
        List<Short> inputShort = new ArrayList<>();
        List<Integer> inputInteger = new ArrayList<>();
        List<Long> inputLong = new ArrayList<>();
        List<Float> inputFloat = new ArrayList<>();
        List<Double> inputDouble = new ArrayList<>();
        List<String> inputString = new ArrayList<>();
        List<Object> inputObject = new ArrayList<>();
        List<Date> inputDate = new ArrayList<>();
        List<LocalDateTime> inputLocalDateTime = new ArrayList<>();
        List<BigDecimal> inputBigDecimal = new ArrayList<>();
    }
}
