package net.lab1024.smartadmin.service.module.support.beanrecord;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModelProperty;
import lombok.extern.slf4j.Slf4j;
import net.lab1024.smartadmin.service.module.support.beanrecord.annotation.FieldBigDecimalValue;
import net.lab1024.smartadmin.service.module.support.beanrecord.annotation.FieldDoc;
import net.lab1024.smartadmin.service.module.support.beanrecord.annotation.FieldEnumValue;
import net.lab1024.smartadmin.service.module.support.beanrecord.annotation.FieldSqlValue;
import net.lab1024.smartadmin.service.third.SmartApplicationContext;
import net.lab1024.smartadmin.service.util.SmartBaseEnumUtil;
import net.lab1024.smartadmin.service.util.SmartBigDecimalUtil;
import net.lab1024.smartadmin.service.util.date.SmartDateFormatterEnum;
import net.lab1024.smartadmin.service.util.date.SmartLocalDateUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [  ]
 *
 * @author 罗伊
 */
@Slf4j
@Service
public class BeanRecordService {

    /**
     * 字段描述缓存
     */
    private ConcurrentHashMap<String, String> fieldDescCacheMap = new ConcurrentHashMap<>();

    /**
     * 类 加注解字段缓存
     */
    private ConcurrentHashMap<Class, List<Field>> fieldMap = new ConcurrentHashMap<>();

    /**
     * 数据批量对比
     * @param oldObjectList
     * @param newObjectList
     * @param <T>
     * @return
     */
    public <T> String beanListParse(List<T> oldObjectList, List<T> newObjectList) {
        boolean valid = this.valid(oldObjectList, newObjectList);
        if (!valid) {
            return "";
        }
        OperateTypeEnum operateType = this.getOperateType(oldObjectList, newObjectList);
        String operateContent = "";
        if (OperateTypeEnum.ADD.equals(operateType) || OperateTypeEnum.DELETE.equals(operateType)) {
            operateContent = this.getObjectListContent(newObjectList);
            if (StringUtils.isEmpty(operateContent)) {
                return "";
            }
            return operateType.getDesc() + ":" + operateContent;
        }
        if (OperateTypeEnum.UPDATE.equals(operateType)) {
            return this.getUpdateContentList(oldObjectList, newObjectList);
        }
        return operateContent;
    }

    /**
     * 单个对象变动内容
     * @param oldObjectList
     * @param newObjectList
     * @param <T>
     * @return
     */
    private <T> String getUpdateContentList(List<T> oldObjectList, List<T> newObjectList) {
        String oldContent = this.getObjectListContent(oldObjectList);
        String newContent = this.getObjectListContent(newObjectList);
        if (oldContent.equals(newContent)) {
            return "";
        }
        if (StringUtils.isEmpty(oldContent) && StringUtils.isEmpty(newContent)) {
            return "";
        }
        return "【原数据】:<br/>" + oldContent + "<br/>" + "【新数据】:<br/>" + newContent;
    }

    /**
     * 获取一个对象的内容信息
     * @param objectList
     * @param <T>
     * @return
     */
    private <T> String getObjectListContent(List<T> objectList) {
        if (CollectionUtils.isEmpty(objectList)) {
            return "";
        }
        List<Field> fields = this.getField(objectList.get(0));
        List<String> contentList = Lists.newArrayList();
        for (Object objItem : objectList) {
            Map<String, String> beanParseMap = this.fieldParse(objItem, fields);
            contentList.add(this.getAddDeleteContent(beanParseMap));
        }
        return StringUtils.join(contentList, "<br/>");
    }


    /**
     * 解析多个对象的变更，删除，新增
     * oldObject 为空 ，newObject 不为空 为新增
     * oldObject 不为空 ，newObject 不空 为删除
     * 都不为空为编辑
     *
     * @param oldObject
     * @param newObject
     * @return
     */
    public String beanParse(Object oldObject, Object newObject) {
        boolean valid = this.valid(oldObject, newObject);
        if (!valid) {
            return null;
        }
        OperateTypeEnum operateType = this.getOperateType(oldObject, newObject);
        String operateContent = "";
        if (OperateTypeEnum.ADD.equals(operateType) || OperateTypeEnum.DELETE.equals(operateType)) {
            operateContent = this.getAddDeleteContent(newObject);
        }
        if (OperateTypeEnum.UPDATE.equals(operateType)) {
            operateContent = this.getUpdateContent(oldObject, newObject);
        }
        if (StringUtils.isEmpty(operateContent)) {
            return "";
        }
        return operateType.getDesc() + ":" + operateContent;
    }

    /**
     * 解析单个bean的内容
     *
     * @param operateDesc
     * @param object
     * @return
     */
    public String beanParse(String operateDesc, Object object) {
        String content = this.getAddDeleteContent(object);
        if (StringUtils.isEmpty(operateDesc)) {
            return content;
        }
        return operateDesc + ":" + content;
    }

    /**
     * 获取新增或删除操作内容
     *
     * @param object 新增或删除的对象
     * @return
     */
    private String getAddDeleteContent(Object object) {
        List<Field> fields = this.getField(object);
        Map<String, String> beanParseMap = this.fieldParse(object, fields);
        return this.getAddDeleteContent(beanParseMap);
    }

