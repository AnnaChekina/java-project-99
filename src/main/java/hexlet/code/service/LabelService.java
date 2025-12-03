package hexlet.code.service;

import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.LabelMapper;
import hexlet.code.model.Label;
import hexlet.code.repository.LabelRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class LabelService {

    private final LabelRepository labelRepository;
    private final LabelMapper labelMapper;
    private final TaskService taskService;

    public List<LabelDTO> getAll() {
        var labels = labelRepository.findAll();
        return labels.stream()
                .map(labelMapper::map)
                .toList();
    }

    public LabelDTO findById(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label Not Found: " + id));
        return labelMapper.map(label);
    }

    public LabelDTO create(LabelCreateDTO labelData) {
        var label = labelMapper.map(labelData);
        try {
            labelRepository.save(label);
            return labelMapper.map(label);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Label with this name already exists");
        }
    }

    public LabelDTO update(LabelUpdateDTO labelData, Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label Not Found: " + id));

        labelMapper.update(labelData, label);

        try {
            labelRepository.save(label);
            return labelMapper.map(label);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Label with this name already exists");
        }
    }

    public void delete(Long id) {
        var label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label Not Found: " + id));

        if (taskService.existsByLabelId(id)) {
            throw new DataIntegrityViolationException("Cannot delete label: label is used in tasks");
        }

        labelRepository.delete(label);
    }

    public Label findByName(String name) {
        return labelRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Label Not Found with name: " + name));
    }
}
