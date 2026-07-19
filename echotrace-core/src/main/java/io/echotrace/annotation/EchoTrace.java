package io.echotrace.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca um método para ser monitorado pelo EchoTrace.
 * * <p>Ao anotar um método, o EchoTrace intercepta a execução para gerar um
 * evento de negócio estruturado, capturando metadados, parâmetros e o retorno.</p>
 *
 * <pre>
 * &#64;EchoTrace(name = "order_created", capture = {"request.id"})
 * public void process(OrderRequest request) { ... }
 * </pre>
 *
 * @author Tiago Luiz
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface EchoTrace {

    /**
     * O identificador único do evento de negócio.
     * <p>Este nome será utilizado no campo "event" do JSON gerado.</p>
     */
    String name();

    /**
     * Define quais campos dos argumentos do método devem ser extraídos.
     * <p>Suporta notação de pontos para acessar objetos aninhados (ex: {@code order.customer.id}).</p>
     * * @return um array de expressões de captura (SpEL ou reflexão simples).
     */
    String[] capture() default {};

    /**
     * Determina se o valor de retorno do método deve ser serializado e incluído no evento.
     * <p>Por padrão é {@code false} para evitar exposição acidental de dados sensíveis.</p>
     */
    boolean captureReturn() default false;

    /** Stable business result represented by this event, for example payment.approval. */
    String outcome() default "";

    /** Stable journey type, for example order.checkout. */
    String journey() default "";

    /** Current stage inside the journey, for example payment. */
    String stage() default "";

    /** Argument path whose value correlates events that belong to the same journey. */
    String correlationId() default "";

    /** Argument path whose numeric value represents the financial impact. */
    String value() default "";

    /** ISO 4217 currency used by {@link #value()}, for example BRL. */
    String currency() default "";
}
