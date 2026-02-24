package com.example.demo.ServiceSearchLank;

import com.example.demo.Entity.SearchLankEntity;
import com.example.demo.Repository.SearchLankRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchLankServiceImpl implements SearchLankService {

    private final SearchLankRepository searchLankRepository;

    @Override
    @Transactional
    public void saveKeyword(String keyword) {

        if(keyword == null || keyword.isBlank()) return;

        keyword = keyword.trim();

        SearchLankEntity entity =
                searchLankRepository.findByKeyword(keyword)
                        .orElse(SearchLankEntity.builder()
                                .keyword(keyword)
                                .count(0)
                                .updatedAt(LocalDateTime.now())
                                .build()
                        );

        entity.setCount(entity.getCount() + 1);
        entity.setUpdatedAt(LocalDateTime.now());

        searchLankRepository.save(entity);
    }

    @Override
    public List<SearchLankEntity> getTopKeywords(){
        return searchLankRepository.findTop5ByOrderByCountDesc();
    }
}


