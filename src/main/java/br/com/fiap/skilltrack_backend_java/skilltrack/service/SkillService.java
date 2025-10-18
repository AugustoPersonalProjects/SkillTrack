package br.com.fiap.skilltrack_backend_java.skilltrack.service;

import br.com.fiap.skilltrack_backend_java.skilltrack.model.Skill;
import br.com.fiap.skilltrack_backend_java.skilltrack.respository.SkillRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> findAllByUser(Long userId) {
        return skillRepository.findByUserIdOrderByNameAsc(userId);
    }

    public Optional<Skill> findById(Long id) {
        return skillRepository.findById(id);
    }

    @Transactional
    public Skill save(Skill skill) {
        if (skill.getProficiency() < 0 || skill.getProficiency() > 100) {
            throw new IllegalArgumentException("Proficiency must be between 0 and 100");
        }
        return skillRepository.save(skill);
    }

    @Transactional
    public void delete(Long id) {
        skillRepository.deleteById(id);
    }

    @Transactional
    public Skill update(Long id, Skill updatedSkill) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Skill not found"));
        skill.setName(updatedSkill.getName());
        skill.setCategory(updatedSkill.getCategory());
        skill.setProficiency(updatedSkill.getProficiency());
        return skillRepository.save(skill);
    }
}
