package ma.myc.inner.donation.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ma.myc.inner.donation.domain.dto.VilleAddRequest;
import ma.myc.inner.donation.domain.dto.VilleResponse;
import ma.myc.inner.donation.mapper.VilleMapper;
import ma.myc.inner.donation.repository.VilleRepository;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class VilleService {

    private final VilleRepository villeRepository;
    private final VilleMapper villeMapper;

    public List<VilleResponse> getAllVilleByPaysCode(String paysCode) {
        log.info("Récuération de la liste des villes pour le code pays {}", paysCode);
        return villeMapper.mapToVilleResponse(
                villeRepository.findAllByPaysCode(paysCode));
    }


    public VilleResponse addNewVille(VilleAddRequest request) {
        return villeMapper.mapToVilleResponse(request);
    }
}
