package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Progress;
import br.com.fiap.skilltrack_backend_java.skilltrack.respository.ProgressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;

    public ProgressService(ProgressRepository progressRepository) {
        this.progressRepository = progressRepository;
    }

    public List<Progress> findAllByUser(Long userId) {
        return progressRepository.findByUserId(userId);
    }

    @Transactional
    public Progress save(Progress progress) {
        if (progress.getPercentage() < 0 || progress.getPercentage() > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
        return progressRepository.save(progress);
    }

    @Transactional
    public Progress update(Long id, Progress updatedProgress) {
        Progress existing = progressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Progress not found"));
        existing.setPercentage(updatedProgress.getPercentage());
        existing.setDescription(updatedProgress.getDescription());
        return progressRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        progressRepository.deleteById(id);
    }
}
