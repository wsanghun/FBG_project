package com.example.demo.ServiceSearchLank;

import com.example.demo.Entity.SearchLankEntity;

import java.util.List;

public interface SearchLankService {
    void saveKeyword(String keyword);
    List<SearchLankEntity> getTopKeywords();
}
