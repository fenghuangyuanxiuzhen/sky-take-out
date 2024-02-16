package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author 杨楠
 * @version 1.0
 */
@Service
@Slf4j
public class DishServiceImpl implements DishService {
    @Autowired
    DishMapper dishMapper;
    @Autowired
    FlavorMapper flavorMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //1先增加菜品回显菜品id放入口味中
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.insert(dish);

        Long id = dish.getId();
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size()>0){

        flavors.forEach(dishFlavor -> {
            dishFlavor.setDishId(id);
        });
        //2数据不为空在增加口味
        flavorMapper.insertBatch(flavors);
    }
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(),page.getResult());
    }

    @Override
    public void delete(List<Long> ids) {
        //1.在售菜品不能删除
        for(Long id : ids){
            Dish dish  = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }
        //2.有套餐的菜品不能删除
         List<Long> setmealIds= setmealDishMapper.getSetMealIdByDishId(ids);
        if(setmealIds != null && setmealIds.size()>0){
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }
        //3.删除菜品和口味
        dishMapper.deletes(ids);
        flavorMapper.deletes(ids);
    }

    @Override
    public DishVO getDishWithFlavorById(Long id) {
        //1.根据id查询菜品
        Dish dish = dishMapper.getById(id);
        //2.根据id查询口味
        List<DishFlavor>  flavors = flavorMapper.getByDishId(id);
        //3.放入DishVO返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(flavors);
        return dishVO;
    }

    @Override
    public void updateDishWithFlavor(DishDTO dishDTO) {
        //1.取出dish然后动态修改
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        //2.删除原来的口味，判断口味是否存在，然后重新插入
        flavorMapper.deleteById(dish.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() > 0) {


            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dish.getId());
            });
            //2数据不为空在增加口味
            flavorMapper.insertBatch(flavors);


        }
    }

    @Override
    public List<Dish> getDishByCategoryId(Long categoryId) {
        List<Dish> dishes = dishMapper.getByCategoryId(categoryId);
        return dishes;
    }

    @Override
    public void startOrStop(int status, Long id) {
        Dish dish = Dish.builder().id(id).status(status).build();
        dishMapper.update(dish);
    }


}
