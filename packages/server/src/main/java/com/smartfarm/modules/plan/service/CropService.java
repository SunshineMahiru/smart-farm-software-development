package com.smartfarm.modules.plan.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.smartfarm.modules.plan.dto.CropSaveRequest;
import com.smartfarm.modules.plan.entity.Crop;
import com.smartfarm.modules.plan.vo.CropOptionVO;

import java.util.List;

public interface CropService {

    IPage<Crop> pageCrops(long pageNum, long pageSize, String keyword, String category);

    List<CropOptionVO> listOptions();

    Crop getCropById(Long cropId);

    void createCrop(CropSaveRequest request);

    void updateCrop(Long cropId, CropSaveRequest request);

    void deleteCrop(Long cropId);
}