    private String getAddDeleteContent(Map<String, String> beanParseMap) {
        List<String> contentList = new ArrayList<>();
        for (Entry<String, String> entry : beanParseMap.entrySet()) {
            contentList.add(entry.getKey() + ":" + entry.getValue());
        }
        String operateContent = StringUtils.join(contentList, ";");
        if (StringUtils.isEmpty(operateContent)) {
            return "";
        }
        return operateContent;
    }


    /**
     * 获取更新操作内容
     *
     * @param oldObject
     * @param newObject
     * @return
     */
    private <T> String getUpdateContent(T oldObject, T newObject) {
        List<Field> fields = this.getField(oldObject);
        List<String> contentList = new ArrayList<>();
        Map<String, String> oldBeanParseMap = this.fieldParse(oldObject, fields);
        Map<String, String> newBeanParseMap = this.fieldParse(newObject, fields);
        //oldBeanParseMap与newBeanParseMap size一定相同
        for (Entry<String, String> entry : oldBeanParseMap.entrySet()) {
            String desc = entry.getKey();
            String oldValue = entry.getValue();
            String newValue = newBeanParseMap.get(desc);
            if (oldValue.equals(newValue)) {
                continue;
            }
            String content = desc + ":" + "由【" + oldValue + "】变更为【" + newValue + "】";
            contentList.add(content);
        }
        if (CollectionUtils.isEmpty(contentList)) {
            return "";
        }
        String operateContent = StringUtils.join(contentList, ";");
        if (StringUtils.isEmpty(operateContent)) {
            return "";
        }
        return operateContent;
    }


    /**
     * 接bean对象
     *
     * @param object
     * @param fields
     * @return <desc,value></>
     */
    private Map<String, String> fieldParse(Object object, List<Field> fields) {
        if (fields == null || fields.size() == 0) {
            return null;
        }
        //对象解析结果
        Map<String, String> objectParse = new HashMap<>(16);
        for (Field field : fields) {
            field.setAccessible(true);
            String desc = this.getFieldDesc(field);
            if (StringUtils.isEmpty(desc)) {
                continue;
            }
            String fieldValue = this.getFieldValue(field, object);
            objectParse.put(desc, fieldValue);
        }
        return objectParse;
    }

    /**
     * 获取字段值
     * @param field
     * @param object
     * @return
     */
    private String getFieldValue(Field field, Object object) {
        Object fieldValue = "";
        Class clazz = object.getClass();
        try {
            PropertyDescriptor pd = new PropertyDescriptor(field.getName(), clazz);
            Method get = pd.getReadMethod();
            fieldValue = get.invoke(object);
        } catch (Exception e) {
            log.error("bean operate log: reflect field value error " + field.getName());
            return "";
        }
        if (fieldValue == null) {
            return "";
        }
        FieldEnumValue fieldEnumValue = field.getAnnotation(FieldEnumValue.class);
        FieldSqlValue fieldSqlValue = field.getAnnotation(FieldSqlValue.class);
        if (fieldEnumValue != null) {
            return SmartBaseEnumUtil.getEnumDescByValue(fieldValue, fieldEnumValue.enumClass());
        }
        if (fieldSqlValue != null) {
            return this.getRelateDisplayValue(fieldValue, fieldSqlValue);
        }
        if (fieldValue instanceof Date) {
            LocalDateTime localDateTime = SmartLocalDateUtil.toLocalDateTime((Date) fieldValue);
            return SmartLocalDateUtil.format(localDateTime, SmartDateFormatterEnum.YMD_HMS);
        }
        if (fieldValue instanceof LocalDateTime) {
            return SmartLocalDateUtil.format((LocalDateTime) fieldValue, SmartDateFormatterEnum.YMD_HMS);
        }
        if (fieldValue instanceof LocalDate) {
            return SmartLocalDateUtil.format((LocalDate) fieldValue, SmartDateFormatterEnum.YMD);
        }
        if (fieldValue instanceof BigDecimal) {
            FieldBigDecimalValue fieldBigDecimalValue = field.getAnnotation(FieldBigDecimalValue.class);
            if (fieldBigDecimalValue != null) {
                BigDecimal value = SmartBigDecimalUtil.setScale((BigDecimal) fieldValue, fieldBigDecimalValue.scale());
                return value.toString();
            }
        }
        return fieldValue.toString();
    }

    /**
     * 获取关联字段的显示值
     *
     * @param fieldValue
     * @return
     */
    private String getRelateDisplayValue(Object fieldValue, FieldSqlValue fieldSqlValue) {
        Class<? extends BaseMapper> relateMapper = fieldSqlValue.relateMapper();
        BaseMapper mapper = SmartApplicationContext.getBean(relateMapper);
        if (mapper == null) {
            return "";
        }
        String relateFieldValue = fieldValue.toString();
        QueryWrapper qw = new QueryWrapper();
        qw.select(fieldSqlValue.relateDisplayColumn());
        qw.eq(fieldSqlValue.relateColumn(), relateFieldValue);
        List<Object> displayValue = mapper.selectObjs(qw);
        if (CollectionUtils.isEmpty(displayValue)) {
            return "";
        }
        return displayValue.get(0).toString();
    }

