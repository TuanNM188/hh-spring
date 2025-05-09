package com.formos.huub.aop.translationtracker;

import com.formos.huub.domain.entity.AbstractAuditingEntity;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.TranslateRepository;
import com.formos.huub.tracker.TrackTranslate;
import java.lang.reflect.Field;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class EntityChangeAspect {

    private final TranslateRepository translateRepository;

    @Autowired
    public EntityChangeAspect(TranslateRepository translateRepository) {
        this.translateRepository = translateRepository;
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.save(..))")
    public Object aroundSave(ProceedingJoinPoint joinPoint) throws Throwable {
        processEntityOperation(joinPoint.getArgs(), false);
        return joinPoint.proceed();
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.saveAll(..))")
    public Object aroundSaveAll(ProceedingJoinPoint joinPoint) throws Throwable {
        processEntitiesOperation(joinPoint.getArgs(), false);
        return joinPoint.proceed();
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.delete(..))")
    public Object aroundDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        processEntityOperation(joinPoint.getArgs(), true);
        return joinPoint.proceed();
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.deleteAll(..))")
    public Object aroundDeleteAll(ProceedingJoinPoint joinPoint) throws Throwable {
        processEntitiesOperation(joinPoint.getArgs(), true);
        return joinPoint.proceed();
    }

    /**
     * Handle single entity (save or delete)
     */
    private void processEntityOperation(Object[] args, boolean isDelete) {
        if (args.length != 1 || !(args[0] instanceof AbstractAuditingEntity<?> entity)) {
            return;
        }
        processEntity(entity, isDelete);
    }

    /**
     * Handle multiple entities (saveAll or deleteAll)
     */
    private void processEntitiesOperation(Object[] args, boolean isDelete) {
        if (args.length != 1 || !(args[0] instanceof Iterable<?> entities)) {
            return;
        }
        for (Object entity : entities) {
            if (entity instanceof AbstractAuditingEntity<?> auditingEntity) {
                processEntity(auditingEntity, isDelete);
            }
        }
    }

    /**
     * Handle Entity
     */
    private void processEntity(AbstractAuditingEntity<?> entity, boolean isDelete) {
        AbstractAuditingEntity<?> oldEntity = entity.getOldEntity();
        Optional.ofNullable(oldEntity).ifPresent(oldEntityData -> {
            if (isDelete) {
                trackDelete(oldEntityData);
            } else {
                trackChanges(oldEntityData, entity);
            }
        });
    }

    /**
     * Check and process changes of fields annotated with @TrackTranslate
     */
    private <T> void trackChanges(T oldEntity, T newEntity) {
        try {
            processAnnotatedFields(oldEntity, field -> {
                Object oldValue = field.get(oldEntity);
                Object newValue = field.get(newEntity);
                if (Optional.ofNullable(oldValue).isPresent() && !oldValue.equals(newValue)) {
                    deleteTranslationByValue(oldValue);
                }
            });
        } catch (IllegalAccessException e) {
            log.error("Error accessing field during tracking changes", e);
        }
    }

    /**
     * Track translation for fields annotated with @TrackTranslate
     */
    private <T> void trackDelete(T oldEntity) {
        try {
            processAnnotatedFields(oldEntity, field -> {
                Object oldValue = field.get(oldEntity);
                if (Optional.ofNullable(oldValue).isPresent()) {
                    deleteTranslationByValue(oldValue);
                }
            });
        } catch (IllegalAccessException e) {
            log.error("Error accessing field during delete tracking", e);
        }
    }

    /**
     * Process fields annotated with @TrackTranslate
     */
    private <T> void processAnnotatedFields(T entity, FieldProcessor processor) throws IllegalAccessException {
        Class<?> clazz = entity.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TrackTranslate.class)) {
                field.setAccessible(true);
                processor.process(field);
            }
        }
    }

    /**
     * Delete translation by value
     */
    private void deleteTranslationByValue(Object value) {
        String checksumString = StringUtils.generateChecksum(String.valueOf(value));
        translateRepository.deleteAllBySourceHash(checksumString);
    }

    /**
     * // Interface to process field
     */
    @FunctionalInterface
    private interface FieldProcessor {
        void process(Field field) throws IllegalAccessException;
    }
}
