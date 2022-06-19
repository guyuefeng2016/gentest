package com.gentest.common;

import com.alibaba.fastjson.JSONObject;
import com.gentest.common.thread.FundExecTaskCallback;
import com.gentest.common.thread.FundHandleResultCallback;
import com.gentest.common.thread.GenTaskExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class GenDingPush {

    @Resource(name="genTaskExecutor")
    GenTaskExecutor taskExecutor;

    static Logger log = LoggerFactory.getLogger(GenDingPush.class);

    @Value("${dingding.accessToken}")
    private String DING_KEY;

    public static void main(String[] a) {
        sendContentPush("7189d6befdbb08e46fda905c0672c7991345a24999f01de10286834422f1b309", "[自动化测试]测试内容22");
    }

    public void sendPush(String title, String text) {
        StringBuilder sb = new StringBuilder();
        String time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sb.append("[自动化测试]").append(time).append("\n").append(title).append("\r\n").append(text);
        final String finalText = sb.toString();
        taskExecutor.addAsyncTask("DingDingPush:safeheron:gateway", new FundExecTaskCallback<String>() {
            @Override
            public String onExec() {
                sendContentPush(DING_KEY, finalText);
                return null;
            }
        }, new FundHandleResultCallback<String>() {
            @Override
            public void onSucc(String result) {

            }
        });
    }

    public void sendPush(String text) {
        StringBuilder sb = new StringBuilder();
        String time= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        sb.append("[自动化测试]").append(time).append("\n").append(text);
        final String finalText = sb.toString();
        taskExecutor.addAsyncTask("DingDingPush:safeheron:gateway", new FundExecTaskCallback<String>() {
            @Override
            public String onExec() {
                sendContentPush(DING_KEY, finalText);
                return null;
            }
        }, new FundHandleResultCallback<String>() {
            @Override
            public void onSucc(String result) {

            }
        });
    }


    public static void sendContentPush(String dingKey,String content, Object... isatAll) {
        ContentDingModel model = new ContentDingModel();
        model.setMsgtype("text");
        ContentDingModel.Text text = new ContentDingModel.Text();
        text.setContent(content);

        log.info("dingdingpush: content={}", content);
        try {
            ContentDingModel.At at = new ContentDingModel.At();
            if (isatAll.length == 1){
                at.setIsAtAll((Boolean) isatAll[0]);
                model.setAt(at);
            } else if (isatAll.length == 2){
                at.setIsAtAll((Boolean) isatAll[0]);
                at.setAtMobiles((String[]) isatAll[1]);
                model.setAt(at);
            }
            model.setText(text);
            OKHttpUtilIn.postJson("https://oapi.dingtalk.com/robot/send?access_token=" + dingKey, JSONObject.toJSONString(model));
        } catch (IOException e) {
            log.error("预警消息发送失败！！！e=", e);
        }
    }


    @Data
    public static class ContentDingModel{
        private String msgtype;
        private Text text;
        private At at;
        @Data
        public static class Text{
            private String content;
        }
        @Data
        public static class At{
            private Boolean isAtAll;
            private String[] atMobiles;
        }
    }
    @Data
    public static class LinkDingModel {
        private String msgtype;
        private Link link;
        @Data
        public static class Link {
            private String text;
            private String title;
            private String picUrl;
            public String getPicUrl(){
                if (StringUtils.isEmpty(picUrl)) {
                    return "";
                }
                return picUrl;
            }
            private String messageUrl;
            public String getMessageUrl() {
                if (StringUtils.isEmpty(messageUrl)) {
                    try {
                        String encode = URLEncoder.encode(text, "utf-8");
                        return String.format("http://pre-api.kanquanbu.com/page/#/dong?title=%s&desc=%s",title,encode);
                    }
                    catch (Exception e){
                        log.error("预警消息发送失败");
                    }
                }
                return messageUrl;
            }
        }
    }


    @Slf4j
    public static class OKHttpUtilIn {
        final static OkHttpClient CLIENT = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build();

        static {
            CLIENT.dispatcher().setMaxRequests(100);
            CLIENT.dispatcher().setMaxRequestsPerHost(2);
        }

        public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        public static String postJson(String url, String json) throws IOException {
            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            Response response = CLIENT.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                throw new IOException("Unexpected code " + response);
            }
        }

    }
}
