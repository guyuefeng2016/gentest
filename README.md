# gentest
自动化测试
使用步骤：

(1) 下载gentest，然后mvn install 安装

(2) 引入当前依赖

     <dependency>
            <groupId>com.gentest</groupId>
            <artifactId>gen-test</artifactId>
            <version>0.0.1</version>
        </dependency>

(3) 在springboot的启动类上面加上对gentest的扫描 @SpringBootApplication(scanBasePackages = {"com.gentest"})

(4) 然后随便创建一个java类，直接写一个main方法测试

    
    public static void main(String[] args) {
        String testPackage = "com/safeheron/gateway/repository";

        GenTestCase.test(testPackage, WebApplication.class, args, new String[]{"com.safeheron.gateway.repository.AommonRepository2:test8#test9",
                "com.safeheron.gateway.repository.MpcTaskManagerRepository:descreseTimeOutCreatePartyTaskCount#getMpcTaskSessionIdIp",
                "com.safeheron.gateway.repository.MpcRegisterAddrRepository:report"},null, null);
    }


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
        ...... 
    }

  （5）使用注解
    GenCtx： 类和方法都有该注解时，方法注解优先级高，主要用来控制 测试哪些方法【public、private】，或者指定名字测试哪些方法，方法开启几个线程测试，测多少次，是否打印性能测试报告等功能
    GenParams: 配合GenParam使用，作用于方法上面
    GenParam: 作用于方法上面，主要用来指定方法的默认值参数
