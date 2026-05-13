package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.modules.homestay.entity.Category;
import clyvasync.Clyvasync.repository.homestay.CategoryRepository;
import clyvasync.Clyvasync.service.homestay.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    @Override
    public Map<Integer, String> getCategoryNamesMap(List<Integer> categoryIds) {
        return categoryRepository.findAllByIdIn(categoryIds).stream()
                .collect(Collectors.toMap(Category::getId, Category::getName));
    }
}
