package com.jasolar.mis.framework.common.convert;

import java.util.List;

import org.mapstruct.Mapper;

/***
 * 实体和DTO的转换
 * 
 * @author galuo
 * @date 2025-04-09 10:43
 *
 * @param <DTO> DTO
 * @param <Entity> 数据库实体DO
 */
public interface IConvert<DTO, Entity> {

    /** 添加{@link Mapper}注解时, 指定为componentModel=spring, 使之被Spring托管 */
    String SPRING = "spring";

    /**
     * 将实体DO转换为DTO
     * 
     * @param e 数据库DO
     * @return DTO
     */
    DTO toDTO(Entity e);

    // /**
    // * 将实体DO转换为DTO列表
    // *
    // * @param entity 数据库DO列表
    // * @return DTO列表
    // */
    // @Deprecated
    // List<DTO> toDTOS(List<Entity> entity);

    /**
     * 将实体DO转换为DTO列表
     * 
     * @param entities 数据库DO列表
     * @return DTO列表
     */
    List<DTO> toDTOs(List<Entity> entities);

    // /**
    // * 复制DTO
    // *
    // * @param d
    // * @return
    // */
    // @Deprecated
    // DTO toSelfDTO(DTO d);
    //
    // /**
    // * 复制DTO列表
    // *
    // * @param source
    // * @return
    // */
    // @Deprecated
    // List<DTO> toSelfDTOS(List<DTO> source);

    /**
     * 复制DTO
     * 
     * @param dto 要复制的DTO
     * @return 新的DTO
     */
    DTO cloneDTO(DTO dto);

    /**
     * 复制DTO列表
     * 
     * @param dtos 要复制的列表
     * @return 新的列表,并且列表中每个对象都是复制的新对象
     */
    List<DTO> cloneDTOs(List<DTO> dto);

    /**
     * 将DTO转换为DO
     * 
     * @param dto DTO
     * @return 数据库DO
     */
    Entity toEntity(DTO dto);

    /**
     * 将DTO转换为实体DO列表
     * 
     * @param dtos DTO列表
     * @return 数据库DO列表
     */
    List<Entity> toEntities(List<DTO> dtos);

    // /**
    // * 复制数据库DO
    // *
    // * @param entity
    // * @return
    // */
    // @Deprecated
    // Entity toSelfEntity(Entity entity);
    //
    // /**
    // * 复制数据库DO列表
    // *
    // * @param entities 数据库DO列表
    // * @return 新的数据库DO列表,列表中每个对象都是复制的新对象
    // */
    // @Deprecated
    // List<Entity> toSelfEntities(List<Entity> entities);

    /**
     * 复制数据库DO
     * 
     * @param entity 数据库实体DO
     * @return 新的对象
     */
    Entity cloneEntity(Entity entity);

    /**
     * 复制数据库DO列表
     * 
     * @param entities 数据库DO列表
     * @return 新的数据库DO列表,列表中每个对象都是复制的新对象
     */
    List<Entity> cloneEntities(List<Entity> entities);

}
