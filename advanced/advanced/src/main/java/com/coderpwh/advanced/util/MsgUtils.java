package com.coderpwh.advanced.util;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;

import java.util.List;

public final class MsgUtils {


    private MsgUtils() {

    }

    public static String getTextContent(Msg msg) {

        if (msg == null) {
            return "";
        }
        List<ContentBlock> content = msg.getContent();
        if (content == null || content.isEmpty()) {
            return "";
        }


        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : content) {
            if (block instanceof TextBlock textBlock) {
                String text = textBlock.getText();
                if (text != null) {
                    sb.append(text);
                }
            }
        }
        return sb.toString();

    }


}
