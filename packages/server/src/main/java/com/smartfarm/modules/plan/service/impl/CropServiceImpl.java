package com.smartfarm.modules.plan.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.smartfarm.common.exception.BusinessException;
import com.smartfarm.modules.plan.dto.CropSaveRequest;
import com.smartfarm.modules.plan.entity.Crop;
import com.smartfarm.modules.plan.mapper.CropMapper;
import com.smartfarm.modules.plan.service.CropService;
import com.smartfarm.modules.plan.vo.CropOptionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CropServiceImpl implements CropService {

    private final CropMapper cropMapper;

    @Override
    public IPage<Crop> pageCrops(long pageNum, long pageSize, String keyword, String category) {
        LambdaQueryWrapper<Crop> wrapper = new LambdaQueryWrapper<Crop>()
                .like(StringUtils.hasText(keyword), Crop::getCropName, keyword)
                .eq(StringUtils.hasText(category), Crop::getCategory, category)
                .orderByAsc(Crop::getCategory)
                .orderByAsc(Crop::getCropName);
        return cropMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<CropOptionVO> listOptions() {
        return cropMapper.selectOptions();
    }

    @Override
    public Crop getCropById(Long cropId) {
        Crop crop = cropMapper.selectById(cropId);
        if (crop == null) {
            throw new BusinessException(404, "Crop not found");
        }
        return crop;
    }

    @Override
    public void createCrop(CropSaveRequest request) {
        checkDuplicateName(null, request.getCropName());
        Crop crop = new Crop();
        BeanUtils.copyProperties(request, crop);
        cropMapper.insert(crop);
    }

    @Override
    public void updateCrop(Long cropId, CropSaveRequest request) {
        Crop existing = getCropById(cropId);
        checkDuplicateName(cropId, request.getCropName());
        BeanUtils.copyProperties(request, existing);
        cropMapper.updateById(existing);
    }

    @Override
    public void deleteCrop(Long cropId) {
        getCropById(cropId);
        if (cropMapper.countPlanByCropId(cropId) > 0) {
            throw new BusinessException("Crop is referenced by planting plans and cannot be deleted");
        }
        cropMapper.deleteById(cropId);
    }

    private void checkDuplicateName(Long cropId, String cropName) {
        Crop existing = cropMapper.selectOne(new LambdaQueryWrapper<Crop>()
                .eq(Crop::getCropName, cropName)
                .ne(cropId != null, Crop::getCropId, cropId)
                .last("LIMIT 1"));
        if (existing != null) {
            throw new BusinessException("Crop name already exists");
        }
    }
}
