//package com.smartgaon.ai.smartgaon_api.villageofficer.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;
//import com.smartgaon.ai.smartgaon_api.villageofficer.repository.GovernmentOfferRepository;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class GovernmentOfferServiceImpl {
//
//    private final GovernmentOfferRepository offerRepository;
//
//    public GovernmentOffer addOffer(GovernmentOffer offer) {
//        return offerRepository.save(offer);
//    }
//
////    public List<GovernmentOffer> getAllOffers() {
////        return offerRepository.findAll();
////    }
//


package com.smartgaon.ai.smartgaon_api.villageofficer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.smartgaon.ai.smartgaon_api.villageofficer.model.GovernmentOffer;
import com.smartgaon.ai.smartgaon_api.villageofficer.repository.GovernmentOfferRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GovernmentOfferServiceImpl implements GovernmentOfferService {

    private final GovernmentOfferRepository offerRepository;

    @Override
    public List<GovernmentOffer> getOffersByDistrict(String district) {
        return offerRepository.findByDistrict(district);
    }
}
//    @Override
//    public List<GovernmentOffer> getOffersByDistrict(String district) {
//        return offerRepository.findByDistrictIgnoreCase(district);
//    }
//    
//    public GovernmentOffer getOfferById(Long id) {
//        return offerRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Offer not found"));
//    }
//
//    public GovernmentOffer updateOffer(Long id, GovernmentOffer updatedOffer) {
//
//        GovernmentOffer offer = getOfferById(id);
//
//        // ✅ FIXED FIELD NAMES
//        offer.setName(updatedOffer.getName());
//        offer.setDepartment(updatedOffer.getDepartment());
//        offer.setPhone(updatedOffer.getPhone());
//        offer.setDistrict(updatedOffer.getDistrict());
//        offer.setState(updatedOffer.getState());
//
//        return offerRepository.save(offer);
//    }
//
//    public void deleteOffer(Long id) {
//        GovernmentOffer offer = getOfferById(id);
//        offerRepository.delete(offer);
//    }
//}