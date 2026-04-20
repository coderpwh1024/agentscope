package com.coderpwh.advanced.hits;

import io.agentscope.core.hook.Hook;
import io.agentscope.core.hook.HookEvent;
import reactor.core.publisher.Mono;

public class ObservationHook implements Hook {



    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        return null;
    }

}
