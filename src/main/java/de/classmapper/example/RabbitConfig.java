package de.classmapper.example;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import de.classmapper.example.wrapper.ExceptionMsg;
import de.classmapper.example.wrapper.MessageContainer;
import de.classmapper.example.wrapper.TransportableException;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListenerConfigurer;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistrar;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConversionException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolverComposite;
import org.springframework.messaging.handler.invocation.InvocableHandlerMethod;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Configuration
public class RabbitConfig implements RabbitListenerConfigurer {

    @Bean
    public RetryOperationsInterceptor retryInterceptor() {
        return RetryInterceptorBuilder.stateless()
                .maxAttempts(1)
                .backOffOptions(1000, 2.0, 10000)
                .recoverer(new RejectAndDontRequeueRecoverer())
                .build();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        return objectMapper;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory container = new SimpleRabbitListenerContainerFactory();
        container.setConnectionFactory(connectionFactory);
        container.setChannelTransacted(true);
        container.setAdviceChain(retryInterceptor());
        container.setMessageConverter(jsonConverter());
        return container;
    }

    @Bean
    public MappingJackson2MessageConverter consumerConverter() {
        return new MappingJackson2MessageConverter();
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper());
        converter.setClassMapper(new MyClassMapper());
        return converter;
    }


    @Bean
    public DefaultMessageHandlerMethodFactory messageHandlerMethodFactory() {
        MyMessageHandlerMethodFactory factory = new MyMessageHandlerMethodFactory();
        factory.setMessageConverter(consumerConverter());
        return factory;
    }

    public static class MyClassMapper extends DefaultJackson2JavaTypeMapper {

        public static final String TYPE_PARAMETER_ID = "__TypeParameterId__";

        public MyClassMapper() {
            addTrustedPackages("*");
        }

        @Override
        public void fromClass(Class<?> clazz, Object body, MessageProperties properties) {
            super.fromClass(clazz, properties);
            if (MessageContainer.class.equals(clazz)) {
                MessageContainer container = (MessageContainer) body;
                if (container.getValue() != null) {
                    addHeader(properties, TYPE_PARAMETER_ID, container.getValue().getClass());
                }
            }
        }

        @Override
        public JavaType constructJavaType(MessageProperties properties) {
            Class<?> aClass = super.toClass(properties);
            String typeParameter = retrieveHeaderAsString(properties, TYPE_PARAMETER_ID);
            if (typeParameter != null && MessageContainer.class.equals(aClass)) {
                try {
                    return TypeFactory.defaultInstance().constructParametricType(aClass, ClassUtils.forName(typeParameter, getClassLoader()));
                } catch (ClassNotFoundException e) {
                    throw new MessageConversionException("failed to resolve class name. Class not found [" + typeParameter + "]", e);
                }
            }
            return super.toJavaType(properties);
        }
    }

    public static class MyMessageHandlerMethodFactory extends DefaultMessageHandlerMethodFactory {
        private HandlerMethodArgumentResolverComposite argumentResolvers;

        public MyMessageHandlerMethodFactory() {
            try {
                //TODO
                Field field = DefaultMessageHandlerMethodFactory.class.getDeclaredField("argumentResolvers");
                field.setAccessible(true);
                argumentResolvers = (HandlerMethodArgumentResolverComposite) field.get(this);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public InvocableHandlerMethod createInvocableHandlerMethod(Object bean, Method method) {
            MyHandlerMethod handlerMethod = new MyHandlerMethod(bean, method);
            handlerMethod.setMessageMethodArgumentResolvers(argumentResolvers);
            return handlerMethod;
        }
    }

    public static class MyHandlerMethod extends InvocableHandlerMethod {
        public MyHandlerMethod(Object bean, Method method) {
            super(bean, method);
        }

        @Override
        protected Object doInvoke(Object... args) throws Exception {
            try {
                Object result = super.doInvoke(args);
                if (result == null) {
                    return null;
                } else {
                    return new MessageContainer<>(result);
                }
            } catch (TransportableException e) {
                return new MessageContainer<>(new ExceptionMsg(e));
            }
        }
    }

    @Override
    public void configureRabbitListeners(RabbitListenerEndpointRegistrar rabbitListenerEndpointRegistrar) {
        rabbitListenerEndpointRegistrar.setMessageHandlerMethodFactory(messageHandlerMethodFactory());
    }
}
