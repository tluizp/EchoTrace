package io.echotrace.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface EchoTrace {

    /**
     * Nome do evento de negócio.
     */
    String name();

    /**
     * Lista de expressões para capturar automaticamente dados do método.
     * Exemplo: {"ce.cpf", "pedido.id"}
     */
    String[] capture() default {};

    /**
     * Captura retorno do evento
     */
    boolean captureReturn() default false;
}

