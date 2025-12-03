package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.model.Label;

import java.util.List;

public interface LabelService {
    List<LabelDTO> getAll();
    LabelDTO findById(Long id);
    LabelDTO create(LabelCreateDTO labelData);
    LabelDTO update(LabelUpdateDTO labelData, Long id);
    void delete(Long id);
    Label findByName(String name);
}
