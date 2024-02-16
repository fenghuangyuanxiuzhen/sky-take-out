package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
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
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Override
    public void insert(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.insert(setmeal);
        Long id = setmeal.getId();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(id);
        });
        setmealDishMapper.insert(setmealDishes);

    }

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page= setmealMapper.pageQuery(setmealPageQueryDTO);
        long total = page.getTotal();
        List<SetmealVO> result = page.getResult();
        return new PageResult(total,result);
    }

    @Override
    public void delete(List<Long> ids) {
        //0在售套餐不能删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if (setmeal.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }
        //1删除套餐
        setmealMapper.deleteBatch(ids);
        //2删除套餐里的菜
        setmealDishMapper.deleteBatch(ids);
    }

    @Override
    public SetmealDTO getSetmealWithDishById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        List <SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
        SetmealDTO setmealDTO = new SetmealDTO();
        BeanUtils.copyProperties(setmeal,setmealDTO);
        setmealDTO.setSetmealDishes(setmealDishes);
        return setmealDTO;
    }

    @Override
    public void update(SetmealDTO setmealDTO) {
        //1.取出对应对象直接修改
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);
        //2.删除原来的  然后插入新的
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmeal.getId());
        });
        setmealDishMapper.deleteById(setmeal.getId());
        setmealDishMapper.insert(setmealDishes);
    }

    @Override
    public void startOrStop(int status, Long id) {
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status).build();
        setmealMapper.update(setmeal);
    }
}
