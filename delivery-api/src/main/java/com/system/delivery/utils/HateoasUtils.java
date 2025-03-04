package com.system.delivery.utils;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

public class HateoasUtils {

    
    private static final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public static <T> Mono<EntityModel<T>> createEntityModel(
            T entity,
            Function<T, Mono<Link>> linkFunction) {

        return Mono.fromCallable(() -> linkFunction.apply(entity))
                .flatMap(linkMono -> linkMono) 
                .map(selfLink -> EntityModel.of(entity, selfLink)) 
                .subscribeOn(reactor.core.scheduler.Schedulers.fromExecutor(virtualThreadExecutor)); 
    }
}
