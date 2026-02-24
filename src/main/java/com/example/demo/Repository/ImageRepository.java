package com.example.demo.Repository;

import com.example.demo.Entity.ImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Integer> {
    List<ImageEntity> findByBoardIdx(Integer boardIdx);
    ImageEntity findByFileUrl(String fileUrl);
    List<ImageEntity> findByMember_UseridAndBoardIdxIsNull(String userid);
    List<ImageEntity> findByMember_UseridAndType(String userid, String type);
    int countByBoardIdx(Integer boardIdx);
}
