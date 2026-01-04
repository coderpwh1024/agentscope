package com.coderpwh.agentscope.util;

import io.agentscope.core.message.*;

import java.util.stream.Collectors;

public class MsgUtils {


    public static  String getTextContent(Msg msg ){
        String thinking = msg.getContent().stream()
                .filter(block->block instanceof ThinkingBlock)
                .map(block->((ThinkingBlock)block).getThinking())
                .collect(Collectors.joining("\n"));

        String text = msg.getContent().stream()
                .filter(block->block instanceof ThinkingBlock)
                .map(block -> ((TextBlock) block).getText())
                .collect(Collectors.joining());


        if (!thinking.isEmpty() && !text.isEmpty()) {
            return thinking + "\n\n" + text;
        } else if (!thinking.isEmpty()) {
            return thinking;
        } else if (!text.isEmpty()) {
            return text;
        } else {
            return "[No response]";
        }
    }

    public static boolean hasTextContent(Msg msg) {
        return msg.getContent().stream()
                .anyMatch(block -> block instanceof TextBlock || block instanceof ThinkingBlock);
    }

    public static boolean hasMediaContent(Msg msg) {
        return msg.getContent().stream()
                .anyMatch(
                        block ->
                                block instanceof ImageBlock
                                        || block instanceof AudioBlock
                                        || block instanceof VideoBlock);
    }

    public static Msg textMsg(String name, MsgRole role, String text) {
        return Msg.builder()
                .name(name)
                .role(role)
                .content(TextBlock.builder().text(text).build())
                .build();
    }

    public static Msg imageMsg(String name, MsgRole role, Source source) {
        return Msg.builder()
                .name(name)
                .role(role)
                .content(ImageBlock.builder().source(source).build())
                .build();
    }

    public static Msg audioMsg(String name, MsgRole role, Source source) {
        return Msg.builder()
                .name(name)
                .role(role)
                .content(AudioBlock.builder().source(source).build())
                .build();
    }

    public static Msg videoMsg(String name, MsgRole role, Source source) {
        return Msg.builder()
                .name(name)
                .role(role)
                .content(VideoBlock.builder().source(source).build())
                .build();
    }

    public static Msg thinkingMsg(String name, MsgRole role, String thinking) {
        return Msg.builder()
                .name(name)
                .role(role)
                .content(ThinkingBlock.builder().thinking(thinking).build())
                .build();
    }

    private MsgUtils() {
        // Utility class
    }



}
