package dev.vspac.referral;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.vspac.referral.domain.ReferralRegister;

import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.ReflectionUtils;

public class ReferralConverter extends AbstractHttpMessageConverter<ReferralRegister> {

    private static final FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
    private static final String EMAIL_PARAM = "email";
    private static final Map<String, Method> setters = new HashMap<>();
    private static final Map<String, Field> fieldsOfInterest = new HashMap<>();

    public ReferralConverter() {
        ReflectionUtils.doWithFields(ReferralRegister.class,
                (field) -> fieldsOfInterest.put(field.getName(), field),
                field -> field.isAnnotationPresent(JsonProperty.class));
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return ReferralRegister.class == clazz;
    }

    @Override
    protected ReferralRegister readInternal(Class<? extends ReferralRegister> clazz, HttpInputMessage inputMessage) throws IOException {
        MultiValueMap<String, String> values = formHttpMessageConverter.read(null, inputMessage);

        ReferralRegister register = new ReferralRegister();

        fieldsOfInterest.forEach((k, field) -> {
            String fieldName = field.getAnnotation(JsonProperty.class).value();
            callSetter(register, field.getName(), getFirstAndRemove(values, fieldName));
        });

        Map<String, Object> result = values.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));

        register.setExtra(result);

        return register;
    }

    private String getFirstAndRemove(MultiValueMap<String, String> values, String param) {
        List<String> listValues = values.remove(param);
        if (CollectionUtils.isEmpty(listValues)) {
            return null;
        } else {
            return listValues.get(0);
        }
    }

    @Override
    protected void writeInternal(ReferralRegister registrationRequest, HttpOutputMessage outputMessage) throws IOException {
        Map<String, Object> values = registrationRequest.getExtra();

        checkValueType(values);

        MultiValueMap<String, Object> multiValueMap = toMultiValueMap(values);
        multiValueMap.add(EMAIL_PARAM, registrationRequest.getEmail());

        formHttpMessageConverter.write(multiValueMap, MediaType.APPLICATION_FORM_URLENCODED, outputMessage);
    }

    private void callSetter(Object obj, String fieldName, Object value) {
        Method setter = setters.computeIfAbsent(fieldName, fn -> getSetterMethod(obj, fn));
        try {
            setter.invoke(obj, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Could not invoke setter for " + fieldName);
        }
    }

    private Method getSetterMethod(Object obj, String fieldName) {
        PropertyDescriptor pd;
        try {
            pd = new PropertyDescriptor(fieldName, obj.getClass());
            return pd.getWriteMethod();
        } catch (IntrospectionException | IllegalArgumentException e) {
            throw new RuntimeException("Missing setter for field " + fieldName);
        }
    }

    @SuppressWarnings("unchecked")
    private MultiValueMap<String, Object> toMultiValueMap(Map<String, Object> values) {
        Map<String, List<Object>> casted = values.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (List<Object>) entry.getValue())
                );
        return new LinkedMultiValueMap<>(casted);
    }

    private void checkValueType(Map<String, Object> values) {
        Optional<Map.Entry<String, Object>> notCorrect = values.entrySet()
                .stream()
                .filter(v -> !(v.getValue() instanceof List))
                .findFirst();

        if (notCorrect.isPresent()) {
            throw new RuntimeException("Expected List<Object>, got " + notCorrect.get().getValue().getClass());
        }
    }
}
