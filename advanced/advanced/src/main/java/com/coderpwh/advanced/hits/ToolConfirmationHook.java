package com.coderpwh.advanced.hits;

import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostReasoningEvent;
import io.agentscope.core.message.ToolUseBlock;
import reactor.core.publisher.Mono;
import io.agentscope.core.message.Msg;

import java.util.Set;

public class ToolConfirmationHook implements Hook {


    private final Set<String> toolsRequiringConfirmation;


    public ToolConfirmationHook(Set<String> toolsRequiringConfirmation) {
        this.toolsRequiringConfirmation = toolsRequiringConfirmation;
    }


    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {

        if (event instanceof PostReasoningEvent post) {
            Msg reasoning = post.getReasoningMessage();
            if (reasoning != null &&hasToolRequiringConfirmation(reasoning)){
                post.stopAgent();
            }
        }
        return  Mono.just(event);
    }


    private boolean hasToolRequiringConfirmation(Msg reasoning) {
        return  reasoning.getContentBlocks(ToolUseBlock.class).stream().anyMatch(t->toolsRequiringConfirmation.contains(t.getName()));
    }
}
