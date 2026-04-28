package com.coderpwh.htilchat.hook;

import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import io.agentscope.core.hook.PostReasoningEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.ToolUseBlock;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author coderpwh
 */


public class ToolConfirmationHook  implements Hook {


     private final Set<String> dangerousTools;

     public ToolConfirmationHook() {
          this.dangerousTools = new HashSet<>();
     }

     public ToolConfirmationHook(Set<String> dangerousTools) {
          this.dangerousTools = new HashSet<>(dangerousTools);
     }

     public void addDangerousTool(String toolName) {
          dangerousTools.add(toolName);
     }


     public void removeDangerousTool(String toolName) {
          dangerousTools.remove(toolName);
     }
     public void setDangerousTools(Set<String> toolNames) {
          dangerousTools.clear();
          if (toolNames != null) {
               dangerousTools.addAll(toolNames);
          }
     }

     public boolean isDangerous(String toolName) {
          return dangerousTools.contains(toolName);
     }

     public Set<String> getDangerousTools() {
          return Set.copyOf(dangerousTools);
     }


     @Override
     public <T extends HookEvent> Mono<T> onEvent(T event) {
        if(event instanceof PostReasoningEvent postReasoningEvent){
             Msg reasoningMessage = postReasoningEvent.getReasoningMessage();
             if(reasoningMessage==null){
                  return Mono.just(event);
             }

             List<ToolUseBlock> toolCalls = reasoningMessage.getContentBlocks(ToolUseBlock.class);

             boolean hasDangerousTool =
                     toolCalls.stream().anyMatch(tool -> dangerousTools.contains(tool.getName()));


             if (hasDangerousTool) {
                  // Stop agent to wait for user confirmation
                  postReasoningEvent.stopAgent();
             }

        }
          return Mono.just(event);
     }
}