    /**
     * 获取字段描述信息 优先 OperateField 没得话swagger判断
     *
     * @param field
     * @return
     */
    private String getFieldDesc(Field field) {
        // 根据字段名称 从缓存中查询
        String fieldName = field.toGenericString();
        String desc = fieldDescCacheMap.get(fieldName);
        if (null != desc) {
            return desc;
        }
        FieldDoc operateField = field.getAnnotation(FieldDoc.class);
        if (operateField != null) {
            desc = operateField.value();
        } else {
            ApiModelProperty apiModelProperty = field.getAnnotation(ApiModelProperty.class);
            desc = null == apiModelProperty ? "" : apiModelProperty.value();
        }
        fieldDescCacheMap.put(fieldName, desc);
        return desc;
    }

    /**
     * 获取操作类型
     *
     * @param oldObject
     * @param newObject
     * @return
     */
    private OperateTypeEnum getOperateType(Object oldObject, Object newObject) {
        if (oldObject == null && newObject != null) {
            return OperateTypeEnum.ADD;
        }
        if (oldObject != null && newObject == null) {
            return OperateTypeEnum.DELETE;
        }
        return OperateTypeEnum.UPDATE;
    }

    /**
     * 校验是否进行比对
     *
     * @param oldObject
     * @param newObject
     * @return
     */
    private boolean valid(Object oldObject, Object newObject) {
        if (oldObject == null && newObject == null) {
            log.error("bean operate log: oldObject and newObject is null");
            return false;
        }
        if (oldObject == null && newObject != null) {
            log.info("bean operate log: oldObject is null,new:" + newObject.getClass().getName() + " " + OperateTypeEnum.ADD.getDesc());
            return true;
        }
        if (oldObject != null && newObject == null) {
            log.info("bean operate log: newObject is null,old:" + oldObject.getClass().getName() + " " + OperateTypeEnum.DELETE.getDesc());
            return true;
        }
        if (oldObject != null && newObject != null) {
            String oldClass = oldObject.getClass().getName();
            String newClass = newObject.getClass().getName();
            if (oldClass.equals(newClass)) {
                log.info("bean operate log: " + oldObject.getClass().getName() + " " + OperateTypeEnum.UPDATE.getDesc());
                return true;
            }
            log.error("bean operate log: is different class:old:" + oldClass + " new:" + newClass);
            return false;
        }
        return true;
    }


    /**
     * 校验
     * @param oldObjectList
     * @param newObjectList
     * @param <T>
     * @return
     */
    private <T> boolean valid(List<T> oldObjectList, List<T> newObjectList) {
        if (CollectionUtils.isEmpty(oldObjectList) && CollectionUtils.isEmpty(newObjectList)) {
            log.error("bean operate log: oldObjectList and newObject is null");
            return false;
        }
        if (CollectionUtils.isEmpty(oldObjectList) && CollectionUtils.isNotEmpty(newObjectList)) {
            log.info("bean operate log: oldObjectList is null,new:" + newObjectList.getClass().getName() + " " + OperateTypeEnum.ADD.getDesc());
            return true;
        }
        if (CollectionUtils.isNotEmpty(oldObjectList) && CollectionUtils.isEmpty(newObjectList)) {
            log.info("bean operate log: newObject is null,old:" + oldObjectList.getClass().getName() + " " + OperateTypeEnum.DELETE.getDesc());
            return true;
        }
        if (CollectionUtils.isNotEmpty(oldObjectList) && CollectionUtils.isNotEmpty(newObjectList)) {
            T oldObject = oldObjectList.get(0);
            T newObject = newObjectList.get(0);
            String oldClass = oldObject.getClass().getName();
            String newClass = newObject.getClass().getName();
            if (oldClass.equals(newClass)) {
                log.info("bean operate log: " + oldObject.getClass().getName() + " " + OperateTypeEnum.UPDATE.getDesc());
                return true;
            }
            log.error("bean operate log: is different class:old:" + oldClass + " new:" + newClass);
            return false;
        }
        return true;
    }

    /**
     * 查询 包含 file key 注解的字段
     * 使用缓存
     *
     * @param obj
     * @return
     */
    private List<Field> getField(Object obj) {
        // 从缓存中查询
        Class tClass = obj.getClass();
        List<Field> fieldList = fieldMap.get(tClass);
        if (null != fieldList) {
            return fieldList;
        }

        // 这一段递归代码 是为了 从父类中获取属性
        Class tempClass = tClass;
        fieldList = new ArrayList<>();
        while (tempClass != null) {
            Field[] declaredFields = tempClass.getDeclaredFields();
            for (Field field : declaredFields) {
                // 过虑出有注解字段
                if (!field.isAnnotationPresent(FieldDoc.class)) {
                    continue;
                }
                field.setAccessible(true);
                fieldList.add(field);
            }
            tempClass = tempClass.getSuperclass();
        }
        fieldMap.put(tClass, fieldList);
        return fieldList;
    }


}