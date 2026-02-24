package com.example.demo.ServiceComent;

import com.example.demo.DTO.ComentDTO;

import java.util.List;

public interface ComentService {
    ComentDTO saveComent(ComentDTO comentDTO);

    List<ComentDTO> getComentsByBoardId(Long boardId);

    ComentDTO updateComent(ComentDTO comentDTO);

    void deleteComent(Long comentIdx, String userId);

}
